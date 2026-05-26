package com.sparta.operationsservice;

import com.sparta.common.architecture.BaseArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.sparta.operationsservice.claim",
        importOptions = {
                ImportOption.DoNotIncludeJars.class,
                ImportOption.DoNotIncludeTests.class
        }
)
public class ClaimArchitectureTest extends BaseArchitectureTest {
    @ArchTest
    public static final ArchRule 클레임_도메인_네이밍_규칙 = domain_prefix_naming_rule("Claim");
}
