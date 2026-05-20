package com.sparta.hubservice.warehouse.application.service;

import com.sparta.hubservice.warehouse.application.dto.WarehouseCreateCommand;
import com.sparta.hubservice.warehouse.application.dto.WarehouseDto;
import com.sparta.hubservice.warehouse.application.dto.WarehouseUpdateCommand;
import com.sparta.hubservice.warehouse.domain.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {
}
