package controller.model.repository;
import java.time.LocalDate;
import jakarta.persistence.Entity; import jakarta.persistence.GeneratedValue; import jakarta.persistence.GenerationType; import jakarta.persistence.Id; import jakarta.persistence.Table;
@Entity @Table(name = "donations")
public class Donation {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 private Long userId; private LocalDate donationDate; private Integer units; private String bloodType; private String status; private LocalDate nextEligibleDate;
 public Long getId(){return id;} public void setId(Long v){id=v;} public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;} public LocalDate getDonationDate(){return donationDate;} public void setDonationDate(LocalDate v){donationDate=v;} public Integer getUnits(){return units;} public void setUnits(Integer v){units=v;} public String getBloodType(){return bloodType;} public void setBloodType(String v){bloodType=v;} public String getStatus(){return status;} public void setStatus(String v){status=v;} public LocalDate getNextEligibleDate(){return nextEligibleDate;} public void setNextEligibleDate(LocalDate v){nextEligibleDate=v;}
}
