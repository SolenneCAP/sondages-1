package fr.simplon.sondages.api;

import fr.simplon.sondages.dao.SondageRepository;
import fr.simplon.sondages.entity.Sondage;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public List<Sondage> sondages()
    {
        return mRepository.findAll();
    }

    @GetMapping(path = "/sondages/{id}")
    public Sondage getSondageById(@PathVariable Long id)
    {
        return mRepository.findById(id).orElseThrow(() -> new RecordNotFoundException(id));
    }

    @PostMapping(path = "/sondages")
    public Sondage createSondage(@RequestBody Sondage sondage)
    {
        mRepository.save(sondage);
        return sondage;
    }

    @PutMapping(path = "/sondages/{id}")
    public Sondage updateSondage(@PathVariable Long id, @RequestBody @Valid Sondage sondage)
    {
        sondage.setId(id);
        mRepository.save(sondage);
        return sondage;
    }

    @DeleteMapping(path = "/sondages/{id}")
    public HttpStatus deleteSondage(@PathVariable Long id)
    {
        mRepository.deleteById(id);
        return HttpStatus.OK;
    }
}
