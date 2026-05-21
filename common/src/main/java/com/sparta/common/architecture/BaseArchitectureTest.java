package com.sparta.common.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public abstract class BaseArchitectureTest {

    /**
     * 아직 모든 도메인이 구현되지 않은 개발 초기 단계에서는 true로 설정하여
     * 특정 레이어의 클래스가 없더라도 테스트를 통과(skip)하도록 관리합니다.
     */
    protected static final boolean ALLOW_EMPTY = true;

    /**
     * [Presentation 계층 규칙]
     * 1. 클래스는 'Controller'로 끝나야 함.
     * 2. @RestController 어노테이션 필수, @Controller 사용 금지.
     * 3. ..presentation.controller 패키지 아래에 위치해야 함.
     */
    @ArchTest
    static final ArchRule presentation_layer_naming_rule =
            classes().that().haveSimpleNameEndingWith("Controller")
                    .should().resideInAPackage("..presentation.controller..")
                    // allowEmptyShould
                    // -> true: 조건에 매칭되는 클래스가 없을 때 테스트 통과 (초기 구현 단계에서 true 설정 -> skip처럼 동작)
                    // -> false: 누락 강제 확인
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("Controller 클래스는 반드시 ..presentation.controller 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule presentation_layer_annotation_rule =
            classes().that().resideInAPackage("..presentation.controller..")
                    .should().beAnnotatedWith(RestController.class)
                    .andShould().notBeAnnotatedWith(org.springframework.stereotype.Controller.class)
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("Presentation 계층의 controller는 @RestController를 사용해야 하며, @Controller는 금지됩니다.");

    /**
     * [Application 계층 규칙]
     * 1. 클래스는 'Service'로 끝나야 함.
     * 2. 클래스명이 Service로 끝나면 @Service 어노테이션 필수.
     * 3. ..application.service 패키지 아래에 위치해야 함.
     */
    @ArchTest
    static final ArchRule application_layer_naming_rule =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().resideInAPackage("..application.service..")
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("Service 클래스는 반드시 ..application.service 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule application_layer_annotation_rule =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().beAnnotatedWith(Service.class)
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("Service 파일은 @Service 어노테이션이 필수입니다.");

    /**
     * [Domain 계층 규칙]
     * 1. JPA 엔티티(@Entity)는 반드시 ..domain.core 아래에 위치해야 함.
     * 2. ..domain 패키지 내 enum 클래스는 반드시 ..domain.core 아래에 위치해야 함.
     * 3. 순수 자바 Repository 인터페이스는 'Repository'로 끝나야 하며 ..domain.repository 아래에 위치해야 함.
     *    (JpaRepository를 상속하는 인터페이스는 JpaRepository로 끝나며 infrastructure에 위치)
     */
    @ArchTest
    static final ArchRule domain_layer_entity_location_rule =
            classes().that().areAnnotatedWith(Entity.class)
                    .should().resideInAPackage("..domain.core..")
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("JPA @Entity 클래스는 반드시 ..domain.core 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule domain_layer_enum_location_rule =
            classes().that().resideInAPackage("..domain..")
                    .and().areEnums()
                    .should().resideInAPackage("..domain.core..")
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("Domain 계층의 enum 클래스는 반드시 ..domain.core 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule domain_layer_repository_naming_rule =
            classes().that().haveSimpleNameEndingWith("Repository")
                    .and().areInterfaces()
                    .and().haveSimpleNameNotContaining("Jpa")
                    .should().resideInAPackage("..domain.repository..")
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("순수 Repository 인터페이스는 ..domain.repository 패키지 아래에 위치해야 합니다.");

    /**
     * [Infrastructure 계층 규칙]
     * 1. JpaRepository를 상속하는 인터페이스는 'JpaRepository'로 끝나야 하며 ..infrastructure 아래에 위치해야 함.
     * 2. Domain Repository 구현체는 'RepositoryImpl'로 끝나야 하며 ..infrastructure 아래에 위치해야 함.
     */
    @ArchTest
    static final ArchRule infrastructure_layer_jpa_repository_naming_rule =
            classes().that().haveSimpleNameEndingWith("JpaRepository")
                    .and().areInterfaces()
                    .should().resideInAPackage("..infrastructure..")
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("JpaRepository 인터페이스는 반드시 ..infrastructure 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule infrastructure_layer_repository_impl_rule =
            classes().that().haveSimpleNameEndingWith("RepositoryImpl")
                    .should().resideInAPackage("..infrastructure..")
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("RepositoryImpl 클래스는 반드시 ..infrastructure 패키지 아래에 위치해야 합니다.");

    /**
     * [고급 제약 규칙]
     * 1. Controller 계층에서 @Transactional 사용 금지
     */
    @ArchTest
    static final ArchRule no_transactional_in_presentation_rule =
            noClasses().that().resideInAPackage("..presentation..")
                    .should().beAnnotatedWith(Transactional.class)
                    .allowEmptyShould(ALLOW_EMPTY)
                    .as("Presentation 계층(Controller)에서는 @Transactional을 사용할 수 없습니다.");

    /**
     * [4계층 DDD 아키텍처 의존성 규칙]
     * 사용자 정의 접근 규칙을 따릅니다.
     * 1. 상위 계층(Application, Domain)은 하위 기술 계층(Infrastructure)을 절대 직접 import 하지 않습니다.
     * 2. 의존성은 언제나 외곽(Infrastructure)에서 내부(Application, Domain)를 향해 DIP로 구현됩니다.
     * 3. 런타임 의존성은 Spring Container의 의존성 주입(DI)을 통해 제어 역전으로 결합됩니다.
     */
    @ArchTest
    static final ArchRule layered_architecture_rule = layeredArchitecture()
            .consideringAllDependencies()
            .layer("Presentation").definedBy("..presentation..")
            .layer("Application").definedBy("..application..")
            .layer("Domain").definedBy("..domain..")
            .layer("Infrastructure").definedBy("..infrastructure..")

            // Presentation: 오직 외부만 바라보며, 내부로는 Application 레이어에만 접근 가능
            .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
            // Application: Presentation과 Infrastructure(구현체) 모두 접근할 수 있어야 함 (DIP)
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation", "Infrastructure")
            // Domain: Application, Infrastructure(구현체), Global(초기화)에서 접근 가능. Presentation은 금지(DTO가 대신함)
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            // Infrastructure: Infrastructure는 그 누구도 직접 접근(import) 금지.
            // (Application과 Domain은 오직 인터페이스만 바라보며, 실제 구현체는 스프링이 DI로 주입합니다.)
            .whereLayer("Infrastructure").mayNotBeAccessedByAnyLayer()
            
            .allowEmptyShould(ALLOW_EMPTY)
            .as("도메인 내 4계층(DDD) 의존성 규칙을 위반했습니다.");

    /**
     * 도메인별 키워드 포함 여부 통합 검증 유틸리티 (English Name)
     */
    public static ArchRule domain_prefix_naming_rule(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return CompositeArchRule.of(List.of(
                        // 1. Controller 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".presentation.controller..")
                                .and().haveSimpleNameEndingWith("Controller")
                                .should().haveSimpleNameContaining(prefix),

                        // 2. Service 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".application.service..")
                                .and().haveSimpleNameEndingWith("Service")
                                .should().haveSimpleNameContaining(prefix),

                        // 3. 엔티티 검증 (domain.core 패키지)
                        classes().that().resideInAPackage(".." + lowerPrefix + ".domain.core..")
                                .and().areAnnotatedWith(Entity.class)
                                .should().haveSimpleNameContaining(prefix),

                        // 4. 순수 Repository 인터페이스 검증 (domain.repository 패키지)
                        classes().that().resideInAPackage(".." + lowerPrefix + ".domain.repository..")
                                .and().haveSimpleNameEndingWith("Repository")
                                .and().areInterfaces()
                                .should().haveSimpleNameContaining(prefix),

                        // 5-1. RepositoryImpl 구현체 검증
                        //     (.or() 는 앞의 패키지 필터를 무시하므로 JpaRepository 와 분리하여 별도 규칙으로 정의)
                        classes().that().resideInAPackage(".." + lowerPrefix + ".infrastructure.repository..")
                                .and().haveSimpleNameEndingWith("RepositoryImpl")
                                .should().haveSimpleNameContaining(prefix),

                        // 5-2. JpaRepository 인터페이스 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".infrastructure.repository..")
                                .and().haveSimpleNameEndingWith("JpaRepository")
                                .should().haveSimpleNameContaining(prefix)
                ))
                .allowEmptyShould(ALLOW_EMPTY)
                .as(prefix + " 도메인 내 핵심 클래스들은 반드시 '" + prefix + "' 키워드를 포함해야 합니다.");
    }
}
