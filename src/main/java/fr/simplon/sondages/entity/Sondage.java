package fr.simplon.sondages.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
public class Sondage
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3, max = 120)
    private String description;

    @Size(min = 3, max = 120)
    private String question;

    @Column(nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreatedDate
    private LocalDateTime createdAt;

    @Future
    @Column(nullable = false)
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime closedAt;

    @NotNull
    @NotBlank
    private String createdBy;

    public Sondage()
    {
        super();
        this.createdAt = LocalDateTime.now();
    }

    public Sondage(
            Long pId,
            String pDescription,
            String pQuestion,
            LocalDateTime pCreatedAt,
            LocalDateTime pClosedAt,
            String pCreatedBy)
    {
        super();
        id = pId;
        description = pDescription;
        question = pQuestion;
        createdAt = pCreatedAt;
        closedAt = pClosedAt;
        createdBy = pCreatedBy;
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long pId)
    {
        id = pId;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String pDescription)
    {
        description = pDescription;
    }

    public String getQuestion()
    {
        return question;
    }

    public void setQuestion(String pQuestion)
    {
        question = pQuestion;
    }

    public LocalDateTime getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime pCreatedAt)
    {
        createdAt = pCreatedAt;
    }

    public LocalDateTime getClosedAt()
    {
        return closedAt;
    }

    public void setClosedAt(LocalDateTime pClosedAt)
    {
        closedAt = pClosedAt;
    }

    public String getCreatedBy()
    {
        return createdBy;
    }

    public void setCreatedBy(String pCreatedBy)
    {
        createdBy = pCreatedBy;
    }
}
