package com.sparta.hubservice.inventory;

import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import jakarta.persistence.OptimisticLockException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class OptimisticLockTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private PlatformTransactionManager txManager;

    @Test
    void 동시_재고수정_충돌시_예외발생() {
        TransactionTemplate tx = new TransactionTemplate(txManager);

        // 1. 재고 생성 (version = 0)
        UUID inventoryId = tx.execute(status -> {
            WarehouseInventory inv = WarehouseInventory.builder()
                    .warehouseId(UUID.randomUUID())
                    .productOptionId(UUID.randomUUID())
                    .quantity(100)
                    .safetyStock(10)
                    .build();
            em.persist(inv);
            return inv.getInventoryId();
        });

        // 2. 두 트랜잭션이 각각 같은 엔티티 조회 (둘 다 version = 0)
        WarehouseInventory snapshot1 = tx.execute(s -> em.find(WarehouseInventory.class, inventoryId));
        WarehouseInventory snapshot2 = tx.execute(s -> em.find(WarehouseInventory.class, inventoryId));

        // 3. 첫 번째 트랜잭션 수정 및 커밋 → version 0→1
        tx.execute(s -> {
            WarehouseInventory managed = em.merge(snapshot1);
            managed.adjust(10);
            return null;
        });

        // 4. 두 번째 트랜잭션이 version=0 상태로 수정 시도 → 충돌
        assertThrows(OptimisticLockException.class, () ->
                tx.execute(s -> {
                    WarehouseInventory managed = em.merge(snapshot2); // version=0이지만 DB는 1
                    managed.adjust(20);
                    return null;
                })
        );
    }
}
