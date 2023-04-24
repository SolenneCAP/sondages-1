package fr.simplon.sondages.dao;

import fr.simplon.sondages.entity.Sondage;
import fr.simplon.sondages.entity.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long>
{
    @Query("SELECT v FROM Vote v WHERE v.sondage = :sondage AND v.user = :user")
    List<Vote> findBySondageAndUser(Sondage sondage, String user);
}
