package controller.model.repository;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications", uniqueConstraints = @UniqueConstraint(columnNames = "event_key"))
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    private Long recipientUserId;
    private String recipientRole;
    private String title;
    @Column(length = 1000) private String message;
    private String eventType;
    @Column(name="event_key", nullable=false, unique=true) private String eventKey;
    private Long referenceId;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getRecipientUserId(){return recipientUserId;} public void setRecipientUserId(Long v){recipientUserId=v;}
    public String getRecipientRole(){return recipientRole;} public void setRecipientRole(String v){recipientRole=v;}
    public String getTitle(){return title;} public void setTitle(String v){title=v;}
    public String getMessage(){return message;} public void setMessage(String v){message=v;}
    public String getEventType(){return eventType;} public void setEventType(String v){eventType=v;}
    public String getEventKey(){return eventKey;} public void setEventKey(String v){eventKey=v;}
    public Long getReferenceId(){return referenceId;} public void setReferenceId(Long v){referenceId=v;}
    public LocalDateTime getCreatedAt(){return createdAt;} public void setCreatedAt(LocalDateTime v){createdAt=v;}
    public LocalDateTime getReadAt(){return readAt;} public void setReadAt(LocalDateTime v){readAt=v;}
}
