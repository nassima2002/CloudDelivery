package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.services.LivreurService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.logging.Logger;

@Named
@ViewScoped
public class LivreurBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = Logger.getLogger(LivreurBean.class.getName());

    @Inject
    private LivreurService livreurService;

    private Livreur nouveauLivreur;
    private Livreur livreurModifie;
    private List<Livreur> livreurs;

    @PostConstruct
    public void init() {
        LOG.info("=== Initialisation du LivreurBean ===");
        initialiserNouveauLivreur();
        initialiserLivreurModifie();
        rafraichirListeLivreurs();
    }

    private void initialiserNouveauLivreur() {
        nouveauLivreur = new Livreur();
        Utilisateur user = new Utilisateur();
        // 🔹 CRITIQUE: Définir le rôle LIVREUR dès l'initialisation
        user.setRole(Role.LIVREUR);
        nouveauLivreur.setUser(user);
        nouveauLivreur.setDisponibiliter("oui"); // Disponible par défaut
        LOG.info("Nouveau livreur initialisé avec rôle LIVREUR");
    }

    private void initialiserLivreurModifie() {
        livreurModifie = new Livreur();
        Utilisateur user = new Utilisateur();
        user.setRole(Role.LIVREUR);
        livreurModifie.setUser(user);
    }

    private void rafraichirListeLivreurs() {
        livreurs = livreurService.getAllLivreurs();
        LOG.info("Liste des livreurs rafraîchie : " + (livreurs != null ? livreurs.size() : 0) + " livreurs trouvés");
    }

    public List<Livreur> getLivreurs() {
        if (livreurs == null) {
            rafraichirListeLivreurs();
        }
        return livreurs;
    }

    public Livreur getNouveauLivreur() {
        if (nouveauLivreur == null) {
            initialiserNouveauLivreur();
        }
        return nouveauLivreur;
    }

    public void setNouveauLivreur(Livreur nouveauLivreur) {
        this.nouveauLivreur = nouveauLivreur;
    }

    public Livreur getLivreurModifie() {
        return livreurModifie;
    }

    public void setLivreurModifie(Livreur livreurModifie) {
        this.livreurModifie = livreurModifie;
    }

    public String ajouterLivreur() {
        try {
            if (nouveauLivreur == null || nouveauLivreur.getUser() == null) {
                addErrorMessage("Données du livreur invalides");
                return null;
            }

            // 🔹 CRITIQUE: S'assurer que le rôle LIVREUR est défini
            nouveauLivreur.getUser().setRole(Role.LIVREUR);

            LOG.info("=== Ajout d'un nouveau livreur ===");
            LOG.info("Nom: " + nouveauLivreur.getUser().getNom());
            LOG.info("Prénom: " + nouveauLivreur.getUser().getPrenom());
            LOG.info("Email: " + nouveauLivreur.getUser().getEmail());
            LOG.info("Rôle: " + nouveauLivreur.getUser().getRole());
            LOG.info("Mot de passe défini: " + (nouveauLivreur.getPassword() != null && !nouveauLivreur.getPassword().isEmpty()));
            LOG.info("Disponibilité: " + nouveauLivreur.getDisponibiliter());

            // Validation des champs obligatoires
            if (nouveauLivreur.getUser().getNom() == null || nouveauLivreur.getUser().getNom().trim().isEmpty()) {
                addErrorMessage("Le nom est obligatoire");
                return null;
            }

            if (nouveauLivreur.getUser().getPrenom() == null || nouveauLivreur.getUser().getPrenom().trim().isEmpty()) {
                addErrorMessage("Le prénom est obligatoire");
                return null;
            }

            if (nouveauLivreur.getUser().getEmail() == null || nouveauLivreur.getUser().getEmail().trim().isEmpty()) {
                addErrorMessage("L'email est obligatoire");
                return null;
            }

            if (nouveauLivreur.getPassword() == null || nouveauLivreur.getPassword().trim().isEmpty()) {
                addErrorMessage("Le mot de passe est obligatoire");
                return null;
            }

            // Validation du format email
            if (!nouveauLivreur.getUser().getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                addErrorMessage("Format d'email invalide");
                return null;
            }

            // Créer le livreur via le service
            livreurService.createLivreur(
                    nouveauLivreur.getUser().getEmail(),
                    nouveauLivreur.getUser().getNom(),
                    nouveauLivreur.getUser().getPrenom(),
                    nouveauLivreur.getLatitude(),
                    nouveauLivreur.getLongitude(),
                    nouveauLivreur.getDisponibiliter(),
                    nouveauLivreur.getPassword()
            );

            LOG.info("✅ Livreur créé avec succès. Il peut maintenant se connecter.");

            // Réinitialiser le formulaire
            initialiserNouveauLivreur();

            rafraichirListeLivreurs();
            addMessage("Livreur ajouté avec succès. Il peut maintenant se connecter.");

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de l'ajout du livreur: " + e.getMessage());
            addErrorMessage("Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void supprimerLivreur(Long id) {
        try {
            LOG.info("🗑️ Tentative de suppression du livreur avec ID: " + id);
            livreurService.deleteLivreur(id);
            rafraichirListeLivreurs();
            addMessage("Livreur supprimé avec succès.");
            LOG.info("✅ Livreur supprimé avec succès");
        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la suppression du livreur: " + e.getMessage());
            addErrorMessage("Erreur lors de la suppression: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void addMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    public void preparerModification(Livreur livreur) {
        try {
            LOG.info("🔧 Préparation de la modification pour le livreur ID: " + livreur.getId());

            // Récupérer l'entité complète depuis la base de données
            Livreur livreurFromDB = livreurService.getLivreurById(livreur.getId());

            if (livreurFromDB == null) {
                LOG.warning("❌ Livreur introuvable avec ID: " + livreur.getId());
                addErrorMessage("Livreur introuvable (ID: " + livreur.getId() + ")");
                return;
            }

            // Créer une nouvelle instance pour éviter les problèmes de détachement
            livreurModifie = new Livreur();
            livreurModifie.setId(livreurFromDB.getId());
            livreurModifie.setDisponibiliter(livreurFromDB.getDisponibiliter());

            // Copier les coordonnées si elles existent
            if (livreurFromDB.getLatitude() != null) {
                livreurModifie.setLatitude(livreurFromDB.getLatitude());
            }
            if (livreurFromDB.getLongitude() != null) {
                livreurModifie.setLongitude(livreurFromDB.getLongitude());
            }

            // Copier les informations utilisateur
            Utilisateur userClone = new Utilisateur();
            if (livreurFromDB.getUser() != null) {
                userClone.setId(livreurFromDB.getUser().getId());
                userClone.setEmail(livreurFromDB.getUser().getEmail());
                userClone.setNom(livreurFromDB.getUser().getNom());
                userClone.setPrenom(livreurFromDB.getUser().getPrenom());
                // 🔹 S'assurer que le rôle reste LIVREUR
                userClone.setRole(Role.LIVREUR);
            }

            livreurModifie.setUser(userClone);

            // Logs pour vérification
            LOG.info("=== Préparation Modification Livreur ===");
            LOG.info("ID: " + livreurModifie.getId());
            LOG.info("Nom: " + livreurModifie.getUser().getNom());
            LOG.info("Prénom: " + livreurModifie.getUser().getPrenom());
            LOG.info("Email: " + livreurModifie.getUser().getEmail());
            LOG.info("Rôle: " + livreurModifie.getUser().getRole());
            LOG.info("Disponibilité: " + livreurModifie.getDisponibiliter());
            LOG.info("========================================");

            // Force le bean à être synchronisé avec la vue
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("formModification");

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la préparation de la modification: " + e.getMessage());
            addErrorMessage("Erreur lors de la préparation de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String modifierLivreur() {
        try {
            if (livreurModifie == null || livreurModifie.getId() == null) {
                LOG.warning("⚠️ Tentative de modification sans livreur sélectionné");
                addErrorMessage("Aucun livreur sélectionné pour modification");
                return null;
            }

            // 🔹 S'assurer que le rôle reste LIVREUR
            if (livreurModifie.getUser() != null) {
                livreurModifie.getUser().setRole(Role.LIVREUR);
            }

            LOG.info("=== Modification Livreur ===");
            LOG.info("ID: " + livreurModifie.getId());
            LOG.info("Nom: " + livreurModifie.getUser().getNom());
            LOG.info("Prénom: " + livreurModifie.getUser().getPrenom());
            LOG.info("Email: " + livreurModifie.getUser().getEmail());
            LOG.info("Rôle: " + livreurModifie.getUser().getRole());
            LOG.info("Disponibilité: " + livreurModifie.getDisponibiliter());
            LOG.info("============================");

            livreurService.updateLivreur(livreurModifie);
            rafraichirListeLivreurs();
            addMessage("Livreur modifié avec succès.");
            LOG.info("✅ Livreur modifié avec succès");

            return null;
        } catch (Exception e) {
            LOG.severe("❌ Erreur lors de la modification: " + e.getMessage());
            addErrorMessage("Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}