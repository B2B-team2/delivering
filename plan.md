# API 테스트 및 문서 가이드 작성 계획 (Notion)

이 문서는 노션의 **'API 테스트 및 문서 가이드'** 페이지에 작성할 구체적인 내용과 구조를 제안합니다. 팀원들이 로컬 환경에서 API를 쉽게 테스트하고, 최신 API 명세를 Postman으로 관리할 수 있도록 가이드를 제공하는 것이 목적입니다.

---

## 1. 문서 구성 및 상세 내용

### 📋 섹션 1: 통합 Swagger UI (Gateway)
- **개요**: API Gateway(8080)를 통해 모든 마이크로서비스의 API 명세를 한곳에서 확인할 수 있는 통합 Swagger 환경 설명.
- **주요 특징**:
    - 서비스별 셀렉박스를 통한 간편한 전환.
    - Gateway 포트(8080) 하나로 모든 서비스 테스트 가능.
- **접속 경로**:
    - Swagger UI: `http://localhost:8080/swagger-ui/index.html`
    - OpenAPI Spec (JSON): `http://localhost:8080/{service-name}/v3/api-docs`

### 📥 섹션 2: Postman 수동 Import 가이드 (Easy Way)
- **설명**: Swagger에서 생성된 JSON 명세를 활용하여 Postman 컬렉션을 즉시 생성하는 방법.
- **단계별 가이드**:
    1. Swagger JSON 경로 접속 (예: `http://localhost:8080/company-service/v3/api-docs`).
    2. JSON 내용 복사 또는 파일 저장.
    3. Postman 상단 **'Import'** 버튼 클릭.
    4. 복사한 JSON 붙여넣기 또는 파일 업로드.
    5. 생성된 컬렉션 확인 및 환경 변수(`baseUrl`) 설정.
- **장점**: 수동으로 엔드포인트를 하나씩 만드는 것보다 훨씬 빠르고 정확함.

### 🤖 섹션 3: Postman MCP를 활용한 자동화
- **개요**: Gemini CLI 및 Postman MCP 도구를 사용하여 API 명세를 Postman에 자동으로 반영하는 고도화된 방법.
- **수행 절차**:
    - `getPostmanContextOverview` 호출을 통한 워크플로우 이해.
    - `searchPostmanElements`를 통한 기존 컬렉션 확인.
    - `generateCollection` 또는 `syncCollectionWithSpec` 명령어를 통한 자동 동기화 예시.
- **목표**: 코드 변경 시 Postman 컬렉션이 항상 최신 상태를 유지하도록 유도.

### 🛠️ 섹션 4: 로컬 테스트 환경 최적화
- **환경 변수 관리**: Postman Environment 설정을 통한 `gateway_url` (http://localhost:8080) 관리법.
- **주의 사항**:
    - **서비스 직접 호출 금지**: 반드시 Gateway를 거쳐 호출 (`api/v1/...`).
    - **공통 응답 규격**: `common` 모듈의 `ApiResponse` 형식을 준수하고 있는지 확인.

---

[노션(Notion) 전용 포맷 규칙]
1. 제목은 무조건 큰 제목(##), 중간 제목(###)의 마크다운만 사용해 줘. (노션에 붙이면 제목 블록으로 자동 변환됨)
2. 핵심 키워드는 **두껍게** 처리하고, 가독성을 위해 한 줄에 글이 너무 길어지지 않게 줄바꿈(엔터)을 자주 쳐줘.
3. 꿀팁이나 요약은 노션 인용구로 변환되도록 맨 앞에 `>` (블록인용)를 꼭 붙여줘.
4. 자바나 SQL 코드가 들어갈 때는 무조건 구문 강조가 먹히는 코드 블록(```java, ```sql)을 감싸서 줘.

## 2. 가독성 향상 전략
- **시각적 강조**: 주요 경로는 코드 블럭(`code block`)으로 처리.
- **콜아웃(Callout)**: 주의사항이나 팁은 노션의 콜아웃 블럭을 사용하여 강조.
- **이미지/토글**: 단계별 가이드는 토글 기능을 활용하여 핵심만 먼저 노출.

---

## 3. 실행 계획
1. **내용 검토**: 위 계획에 대해 팀 내 피드백 수렴.
2. **노션 작성**: 확정된 내용을 바탕으로 노션 페이지에 한글로 상세 내용 작성.
3. **검증**: 가이드대로 따라했을 때 정상적으로 Postman에 반영되는지 최종 확인.
