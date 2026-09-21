package controller.model.repository;
import java.time.LocalDateTime; import jakarta.persistence.Entity; import jakarta.persistence.GeneratedValue; import jakarta.persistence.GenerationType; import jakarta.persistence.Id; import jakarta.persistence.Table;
@Entity @Table(name="blood_requests")
public class BloodRequest {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private String patientName; private String hospital; private String bloodType; private Integer units; private String priority; private String status; private LocalDateTime createdAt;
 public Long getId(){return id;} public void setId(Long v){id=v;} public String getPatientName(){return patientName;} public void setPatientName(String v){patientName=v;} public String getHospital(){return hospital;} public void setHospital(String v){hospital=v;} public String getBloodType(){return bloodType;} public void setBloodType(String v){bloodType=v;} public Integer getUnits(){return units;} public void setUnits(Integer v){units=v;} public String getPriority(){return priority;} public void setPriority(String v){priority=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
}
