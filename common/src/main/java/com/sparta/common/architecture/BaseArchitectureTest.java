package com.sparta.common.architecture;

import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

public abstract class BaseArchitectureTest {

    // common 의존성 규칙
    @ArchTest
    static final ArchRule common_module_dependency_rule =
            noClasses().that().resideInAPackage("..common..")
                    .should().dependOnClassesThat().resideInAPackage("..service..") // 💡 특정 서비스가 아닌 모든 *service 모듈 차단
                    .allowEmptyShould(true)
                    .as("공통(common) 모듈은 상위 서비스 모듈들을 참조할 수 없습니다.");




    // 패키지 구조 규칙

    // 네이밍 규칙
    // presentation 패키지 안의 클래스는 이름이 Controller로 끝나야 함
    @ArchTest
    public static final ArchRule 컨트롤러_네이밍_규칙 = classes()
            .that().resideInAPackage("..presentation..")
            .should().haveSimpleNameEndingWith("Controller")
            .allowEmptyShould(true)
            .as("컨트롤러 네이밍 규칙을 위반했습니다.");

    //TODO
    //다른 네이밍 규칙 추가필요



    // @ 규칙
    // @Service는 반드시 application 내부 또는 하위에 위치
    @ArchTest
    static final ArchRule service_package_rule =
            classes().that().areAnnotatedWith(org.springframework.stereotype.Service.class)
                    .should().resideInAPackage("..application..")
                    .allowEmptyShould(true)
                    .as("@Service 어노테이션이 붙은 클래스는 반드시 application 패키지 내부에 위치해야 합니다.");

    // 인터페이스 구현 규칙: Repository 인터페이스는 반드시 'Domain'에 위치 (DIP 준수)
    @ArchTest
    static final ArchRule repository_interface_location_rule =
            classes().that().areInterfaces()
                    .and().haveSimpleNameEndingWith("Repository")
                    .should().resideInAPackage("..domain..")
                    .allowEmptyShould(true)
                    .as("Repository 인터페이스(껍데기)는 순수 비즈니스 레이어인 ..domain.. 패키지에 위치해야 합니다.");

    // Repository 실제 구현체(Impl)는 반드시 'Infrastructure'에 위치
    @ArchTest
    static final ArchRule repository_implementation_location_rule =
            classes().that().haveSimpleNameEndingWith("RepositoryImpl")
                    .should().resideInAPackage("..infrastructure..")
                    .allowEmptyShould(true)
                    .as("Repository의 실제 JPA/QueryDSL 구현체(Impl)는 ..infrastructure.. 패키지에 위치해야 합니다.");

    // 4계층 의존성 검증
    @ArchTest
    public static final ArchRule 네계층_클린_아키텍처_규칙 = layeredArchitecture()
            .consideringAllDependencies()
            // 1. 노션에 적힌 4대 계층(레이어)의 주소를 정의합니다.
            .layer("Presentation").definedBy("..presentation..")
            .layer("Application").definedBy("..application..")
            .layer("Domain").definedBy("..domain..")
            .layer("Infrastructure").definedBy("..infrastructure..")

            // 2. 출입 통제 규칙 (상위 ➔ 하위 방향만 허용)
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
}
