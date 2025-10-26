package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.services.LivreurService;
import com.project.deliveryms.services.UtilisateurService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.Serializable;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private Utilisateur utilisateur;
    private String email;
    private String motDePasse;
    private Role role;

    @Inject
    private UtilisateurService utilisateurService;

    @Inject
    private LivreurService livreurService;

    public String login() {
        String result = utilisateurService.authentifier(email, motDePasse);

        if ("Connexion réussie".equals(result)) {
            // Récupérer l'utilisateur par email
            Utilisateur utilisateur = utilisateurService.findUserByEmail(email);

            if (utilisateur != null) {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

                Role role = utilisateur.getRole();
                session.setAttribute("utilisateurConnecte", utilisateur);

                // Redirection selon le rôle
                switch (role) {
                    case ADMIN:
                        return "/admin/admin-dashboard.xhtml?faces-redirect=true";

                    case LIVREUR:
                        Livreur livreur = livreurService.findByEmail(utilisateur.getEmail());
                        if (livreur != null) {
                            session.setAttribute("utilisateurConnecte", livreur);
                        }
                        return "/livreur/livreur-dashboard.xhtml?faces-redirect=true";

                    case CLIENT:
                        return "/pages/dashboard.xhtml?faces-redirect=true";

                    default:
                        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Erreur", "Rôle utilisateur non reconnu"));
                        return null;
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Utilisateur introuvable"));
                return null;
            }
        } else {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().getFlash().setKeepMessages(true);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, result, null));
            return null;
        }
    }

    public String logout() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        Role roleUtilisateur = null;

        if (session != null) {
            // 🔹 Récupérer le rôle avant d’invalider la session
            Object userObj = session.getAttribute("utilisateurConnecte");
            if (userObj instanceof Utilisateur) {
                roleUtilisateur = ((Utilisateur) userObj).getRole();
            } else if (userObj instanceof Livreur) {
                roleUtilisateur = Role.LIVREUR; // cas où on a stocké un livreur
            }

            // 🔹 Supprimer la session après avoir récupéré le rôle
            session.invalidate();
        }

        try {
            String basePath = facesContext.getExternalContext().getRequestContextPath();

            // 🔹 Redirection selon le rôle
            if (roleUtilisateur == Role.ADMIN) {
                facesContext.getExternalContext().redirect(basePath + "/admin/login.xhtml");
            } else if (roleUtilisateur == Role.LIVREUR) {
                facesContext.getExternalContext().redirect(basePath + "/livreur/login.xhtml");
            } else {
                facesContext.getExternalContext().redirect(basePath + "/pages/login.xhtml");
            }

        } catch (IOException e) {
            e.printStackTrace();
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

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
