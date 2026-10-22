package controller.model.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "health_assessments")
public class HealthAssessment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private LocalDate birthDate;
    private Double weightKg;
    private String feelingWell;
    private String hasFever;
    private String takingMedication;
    private String recentSurgery;
    private String pregnantOrBreastfeeding;
    private String highRiskExposure;
    private String eligibilityStatus;
    private Integer readinessScore;
    private String reviewNotes;
    private LocalDateTime submittedAt;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public LocalDate getBirthDate() { return birthDate; } public void setBirthDate(LocalDate birthDate) { this.birthDate = birthDate; }
    public Double getWeightKg() { return weightKg; } public void setWeightKg(Double weightKg) { this.weightKg = weightKg; }
    public String getFeelingWell() { return feelingWell; } public void setFeelingWell(String feelingWell) { this.feelingWell = feelingWell; }
    public String getHasFever() { return hasFever; } public void setHasFever(String hasFever) { this.hasFever = hasFever; }
    public String getTakingMedication() { return takingMedication; } public void setTakingMedication(String takingMedication) { this.takingMedication = takingMedication; }
    public String getRecentSurgery() { return recentSurgery; } public void setRecentSurgery(String recentSurgery) { this.recentSurgery = recentSurgery; }
    public String getPregnantOrBreastfeeding() { return pregnantOrBreastfeeding; } public void setPregnantOrBreastfeeding(String value) { this.pregnantOrBreastfeeding = value; }
    public String getHighRiskExposure() { return highRiskExposure; } public void setHighRiskExposure(String value) { this.highRiskExposure = value; }
    public String getEligibilityStatus() { return eligibilityStatus; } public void setEligibilityStatus(String value) { this.eligibilityStatus = value; }
    public Integer getReadinessScore() { return readinessScore; } public void setReadinessScore(Integer value) { this.readinessScore = value; }
    public String getReviewNotes() { return reviewNotes; } public void setReviewNotes(String value) { this.reviewNotes = value; }
    public LocalDateTime getSubmittedAt() { return submittedAt; } public void setSubmittedAt(LocalDateTime value) { this.submittedAt = value; }
}
