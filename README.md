# 일정 관리 시스템 (Schedule Management System)

Java Swing을 기반으로 한 데스크톱 일정 관리 시스템입니다. 사용자가 일정을 효율적으로 관리하고, 생산성을 향상시킬 수 있도록 설계된 종합적인 일정 관리 애플리케이션입니다.

## 🚀 주요 기능

### 핵심 기능
- **일정 관리**: 일정 생성/수정/삭제, 반복 일정 설정, 일정 공유
- **할 일 관리**: 할 일 목록, 우선순위 설정, 완료 상태 관리
- **캘린더 통합**: 일정을 시각적으로 확인할 수 있는 캘린더 인터페이스
- **반복 일정**: 일간, 주간, 월간, 연간 반복 일정 지원
- **위치 매핑**: 일정과 특정 위치 연결
- **우선순위 관리**: 일정 우선순위 설정 및 관리
- **카테고리 분류**: 카테고리별 일정 정리

### 고급 기능
- **스마트 추천**: 사용자 패턴 분석을 통한 AI 기반 일정 추천
- **통계 및 분석**: 종합적인 일정 통계 및 분석
- **알림 시스템**: 다가오는 일정에 대한 실시간 알림
- **데이터 저장**: 자동 데이터 저장 및 복원
- **사용자 관리**: 사용자 프로필을 지원하는 다중 사용자 시스템
- **설정 관리**: 사용자 정의 가능한 애플리케이션 설정
- **내보내기/가져오기**: 일정 데이터 내보내기 및 가져오기 기능

### 기술적 특징
- **현대적인 GUI**: Java Swing 기반 사용자 인터페이스
- **데이터 저장**: 파일 기반 데이터 영속성 시스템
- **이벤트 기반 아키텍처**: 반응형 이벤트 처리
- **모듈화 설계**: 잘 구조화된 유지보수 가능한 코드베이스
- **오류 처리**: 종합적인 오류 관리
- **로깅**: 디버깅을 위한 애플리케이션 로깅

## 🛠️ 기술 스택

- **개발 언어**: Java 23
- **GUI 프레임워크**: Java Swing
- **빌드 도구**: Maven
- **데이터 저장**: 파일 기반 (JSON/Properties)
- **아키텍처**: MVC 패턴

## 📋 요구사항

- Java Development Kit (JDK) 23 이상
- Maven 3.6 이상

## 🔧 설치 및 실행

### 1. 저장소 클론
```bash
git clone https://github.com/sukyungi/schedule-management-system.git
cd schedule-management-system
```

### 2. 프로젝트 빌드
```bash
mvn clean compile
```

### 3. 애플리케이션 실행
```bash
mvn exec:java -Dexec.mainClass="Main"
```

또는 대안으로:
```bash
java -cp target/classes Main
```

## 📁 프로젝트 구조

```
src/
├── Main.java                    # 애플리케이션 진입점
├── ScheduleGUI.java             # 메인 GUI 컨트롤러
├── ScheduleManager.java         # 핵심 일정 관리 로직
├── Schedule.java                # 일정 데이터 모델
├── Task.java                    # 할 일 데이터 모델
├── User.java                    # 사용자 데이터 모델
├── DataStorage.java             # 데이터 영속성 계층
├── NotificationManager.java     # 알림 시스템
├── ScheduleRecommender.java     # AI 추천 엔진
├── DashboardPanel.java          # 메인 대시보드 인터페이스
├── CalendarPanel.java           # 캘린더 뷰 컴포넌트
├── ScheduleListPanel.java       # 일정 목록 인터페이스
├── TaskListPanel.java           # 할 일 목록 인터페이스
├── StatisticsPanel.java         # 통계 및 분석
├── SettingsPanel.java           # 애플리케이션 설정
└── [기타 UI 컴포넌트들...]      # 추가 GUI 컴포넌트
```

## 📖 사용법

### 시작하기
1. 애플리케이션 실행
2. 새 사용자 계정 생성 또는 로그인
3. 첫 번째 일정 생성 시작
4. 캘린더 뷰를 사용하여 일정 시각화
5. 대시보드를 탐색하여 활동 개요 확인

### 주요 기능
- **대시보드**: 다가오는 일정 및 할 일 개요
- **캘린더 뷰**: 일정의 시각적 표현
- **일정 생성**: 사용하기 쉬운 일정 생성 마법사
- **할 일 관리**: 일정 내에서 할 일 추가 및 관리
- **통계**: 일정 사용에 대한 상세한 분석 보기
- **설정**: 애플리케이션 동작 및 외관 사용자 정의

## 🎯 주요 특징

- **직관적인 사용자 인터페이스**: 초보자도 쉽게 사용할 수 있는 직관적인 GUI
- **다양한 뷰 옵션**: 월간, 주간, 일간 뷰를 통한 유연한 일정 확인
- **스마트 알림 시스템**: 사용자 정의 알림 시간 설정
- **통계 및 분석**: 데이터 기반 생산성 분석
- **AI 기반 일정 추천**: 사용자 패턴 분석을 통한 개인화된 추천
- **반복 일정 관리**: 다양한 반복 패턴 지원
- **일정 공유 기능**: 팀워크를 위한 일정 공유 시스템

## 🔮 향후 계획

- [ ] 데이터베이스 연동 (MySQL, PostgreSQL)
- [ ] 웹 애플리케이션 버전 (Spring Boot)
- [ ] 모바일 앱 (Android/iOS)
- [ ] 클라우드 동기화 (Google Calendar, Outlook)
- [ ] AI 기능 강화 (머신러닝 기반 추천)

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 👨‍💻 개발자

- **개발자**: Sukyung Lim
- **GitHub**: [@sukyungi](https://github.com/sukyungi)
- **저장소**: [schedule-management-system](https://github.com/sukyungi/schedule-management-system)
