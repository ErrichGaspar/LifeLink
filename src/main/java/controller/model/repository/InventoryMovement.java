package controller.model.repository;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "inventory_movements")
public class InventoryMovement {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private String bloodType;
    private Integer quantity;
    private String movementType;
    private String referenceType;
    private Long referenceId;
    private String location;
    private LocalDateTime createdAt;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public String getBloodType() { return bloodType; } public void setBloodType(String bloodType) { this.bloodType = bloodType; }
    public Integer getQuantity() { return quantity; } public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public String getMovementType() { return movementType; } public void setMovementType(String movementType) { this.movementType = movementType; }
    public String getReferenceType() { return referenceType; } public void setReferenceType(String referenceType) { this.referenceType = referenceType; }
    public Long getReferenceId() { return referenceId; } public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }
    public String getLocation() { return location; } public void setLocation(String location) { this.location = location; }
    public LocalDateTime getCreatedAt() { return createdAt; } public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
