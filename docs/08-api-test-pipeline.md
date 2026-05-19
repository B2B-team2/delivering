# 08 - API Specification Sync Guide (AI Manual)

> Spring Boot 코드 기반 OpenAPI(Swagger) 명세 생성 및 AI 에이전트를 이용한 Postman 수동 동기화 가이드

---

## 1. 개요
본 문서는 백엔드 코드의 변경 사항이 API 명세서에 반영되도록 보장하는 **AI 기반 수동 동기화** 가이드를 제공합니다.

CI 과정에서 자동으로 동기화하는 대신, 개발자가 필요 시 AI 에이전트(Gemini CLI 등)에게 요청하여 최신 코드를 분석하고 Postman 워크스페이스에 즉시 반영하는 방식을 취합니다.

- **핵심 목표**: 개발자가 원하는 시점에 코드와 API 문서 간의 동기화 수행 (중복 방지 포함)
- **사용 기술**: Springdoc OpenAPI, Postman API, AI 에이전트
- **대상 워크스페이스**: `jojo Workspace` (ID: `e51d0d84-6da4-460a-bdf0-2d01d1a88fd6`)

---

## 2. 동기화 프로세스

1. **코드 수정**: 개발자가 Spring Boot 컨트롤러 또는 DTO 코드 수정
2. **AI 요청**: 개발자가 AI 에이전트에게 본 문서에 정의된 프롬프트를 사용하여 동기화 요청
3. **지능형 동기화 수행**:
    - AI가 `jojo Workspace` 내에 동일한 이름의 컬렉션이나 API 스펙이 있는지 먼저 검색합니다.
    - **존재하는 경우**: 해당 ID를 타겟으로 기존 명세와 컬렉션을 **업데이트(Update/Overwrite)**합니다.
    - **존재하지 않는 경우**: 신규로 **생성(Create)**합니다.

---

## 3. 서비스별 사전 설정 가이드

AI가 OpenAPI 명세를 추출할 수 있도록 각 마이크로서비스의 `build.gradle`에 다음과 같은 플러그인 설정이 필요합니다.

### 3.1 `build.gradle` 설정 예시 (예: user-service)

```gradle
plugins {
    id 'org.springdoc.openapi-gradle-plugin' version '1.9.0'
}

dependencies {
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0'
}

openApi {
    apiDocsUrl.set("http://localhost:19091/v3/api-docs")
    outputDir.set(file("$buildDir/docs"))
    outputFileName.set("openapi.json")
    waitTimeInSeconds.set(30)
}
```

---

## 4. AI 에이전트 요청 프롬프트

API 명세 동기화가 필요할 때, AI 에이전트에게 다음 프롬프트를 복사하여 전달하세요.

### 4.1 지능형 동기화 프롬프트 (중복 방지)
> **프롬프트**:
> `docs/08-api-test-pipeline.md` 문서를 참고해서, 현재 프로젝트의 마이크로서비스 OpenAPI 스펙을 Postman **jojo Workspace**(`e51d0d84-6da4-460a-bdf0-2d01d1a88fd6`)에 동기화해줘.
> 
> **수행 가이드**:
> 1. 먼저 `./gradlew generateOpenApiDocs`를 실행해서 최신 `openapi.json`을 생성해.
> 2. Postman 워크스페이스 내에 해당 서비스와 이름이 같은 **기존 컬렉션이 있는지 검색**해.
> 3. 만약 기존 컬렉션이 있다면, 신규로 임포트하지 말고 **해당 ID의 내용을 최신 스펙으로 업데이트(Overwrite)**해줘.
> 4. 만약 없다면, 그때는 신규로 임포트(Create)를 진행해.
> 5. 완료 후 어떤 컬렉션이 업데이트되었는지 요약해줘.

---

## 5. 지능형 동기화 시 주의사항 (Critical Precautions)

AI 에이전트가 동기화를 수행할 때 반드시 다음 사항을 준수해야 합니다.

### 5.1 Gateway URL 매핑 규칙
- **절대 금지**: `http://localhost:8080/company/api/v1/...` 처럼 Gateway 설정상의 서비스 프리픽스를 URL에 포함하지 마세요.
- **준수 사항**: 모든 요청 URL은 반드시 **`http://localhost:8080/api/v1/...`** 형식을 유지해야 합니다. 프로젝트 규칙에 따라 모든 API는 게이트웨이 포트(8080)와 글로벌 프리픽스(`/api/v1`)를 기반으로 직접 호출 가능한 주소로 작성되어야 합니다.

### 5.2 JSON Body 포맷팅
- **불필요한 줄바꿈 제거**: Body 내부에 불필요한 개행 문자(`\n\n`)가 포함되어 데이터가 지저분하게 보이지 않도록 하세요.
- **가독성 유지**: Postman UI에서 바로 읽고 수정할 수 있도록 표준 JSON 들여쓰기를 적용하여 깔끔한 `raw` 텍스트로 입력하세요. 탈출 문자(`\n`)가 UI 상에 그대로 노출되지 않도록 실제 개행이 반영된 텍스트로 관리되어야 합니다.

---

## 6. 일반 주의사항

- **Postman API Key**: AI 에이전트가 Postman API를 호출할 수 있도록 환경 변수 또는 설정에 유효한 `POSTMAN_API_KEY`가 포함되어 있어야 합니다.
- **로컬 빌드 환경**: `./gradlew` 명령어가 정상적으로 실행될 수 있도록 로컬에 Java 환경이 구성되어 있어야 합니다.
- **애노테이션 필수**: 완벽한 문서 생성을 위해 컨트롤러에 `@Tag`, `@Operation` 등의 Swagger 애노테이션이 적절히 작성되었는지 확인하세요.
