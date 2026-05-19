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
                    .allowEmptyShould(false)
                    .as("Controller 클래스는 반드시 ..presentation.controller 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule presentation_layer_annotation_rule =
            classes().that().resideInAPackage("..presentation.controller..")
                    .should().beAnnotatedWith(RestController.class)
                    .andShould().notBeAnnotatedWith(org.springframework.stereotype.Controller.class)
                    .allowEmptyShould(false)
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
                    .allowEmptyShould(false)
                    .as("Service 클래스는 반드시 ..application.service 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule application_layer_annotation_rule =
            classes().that().haveSimpleNameEndingWith("Service")
                    .should().beAnnotatedWith(Service.class)
                    .allowEmptyShould(false)
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
                    .allowEmptyShould(false)
                    .as("JPA @Entity 클래스는 반드시 ..domain.core 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule domain_layer_enum_location_rule =
            classes().that().resideInAPackage("..domain..")
                    .and().areEnums()
                    .should().resideInAPackage("..domain.core..")
                    .allowEmptyShould(false)
                    .as("Domain 계층의 enum 클래스는 반드시 ..domain.core 패키지 아래에 위치해야 합니다.");

    @ArchTest
    static final ArchRule domain_layer_repository_naming_rule =
            classes().that().haveSimpleNameEndingWith("Repository")
                    .and().areInterfaces()
                    .and().haveSimpleNameNotContaining("Jpa")
                    .should().resideInAPackage("..domain.repository..")
                    .allowEmptyShould(false)
                    .as("순수 Repository 인터페이스는 ..domain.repository 패키지 아래에 위치해야 합니다.");

    /**
     * [Infrastructure 계층 규칙]
     * 1. JpaRepository를 상속하는 인터페이스는 'JpaRepository'로 끝나야 하며 ..infrastructure 아래에 위치해야 함.
     */
    @ArchTest
    static final ArchRule infrastructure_layer_jpa_repository_naming_rule =
            classes().that().haveSimpleNameEndingWith("JpaRepository")
                    .and().areInterfaces()
                    .should().resideInAPackage("..infrastructure..")
                    .allowEmptyShould(false)
                    .as("JpaRepository 인터페이스는 반드시 ..infrastructure 패키지 아래에 위치해야 합니다.");

    /**
     * [고급 제약 규칙]
     * 1. Controller 계층에서 @Transactional 사용 금지
     */
    @ArchTest
    static final ArchRule no_transactional_in_presentation_rule =
            noClasses().that().resideInAPackage("..presentation..")
                    .should().beAnnotatedWith(Transactional.class)
                    .allowEmptyShould(false)
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

            // Presentation: 오직 외부만 바라보며, 내부로는 Application 레이어에만 접근 가능
            .whereLayer("Presentation").mayNotBeAccessedByAnyLayer()
            // Application: Presentation 또는 Global(초기화)에서 접근 가능
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Presentation")
            // Domain: Application, Infrastructure(구현체), Global(초기화)에서 접근 가능. Presentation은 금지(DTO가 대신함)
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure")
            // Infrastructure: 도메인 로직을 실행하는 Application 또는 Global에서 접근 가능. Domain은 인터페이스만 가지며 Infra를 호출하지 않음
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Application", "Domain")
            
            .allowEmptyShould(false)
            .as("도메인 내 4계층(DDD) 의존성 규칙을 위반했습니다.");

    /**
     * 도메인별 접두사 일치 여부 통합 검증 유틸리티 (English Name)
     */
    public static ArchRule domain_prefix_naming_rule(String prefix) {
        String lowerPrefix = prefix.toLowerCase();
        return CompositeArchRule.of(List.of(
                        // 1. Controller 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".presentation.controller..")
                                .and().haveSimpleNameEndingWith("Controller")
                                .should().haveSimpleNameStartingWith(prefix),

                        // 2. Service 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".application.service..")
                                .and().haveSimpleNameEndingWith("Service")
                                .should().haveSimpleNameStartingWith(prefix),

                        // 3. 엔티티 검증 (domain.core 패키지)
                        classes().that().resideInAPackage(".." + lowerPrefix + ".domain.core..")
                                .and().areAnnotatedWith(Entity.class)
                                .should().haveSimpleNameStartingWith(prefix),

                        // 4. 순수 Repository 인터페이스 검증 (domain.repository 패키지)
                        classes().that().resideInAPackage(".." + lowerPrefix + ".domain.repository..")
                                .and().haveSimpleNameEndingWith("Repository")
                                .and().areInterfaces()
                                .should().haveSimpleNameStartingWith(prefix),

                        // 5. Repository 구현체 검증
                        classes().that().resideInAPackage(".." + lowerPrefix + ".infrastructure.repository..")
                                .and().haveSimpleNameEndingWith("RepositoryImpl")
                                .or().haveSimpleNameEndingWith("JpaRepository")
                                .should().haveSimpleNameStartingWith(prefix)
                ))
                .allowEmptyShould(false)
                .as(prefix + " 도메인 내 핵심 클래스들은 반드시 '" + prefix + "' 접두사로 시작해야 합니다.");
    }
}
