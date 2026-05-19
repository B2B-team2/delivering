package com.sparta.common.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public abstract class BaseArchitectureTest {

    /**
     * [Presentation 계층 규칙]
     * 1. 클래스는 'Controller'로 끝나야 함.
     * 2. @RestController 어노테이션 필수, @Controller 사용 금지.
     * 3. 별도의 서브 패키지 없이 ..presentation 바로 아래에 위치해야 함.
     */
    @ArchTest
    static final ArchRule presentation_layer_naming_rule =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..controller")
                    .allowEmptyShould(true)
                    .as("Controller 클래스는 반드시 ..controller 패키지 바로 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule presentation_layer_annotation_rule =
            classes().that().resideInAPackage("..controller")
                    .should().beAnnotatedWith(RestController.class)
                    .andShould().notBeAnnotatedWith(org.springframework.stereotype.Controller.class)
                    .allowEmptyShould(true)
                    .as("Presentation 계층의 controller는 @RestController를 사용해야 하며, @Controller는 금지됩니다.");

    /**
     * [Application 계층 규칙]
     * 1. 클래스는 'Service'로 끝나야 함.
     * 2. 클래스명이 Service로 끝나면 @Service 어노테이션 필수.
     * 3. 별도의 서브 패키지 없이 ..application 바로 아래에 위치해야 함.
     */
    @ArchTest
    static final ArchRule application_layer_naming_rule =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage("..service")
                    .allowEmptyShould(true)
                    .as("Service 클래스는 반드시 ..service 패키지 바로 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule application_layer_annotation_rule =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().beAnnotatedWith(Service.class)
                    .allowEmptyShould(true)
                    .as("Service 파일은 @Service 어노테이션이 필수입니다.");

    /**
     * [Domain 계층 규칙]
     * 1. 레포지토리 인터페이스는 'Repository'로 끝나야 하며 ..domain 바로 아래에 위치해야 함.
     */
    @ArchTest
    static final ArchRule domain_layer_repository_naming_rule =
            classes().that().haveSimpleNameEndingWith("Repository")
                    .and().areInterfaces()
                    .should().resideInAPackage("..domain")
                    .allowEmptyShould(true)
                    .as("Repository 인터페이스는 ..domain 패키지 바로 아래에 위치해야 합니다.");

    /**
     * [고급 제약 규칙]
     * 1. Controller 계층에서 @Transactional 사용 금지
     */
    @ArchTest
    static final ArchRule no_transactional_in_presentation_rule =
            noClasses().that().resideInAPackage("..presentation..")
                    .should().beAnnotatedWith(Transactional.class)
                    .allowEmptyShould(true)
                    .as("Presentation 계층(Controller)에서는 @Transactional을 사용할 수 없습니다.");

    /**
     * [4계층 DDD 아키텍처 의존성 규칙]
     * 사용자 정의 접근 규칙을 따릅니다.
     */
    @ArchTest
    static final ArchRule layered_architecture_rule = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Presentation").definedBy("..presentation..")
            .layer("Application").definedBy("..application..")
            .layer("Domain").definedBy("..domain..")
            .layer("Infrastructure").definedBy("..infrastructure..")

            // Presentation(표현)은 대문이므로 그 어떤 레이어도 접근할 수 없음
            .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
            // Application(응용)은 오직 Presentation에서만 접근 가능
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation")
            // Domain(도메인)은 상위 계층인 Application에서만 접근 가능
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application")
            // 인프라는 실행 지휘관인 Application과 껍데기를 쥐고 있는 Domain 둘 다 접근 허용
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application", "Domain")
            .allowEmptyShould(true)
            .as("도메인 내 4계층(DDD) 의존성 규칙을 위반했습니다.");

    /**
     * 도메인별 접두사 일치 여부 통합 검증 유틸리티 (English Name)
     */
    public static ArchRule domain_prefix_naming_rule(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return CompositeArchRule.of(List.of(
                        // 1. Controller 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".presentation..")
                                .and().haveSimpleNameEndingWith("Controller")
                                .should().haveSimpleNameStartingWith(prefix),

                        // 2. Service 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".application..")
                                .and().haveSimpleNameEndingWith("Service")
                                .should().haveSimpleNameStartingWith(prefix),

                        // 3. Repository 인터페이스 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".domain..")
                                .and().haveSimpleNameEndingWith("Repository")
                                .should().haveSimpleNameStartingWith(prefix),

                        // 4. Repository 구현체 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".infrastructure..")
                                .and().haveSimpleNameEndingWith("RepositoryImpl")
                                .should().haveSimpleNameStartingWith(prefix)
                ))
                .allowEmptyShould(true)
                .as(prefix + " 도메인 내 핵심 클래스들은 반드시 '" + prefix + "' 접두사로 시작해야 합니다.");
    }
}
