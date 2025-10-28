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

    public String login() {
        LOG.info("═══════════════════════════════════");
        LOG.info("🔐 TENTATIVE DE CONNEXION");
        LOG.info("═══════════════════════════════════");
        LOG.info("Email: " + email);

        String result = utilisateurService.authentifier(email, motDePasse);

        if ("Connexion réussie".equals(result)) {
            // Récupérer l'utilisateur par email
            Utilisateur utilisateur = utilisateurService.findUserByEmail(email);

            if (utilisateur != null) {
                LOG.info("✅ Utilisateur trouvé: " + utilisateur.getNom() + " " + utilisateur.getPrenom());
                LOG.info("   Rôle: " + utilisateur.getRole());

                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

                Role role = utilisateur.getRole();

                // ✅ TOUJOURS stocker l'utilisateur dans "utilisateurConnecte"
                session.setAttribute("utilisateurConnecte", utilisateur);
                LOG.info("✅ Utilisateur stocké en session");

                // Redirection selon le rôle
                switch (role) {
                    case ADMIN:
                        LOG.info("➡️  Redirection ADMIN");
                        LOG.info("═══════════════════════════════════");
                        return "/admin/admin-dashboard.xhtml?faces-redirect=true";

                    case LIVREUR:
                        LOG.info("➡️  Redirection LIVREUR");
                        Livreur livreur = livreurService.findByEmail(utilisateur.getEmail());
                        if (livreur != null) {
                            LOG.info("   Livreur ID: " + livreur.getId());
                            LOG.info("   Disponibilité: " + livreur.getDisponibiliter());

                            // ✅ STOCKER LE LIVREUR DANS UN ATTRIBUT SÉPARÉ
                            // NE PAS écraser "utilisateurConnecte"
                            session.setAttribute("livreurConnecte", livreur);
                            LOG.info("✅ Livreur stocké en session (attribut séparé)");
                        } else {
                            LOG.warning("⚠️  Aucun livreur trouvé pour cet utilisateur");
                        }
                        LOG.info("═══════════════════════════════════");
                        return "/livreur/livreur-dashboard.xhtml?faces-redirect=true";

                    case CLIENT:
                        LOG.info("➡️  Redirection CLIENT");
                        LOG.info("═══════════════════════════════════");
                        return "/pages/dashboard.xhtml?faces-redirect=true";

                    default:
                        LOG.warning("❌ Rôle non reconnu: " + role);
                        LOG.info("═══════════════════════════════════");
                        facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                "Erreur", "Rôle utilisateur non reconnu"));
                        return null;
                }
            } else {
                LOG.warning("❌ Utilisateur introuvable après authentification");
                LOG.info("═══════════════════════════════════");
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Utilisateur introuvable"));
                return null;
            }
        } else {
            LOG.warning("❌ Échec authentification: " + result);
            LOG.info("═══════════════════════════════════");
            FacesContext facesContext = FacesContext.getCurrentInstance();
            facesContext.getExternalContext().getFlash().setKeepMessages(true);
            facesContext.addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, result, null));
            return null;
        }
    }

    public String logout() {
        LOG.info("🚪 Déconnexion en cours...");

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);

        Role roleUtilisateur = null;

        if (session != null) {
            // 🔹 Récupérer le rôle avant d'invalider la session
            Object userObj = session.getAttribute("utilisateurConnecte");
            if (userObj instanceof Utilisateur) {
                roleUtilisateur = ((Utilisateur) userObj).getRole();
                LOG.info("Rôle utilisateur: " + roleUtilisateur);
            }

            // 🔹 Invalider la session (supprime tous les attributs)
            session.invalidate();
            LOG.info("✅ Session invalidée");
        }

        try {
            String basePath = facesContext.getExternalContext().getRequestContextPath();

            // 🔹 Redirection selon le rôle
            if (roleUtilisateur == Role.ADMIN) {
                LOG.info("➡️  Redirection vers login admin");
                facesContext.getExternalContext().redirect(basePath + "/admin/login.xhtml");
            } else if (roleUtilisateur == Role.LIVREUR) {
                LOG.info("➡️  Redirection vers login livreur");
                facesContext.getExternalContext().redirect(basePath + "/livreur/login.xhtml");
            } else {
                LOG.info("➡️  Redirection vers login client");
                facesContext.getExternalContext().redirect(basePath + "/pages/login.xhtml");
            }

        } catch (IOException e) {
            LOG.severe("❌ Erreur lors de la redirection: " + e.getMessage());
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