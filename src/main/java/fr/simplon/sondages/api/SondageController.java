package fr.simplon.sondages.api;

import fr.simplon.sondages.dao.SondageRepository;
import fr.simplon.sondages.entity.Sondage;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Collectors;

/**
 * Contrôleur CRUD pour les sondages.
 */
@RestController
@RequestMapping("/api")
public class SondageController
{
    private SondageRepository mRepository;

    @Autowired
    public SondageController(SondageRepository pRepository)
    {
        mRepository = pRepository;
    }

    @GetMapping(path = "/sondages")
    @ApiResponse(responseCode = "200", description = "Les ressources ont été trouvées et renvoyées avec succès.")
    public List<Sondage> sondages()
    {
        List<Sondage> all = mRepository.findAll();
        return all;
    }

    @GetMapping(path = "/sondages/{id}")
    @ApiResponse(responseCode = "200", description = "La ressource a été trouvée et renvoyée avec succès.")
    @ApiResponse(responseCode = "404", description = "La ressource n'existe pas.")
    public ResponseEntity<Sondage> getSondageById(@PathVariable Long id)
    {
        return ResponseEntity.of(mRepository.findById(id));
    }

    @PostMapping(path = "/sondages")
    @ApiResponse(responseCode = "201", description = "La ressource a été créée avec succès.")
    @ApiResponse(responseCode = "400", description = "En cas d'erreur de validation.")
    public ResponseEntity<?> createSondage(
            @Valid @RequestBody Sondage sondage, BindingResult validation, HttpServletRequest request)
    {
        if (validation.hasErrors())
        {
            List<String> errors = validation.getAllErrors().stream()//
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)//
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(errors);
        }

        // Force un identifiant à null dans le cas où le client envoie l'id d'un sondage qui existe déjà
        // pour tenter de le modifier.
        sondage.setId(null);
        sondage = mRepository.save(sondage);

        // Construction de la réponse
        URI location = ServletUriComponentsBuilder.fromRequest(request)//
                .path("/{id}")//
                .buildAndExpand(sondage.getId())//
                .toUri();
        return ResponseEntity.created(location).body(sondage);
    }

    @PutMapping(path = "/sondages/{id}")
    @ApiResponse(responseCode = "200", description = "La ressource a été mise à jour avec succès.")
    @ApiResponse(responseCode = "404", description = "La ressource à mettre à jour n'a pas été trouvée.")
    public ResponseEntity<?> updateSondage(
            @PathVariable Long id,
            @RequestBody @Valid Sondage sondage,
            BindingResult validation,
            HttpServletRequest request)
    {
        if (validation.hasErrors())
        {
            List<String> errors = validation.getAllErrors().stream()//
                    .map(DefaultMessageSourceResolvable::getDefaultMessage)//
                    .collect(Collectors.toList());

            return ResponseEntity.badRequest().body(errors);
        }

        Sondage updated = mRepository.findById(id).map(s -> {
            s.setDescription(sondage.getDescription());
            s.setQuestion(sondage.getQuestion());
            s.setClosedAt(sondage.getClosedAt());
            s.setCreatedBy(sondage.getCreatedBy());
            return mRepository.save(s);
        }).orElseGet(() -> null);

        if (updated == null)
        {
            return ResponseEntity.notFound()//
                    .location(ServletUriComponentsBuilder.fromRequest(request).build().toUri())//
                    .build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping(path = "/sondages/{id}")
    @ApiResponse(responseCode = "200", description = "La ressource n'existe pas, requête ignorée.")
    @ApiResponse(responseCode = "204", description = "La ressource a été supprimée avec succès.")
    public ResponseEntity deleteSondage(@PathVariable Long id)
    {
        if (mRepository.existsById(id))
        {
            mRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }
}
