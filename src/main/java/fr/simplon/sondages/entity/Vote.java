package fr.simplon.sondages.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class Vote
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "id_sondage", referencedColumnName = "id", nullable = false)
    @JsonIgnore
    private Sondage sondage;

    @NotNull
    @Column(nullable = false)
    private Boolean value;

    @Column(nullable = false)
    private LocalDateTime votedAt;

    @NotNull
    @NotBlank
    @Column(nullable = false)
    private String user;

    public Vote(Long pId, Sondage pSondage, Boolean pValue, LocalDateTime pVotedAt, String pUser)
    {
        id = pId;
        sondage = pSondage;
        value = pValue;
        votedAt = pVotedAt;
        user = pUser;
    }

    public Vote()
    {
    }

    public Long getId()
    {
        return id;
    }

    public void setId(Long pId)
    {
        id = pId;
    }

    public Sondage getSondage()
    {
        return sondage;
    }

    public void setSondage(Sondage pSondage)
    {
        sondage = pSondage;
    }

    public Boolean getValue()
    {
        return value;
    }

    public void setValue(Boolean pVote)
    {
        value = pVote;
    }

    public LocalDateTime getVotedAt()
    {
        return votedAt;
    }

    public void setVotedAt(LocalDateTime pVotedAt)
    {
        votedAt = pVotedAt;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String pUser)
    {
        user = pUser;
    }
}
