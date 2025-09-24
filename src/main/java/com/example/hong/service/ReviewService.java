// src/main/java/com/example/HONGAROUND/service/ReviewService.java
package com.example.hong.service;

import com.example.hong.dto.AspectScoreDto;
import com.example.hong.dto.HateSpeechResponseDto;
import com.example.hong.dto.ReviewItemDto;
import com.example.hong.entity.Cafe;
import com.example.hong.entity.Review;
import com.example.hong.entity.ReviewAspectScore;
import com.example.hong.entity.User;
import com.example.hong.repository.CafeRepository;
import com.example.hong.repository.ReviewAspectScoreRepository;
import com.example.hong.repository.ReviewRepository;
import com.example.hong.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final CafeRepository cafeRepo;
    private final UserRepository userRepo;
    private final ReviewAspectScoreRepository aspectScoreRepo;
    private final RestTemplate restTemplate;

    @Value("${app.upload.dir:./uploads}")
    private String uploadRoot;

    @Value("${python.api.predict-url}") // [추가] application.properties에서 API 주소 가져오기
    private String predictApiUrl;

    private static final String REVIEW_DIR = "reviews";
    private static final String URL_PREFIX = "/uploads/reviews";
    private static final Set<String> IMG_EXT = Set.of("jpg","jpeg","png","gif","webp","bmp");
    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    /* ================== 생성 ================== */
    @Transactional
    public void addWithImages(Long userId, Long cafeId, int rating, String content, List<MultipartFile> images) {
        HateSpeechResponseDto validationResult = validateReviewContent(content);
        if (validationResult.isHateSpeech()) {
            // 부적절한 내용이 감지되면 예외를 발생시켜 리뷰 저장을 중단
            String labels = String.join(", ", validationResult.getPredictedLabels());
            throw new IllegalArgumentException("부적절한 내용이 포함되어 리뷰를 등록할 수 없습니다. (감지된 유형: " + labels + ")");
        }
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");

        User user = userRepo.findById(userId).orElseThrow();
        Cafe cafe = cafeRepo.findById(cafeId).orElseThrow();

        if (cafe.getOwner() != null && cafe.getOwner().getId().equals(userId)) {
            throw new IllegalArgumentException("본인 매장에는 리뷰를 작성할 수 없습니다.");
        }
        if (reviewRepo.existsByUserAndCafeAndDeletedFalse(user, cafe)) {
            throw new IllegalArgumentException("이미 이 매장에 리뷰를 작성했습니다.");
        }

        List<String> urls = saveUpToFive(images);

        Review.ReviewBuilder b = Review.builder()
                .user(user)
                .cafe(cafe)
                .rating(rating)
                .content(content);

        if (urls.size() > 0) b.imageUrl1(urls.get(0));
        if (urls.size() > 1) b.imageUrl2(urls.get(1));
        if (urls.size() > 2) b.imageUrl3(urls.get(2));
        if (urls.size() > 3) b.imageUrl4(urls.get(3));
        if (urls.size() > 4) b.imageUrl5(urls.get(4));

        // [수정됨] 저장된 리뷰 객체를 받아옴
        Review savedReview = reviewRepo.save(b.build());

        // [추가됨] 리뷰 저장 후, 비동기적으로 감성 분석 실행 및 결과 저장
        try {
            analyzeAndSaveAspectScores(savedReview);
        } catch (Exception e) {
            // 감성 분석 실패가 리뷰 생성 자체에 영향을 주지 않도록 예외 처리
            log.error("리뷰 감성 분석 실패 (리뷰 ID: {}): {}", savedReview.getId(), e.getMessage());
        }
    }
    //Python API를 호출하는 private 헬퍼 메소드
    private HateSpeechResponseDto validateReviewContent(String content) {
        try {
            // 1. Python API로 보낼 요청 본문(body) 생성
            Map<String, String> requestBody = Map.of("comment", content);

            // 2. RestTemplate을 사용해 POST 요청 보내고 응답을 DTO로 받기
            return restTemplate.postForObject(predictApiUrl, requestBody, HateSpeechResponseDto.class);

        } catch (Exception e) {
            log.error("혐오 표현 분석 API 호출 실패: {}", e.getMessage());
            // API 서버에 문제가 생겼을 경우, 우선 리뷰는 통과시키도록 처리 (정책에 따라 변경 가능)
            HateSpeechResponseDto fallbackResponse = new HateSpeechResponseDto();
            fallbackResponse.setHateSpeech(false);
            return fallbackResponse;
        }
    }

    // Python 감성 분석 스크립트를 실행하고 결과를 DB에 저장하는 메소드
    private void analyzeAndSaveAspectScores(Review review) throws Exception {
        // Python 가상환경 실행 파일과 스크립트 경로 (사용자 환경에 맞게 수정 필수)
        String pythonExecutable = "C:\\Users\\cnrrn\\Desktop\\project10\\project10\\project5\\python-classifier\\venv\\Scripts\\python.exe";
        String pythonScript = "C:\\Users\\cnrrn\\Desktop\\project10\\project10\\project5\\python-classifier\\Postive.py";

        log.info("--- Python 스크립트 실행 시작 ---");
        log.info("실행 파일: {}", pythonExecutable);
        log.info("스크립트: {}", pythonScript);
        log.info("입력 리뷰: {}", review.getContent());

        ProcessBuilder processBuilder = new ProcessBuilder(pythonExecutable, pythonScript, review.getContent());
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // 파이썬 스크립트의 print 출력을 읽어옴
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
        }

        int exitCode = process.waitFor();
        log.info("Python 스크립트 종료 코드: {}", exitCode);
        log.info("Python 스크립트 원본 출력: '{}'", output.toString());

        if (exitCode != 0) {
            throw new RuntimeException("Python 스크립트 실행 실패: " + output);
        }

// =======================================================
// [수정된 부분] 전체 출력에서 JSON만 추출하기
// =======================================================
        String rawOutput = output.toString();
        int jsonStart = rawOutput.indexOf('{'); // 첫 '{' 위치 찾기
        int jsonEnd = rawOutput.lastIndexOf('}'); // 마지막 '}' 위치 찾기

// JSON 부분을 찾을 수 없거나 형식이 이상하면 예외 처리
        if (jsonStart == -1 || jsonEnd == -1 || jsonStart > jsonEnd) {
            throw new RuntimeException("Python 스크립트에서 유효한 JSON 출력을 찾을 수 없습니다: " + rawOutput);
        }

// 첫 '{'부터 마지막 '}'까지 잘라내어 순수한 JSON 문자열을 만듦
        String jsonOutput = rawOutput.substring(jsonStart, jsonEnd + 1);
        log.info("추출된 JSON: '{}'", jsonOutput); // 잘라낸 결과 로그 확인

// JSON 결과를 Map으로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> aspectScores = objectMapper.readValue(jsonOutput, new TypeReference<>() {});
// =======================================================

        log.info("파싱된 점수 Map: {}", aspectScores);

        // 각 항목 점수를 DB에 저장
        List<ReviewAspectScore> scoresToSave = new ArrayList<>();
        for (Map.Entry<String, Object> entry : aspectScores.entrySet()) {
            if (entry.getValue() instanceof Number) {
                scoresToSave.add(ReviewAspectScore.builder()
                        .review(review)
                        .aspect(entry.getKey())
                        .score(new BigDecimal(entry.getValue().toString()))
                        .build());
            }
        }

        log.info("DB에 저장할 점수 개수: {}", scoresToSave.size());

        if (!scoresToSave.isEmpty()) {
            aspectScoreRepo.saveAll(scoresToSave);
            log.info("감성 분석 점수 저장 완료.");
        }
    }

    /* ================== 조회: 카페 상세용 ================== */
    @Transactional(Transactional.TxType.SUPPORTS) // readOnly 성격
    public List<ReviewItemDto> listForCafeDtos(Long cafeId) {
        return reviewRepo.findForCafeWithUser(cafeId).stream()
                .map(this::toItemDto)
                .toList();
    }

    // (기존 시그니처를 이미 컨트롤러에서 쓰고 있으면 유지하고 내부에서 DTO로 대체)
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ReviewItemDto> listForCafe(Long cafeId) {
        return listForCafeDtos(cafeId);
    }

    private ReviewItemDto toItemDto(Review r) {
        String created = (r.getCreatedAt() == null) ? "" : r.getCreatedAt().format(DT_FMT);
        String nickname = resolveNickname(r.getUser()); // fetch join으로 세션 밖에서도 안전

        // [추가된 부분]
        // 1. Review 엔티티에 연결된 감성 분석 점수 목록을 가져옵니다.
        //    (Review 엔티티에 getReviewAspectScores() 메소드가 있어야 합니다.)
        List<AspectScoreDto> aspectScores = r.getReviewAspectScores().stream()
                .map(score -> new AspectScoreDto(score.getAspect(), score.getScore()))
                .toList();

        // 2. builder()를 사용하여 DTO를 생성할 때, aspectScores 리스트를 추가합니다.
        return ReviewItemDto.builder()
                .id(r.getId())
                .rating(r.getRating())
                .content(r.getContent())
                .createdAt(created)
                .nickname(nickname)
                .imageUrl1(r.getImageUrl1())
                .imageUrl2(r.getImageUrl2())
                .imageUrl3(r.getImageUrl3())
                .imageUrl4(r.getImageUrl4())
                .imageUrl5(r.getImageUrl5())
                .aspectScores(aspectScores) // [추가] 감성 분석 점수 리스트를 DTO에 포함
                .build();
    }

    /* ================== 조회: 마이페이지용 ================== */
    @Transactional(Transactional.TxType.SUPPORTS)
    public List<ReviewItemDto> myReviews(Long userId) {
        return reviewRepo.findByUserIdWithUser(userId).stream()
                .map(this::toItemDto)
                .toList();
    }

    /* ================== 수정/삭제 ================== */
    @Transactional
    public void updateMyReview(Long userId, Long reviewId, String content, Integer rating) {
        var r = reviewRepo.findByIdAndUserIdAndDeletedFalse(reviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("수정 권한이 없습니다."));
        if (rating != null) r.setRating(rating);
        if (content != null) r.setContent(content);
    }

    @Transactional
    public void deleteMyReview(Long userId, Long reviewId) {
        var r = reviewRepo.findByIdAndUserIdAndDeletedFalse(reviewId, userId)
                .orElseThrow(() -> new IllegalArgumentException("삭제 권한이 없습니다."));
        r.setDeleted(true);
    }

    /* ================== 내부 유틸 ================== */
    private List<String> saveUpToFive(List<MultipartFile> images) {
        List<String> urls = new ArrayList<>();
        if (images == null) return urls;

        try {
            Path dir = Paths.get(uploadRoot, REVIEW_DIR);
            Files.createDirectories(dir);

            int count = 0;
            for (MultipartFile f : images) {
                if (f == null || f.isEmpty()) continue;
                if (++count > 5) break;

                String orig = f.getOriginalFilename();
                String ext = (orig != null && orig.contains(".")) ?
                        orig.substring(orig.lastIndexOf('.') + 1).toLowerCase() : "";
                if (!IMG_EXT.contains(ext)) throw new IllegalArgumentException("이미지 형식이 아닙니다: " + ext);

                String filename = UUID.randomUUID() + "." + ext;
                Files.copy(f.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
                urls.add(URL_PREFIX + "/" + filename);
            }
            return urls;
        } catch (Exception e) {
            throw new IllegalStateException("리뷰 이미지를 저장하지 못했습니다.", e);
        }
    }

    private String resolveNickname(User u) {
        if (u == null) return "알 수 없음";
        if (u.getNickname() != null && !u.getNickname().isBlank()) return u.getNickname();
        if (u.getName() != null && !u.getName().isBlank()) return u.getName();
        String email = u.getEmail();
        return (email != null && !email.isBlank()) ? email.split("@")[0] : "사용자";
    }
}
