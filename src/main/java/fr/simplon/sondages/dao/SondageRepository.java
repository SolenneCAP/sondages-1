package fr.simplon.sondages.dao;

import fr.simplon.sondages.entity.Sondage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SondageRepository extends JpaRepository<Sondage,Long>
{
}
