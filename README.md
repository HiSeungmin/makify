# 🏆 Makify - 챌린지 플랫폼

습관을 만들고, 목표를 달성하는 챌린지 플랫폼

## 📖 프로젝트 소개

**Makify**는 사용자들이 개인적인 목표나 습관을 달성할 수 있도록 돕는 챌린지 플랫폼입니다. 
예치금 시스템을 통해 동기부여를 제공하고, 실시간 인증을 통해 목표 달성을 재미있게 만듭니다.
스터디 모임에서 지각 벌금을 모임통장으로 관리하던 경험에서 시작했습니다. "8시까지 학원 도착하기", "벌금으로 커피 쏘기" 같은 소소한 약속들을 더 체계적으로 관리하고 싶었고,  
**챌린저스** 앱에서 영감을 받아 웹에서 직접 인증할 수 있는 나만의 챌린지 서비스를 구현했습니다.

🪄 [챌린지 웹 서비스 바로가기](https://makiify.life)

---

## ✨ 주요 기능

### 챌린지
- 챌린지 생성 / 목록 / 상세 조회
- 카테고리, 공개 여부, 인증 빈도 설정
- 예치금 기반 참여 시스템 (고정 / 자유 예치금)
- 비공개 챌린지 참여 코드 지원

### 인증
- 카메라 / 앨범 업로드로 인증 사진 제출
- 인증 시간대 제한 설정
- AWS S3 이미지 저장

### 검색
- **Trie + Redis** 기반 실시간 자동완성
- AOP 기반 검색 로그 수집

### 결제
- Iamport API 연동 예치금 결제
- Facade 패턴으로 결제 로직 분리
- BigDecimal 기반 금액 계산

### 보안 / 공통
- JWT 기반 인증 (Access + Refresh Token, Redis 저장)
- Spring AOP 로깅 (IP 추적, 민감정보 마스킹)

### 배포

- AWS EC2 + RDS + S3 연동 완료
- HTTPS 적용 (Nginx + Let's Encrypt)
- GitHub Actions CI/CD 자동 배포

---

## 🛠️ 기술 스택

### Backend
* **Java 17** - 최신 자바 LTS 버전
* **Spring Boot 3.2** - 메인 프레임워크
* **Spring Security** - 인증/인가 시스템
* **Spring Data JPA** - ORM 및 데이터 액세스
* **JWT** - 토큰 기반 인증

### Database
* **MySQL 8.0** - 메인 데이터베이스
* **Redis 7.0** - 캐싱 및 세션 스토어

### Frontend
* **Thymeleaf** - 서버사이드 템플릿 엔진
* **Bootstrap 5** - UI 프레임워크
* **JavaScript ES6+** - 클라이언트 사이드 로직

### Infrastructure
* **AWS EC2** - 서버 호스팅
* **Gradle** - 빌드 도구
* **Git** - 버전 관리


## 프로젝트 구조

```
src/main/java/com/xladmt/makify/
├── application/          # 애플리케이션 서비스 (Facade 패턴)
│   └── PaymentFacade.java
├── challenge/           # 챌린지 도메인
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── dto/
│   └── validator/
├── payment/            # 결제 도메인
│   ├── controller/
│   ├── service/
│   ├── repository/
│   └── dto/
├── member/             # 회원 도메인
│   ├── controller/
│   ├── service/
│   └── repository/
├── common/             # 공통 컴포넌트
│   ├── entity/         # 공통 엔티티
│   ├── constant/       # 상수 정의
│   ├── config/         # 설정 클래스
│   ├── exception/      # 예외 처리
│   └── validator/      # 검증 로직
└── MakifyApplication.java
```

## 주요 화면

### 홈페이지
서비스 소개 및 인기 챌린지 목록을 표시하며, 동영상 백그라운드로 시각적 임팩트를 제공합니다.

### 챌린지 목록
카테고리별 필터링(운동, 식습관, 자기계발 등), 검색 기능 및 정렬 옵션을 제공하며 페이지네이션을 지원합니다.

### 챌린지 상세
챌린지 정보 및 참여 조건을 표시하고, 현재 참여자 수 및 진행 상황을 보여줍니다. 결제 연동을 통한 참여 기능을 제공합니다.

### 마이페이지
참여 중인 챌린지 현황, 인증 기록 및 통계, 예치금 및 상금 관리 기능을 제공합니다.


