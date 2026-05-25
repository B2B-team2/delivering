package com.sparta.deliveryservice;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.springframework.boot.test.context.SpringBootTest;

import static com.sparta.common.architecture.BaseArchitectureTest.domain_prefix_naming_rule;

@AnalyzeClasses(packages = "com.sparta.deliveryservice",
        importOptions = {
                ImportOption.DoNotIncludeJars.class,  // 외부 라이브러리 스캔 제외 (성능 최적화 핵심)
                ImportOption.DoNotIncludeTests.class  // 테스트 코드끼리 검사하는 것 제외
        }
)

@SpringBootTest
class DeliveryServiceApplicationTests {

    @ArchTest
    public static final ArchRule 배달_도메인_네이밍_규칙 = domain_prefix_naming_rule("Delivery");
}