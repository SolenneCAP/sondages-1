package fr.simplon.sondages.api;

import fr.simplon.sondages.dao.SondageRepository;
import fr.simplon.sondages.dao.VoteRepository;
import fr.simplon.sondages.entity.Sondage;
import fr.simplon.sondages.entity.Vote;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Contrôleur des votes.
 */
@Controller
public class VoteController
{
    private SondageRepository mRepository;
    private VoteRepository    mVoteRepository;

    @Autowired
    public VoteController(SondageRepository pRepository, VoteRepository pVoteRepository)
    {
        mRepository = pRepository;
        mVoteRepository = pVoteRepository;
    }

    /**
     * Page des votes d'un sondage.
     *
     * @param sondageId L'identifiant du sondage.
     * @param model     Modèle Thymeleaf.
     * @return La page de listing des votes du sondage.
     */
    @GetMapping(path = "/votes/{sondageId}")
    public String votesBySondage(@PathVariable Long sondageId, Model model)
    {
        Optional<Sondage> sondage = mRepository.findById(sondageId);
        if (sondage.isPresent())
        {
            model.addAttribute("sondage", sondage.get());
            model.addAttribute("votes", sondage.get().getVotes());
        }
        else
        {
            throw new RecordNotFoundException(sondageId);
        }
        return "votes";
    }

    /**
     * URL permettant de voter à partir du formulaire en page d'accueil.
     *
     * @param sondageId  L'identifiant du sondage.
     * @param vote       Le vote oui ou non.
     * @param validation Le résultat de validation par Spring Validation.
     * @param model      Modèle Thymeleaf.
     * @return la vue Thymeleaf d'où vient l'utilisateur.
     */
    @PostMapping(path = "/votes/{sondageId}")
    public String vote(
            @PathVariable Long sondageId, @Valid @ModelAttribute Vote vote, BindingResult validation,
            Model model)
    {
        Sondage sondage = mRepository.getReferenceById(sondageId);
        if (sondage != null)
        {
            model.addAttribute("sondage", sondage);
            model.addAttribute("vote", vote);
            model.addAttribute("votes", sondage.getVotes());

            if (!validation.hasErrors())
            {
                List<Vote> existingVotes = mVoteRepository.findBySondageAndUser(sondage, vote.getUser());
                if (existingVotes.isEmpty())
                {
                    vote.setSondage(sondage);
                    vote.setVotedAt(LocalDateTime.now());
                    mVoteRepository.save(vote);
                    model.addAttribute("votes", sondage.getVotes());
                }
                else
                {
                    String message = String.format("L'utilisateur %s a déjà voté %d fois pour ce sondage.",
                                                   vote.getUser(),
                                                   existingVotes.size());
                    model.addAttribute("alreadyVoted", message);
                }
            }
            else
            {
                model.addAttribute("errors", validation);
            }
        }
        else
        {
            throw new RecordNotFoundException(sondageId);
        }

        return "votes";
    }
}
