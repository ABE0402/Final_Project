# 홍어라운드 (HongAround)

홍어라운드는 홍대 지역의 카페를 추천 및 검색할 수 있는 웹 서비스입니다. 사용자의 성별, 연령, 검색 로그 등 다양한 정보를 바탕으로 맞춤형 카페 추천을 제공하며, 카카오맵을 통해 위치 정보를 확인할 수 있습니다. 또한, AI 기반 리뷰 감정 분석 및 욕설 필터링 기능을 제공합니다.

## 주요 기능

1.  **맞춤형 카페 추천**
      * 사용자의 연령, 성별에 따라 카페 페이지 클릭, 리뷰, 즐겨찾기, 예약에 가중치를 부여하여 선호도가 높은 카페를 추천합니다.
      * 사용자의 검색 로그를 Jaccard 유사도 및 점수 기반 알고리즘으로 분석하여 개인화된 추천을 제공합니다.
2.  **검색 기능**
      * 키워드 입력 시 Elasticsearch 기반의 유사도 순으로 카페를 검색합니다.
      * 카페의 태그(분위기, 편의시설, 동반인 등)를 기반으로 세분화된 검색이 가능합니다.
3.  **카카오맵 연동**
      * 카카오맵 API를 활용하여 카페의 위치를 지도에서 시각적으로 확인할 수 있습니다.
4.  **AI 리뷰 분석 및 필터링**
      * 별도로 실행되는 Python Flask 서버 및 Hugging Face AI 모델을 활용하여 리뷰 작성 시 욕설을 필터링하고 감정 분석을 수행합니다.
      * 1차로 사용자 정의 욕설 DB(`final_abuse_db.json`)에서 필터링하고, 2차로 머신러닝 모델(`hate_speech_classifier.pkl`)을 통해 혐오 표현을 탐지합니다.
5.  **예약 및 즐겨찾기**
      * 사용자는 날짜, 시간, 인원수를 선택하여 카페를 예약할 수 있으며, 예약 완료 시 사용자와 점주에게 알림이 전송됩니다.
      * 점주는 예약을 승인하거나 취소할 수 있습니다.
6.  **권한 분리**
      * **User**: 마이페이지 (프로필, 예약/리뷰/즐겨찾기 관리), 리뷰 작성, 예약, 즐겨찾기 기능
      * **Owner**: 점주 페이지, 카페 등록 및 관리, 태그 선택, 리뷰(댓글) 관리
      * **Admin**: 관리자 페이지, 점주 신청 승인, 가게 등록 승인, 모든 리뷰/유저/가게 관리

-----

##  기술 스택

| 구분 | 기술 | 버전/정보 |
| :--- | :--- | :--- |
| **Backend** | Java | `17.0.15` |
| | Spring Boot | `3.1.0` |
| | Spring Data JPA | `3.1.0` (Querydsl `5.0.0` 연동) |
| | Spring Security | OAuth2 Client 포함 |
| | Kotlin | `1.9.24` (JVM) |
| | Lombok | 의존성 |
| **Frontend** | Mustache | Spring Boot Starter |
| | JavaScript, CSS | |
| **Database** | MySQL | `8.0.42` |
| | Elasticsearch | `8.8.2` |
| **AI (Server)** | Python | Flask |
| | AI/ML | Hugging Face (Model), `joblib` |
| **Build/Tools**| Gradle | |
| | Docker | (Elasticsearch 실행용) |
| | OpenCSV | `5.7.1` |
| **APIs** | Kakao Map API | |

-----

##  설치 및 실행 방법

본 프로젝트는 **Spring Boot 백엔드 서버**와 **Flask AI 서버** 두 부분으로 구성되어 있습니다.

### 1\. 사전 요구사항

  * Java `17` 설치
  * MySQL `8.0` 이상 설치 및 실행
  * Docker 및 Docker Compose
  * Python (Flask, joblib 필요)

### 2\. 데이터베이스 및 검색 엔진 실행

1.  **Elasticsearch 실행 (Docker)**
    프로젝트 루트의 `docker-compose.yml` 파일을 사용하여 Elasticsearch 컨테이너를 실행합니다.

    ```bash
    docker-compose up -d
    ```

    (Elasticsearch는 `http://localhost:9200`에서 실행됩니다.)

2.  **MySQL 설정**

      * 수동으로 MySQL 서버를 실행합니다.
      * Spring Boot가 연결할 데이터베이스 스키마를 생성합니다.
      * `src/main/resources/application.yml` 파일 또는 환경 변수를 통해 DB 연결 정보를 (username, password) Spring Boot 서버에 전달해야 합니다. (현재 `application.yml`에는 DB 설정이 없습니다.)

### 3\. AI 리뷰 분석 서버 실행 (Flask)

1.  **Python 가상 환경 및 패키지 설치**
    AI 서버 디렉토리로 이동하여 필요한 패키지를 설치합니다.

    ```bash
    cd project5/python-classifier/
    pip install flask joblib
    ```

2.  **모델 및 DB 파일 확인**
    해당 디렉토리에 AI 모델(`hate_speech_classifier.pkl`)과 욕설 DB(`final_abuse_db.json`) 파일이 있는지 확인해야 합니다.

3.  **Flask 서버 실행**

    ```bash
    python forFlask.py
    ```

    (서버는 `http://localhost:5000`에서 실행됩니다.)

### 4\. 백엔드 서버 실행 (Spring Boot)

1.  **환경 변수 (API 키) 설정**
    `src/main/resources/application.yml` 파일에 Kakao API 키를 설정해야 합니다.

    ```yaml
    kakao:
      js-key: ${KAKAO_JS_KEY:실제_JS_키}
      rest-api-key: ${KAKAO_REST_API_KEY:실제_REST_API_키}
    ```

2.  **Gradle 빌드 및 실행**
    프로젝트 루트 디렉토리에서 Gradle을 사용하여 빌드하고 실행합니다.

    ```bash
    ./gradlew build
    java -jar build/libs/project-0.0.1-SNAPSHOT.jar
    ```

    (서버는 `http://localhost:8080`에서 실행됩니다.)

-----

##  AI 리뷰 분석 서버 (Flask) 상세

`forFlask.py` 파일은 리뷰 텍스트를 분석하기 위한 API 서버를 정의합니다.

### 주요 로직

1.  **욕설 DB 우선 탐지**: 요청된 텍스트를 `final_abuse_db.json`의 패턴과 비교하여 1차 필터링합니다.
2.  **AI 모델 예측**: 1차에서 탐지되지 않은 경우, 로드된 `hate_speech_classifier.pkl` 머신러닝 모델을 사용하여 혐오 표현 여부를 예측합니다.
3.  **결과 반환**: 혐오 표현 여부(`is_hate_speech`)와 예측된 라벨(`predicted_labels`)을 JSON으로 반환합니다.

### API 엔드포인트

  * `GET /health`: 서버의 상태와 모델 로드 여부를 확인합니다.
  * `POST /predict` (권장) 또는 `/filter_comment`: 단일 텍스트를 분석합니다.
      * **Request Body**: `{"comment": "분석할 텍스트"}`
      * **Response Body**: `{"comment": "원본 텍스트", "is_hate_speech": true/false, "predicted_labels": ["욕설/비속어", ...]}`
  * `POST /predict_batch`: 여러 텍스트를 배치로 분석합니다.
      * **Request Body**: `{"comments": ["텍스트1", "텍스트2"]}`

### 혐오 표현 라벨

모델이 예측하는 혐오 표현의 종류입니다.

  * `0`: 욕설/비속어
  * `1`: 성차별/성적 혐오
  * `2`: 인종/국적 차별
  * `3`: 지역/정치적 혐오 표현
  * `4`: 외모 비하
  * `5`: 특정 세대 혐오
  * `6`: 종교/사회집단 혐오
  * `7`: 기타 공격/모욕
  * `8`: 혐오 표현 아님
