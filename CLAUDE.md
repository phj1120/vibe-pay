# 프로젝트 개발 가이드

## 기술 스택 및 버전

- **Frontend**: Next.js 14.2.x, React 18.3.x
- **Backend**: Spring Boot 3.3.1, Java 21
- **Database**: PostgreSQL
- **ORM**: MyBatis

---

## 개발 가이드 문서

각 영역별 상세 가이드는 아래 파일을 참조하세요. 
**해당 영역의 작업이 처음 언급될 때 한 번만 읽고, 이후에는 처음 읽은 내용을 기반으로 작업합니다.**

### Frontend (FO)
- 경로: `/docs/fo-guide.md`
- Next.js, React 개발 시 반드시 준수

### Backend (API)
- 경로: `/docs/api-guide.md`
- Spring Boot API 개발 시 반드시 준수
- **MyBatis XML 매퍼에서 SQL 작성 시에는 Database 가이드도 함께 참조**

### Database (SQL)
- 경로: `/docs/sql-guide.md`
- PostgreSQL 쿼리 작성 시 반드시 준수
- **MyBatis XML 매퍼의 SQL 작성 시에도 반드시 준수**

---

## 작업 방식

1. **Frontend 작업**이 언급되면 → `/docs/fo-guide.md` 읽기 (최초 1회)
2. **Backend 작업**이 언급되면 → `/docs/api-guide.md` 읽기 (최초 1회)
3. **Database 작업 또는 MyBatis 쿼리 작성**이 언급되면 → `/docs/sql-guide.md` 읽기 (최초 1회)
4. **Backend 작업 중 MyBatis XML 작성 시** → 두 가이드 모두 참조 (API 가이드 + Database 가이드)
5. 이후 동일 영역 작업은 처음 읽은 가이드를 기반으로 진행

---

## 주의사항

- 각 가이드는 해당 영역의 코딩 컨벤션과 필수 규칙을 담고 있으므로 반드시 준수
- **MyBatis XML 매퍼 작성 시**: Java 코드는 API 가이드, SQL 쿼리는 Database 가이드 준수
- 가이드에 없는 내용은 일반적인 Best Practice 적용
- 가이드 내용과 충돌 시 가이드 우선
