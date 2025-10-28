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
import java.time.LocalDateTime;
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
        LOG.info("Initialisation du LivreurBean");
        nouveauLivreur = new Livreur();
        nouveauLivreur.setUser(new Utilisateur());

        livreurModifie = new Livreur();
        livreurModifie.setUser(new Utilisateur());

        rafraichirListeLivreurs();
    }

    private void rafraichirListeLivreurs() {
        livreurs = livreurService.getAllLivreurs();
        LOG.info("Liste des livreurs rafraîchie : " + livreurs.size() + " livreurs trouvés");
    }

    public List<Livreur> getLivreurs() {
        if (livreurs == null) {
            rafraichirListeLivreurs();
        }
        return livreurs;
    }

    public Livreur getNouveauLivreur() {
        if (nouveauLivreur == null) {
            nouveauLivreur = new Livreur();
            nouveauLivreur.setUser(new Utilisateur());
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
            if (nouveauLivreur != null && nouveauLivreur.getUser() != null) {
                LOG.info("Tentative d'ajout d'un livreur: " + nouveauLivreur.getUser().getNom() + " " +
                        nouveauLivreur.getUser().getPrenom());

                // ✅ CORRECTION: Utiliser le mot de passe de l'utilisateur
                String motDePasse = nouveauLivreur.getUser().getMotDePasse();

                if (motDePasse == null || motDePasse.trim().isEmpty()) {
                    LOG.warning("Mot de passe vide ou null");
                    addErrorMessage("Le mot de passe est obligatoire");
                    return null;
                }

                LOG.info("Mot de passe reçu (longueur): " + motDePasse.length());

                livreurService.createLivreur(
                        nouveauLivreur.getUser().getEmail(),
                        nouveauLivreur.getUser().getNom(),
                        nouveauLivreur.getUser().getPrenom(),
                        nouveauLivreur.getLatitude(),
                        nouveauLivreur.getLongitude(),
                        nouveauLivreur.getDisponibiliter(),
                        motDePasse  // ✅ Mot de passe en clair (sera hashé par le service)
                );

                nouveauLivreur = new Livreur();
                nouveauLivreur.setUser(new Utilisateur());

                rafraichirListeLivreurs();
                addMessage("Livreur ajouté avec succès.");
                LOG.info("Livreur ajouté avec succès");
            }
        } catch (Exception e) {
            LOG.severe("Erreur lors de l'ajout du livreur: " + e.getMessage());
            addErrorMessage("Erreur lors de l'ajout: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void supprimerLivreur(Long id) {
        try {
            LOG.info("Tentative de suppression du livreur avec ID: " + id);
            livreurService.deleteLivreur(id);
            rafraichirListeLivreurs();
            addMessage("Livreur supprimé avec succès.");
            LOG.info("Livreur supprimé avec succès");
        } catch (Exception e) {
            LOG.severe("Erreur lors de la suppression du livreur: " + e.getMessage());
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
            LOG.info("Préparation de la modification pour le livreur ID: " + livreur.getId());

            Livreur livreurFromDB = livreurService.getLivreurById(livreur.getId());

            if (livreurFromDB == null) {
                LOG.warning("Livreur introuvable avec ID: " + livreur.getId());
                addErrorMessage("Livreur introuvable (ID: " + livreur.getId() + ")");
                return;
            }

            livreurModifie = new Livreur();
            livreurModifie.setId(livreurFromDB.getId());
            livreurModifie.setDisponibiliter(livreurFromDB.getDisponibiliter());

            if (livreurFromDB.getLatitude() != null) {
                livreurModifie.setLatitude(livreurFromDB.getLatitude());
            }
            if (livreurFromDB.getLongitude() != null) {
                livreurModifie.setLongitude(livreurFromDB.getLongitude());
            }

            Utilisateur userClone = new Utilisateur();
            if (livreurFromDB.getUser() != null) {
                userClone.setId(livreurFromDB.getUser().getId());
                userClone.setEmail(livreurFromDB.getUser().getEmail());
                userClone.setNom(livreurFromDB.getUser().getNom());
                userClone.setPrenom(livreurFromDB.getUser().getPrenom());
                userClone.setRole(livreurFromDB.getUser().getRole());
            }

            livreurModifie.setUser(userClone);

            LOG.info("--- Préparation Modification Livreur ---");
            LOG.info("ID: " + livreurModifie.getId());
            LOG.info("Nom: " + livreurModifie.getUser().getNom());
            LOG.info("Prénom: " + livreurModifie.getUser().getPrenom());
            LOG.info("Email: " + livreurModifie.getUser().getEmail());
            LOG.info("Disponibilité: " + livreurModifie.getDisponibiliter());
            LOG.info("-----------------------------------");

            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("formModification");

        } catch (Exception e) {
            LOG.severe("Erreur lors de la préparation de la modification: " + e.getMessage());
            addErrorMessage("Erreur lors de la préparation de la modification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public String modifierLivreur() {
        try {
            if (livreurModifie == null || livreurModifie.getId() == null) {
                LOG.warning("Tentative de modification sans livreur sélectionné");
                addErrorMessage("Aucun livreur sélectionné pour modification");
                return null;
            }

            LOG.info("--- Modification Livreur ---");
            LOG.info("ID: " + livreurModifie.getId());
            LOG.info("Nom: " + livreurModifie.getUser().getNom());
            LOG.info("Prénom: " + livreurModifie.getUser().getPrenom());
            LOG.info("Email: " + livreurModifie.getUser().getEmail());
            LOG.info("Disponibilité: " + livreurModifie.getDisponibiliter());
            LOG.info("----------------------------");

            livreurService.updateLivreur(livreurModifie);
            rafraichirListeLivreurs();
            addMessage("Livreur modifié avec succès.");
            LOG.info("Livreur modifié avec succès");

            return null;
        } catch (Exception e) {
            LOG.severe("Erreur lors de la modification: " + e.getMessage());
            addErrorMessage("Erreur lors de la modification: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}