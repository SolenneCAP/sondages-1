package fr.simplon.sondages.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserForm
{
    @NotBlank
    @Size(min=1,max=255)
    private String login;
    @NotBlank
    @Size(min=1,max=255)
    private String password;
    @NotBlank
    @Size(min=1,max=255)
    private String confirmPassword;


    /**
     * get field @NotBlank
     @Size(min=1,max=255)

      *
      * @return login @NotBlank
     @Size(min=1,max=255)

     */
    public String getLogin() {
        return this.login;
    }

    /**
     * set field @NotBlank
     @Size(min=1,max=255)

      *
      * @param login @NotBlank
     @Size(min=1,max=255)

     */
    public void setLogin(String login) {
        this.login = login;
    }

    /**
     * get field @NotBlank
     @Size(min=1,max=255)

      *
      * @return password @NotBlank
     @Size(min=1,max=255)

     */
    public String getPassword() {
        return this.password;
    }

    /**
     * set field @NotBlank
     @Size(min=1,max=255)

      *
      * @param password @NotBlank
     @Size(min=1,max=255)

     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * get field @NotBlank
     @Size(min=1,max=255)

      *
      * @return confirmPassword @NotBlank
     @Size(min=1,max=255)

     */
    public String getConfirmPassword() {
        return this.confirmPassword;
    }

    /**
     * set field @NotBlank
     @Size(min=1,max=255)

      *
      * @param confirmPassword @NotBlank
     @Size(min=1,max=255)

     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}