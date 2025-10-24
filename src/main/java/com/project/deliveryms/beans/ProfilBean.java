package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.utils.PasswordUtils;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.project.deliveryms.services.UtilisateurService;

@Named
@SessionScoped
public class ProfilBean implements Serializable {

    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    @Inject
    private LoginBean loginBean; // Utilisateur connecté

    @Inject
    private UtilisateurService utilisateurService;

    // Méthode pour mettre à jour les infos personnelles
    public void updateInfo() {
        FacesContext context = FacesContext.getCurrentInstance();
        UIComponent component = findComponent("infoForm");

        try {
            Utilisateur utilisateur = loginBean.getUtilisateur();

            // Validation des champs obligatoires
            if (utilisateur.getNom() == null || utilisateur.getNom().trim().isEmpty()) {
                context.addMessage(component.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur. Le nom ne peut pas être vide.", "Le nom ne peut pas être vide."));
                return;
            }

            if (utilisateur.getPrenom() == null || utilisateur.getPrenom().trim().isEmpty()) {
                context.addMessage(component.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur. Le prénom ne peut pas être vide.", "Le prénom ne peut pas être vide."));
                return;
            }

            if (utilisateur.getEmail() == null || utilisateur.getEmail().trim().isEmpty()) {
                context.addMessage(component.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur. L'email ne peut pas être vide.", "L'email ne peut pas être vide."));
                return;
            }

            // Vérification si l'email existe déjà pour un autre utilisateur
            Utilisateur existingUser = utilisateurService.findUserByEmail(utilisateur.getEmail());
            if (existingUser != null && !existingUser.getId().equals(utilisateur.getId())) {
                context.addMessage(component.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur. Cet email est déjà utilisé par un autre compte.", "Cet email est déjà utilisé par un autre compte."));
                return;
            }

            // Si toutes les validations sont passées, on met à jour la base de données
            utilisateurService.update(utilisateur);

            // Message de succès
            context.addMessage(component.getClientId(),
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès. Informations mises à jour avec succès.", "Informations mises à jour avec succès."));
        } catch (Exception e) {
            context.addMessage(component.getClientId(),
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur. Cet email est déjà utilisé par un autre compte. ", "Échec de la mise à jour: " + e.getMessage()));
        }
    }

    // Méthode pour mettre à jour le mot de passe
    public void updatePassword() {
        FacesContext context = FacesContext.getCurrentInstance();
        UIComponent component = findComponent("passwordForm");

        try {
            Utilisateur utilisateur = loginBean.getUtilisateur();

            // Vérification du mot de passe actuel avec BCrypt
            if (!PasswordUtils.checkPassword(oldPassword, utilisateur.getMotDePasse())) {
                context.addMessage(component.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur. Mot de passe actuel incorrect.", "Mot de passe actuel incorrect."));
                return;
            }

            // Vérifie que le nouveau mot de passe et sa confirmation correspondent
            if (!newPassword.equals(confirmPassword)) {
                context.addMessage(component.getClientId(),
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur. Les mots de passe ne correspondent pas.", "Les mots de passe ne correspondent pas."));
                return;
            }

            // Hachage du nouveau mot de passe
            String hashedPassword = PasswordUtils.hashPassword(newPassword);
            utilisateur.setMotDePasse(hashedPassword);

            // Mise à jour dans la base de données
            utilisateurService.update(utilisateur);

            // Réinitialiser les champs du formulaire
            oldPassword = null;
            newPassword = null;
            confirmPassword = null;

            context.addMessage(component.getClientId(),
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès. Mot de passe mis à jour avec succès.", "Mot de passe mis à jour avec succès."));
        } catch (Exception e) {
            context.addMessage(component.getClientId(),
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur. Échec de la mise à jour du mot de passe: ", "Échec de la mise à jour du mot de passe: " + e.getMessage()));
        }
    }

    // Méthode utilitaire pour trouver un composant par ID
    private UIComponent findComponent(String id) {
        UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
        return viewRoot.findComponent(id);
    }

    // Getters & Setters
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    // Méthodes pour récupérer les dates de création et de dernière connexion
    public LocalDateTime getCreationDate() {
        if (loginBean != null && loginBean.getUtilisateur() != null) {
            return loginBean.getUtilisateur().getCreationDate();
        }
        return null;
    }

    public LocalDateTime getLastConnectionDate() {
        if (loginBean != null && loginBean.getUtilisateur() != null) {
            return loginBean.getUtilisateur().getLastConnectionDate();
        }
        return null;
    }

    // Méthode pour formater les dates
    public String formatDate(LocalDateTime date) {
        if (date == null) {
            return "Non disponible";
        }
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
