package fr.simplon.sondages.web;

import fr.simplon.sondages.dao.SondageRepository;
import fr.simplon.sondages.dao.VoteRepository;
import fr.simplon.sondages.entity.Sondage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur pour URLs qui retournent des pages HTML standard (c'est à dire pas de JSON).
 */
@Controller
public class WebController
{
    /** Nombre de sondages par page de résultats. */
    public static final int DEFAULT_PAGE_COUNT = 5;

    private SondageRepository mSondageRepository;
    private VoteRepository    mVoteRepository;

    /**
     * Constructeur.
     *
     * @param pSondageRepository
     * @param pVoteRepository
     */
    @Autowired
    public WebController(SondageRepository pSondageRepository, VoteRepository pVoteRepository)
    {
        mSondageRepository = pSondageRepository;
        mVoteRepository = pVoteRepository;
    }

    /**
     * Page d'accueil.
     *
     * @param page  Numéro de page de sondages (optionnel).
     * @param model Modèle Thymeleaf.
     * @return La page d'accueil HTML.
     */
    @GetMapping(path = {"/", "/index"})
    public String index(
            @RequestParam(required = false, defaultValue = "0") Integer page, Model model)
    {
        fillModelWithPaginationAttributes(model, page);
        model.addAttribute("newSondage", new Sondage());
        return "index";
    }

    /**
     * Fournit le HTML correspondant à la liste de tous les sondages (avec pagination).
     *
     * @param page  Numéro de la page demandée.
     * @param model Modèle Thymeleaf.
     * @return Le HTML correspondant à la liste des sondages demandés.
     */
    @GetMapping(path = "/fragments/sondages")
    public String fragmentSondages(
            @RequestParam(required = false, defaultValue = "0") Integer page, Model model)
    {
        fillModelWithPaginationAttributes(model, page);
        return "index :: all-sondages";
    }

    /**
     * Fournit le HTML correspondant à un seul sondage (fragment).
     *
     * @param id    Identifiant du sondage.
     * @param model Modèle Thymeleaf.
     * @return Le contenu du fragment Thymeleaf correspondant au sondage demandé.
     */
    @GetMapping(path = "/fragments/sondages/{id}")
    public String fragmentSondage(@PathVariable Long id, Model model)
    {
        Optional<Sondage> sondage = mSondageRepository.findById(id);
        model.addAttribute("sondage", sondage.get());
        return "fragment-sondage :: single-sondage";
    }

    /**
     * Remplissage du modèle avec les attributs liés à la pagination.
     *
     * @param model Modèle à remplir.
     * @param page  Numéro de page courante.
     */
    private void fillModelWithPaginationAttributes(Model model, int page)
    {
        long count = mSondageRepository.count();
        long pageCount = count % DEFAULT_PAGE_COUNT > 0L ? (count / DEFAULT_PAGE_COUNT + 1) : (count / DEFAULT_PAGE_COUNT);
        List<Sondage> all = getSondages(page);
        model.addAttribute("sondages", all);
        model.addAttribute("page", page);
        model.addAttribute("pageCount", pageCount);
    }

    /**
     * Retourne la liste des sondages avec d'abord les sondages ouverts, classés par date de fermeture croissante.
     *
     * @param page Le numéro de page demandée.
     * @return
     */
    private List<Sondage> getSondages(int page)
    {
        Pageable pageable = PageRequest.of(page, DEFAULT_PAGE_COUNT, Sort.by("closedAt").ascending());
        Page<Sondage> all = mSondageRepository.findAllOrderByClosedAt(pageable, LocalDateTime.now());
        List<Sondage> openSondages = all.filter(sondage -> sondage.getClosedAt().compareTo(LocalDateTime.now()) > 0).stream().collect(Collectors.toList());
        List<Sondage> closedSondages = all.filter(sondage -> sondage.getClosedAt().compareTo(LocalDateTime.now()) < 0).stream().collect(Collectors.toList());
        openSondages.addAll(closedSondages);
        return openSondages;
    }

}
