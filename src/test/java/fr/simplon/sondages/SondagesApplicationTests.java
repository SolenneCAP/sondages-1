package fr.simplon.sondages;

import fr.simplon.sondages.api.SondageController;
import fr.simplon.sondages.entity.Sondage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.sql.DataSource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Tests automatiques du WebService REST "localhost:8080/sondages" permettant d'accéder à la table des sondages en BDD.
 * On crée un faux client web (RestTemplate) pour tester les différentes URLs REST du WebService :
 * <p>
 *     <ul>
 *         <li>GET /sondages ==&gt; {@link SondageController#sondages()}</li>
 *         <li>POST /sondages ==&gt; {@link SondageController#createSondage(Sondage)}</li>
 *         <li>GET /sondages{id} ==&gt; {@link SondageController#getSondageById(Long)}</li>
 *         <li>PUT /sondages{id} ==&gt; {@link SondageController#updateSondage(Long, Sondage)}</li>
 *         <li>DELETE /sondages{id} ==&gt; {@link SondageController#deleteSondage(Long)}</li>
 *     </ul>
 * </p>
 */
@SpringBootTest
class SondagesApplicationTests
{
    /**
     * URL du WebService que nous testons (ce peut être l'URL d'un serveur distant ou un serveur local mais dans notre
     * cas il s'agit d'un serveur Spring Boot qui tourne sur notre propre machine, donc local).
     */
    private final String URL = "http://localhost:8080/api/sondages";

    /**
     * Client REST permettant de faire des appels distants.
     */
    private RestTemplate restTemplate;

    /**
     * *****************************************************************************************************************
     * Code donné à titre indicatif (dans notre cas, ce Bean de configuration ne sert à rien).
     * *****************************************************************************************************************
     * <p>
     * Ce code permet de court-circuiter l'utilisation de la base de données MySQL (ie. celle qui est configurée dans le
     * fichier "application.properties) au profit d'une base de données en mémoire (nommée "H2") qui ne sera créée au
     * début du test et détruite en fin de test.
     * </p>
     * <p>Ceci permet de ne pas polluer notre base de données avec des données de test unitaire. Un test unitaire ou
     * automatique ne devrait jamais laisser de traces de son exécution.</p>
     * <p>
     * Le seul cas où c'est intéressant de mettre cette config, c'est pour éviter de modifier la BDD MySQL par erreur si
     * on utilise des instances d'objets {@link fr.simplon.sondages.dao.SondageRepository} injectés par Spring.
     * </p>
     */
    @Bean // Indique qu'il s'agit d'un Bean de configuration pour Spring.
    @Primary // Indique que si plusieurs Beans sont trouvés par Spring, celui-ci doit avoir la priorité.
    public DataSource dataSource()
    {
        return new EmbeddedDatabaseBuilder() //
                .generateUniqueName(true) //
                .setType(EmbeddedDatabaseType.H2) //
                .ignoreFailedDrops(true) //
                .setScriptEncoding("UTF-8") //
                //.addScripts("user_data.sql") // Pour charger un jeu de données de test par exemple
                .build();
    }

    /**
     * Objet recréé au début de chaque test, cf. {@link #init()}.
     */
    private Sondage sondage;

    /**
     * Initialisation du client REST utilisé pour nos tests.
     */
    @BeforeEach
    public void init()
    {
        this.restTemplate = new RestTemplate();

        this.sondage = new Sondage();
        this.sondage.setDescription("Un sondage");
        this.sondage.setQuestion("Voulez-vous voter pour ou contre ?");
        this.sondage.setClosedAt(LocalDateTime.now().plus(Duration.ofDays(2)));
        this.sondage.setCreatedBy(System.getProperty("user.name"));
    }

    /**
     * Test du WebService "GET /sondages".
     */
    @Test
    public void testFindAll()
    {
        ResponseEntity<Sondage[]> response = restTemplate.getForEntity(URL, Sondage[].class);
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);

        for (Sondage sondage : response.getBody())
        {
            Assertions.assertNotNull(sondage);
        }
    }

    /**
     * Test du WebService "POST /sondages".
     */
    @Test
    public void testCreate()
    {
        // Requête du service "POST /sondages"
        ResponseEntity<Sondage> response = restTemplate.postForEntity(URL, sondage, Sondage.class);

        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Sondage body = response.getBody();
        Assertions.assertNotNull(body.getId());
        Assertions.assertEquals(body.getQuestion(), sondage.getQuestion());
        Assertions.assertEquals(body.getDescription(), sondage.getDescription());
        Assertions.assertEquals(body.getClosedAt(), sondage.getClosedAt());
    }

    /**
     * Test du WebService "PUT /sondages/{id}".
     */
    @Test
    public void testUpdate()
    {
        // Création d'un nouveau sondage
        ResponseEntity<Sondage> response = restTemplate.postForEntity(URL, sondage, Sondage.class);  // POST /sondages
        Long newSondageId = response.getBody().getId();
        Assertions.assertNotNull(newSondageId);

        // Mise à jour (nouvelle URL = url + "/" + newSondageId)
        sondage.setDescription("New description");
        sondage.setQuestion("New question ?");
        sondage.setClosedAt(sondage.getClosedAt().plus(Duration.ofDays(2)).truncatedTo(ChronoUnit.DAYS));
        restTemplate.put(URL + "/" + newSondageId, sondage);  // PUT /sondages/{id}

        // Requête du nouveau sondage avec son ID
        response = restTemplate.getForEntity(URL + "/" + newSondageId, Sondage.class); // GET /sondages/{id}
        Assertions.assertEquals(response.getStatusCode(), HttpStatus.OK);
        Assertions.assertEquals(response.getBody().getDescription(), sondage.getDescription());
        Assertions.assertEquals(response.getBody().getQuestion(), sondage.getQuestion());
        Assertions.assertEquals(response.getBody().getClosedAt(), sondage.getClosedAt());
    }

    /**
     * Test du WebService "PUT /sondages/{id}".
     */
    @Test
    public void testUpdateWithBadValues()
    {
        // Création d'un nouveau sondage
        ResponseEntity<Sondage> response = restTemplate.postForEntity(URL, sondage, Sondage.class);  // POST /sondages
        Long newSondageId = response.getBody().getId();
        Assertions.assertNotNull(newSondageId);

        // Mise à jour (nouvelle URL = url + "/" + newSondageId)
        sondage.setDescription("Modified sondage");
        sondage.setQuestion("Do you want to answer the NEW question?");
        sondage.setClosedAt(sondage.getCreatedAt().minusDays(2));

        // L'appel au WebService doit échouer
        Assertions.assertThrows(HttpClientErrorException.BadRequest.class, () -> restTemplate.put(URL + "/" + newSondageId, sondage));  // PUT /sondages/{id}
    }

    /**
     * Test du WebService "DELETE /sondages/{id}".
     */
    @Test
    public void testDelete()
    {
        ResponseEntity<Sondage> response = restTemplate.postForEntity(URL, sondage, Sondage.class);
        Sondage body = response.getBody();
        Assertions.assertNotNull(body.getId());

        // Re-lecture de l'entité via le WebService avec l'ID de l'objet retourné précédemment
        // pour vérifier que l'objet a bien été créé en BDD
        ResponseEntity<Sondage> readSondage = Assertions.assertDoesNotThrow(() -> {
            return restTemplate.getForEntity(URL + "/" + body.getId(), Sondage.class);
        });

        // Suppression de l'objet
        Assertions.assertDoesNotThrow(() -> {
            restTemplate.delete(URL + "/" + body.getId());  // DELETE /sondages/{id}
        });

        // Re-lecture de l'entité via le WebService avec l'ID de l'objet
        // pour vérifier que l'objet n'existe plus (le webService doit générer une erreur 404)
        HttpClientErrorException.NotFound err = Assertions.assertThrows(HttpClientErrorException.NotFound.class, () -> {
            restTemplate.getForEntity(URL + "/" + body.getId(), Sondage.class);
        });
        Assertions.assertEquals(404, err.getStatusCode().value());
    }
}
