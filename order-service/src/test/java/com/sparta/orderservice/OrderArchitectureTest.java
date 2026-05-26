package com.sparta.orderservice;

import com.sparta.common.architecture.BaseArchitectureTest;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "com.sparta.orderservice")
public class OrderArchitectureTest extends BaseArchitectureTest {

    @ArchTest
    static final ArchRule order_prefix_rule = domain_prefix_naming_rule("Order");

    @ArchTest
    static final ArchRule payment_prefix_rule = domain_prefix_naming_rule("Payment");

    @ArchTest
    static final ArchRule draft_prefix_rule = domain_prefix_naming_rule("Draft");

}
