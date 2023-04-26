package fr.simplon.sondages;

import fr.simplon.sondages.dao.SondageRepository;
import fr.simplon.sondages.entity.Sondage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testing of "/sondages" REST Services.
 * <p>
 * Chosen strategy is to start up a single SpringBoot instance with a mock playing the role of SondageRepository. Thus
 * we can decouple our testing environment from the original database and not to pollute regular database with our test
 * values when we will test POST/PUT/DELETE methods.
 * <p>
 * For REST testing, we use the special TestRestTemplate class. This class allows us to send HTTP requests over the
 * network and then test all the supply chain, contrary to the {@link org.springframework.test.web.servlet.MockMvc}
 * strategy which will test the {@link RestController @RestController} with regular method calls in the same thread
 * stack.
 * <p>
 * This strategy is considered as <strong>PURE UNIT TESTING</strong>, it is not an integration test. If you want to
 * perform integration test, you should start both a production server without any mocking tuning and then access it
 * through the network from a separate SpringBoot instance. Here the <code>@SpringBootTest(webEnvironment =
 * SpringBootTest.WebEnvironment.RANDOM_PORT)</code> annotation make it possible to start the @RestController and expose
 * its endpoints in a random port.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StandaloneSondagesApplicationTests
{
    /**
     * We ask Spring to mock the SondageRepository component with a Mockito mock, thanks to the @MockBean annotation.
     * This annotation will tell Spring that it must inject this mock everywhere it is required by a @Autowired
     * annotation.
     * <p>
     * In our tests, we have to configure this mock's behavior by calling Mockito.when() method or Mockito.doAnswer()
     * method to make it return our objects used for validation.
     */
    @MockBean
    private SondageRepository repository;

    /**
     * TestRestTemplate is like RestTemplate but it has authentication facilities for security concerns. This is not
     * required for this projet but I encourage you to use it instead of RestTemplate because it provides more features
     * and you can use it the same way you use RestTemplate.
     */
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * This annotation allows to inject automatically the embedded SpringBoot's Web Server's port. This means that when
     * the embedded server's (Tomcat) is started, this variable will take the randomly allocated port value with
     *
     * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT).
     */
    @LocalServerPort
    private int serverPort;

    /**
     * This variable is initialized before each test with a fixed base URL ("http://localhost:") + the server's port.
     */
    private static String url;

    /**
     * Sample data used for validation purpose.
     */
    private Sondage sample1;

    /**
     * Prepare mocks and data used for validation purpose.
     */
    @BeforeEach
    public void beforeAll()
    {
        sample1 = new Sondage();
        sample1.setId(120L);
        sample1.setCreatedBy("JUnit");
        sample1.setCreatedAt(LocalDateTime.now());
        sample1.setClosedAt(LocalDateTime.now().plusDays(3));
        sample1.setDescription("Description");
        sample1.setQuestion("Question");

        url = String.format("http://localhost:%d/api/sondages", serverPort);

        Mockito.reset(repository);
    }

    @Test
    void testGetAllSondages()
    {
        // Configure the repository mock to return our sample1 object when findAll() method is called
        Mockito.when(repository.findAll()).thenReturn(Collections.singletonList(sample1));

        // Send a "GET /sondages" HTTP request to get all entities
        ResponseEntity<List<Sondage>> response = restTemplate.exchange(url, HttpMethod.GET, null, new ListSondage());
        List<Sondage> sondages = response.getBody();

        // Verifications
        Mockito.verify(repository).findAll();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, sondages.size());
        Sondage sondage = sondages.get(0);
        assertTrue(sondage != sample1);
        assertEquals(sondage.getId(), sample1.getId());
        assertEquals(sondage.getCreatedBy(), sample1.getCreatedBy());
        assertEquals(sondage.getCreatedAt(), sample1.getCreatedAt());
        assertEquals(sondage.getClosedAt(), sample1.getClosedAt());
        assertEquals(sondage.getQuestion(), sample1.getQuestion());
        assertEquals(sondage.getDescription(), sample1.getDescription());
    }

    @Test
    void testGetSondage()
    {
        // Configure the mock to return our sample1 object when findById(Long) method is called
        Mockito.when(repository.findById(sample1.getId())).thenReturn(Optional.of(sample1));

        // Create the final URL from the base endpoint's URL and the {id} PathVariable.
        String url = this.url + "/{id}";

        // Send a "GET /sondages/<id>" HTTP request with the sample1's object id
        ResponseEntity<Sondage> response = restTemplate.getForEntity(url, Sondage.class, sample1.getId());
        HttpStatusCode statusCode = response.getStatusCode();
        Sondage sondage = response.getBody();

        // Verifications
        Mockito.verify(repository).findById(sample1.getId());
        assertEquals(HttpStatus.OK, statusCode);
        assertNotNull(sondage);
        assertEquals(sondage.getId(), sample1.getId());
        assertEquals(sondage.getCreatedBy(), sample1.getCreatedBy());
        assertEquals(sondage.getCreatedAt(), sample1.getCreatedAt());
        assertEquals(sondage.getClosedAt(), sample1.getClosedAt());
        assertEquals(sondage.getQuestion(), sample1.getQuestion());
        assertEquals(sondage.getDescription(), sample1.getDescription());
    }

    @Test
    void testAddSondage()
    {
        // Prevent the repository from saving our test data
        Mockito.when(repository.save(Mockito.any(Sondage.class))).thenReturn(sample1);

        // Performe the "POST /sondages" request with the "JSONified" sample1 object as body
        ResponseEntity<Sondage> response = restTemplate.postForEntity(url, sample1, Sondage.class);

        // Store results in variables
        HttpStatusCode statusCode = response.getStatusCode();
        Sondage result = response.getBody();

        // Check results
        assertEquals(HttpStatus.OK, statusCode);
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(sample1.getId(), result.getId());
        assertEquals(sample1.getQuestion(), result.getQuestion());
        assertEquals(sample1.getDescription(), result.getDescription());
        assertEquals(sample1.getCreatedAt(), result.getCreatedAt());
        assertEquals(sample1.getClosedAt(), result.getClosedAt());
        assertEquals(sample1.getCreatedBy(), result.getCreatedBy());
    }

    @Test
    void testDelSondage()
    {
        // Configure the repository mock to return our sample1 object when calling findById()
        Mockito.when(repository.findById(sample1.getId())).thenReturn(Optional.of(sample1));

        // Send a "GET /sondages/<id>" HTTP request
        String url = this.url + "/{id}";
        ResponseEntity<Sondage> response = restTemplate.getForEntity(url, Sondage.class, sample1.getId());
        Sondage result = response.getBody();

        // Verifications
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(sample1.getId(), result.getId());

        // Verifies that the repository mock has been called with the expected arguments
        Mockito.verify(repository).findById(sample1.getId());

        // Now reconfigure the repository mock.
        // When the delete() method is called, we tell the mock to return null next time the deleted Sondage is requested.
        Mockito.reset(repository);
        Mockito.doAnswer((InvocationOnMock invocation) -> {
            Mockito.when(repository.findById(sample1.getId())).thenReturn(Optional.empty());
            return null;
        }).when(repository).deleteById(sample1.getId());

        // Now send "DELETE /sondages/<id>" HTTP request to delete the entity
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class, sample1.getId());
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertNull(deleteResponse.getBody());

        // Send a new HTTP Request to get the deleted entity and expect a NOT_FOUND response
        response = restTemplate.getForEntity(url, Sondage.class, sample1.getId());
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateSondage()
    {
        // Configure the repository mock to return our sample1 object when calling findById() method
        Mockito.when(repository.findById(sample1.getId())).thenReturn(Optional.of(sample1));

        long id = sample1.getId();
        String url = this.url + "/{id}";
        ResponseEntity<Sondage> response = restTemplate.getForEntity(url, Sondage.class, id);
        Sondage originalSondage = response.getBody();

        // Verifications
        Mockito.verify(repository).findById(sample1.getId());
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(originalSondage);
        assertTrue(originalSondage != sample1);

        // Update the survey
        originalSondage.setDescription("Pot-Luck");
        originalSondage.setQuestion("Do you want to bring food to share on Tuesday?");
        originalSondage.setCreatedAt(LocalDateTime.now());
        originalSondage.setClosedAt(LocalDateTime.now().plusDays(2));
        originalSondage.setCreatedBy("JUnit");

        // When the mock is performed with the save() method, we re-program it to return
        // the new expected value the next time
        Mockito.reset(repository);
        Mockito.doAnswer((InvocationOnMock invocation) -> {
            Mockito.when(repository.findById(id)).thenReturn(Optional.of(originalSondage));
            return null;
        }).when(repository).save(Mockito.any());

        // Send a "PUT /sondages{id}" HTTP request with the new Sondage as the request's body
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity httpRequest = new HttpEntity(originalSondage, headers);
        ResponseEntity<Void> exchange = restTemplate.exchange(url, HttpMethod.PUT, httpRequest, Void.class, id);

        // Basic verifications
        Mockito.verify(repository).save(Mockito.any());
        assertEquals(HttpStatus.OK, exchange.getStatusCode());
        assertNull(exchange.getBody());

        // Send a "GET /sondages/<id>" HTTP request to get the updated entity
        ResponseEntity<Sondage> updatedResponse = restTemplate.getForEntity(url, Sondage.class, id);
        Sondage result = updatedResponse.getBody();

        // Verifications
        Mockito.verify(repository).findById(id);
        assertEquals(HttpStatus.OK, updatedResponse.getStatusCode());
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result != originalSondage);
        assertEquals(originalSondage.getId(), result.getId());
        assertEquals(originalSondage.getQuestion(), result.getQuestion());
        assertEquals(originalSondage.getDescription(), result.getDescription());
        assertEquals(originalSondage.getCreatedAt(), result.getCreatedAt());
        assertEquals(originalSondage.getClosedAt(), result.getClosedAt());
        assertEquals(originalSondage.getCreatedBy(), result.getCreatedBy());
    }

}




