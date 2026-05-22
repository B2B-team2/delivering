package com.sparta.orderservice.order.infrastructure.repository;

import com.sparta.orderservice.order.domain.core.CompanyOrder;
import com.sparta.orderservice.order.domain.repository.CompanyOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class CompanyOrderRepositoryImpl implements CompanyOrderRepository {

    private final CompanyOrderJpaRepository companyOrderJpaRepository;

    @Override
    public Optional<CompanyOrder> findCompanyOrderById(UUID companyOrderId) {
        return companyOrderJpaRepository.findByCompanyOrderIdAndDeletedAtIsNull(companyOrderId);
    }
}
