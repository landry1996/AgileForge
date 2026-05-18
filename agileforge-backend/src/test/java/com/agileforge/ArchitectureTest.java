package com.agileforge;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Architecture Rules - Hexagonal Architecture Enforcement")
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.agileforge");
    }

    @Test
    @DisplayName("Domain layer should not depend on infrastructure")
    void domainShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on application layer")
    void domainShouldNotDependOnApplication() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("..application..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Domain layer should not depend on Spring framework")
    void domainShouldNotDependOnSpring() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Application layer should not depend on infrastructure")
    void applicationShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Controllers should only be in web package")
    void controllersShouldBeInWebPackage() {
        classes()
                .that().haveSimpleNameEndingWith("Controller")
                .should().resideInAPackage("..infrastructure.web.controller..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("JPA entities should only be in persistence package")
    void entitiesShouldBeInPersistencePackage() {
        classes()
                .that().areAnnotatedWith(jakarta.persistence.Entity.class)
                .should().resideInAPackage("..infrastructure.persistence.entity..")
                .check(importedClasses);
    }

    @Test
    @DisplayName("Repositories should only be in persistence package")
    void repositoriesShouldBeInPersistencePackage() {
        classes()
                .that().haveSimpleNameEndingWith("Repository")
                .and().areInterfaces()
                .should().resideInAnyPackage(
                        "..infrastructure.persistence.repository..",
                        "..domain.port.out.."
                )
                .check(importedClasses);
    }
}
