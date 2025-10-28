package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.services.LivreurService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.util.logging.Logger;

/**
 * Bean de test pour créer rapidement un livreur et tester la connexion
 * ⚠️ À SUPPRIMER EN PRODUCTION
 */
@Named
@ViewScoped
public class TestLivreurBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(TestLivreurBean.class.getName());

    @Inject
    private LivreurService livreurService;

    private String email = "test.livreur@example.com";
    private String motDePasse = "Test123!";
    private String nom = "Test";
    private String prenom = "Livreur";
    private boolean livreurCree = false;
    private Long livreurId;
    private String hashGenere;

    @PostConstruct
    public void init() {
        LOG.info("=== TestLivreurBean initialisé ===");
    }

    /**
     * Crée un livreur de test avec les informations prédéfinies
     */
    public void creerLivreurTest() {
        try {
            LOG.info("╔═══════════════════════════════════════════╗");
            LOG.info("║   🧪 CRÉATION LIVREUR DE TEST             ║");
            LOG.info("╚═══════════════════════════════════════════╝");
            LOG.info("Email: " + email);
            LOG.info("Mot de passe: " + motDePasse);
            LOG.info("Nom: " + nom + " " + prenom);

            // Vérifier si le livreur existe déjà
            Livreur existant = livreurService.findByEmail(email);
            if (existant != null) {
                LOG.warning("⚠️ Un livreur avec cet email existe déjà (ID: " + existant.getId() + ")");
                addWarningMessage("Ce livreur existe déjà. Supprimez-le d'abord ou utilisez un autre email.");

                // Afficher les infos du livreur existant
                livreurId = existant.getId();
                livreurCree = true;

                return;
            }

            // 🔹 IMPORTANT: Générer le hash pour vérification
            LOG.info("🔐 Génération du hash BCrypt pour vérification...");
            hashGenere = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
            LOG.info("Hash généré: " + hashGenere);
            LOG.info("Longueur: " + hashGenere.length() + " caractères");

            // Créer le livreur
            Livreur livreur = livreurService.createLivreur(
                    email,
                    nom,
                    prenom,
                    33.5731,  // Latitude Casablanca
                    -7.5898,  // Longitude Casablanca
                    "oui",
                    motDePasse  // ✅ En clair, sera hashé dans le service
            );

            livreurId = livreur.getId();
            livreurCree = true;

            LOG.info("╔═══════════════════════════════════════════╗");
            LOG.info("║   ✅ LIVREUR CRÉÉ AVEC SUCCÈS             ║");
            LOG.info("╚═══════════════════════════════════════════╝");
            LOG.info("ID du livreur: " + livreurId);
            LOG.info("");
            LOG.info("📋 IDENTIFIANTS DE CONNEXION:");
            LOG.info("   Email: " + email);
            LOG.info("   Mot de passe: " + motDePasse);
            LOG.info("");
            LOG.info("🔗 URL de connexion: /livreur/login.xhtml");
            LOG.info("╚═══════════════════════════════════════════╝");

            addSuccessMessage("✅ Livreur créé avec succès !");
            addInfoMessage("Email: " + email);
            addInfoMessage("Mot de passe: " + motDePasse);
            addInfoMessage("Vous pouvez maintenant vous connecter sur /livreur/login.xhtml");

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la création du livreur de test: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur: " + e.getMessage());
        }
    }

    /**
     * Teste la vérification d'un mot de passe avec un hash
     */
    public void testerVerificationMotDePasse() {
        try {
            LOG.info("=== Test de vérification BCrypt ===");

            if (hashGenere == null || hashGenere.isEmpty()) {
                hashGenere = BCrypt.hashpw(motDePasse, BCrypt.gensalt());
            }

            boolean match = BCrypt.checkpw(motDePasse, hashGenere);

            LOG.info("Mot de passe: " + motDePasse);
            LOG.info("Hash: " + hashGenere);
            LOG.info("Vérification: " + (match ? "✅ SUCCÈS" : "❌ ÉCHEC"));

            if (match) {
                addSuccessMessage("✅ La vérification BCrypt fonctionne correctement !");
            } else {
                addErrorMessage("❌ Échec de la vérification BCrypt");
            }

        } catch (Exception e) {
            LOG.severe("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur: " + e.getMessage());
        }
    }

    /**
     * Supprime le livreur de test
     */
    public void supprimerLivreurTest() {
        try {
            Livreur livreur = livreurService.findByEmail(email);

            if (livreur == null) {
                addWarningMessage("Aucun livreur trouvé avec cet email");
                return;
            }

            LOG.info("🗑️ Suppression du livreur de test ID: " + livreur.getId());
            livreurService.deleteLivreur(livreur.getId());

            livreurCree = false;
            livreurId = null;
            hashGenere = null;

            LOG.info("✅ Livreur de test supprimé");
            addSuccessMessage("Livreur de test supprimé avec succès");

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Erreur: " + e.getMessage());
        }
    }

    /**
     * Génère un nouveau hash BCrypt pour le mot de passe actuel
     */
    public void genererNouveauHash() {
        try {
            LOG.info("🔐 Génération d'un nouveau hash...");
            hashGenere = BCrypt.hashpw(motDePasse, BCrypt.gensalt());

            LOG.info("Mot de passe: " + motDePasse);
            LOG.info("Nouveau hash: " + hashGenere);
            LOG.info("Longueur: " + hashGenere.length());

            addSuccessMessage("Hash généré avec succès !");
            addInfoMessage("Hash: " + hashGenere);

        } catch (Exception e) {
            LOG.severe("❌ Erreur: " + e.getMessage());
            addErrorMessage("Erreur: " + e.getMessage());
        }
    }

    // Méthodes utilitaires
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void addWarningMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    // Getters et Setters
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

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public boolean isLivreurCree() {
        return livreurCree;
    }

    public Long getLivreurId() {
        return livreurId;
    }

    public String getHashGenere() {
        return hashGenere;
    }
}