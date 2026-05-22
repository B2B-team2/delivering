package com.sparta.hubservice.inventory;

import com.sparta.hubservice.inventory.domain.core.WarehouseInventory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.RollbackException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(OptimisticLockTest.TestConfig.class)
class OptimisticLockTest {

    @TestConfiguration
    @EnableJpaAuditing
    static class TestConfig {}

    @Autowired
    private EntityManagerFactory emf;

    @Test
    void 동시_재고수정_충돌시_예외발생() {
        // 1. 재고 생성 (version = 0)
        UUID inventoryId;
        EntityManager em0 = emf.createEntityManager();
        em0.getTransaction().begin();
        WarehouseInventory inv = WarehouseInventory.builder()
                .warehouseId(UUID.randomUUID())
                .productOptionId(UUID.randomUUID())
                .quantity(100)
                .safetyStock(10)
                .build();
        em0.persist(inv);
        em0.getTransaction().commit();
        inventoryId = inv.getInventoryId();
        em0.close();

        // 2. 두 세션이 독립적으로 같은 엔티티 조회 (둘 다 version = 0)
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();

        em1.getTransaction().begin();
        WarehouseInventory inv1 = em1.find(WarehouseInventory.class, inventoryId);

        em2.getTransaction().begin();
        WarehouseInventory inv2 = em2.find(WarehouseInventory.class, inventoryId);

        // 3. 첫 번째 세션 수정 및 커밋 → version 0→1
        inv1.adjust(10);
        em1.getTransaction().commit();
        em1.close();

        // 4. 두 번째 세션이 구버전(version=0)으로 커밋 시도 → 충돌
        inv2.adjust(20);
        assertThrows(RollbackException.class, () -> em2.getTransaction().commit());
        em2.close();
    }
}
