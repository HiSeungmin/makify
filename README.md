# 🏆 Makify - 챌린지 플랫폼

습관을 만들고, 목표를 달성하는 챌린지 플랫폼

## 📖 프로젝트 소개

**Makify**는 사용자들이 개인적인 목표나 습관을 달성할 수 있도록 돕는 챌린지 플랫폼입니다. 
예치금 시스템을 통해 동기부여를 제공하고, 실시간 인증을 통해 목표 달성을 재미있게 만듭니다.

🪄 [챌린지 웹 서비스 바로가기](https://makiify.life)


## ✨ 주요 기능

* **챌린지 생성 및 참여**: 다양한 카테고리의 챌린지 생성 및 참가
* **예치금 시스템**: 목표 달성 동기부여를 위한 예치금 기반 참여
* **실시간 인증**: 카메라/앨범을 통한 챌린지 인증
* **챌린지 검색**: 카테고리별 필터링 및 검색 기능
* **마이페이지**: 개인 챌린지 현황 및 통계 확인
* **안전한 결제**: 아임포트 연동을 통한 안전한 결제 시스템

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


