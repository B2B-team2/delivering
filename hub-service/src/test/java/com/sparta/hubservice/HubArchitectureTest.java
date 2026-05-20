package com.sparta.hubservice;

import com.sparta.common.architecture.BaseArchitectureTest;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.sparta.hubservice",
        importOptions = {
                ImportOption.DoNotIncludeJars.class,
                ImportOption.DoNotIncludeTests.class
        }
)
public class HubArchitectureTest extends BaseArchitectureTest {

    @ArchTest
    public static final ArchRule 허브_도메인_네이밍_규칙 = domain_prefix_naming_rule("Hub");
}
