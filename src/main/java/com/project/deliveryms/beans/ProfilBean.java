package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.utils.PasswordUtils;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.project.deliveryms.services.UtilisateurService;

@Named
@SessionScoped
public class ProfilBean implements Serializable {

    @Inject
    private LoginBean loginBean;

    @Inject
    private UtilisateurService utilisateurService;

    // Propriétés pour le mot de passe uniquement
    private String oldPassword;
    private String newPassword;
    private String confirmPassword;

    // Initialisation : Vérifier et charger l'utilisateur connecté
    @PostConstruct
    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            // Vérifier si loginBean et son utilisateur existent
            if (loginBean == null || loginBean.getUtilisateur() == null) {
                // Essayer de récupérer l'utilisateur depuis la session
                HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

                if (session != null) {
                    Object utilisateurSession = session.getAttribute("utilisateurConnecte");

                    if (utilisateurSession != null && loginBean != null) {
                        // Recharger l'utilisateur depuis la base de données
                        String emailUtilisateur = null;

                        if (utilisateurSession instanceof Utilisateur) {
                            emailUtilisateur = ((Utilisateur) utilisateurSession).getEmail();
                        }

                        if (emailUtilisateur != null) {
                            Utilisateur utilisateurActuel = utilisateurService.findUserByEmail(emailUtilisateur);
                            if (utilisateurActuel != null) {
                                loginBean.setUtilisateur(utilisateurActuel);
                                return; // Utilisateur chargé avec succès
                            }
                        }
                    }
                }

                // Si on arrive ici, pas d'utilisateur connecté
                context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/login.xhtml");
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                context.getExternalContext().redirect(context.getExternalContext().getRequestContextPath() + "/login.xhtml");
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    // Méthode pour mettre à jour les infos personnelles
    public void updateInfo() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            Utilisateur utilisateur = loginBean.getUtilisateur();

            // Validation des champs obligatoires
            if (utilisateur.getNom() == null || utilisateur.getNom().trim().isEmpty()) {
                context.addMessage("infoForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le nom ne peut pas être vide."));
                return;
            }

            if (utilisateur.getPrenom() == null || utilisateur.getPrenom().trim().isEmpty()) {
                context.addMessage("infoForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le prénom ne peut pas être vide."));
                return;
            }

            if (utilisateur.getEmail() == null || utilisateur.getEmail().trim().isEmpty()) {
                context.addMessage("infoForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "L'email ne peut pas être vide."));
                return;
            }

            // Vérification si l'email existe déjà pour un autre utilisateur
            Utilisateur existingUser = utilisateurService.findUserByEmail(utilisateur.getEmail());
            if (existingUser != null && !existingUser.getId().equals(utilisateur.getId())) {
                context.addMessage("infoForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Cet email est déjà utilisé par un autre compte."));
                return;
            }

            // Sauvegarder dans la base de données
            utilisateurService.update(utilisateur);

            // Message de succès
            context.addMessage("infoForm",
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Informations mises à jour avec succès."));
        } catch (Exception e) {
            context.addMessage("infoForm",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Échec de la mise à jour: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // Méthode pour mettre à jour le mot de passe
    public void updatePassword() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            Utilisateur utilisateur = loginBean.getUtilisateur();

            // Validation des champs
            if (oldPassword == null || oldPassword.trim().isEmpty()) {
                context.addMessage("passwordForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le mot de passe actuel est obligatoire."));
                return;
            }

            if (newPassword == null || newPassword.trim().isEmpty()) {
                context.addMessage("passwordForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le nouveau mot de passe est obligatoire."));
                return;
            }

            if (confirmPassword == null || confirmPassword.trim().isEmpty()) {
                context.addMessage("passwordForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "La confirmation du mot de passe est obligatoire."));
                return;
            }

            // Vérification du mot de passe actuel avec BCrypt
            if (!PasswordUtils.checkPassword(oldPassword, utilisateur.getMotDePasse())) {
                context.addMessage("passwordForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Mot de passe actuel incorrect."));
                return;
            }

            // Vérifie que le nouveau mot de passe et sa confirmation correspondent
            if (!newPassword.equals(confirmPassword)) {
                context.addMessage("passwordForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Les mots de passe ne correspondent pas."));
                return;
            }

            // Validation du format du mot de passe (optionnel)
            if (newPassword.length() < 8) {
                context.addMessage("passwordForm",
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le mot de passe doit contenir au moins 8 caractères."));
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

            context.addMessage("passwordForm",
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Mot de passe mis à jour avec succès."));
        } catch (Exception e) {
            context.addMessage("passwordForm",
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Échec de la mise à jour du mot de passe: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    // Getters & Setters pour le mot de passe
    public String getOldPassword() { return oldPassword; }
    public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }

    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }

    // Méthodes pour récupérer les dates
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