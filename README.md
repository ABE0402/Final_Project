홍어라운드 (HongAround)
홍어라운드는 홍대 지역의 카페를 추천 및 검색할 수 있는 웹 서비스입니다. 사용자의 성별, 연령, 검색 로그 등 다양한 정보를 바탕으로 맞춤형 카페 추천을 제공하며, 카카오맵을 통해 위치 정보를 확인할 수 있습니다. 또한, AI 기반 리뷰 감정 분석 및 욕설 필터링 기능을 제공합니다.

-주요 기능

1.맞춤형 카페 추천
사용자의 연령, 성별에 따라 카페페이지 클릭, 리뷰, 즐겨찾기, 예약에 이벤트를 적용하고 각 이벤트에 값을 달리하여 가중치가 높은 카페순으로 추천합니다.
사용자의 검색 로그를 디비에 저장하여 데이터를 자카드 유사도 및 점수 기반 알고리즘을 활용하여 개인화된 추천 결과를 제공합니다.

2.검색 기능
키워드 입력 시 높은 유사도 순으로 카페를 검색 결과로 표시합니다.
카페의 태그(분위기, 우선순위, 편의 및 서비스, 동반인, 종류)를 기반으로 세분화된 검색이 가능합니다.

3.카카오맵 연동
카카오맵 API를 활용하여 카페의 위치를 지도에서 확인할 수 있는 기능을 제공합니다.

4.리뷰 및 감정 분석
허깅페이스 외부 AI 모델을 활용하여 리뷰 작성 시 욕설 필터링 및 감정 분석을 진행합니다.

5.예약(알림) 및 즐겨찾기
카페 예약시 시간, 인원수, 날짜를 선택 가능하고 예약시 사용자와 점주에게 알림이 갑니다. 
예약을 점주가 취소, 승인이 가능합니다. 
즐겨찾기시 사용자의 마이페이지에서 확인 가능하고 즐겨찾기 취소도 가능합니다.

6.유저, 점주, 관리자 기능 구분
유저: 마이 페이지, 마이페이지, 리뷰 작성, 예약, 즐겨찾기 등 
점주: 점주 페이지, 카페 등록, 태그 선택, 리뷰 관리 등
관리자: 관리자 페이지, 점주 신청 승인, 가게 등록 승인, 리뷰/유저/가게 관리 등

-기술 스택
빌드/의존성:Gradle + 플러그인: java, org.springframework.boot:3.1.0, io.spring.dependency-management:1.1.7, org.jetbrains.kotlin.jvm
언어: JAVA(java 17.0.15)
런타임: Kotlin 1.9.24
백엔드: Spring Framework (IntelliJ IDEA)
개발 편의: spring-boot-devtools
데이터베이스: MySQL(mysql  Ver 8.0.42)
지도 API: Kakao Map API
AI 리뷰 분석: 허깅페이스 AI 모델 (Python, Flask, Visual Studio Code)
프론트엔드: mustache, js, css
보일러플레이트/유틸:Lombok(compileOnly + annotationProcessor)

-Spring Boot 스타터
웹: spring-boot-starter-web
뷰: spring-boot-starter-mustache
데이터: spring-boot-starter-data-jpa
검증: spring-boot-starter-validation
보안: spring-boot-starter-security, spring-boot-starter-oauth2-client

설치 및 실행 방법
1. 백엔드(Spring) 서버 실행
backend 디렉토리에서 필요한 의존성 설치 및 빌드
MySQL 데이터베이스 설정 및 연동
서버 실행
2. AI 리뷰 분석 서버 실행
Python 가상환경 생성 및 패키지 설치
Flask 서버 실행
3. 허기페이스 AI 모델: 리뷰 감정 분석 및 욕설 필터링
4. 환경 변수 및 API 키 설정
Kakao Map API 키, 허기페이스 모델 연동 등 필요한 환경 변수 설정
사용된 외부 API 및 라이브러리
Kakao Map API: 카페 위치 지도 표시
