package controller.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository extends JpaRepository<InventoryMovement, Long> {
    boolean existsByReferenceTypeAndReferenceIdAndMovementType(String referenceType, Long referenceId, String movementType);
}
