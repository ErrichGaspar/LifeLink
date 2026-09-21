package controller.model.repository;
import jakarta.persistence.Entity; import jakarta.persistence.Id; import jakarta.persistence.Table;
@Entity @Table(name="blood_inventory") public class BloodInventory { @Id private String bloodType; private Integer units; public String getBloodType(){return bloodType;} public void setBloodType(String v){bloodType=v;} public Integer getUnits(){return units;} public void setUnits(Integer v){units=v;} }
