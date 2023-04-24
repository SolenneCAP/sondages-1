package fr.simplon.sondages.dao;

import fr.simplon.sondages.entity.Sondage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SondageRepository extends JpaRepository<Sondage, Long>
{
    @Query("SELECT s FROM Sondage s ORDER BY CASE WHEN s.closedAt > :now THEN 1 ELSE 2 END, s.closedAt ASC")
    Page<Sondage> findAllOrderByClosedAt(Pageable pPageable, @Param("now") LocalDateTime now);

}
