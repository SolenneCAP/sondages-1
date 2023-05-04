package fr.simplon.sondages.web;


import fr.simplon.sondages.dto.UserForm;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collection;

@Controller
public class UsersController
{
    private PasswordEncoder passwordEncoder;
    private UserDetailsManager userDetailsManager;
    @Autowired
    public UsersController(
            PasswordEncoder pPasswordEncoder,
            UserDetailsManager pUserDetailsManager)
    {
        passwordEncoder = pPasswordEncoder;
        userDetailsManager = pUserDetailsManager;
    }
    @GetMapping(path = "/admin/createUser")
    public String getCreateUserForm(@ModelAttribute(name="user") UserForm user, Model model)
    {
        if (user == null || !model.containsAttribute("user"))
        {
            model.addAttribute("user", new UserForm());
        }
        return "/admin/createUser";
    }
    @PostMapping("/admin/createUser")
    @Transactional
    public String createUser(
            @Valid @ModelAttribute(name = "user") UserForm user,
            BindingResult validation, Model model)
    {
        if (!user.getPassword().equals(user.getConfirmPassword()))
        {
            user.setConfirmPassword("");
            validation.addError(new FieldError("user", "confirmPassword",
                    "Les mots de passe ne correspondent pas"));
        }
        if (userDetailsManager.userExists(user.getLogin()))
        {
            user.setLogin("");
            validation.addError(new ObjectError("user", "Cet utilisateur existe déjà"));
        }
        if (validation.hasErrors())
        {
            return "/admin/createUser";
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
// Roles for new user
        Collection<? extends GrantedAuthority> roles = Arrays.asList(new SimpleGrantedAuthority("USER"));
        UserDetails userDetails = new User(user.getLogin(), encodedPassword, roles);
// Create the account in database with all its roles
        userDetailsManager.createUser(userDetails);
        return "redirect:/login";
    }

    @GetMapping(path = "/inscription")
    public String getInscription(@ModelAttribute(name="user") UserForm user, Model model)
    {
        if (user == null || !model.containsAttribute("user"))
        {
            model.addAttribute("user", new UserForm());
        }
        return "/inscription";
    }
    @PostMapping("/insciption")
    @Transactional
    public String inscription(
            @Valid @ModelAttribute(name = "user") UserForm user,
            BindingResult validation, Model model)
    {
        if (!user.getPassword().equals(user.getConfirmPassword()))
        {
            user.setConfirmPassword("");
            validation.addError(new FieldError("user", "confirmPassword",
                    "Les mots de passe ne correspondent pas"));
        }
        if (userDetailsManager.userExists(user.getLogin()))
        {
            user.setLogin("");
            validation.addError(new ObjectError("user", "Cet utilisateur existe déjà"));
        }
        if (validation.hasErrors())
        {
            return "/inscription";
        }
        String encodedPassword = passwordEncoder.encode(user.getPassword());
// Roles for new user
        Collection<? extends GrantedAuthority> roles = Arrays.asList(new SimpleGrantedAuthority("USER"));
        UserDetails userDetails = new User(user.getLogin(), encodedPassword, roles);
// Create the account in database with all its roles
        userDetailsManager.createUser(userDetails);
        return "redirect:/login";
    }




    @PutMapping("/changermdp")
    public String getchangermdp( UserForm) {
        userDetailsManager.changePassword(); = new User



        return "changermdp";
    }
    @PutMapping("/changemdp")
    public ResponseEntity<String> updateUserPassword(
            @AuthenticationPrincipal Principal principal,
            @RequestBody UserForm userForm) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("You must be authenticated to change your password");
        }

        String username = principal.getName();
        User user = UserForm.findByLogin(login);

        if (!user.equals(userForm.getCurrentPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }

        if (!userForm.getNewPassword().equals(userForm.getConfirmNewPassword())) {
            return ResponseEntity.badRequest().body("Passwords do not match");
        }

        user.setPassword(userForm.getNewPassword());
        userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }

}