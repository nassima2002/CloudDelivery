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
import java.util.logging.Logger;

@Named
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LoginBean.class.getName());

    private Utilisateur utilisateur;
    private String email;
    private String motDePasse;
    private Role role;

    @Inject
    private UtilisateurService utilisateurService;

    @Inject
    private LivreurService livreurService;

    public void login() {
        LOG.info("========================================");
        LOG.info("=== DÉBUT LOGIN BEAN ===");
        LOG.info("Email: " + email);
        LOG.info("Mot de passe (longueur): " + (motDePasse != null ? motDePasse.length() : "NULL"));

        try {
            // Vérification des champs
            if (email == null || email.trim().isEmpty()) {
                LOG.warning("❌ Email vide");
                addErrorMessage("L'email est obligatoire");
                return;
            }

            if (motDePasse == null || motDePasse.trim().isEmpty()) {
                LOG.warning("❌ Mot de passe vide");
                addErrorMessage("Le mot de passe est obligatoire");
                return;
            }

            // Authentification
            String result = utilisateurService.authentifier(email, motDePasse);
            LOG.info("Résultat authentification: " + result);

            if (!"Connexion réussie".equals(result)) {
                LOG.warning("❌ Échec authentification: " + result);
                addErrorMessage(result);
                return;
            }

            LOG.info("✅ Authentification réussie");

            // Récupération de l'utilisateur
            Utilisateur utilisateur = utilisateurService.findUserByEmail(email);

            if (utilisateur == null) {
                LOG.severe("❌ Utilisateur NULL après authentification réussie!");
                addErrorMessage("Erreur: Utilisateur introuvable");
                return;
            }

            LOG.info("✅ Utilisateur récupéré: " + utilisateur.getEmail());
            LOG.info("   - ID: " + utilisateur.getId());
            LOG.info("   - Rôle: " + utilisateur.getRole());

            // Création de la session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            LOG.info("✅ Session créée: " + session.getId());

            Role role = utilisateur.getRole();
            String redirectUrl = null;

            // Déterminer l'URL de redirection selon le rôle
            switch (role) {
                case ADMIN:
                    LOG.info("🔄 Redirection ADMIN vers /admin/admin-dashboard.xhtml");
                    session.setAttribute("utilisateurConnecte", utilisateur);
                    redirectUrl = "/admin/admin-dashboard.xhtml";
                    break;

                case LIVREUR:
                    LOG.info("🔍 Recherche du profil LIVREUR pour: " + utilisateur.getEmail());
                    Livreur livreur = livreurService.findByEmail(utilisateur.getEmail());

                    if (livreur != null) {
                        LOG.info("✅ Livreur trouvé - ID: " + livreur.getId());
                        LOG.info("   - Latitude: " + livreur.getLatitude());
                        LOG.info("   - Longitude: " + livreur.getLongitude());
                        LOG.info("   - Disponibilité: " + livreur.getDisponibiliter());

                        session.setAttribute("utilisateurConnecte", livreur);
                        LOG.info("✅ Livreur stocké en session");
                        LOG.info("🔄 Redirection LIVREUR vers /admin/admin-livreurs.xhtml");

                        redirectUrl = "/livreur/dashboard.xhtml";
                    } else {
                        LOG.severe("❌ Profil livreur NON trouvé pour: " + utilisateur.getEmail());
                        addErrorMessage("Profil livreur non trouvé pour cet utilisateur");
                        return;
                    }
                    break;

                case CLIENT:
                    LOG.info("🔄 Redirection CLIENT vers /pages/dashboard.xhtml");
                    session.setAttribute("utilisateurConnecte", utilisateur);
                    redirectUrl = "/pages/dashboard.xhtml";
                    break;

                default:
                    LOG.warning("❌ Rôle non reconnu: " + role);
                    addErrorMessage("Rôle utilisateur non reconnu");
                    return;
            }

            // Effectuer la redirection
            if (redirectUrl != null) {
                String contextPath = facesContext.getExternalContext().getRequestContextPath();
                String fullUrl = contextPath + redirectUrl;
                LOG.info("🚀 Redirection vers: " + fullUrl);

                facesContext.getExternalContext().redirect(fullUrl);
                facesContext.responseComplete();
            }

        } catch (IOException e) {
            LOG.severe("❌ ERREUR lors de la redirection: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur lors de la redirection: " + e.getMessage());
        } catch (Exception e) {
            LOG.severe("❌ ERREUR CRITIQUE lors du login: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur lors de la connexion: " + e.getMessage());
        } finally {
            LOG.info("=== FIN LOGIN BEAN ===");
            LOG.info("========================================");
        }
    }

    public String logout() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        Role roleUtilisateur = null;

        if (session != null) {
            Object userObj = session.getAttribute("utilisateurConnecte");
            if (userObj instanceof Utilisateur) {
                roleUtilisateur = ((Utilisateur) userObj).getRole();
            } else if (userObj instanceof Livreur) {
                roleUtilisateur = Role.LIVREUR;
            }

            session.invalidate();
        }

        try {
            String basePath = facesContext.getExternalContext().getRequestContextPath();

            if (roleUtilisateur == Role.ADMIN) {
                facesContext.getExternalContext().redirect(basePath + "/admin/login.xhtml");
            } else if (roleUtilisateur == Role.LIVREUR) {
                facesContext.getExternalContext().redirect(basePath + "/livreur/login.xhtml");
            } else {
                facesContext.getExternalContext().redirect(basePath + "/pages/login.xhtml");
            }

        } catch (IOException e) {
            LOG.severe("Erreur lors du logout: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    private void addErrorMessage(String message) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        facesContext.getExternalContext().getFlash().setKeepMessages(true);
        facesContext.addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
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