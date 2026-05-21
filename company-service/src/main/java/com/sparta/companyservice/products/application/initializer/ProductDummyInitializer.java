package com.sparta.companyservice.products.application.initializer;

import com.sparta.companyservice.products.application.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductDummyInitializer implements ApplicationRunner {

    private final ProductService productService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // TODO: 초기 데이터 생성 로직 구현 예정
    }
}
