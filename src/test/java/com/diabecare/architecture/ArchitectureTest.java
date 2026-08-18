package com.diabecare.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;

@DisplayName("Reglas de Arquitectura Hexagonal")
class ArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.diabecare");
    }

    @Test
    @DisplayName("el dominio no depende de infraestructura ni presentación")
    void domainDoesNotDependOnInfraOrPresentation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..", "..presentation..");

        rule.check(classes);
    }

    @Test
    @DisplayName("los casos de uso no dependen de infraestructura ni presentación")
    void useCasesDoNotDependOnInfraOrPresentation() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..application.usecase..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure..", "..presentation..");

        rule.check(classes);
    }

    @Test
    @DisplayName("los controllers no dependen de persistence")
    void controllersDoNotDependOnPersistence() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..presentation.controller..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure.persistence..");

        rule.check(classes);
    }

    @Test
    @DisplayName("los controllers solo dependen de infrastructure.config (lectura de propiedades), nunca de otro paquete de infraestructura")
    void controllersOnlyDependOnConfigWithinInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..presentation.controller..")
                .should().dependOnClassesThat(
                        com.tngtech.archunit.core.domain.JavaClass.Predicates
                                .resideInAPackage("..infrastructure..")
                                .and(com.tngtech.archunit.base.DescribedPredicate.not(
                                        com.tngtech.archunit.core.domain.JavaClass.Predicates
                                                .resideInAPackage("..infrastructure.config..")))
                );

        rule.check(classes);
    }

    @Test
    @DisplayName("los repositorios JPA solo se usan desde adaptadores de persistencia")
    void jpaRepositoriesOnlyUsedFromAdapters() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..infrastructure.persistence..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure.persistence.repository..");

        rule.check(classes);
    }

    @Test
    @DisplayName("las entidades JPA no se usan fuera de la capa de infraestructura")
    void jpaEntitiesNotUsedOutsideInfrastructure() {
        ArchRule rule = noClasses()
                .that().resideOutsideOfPackage("..infrastructure..")
                .should().dependOnClassesThat()
                .resideInAPackage("..infrastructure.persistence.entity..");

        rule.check(classes);
    }

    @Test
    @DisplayName("los servicios de dominio no tienen dependencias de Spring")
    void domainServicesHaveNoSpringDependencies() {
        ArchRule rule = noClasses()
                .that().resideInAPackage("..domain.service..")
                .should().dependOnClassesThat()
                .resideInAPackage("org.springframework..");

        rule.check(classes);
    }

    @Test
    @DisplayName("los adaptadores de persistencia implementan puertos de salida")
    void persistenceAdaptersImplementOutPorts() {
        ArchRule rule = classes()
                .that().resideInAPackage("..infrastructure.persistence.adapter..")
                .should().implement(
                        com.tngtech.archunit.core.domain.JavaClass.Predicates
                                .resideInAPackage("..application.port.out.."));

        rule.check(classes);
    }
}