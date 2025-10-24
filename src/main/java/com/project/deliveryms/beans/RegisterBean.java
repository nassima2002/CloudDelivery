package com.project.deliveryms.beans;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.services.UtilisateurService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;

import java.io.Serializable;

@Named
@RequestScoped
public class RegisterBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private Utilisateur utilisateur = new Utilisateur();

    @Inject
    private UtilisateurService utilisateurService;

    // Constructor to set default role
    public RegisterBean() {
        // Assurez-vous que le rôle par défaut est "CLIENT"
        if (utilisateur.getRole() == null) {
            utilisateur.setRole(Role.CLIENT); // Le rôle "CLIENT" est par défaut
        }
    }


    public String register() {
        // Appeler la méthode inscrire du service UtilisateurService
        String result = utilisateurService.inscrire(utilisateur);

        // Récupérer l'instance de FacesContext une seule fois
        FacesContext facesContext = FacesContext.getCurrentInstance();

        // Vérifier le résultat de l'inscription
        if ("Inscription réussie".equals(result)) {
            // Ajouter un message de succès
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, result, null));

            // Redirection vers la page de login
            return "/pages/login.xhtml?faces-redirect=true";
        } else {
            // Ajouter un message d'erreur
            facesContext.getExternalContext().getFlash().setKeepMessages(true); // Garder les messages dans la redirection
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, result, null));

            // Débogage pour vérifier le message
            System.out.println("Message ajouté: " + result);

            // Rester sur la même page sans redirection
            return null;
        }
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }

    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
}
