package com.sparta.companyservice;


import com.sparta.common.architecture.BaseArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;

// 💡 중요: 여기서는 com.sparta.companyservice 패키지만 긁어오라고 대상을 바꿔줍니다!
@AnalyzeClasses(packages = "com.sparta.companyservice",
        importOptions = {
                ImportOption.DoNotIncludeJars.class,  // 외부 라이브러리 스캔 제외 (성능 최적화 핵심)
                ImportOption.DoNotIncludeTests.class  // 테스트 코드끼리 검사하는 것 제외
        }
)
public class CompanyArchitectureTest extends BaseArchitectureTest {
    // 공통 규칙(컨트롤러 네이밍 등)을 그대로 이어받으므로 여기도 텅 비워둡니다.
}