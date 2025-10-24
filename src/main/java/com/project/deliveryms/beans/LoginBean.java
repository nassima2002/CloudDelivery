package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.services.LivreurService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.io.Serializable;
import com.project.deliveryms.services.UtilisateurService;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private Utilisateur utilisateur;
    private String email;
    private String motDePasse;

    private Role role; // "admin", "livreur", etc.

    @Inject
    private UtilisateurService utilisateurService; // Injection du service
    @Inject
    private  LivreurService livreurService;; // Injection du service


    public String login() {
        String result = utilisateurService.authentifier(email, motDePasse);

        if ("Connexion réussie".equals(result)) {
            // Utilisation du service pour trouver l'utilisateur par email
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(email);

            if (utilisateur != null) {
                // Stocker l'utilisateur dans la session
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

                Role role = utilisateur.getRole();

                if (role == Role.ADMIN) {
                    session.setAttribute("utilisateurConnecte", utilisateur); // Admin
                    return "/admin/admin-dashboard.xhtml?faces-redirect=true";
                } else if (role == Role.LIVREUR) {
                    // Chercher le livreur associé à l'utilisateur
                    Livreur livreur = livreurService.findByEmail(utilisateur.getEmail());
                    if (livreur != null) {
                        session.setAttribute("utilisateurConnecte", livreur); // Livreur stocké
                    } else {
                        session.setAttribute("utilisateurConnecte", utilisateur); // fallback
                    }
                    return "/livreur/dashboard.xhtml?faces-redirect=true";
                } else {
                    session.setAttribute("utilisateurConnecte", utilisateur);
                    return "/pages/dashboard.xhtml?faces-redirect=true";
                }
            }
        } else {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().getFlash().setKeepMessages(true);
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, result, null));
        }

        return null;
    }




    // Getters et setters
    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }
    public void logout() {
        // Implement the logout functionality, such as invalidating the session
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        // Optionally, redirect to a different page, like the login page
        try {
            FacesContext.getCurrentInstance().getExternalContext().redirect("login.xhtml");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
