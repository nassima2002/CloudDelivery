package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Colis;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.StatusColis;
import com.project.deliveryms.services.ColisService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class TrackingBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // Propriétés pour la recherche
    private String numeroSuivi;
    private boolean searchAttempted;
    private boolean colisFound;
    private Colis colis;

    @Inject
    private ColisService colisService;

    @Inject
    private LoginBean loginBean;

    @PostConstruct
    public void init() {
        resetFields();
    }

    public void resetFields() {
        numeroSuivi = "";
        searchAttempted = false;
        colisFound = false;
        colis = null;
    }

    /**
     * Méthode pour rechercher un colis par son numéro de suivi
     * et l'assigner à l'utilisateur connecté
     */
    public String rechercherColis() {
        searchAttempted = true;

        if (numeroSuivi != null && !numeroSuivi.trim().isEmpty()) {
            colis = colisService.getColisByNumeroSuivi(numeroSuivi.trim());
            colisFound = (colis != null);

            // Si le colis est trouvé et l'utilisateur est connecté, assigner le colis à l'utilisateur
            if (colisFound && loginBean != null && loginBean.getUtilisateur() != null) {
                Utilisateur utilisateur = loginBean.getUtilisateur();

                // Vérifier si le colis n'est pas déjà assigné à cet utilisateur
                if (colis.getUtilisateur() != null && colis.getUtilisateur().getId().equals(utilisateur.getId())) {
                    // Le colis est déjà assigné à cet utilisateur
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, 
                        "Information", "Ce colis est déjà associé à votre compte."));
                } else {
                    try {
                        // Assigner le colis à l'utilisateur
                        colisService.associerColisAUtilisateur(colis.getId(), utilisateur.getId());

                        // Mettre à jour l'objet colis local avec le nouvel utilisateur
                        colis.setUtilisateur(utilisateur);

                        FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_INFO, 
                            "Succès", "Le colis a été associé à votre compte."));
                    } catch (Exception e) {
                        FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, 
                            "Erreur", "Impossible d'associer le colis à votre compte: " + e.getMessage()));
                    }
                }
            }
        } else {
            colisFound = false;
        }

        return null;
    }

    /**
     * Méthode pour formater une date
     * @param dateTime La date à formater
     * @return La date formatée en chaîne de caractères
     */
    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dateTime.format(formatter);
    }

    /**
     * Méthode pour formater une date et heure
     * @param dateTime La date et heure à formater
     * @return La date et heure formatée en chaîne de caractères
     */
    public String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }

    /**
     * Méthode pour obtenir le libellé d'un statut
     * @param status Le statut du colis
     * @return Le libellé du statut
     */
    public String getStatusLabel(StatusColis status) {
        if (status == null) {
            return "";
        }

        switch (status) {
            case EN_TRANSIT:
                return "En transit";
            case LIVRE:
                return "Livré";
            case EN_ATTENTE:
                return "En attente";
            case RETOURNE:
                return "Retourné";
            case ANNULE:
                return "Annulé";
            default:
                return "";
        }
    }

    /**
     * Classe interne pour représenter une étape de suivi
     */
    public static class EtapeSuivi {
        private String titre;
        private String description;
        private LocalDateTime date;
        private boolean estCourant;

        public EtapeSuivi(String titre, String description, LocalDateTime date, boolean estCourant) {
            this.titre = titre;
            this.description = description;
            this.date = date;
            this.estCourant = estCourant;
        }

        // Getters
        public String getTitre() {
            return titre;
        }

        public String getDescription() {
            return description;
        }

        public LocalDateTime getDate() {
            return date;
        }

        public boolean isEstCourant() {
            return estCourant;
        }
    }

    /**
     * Méthode pour obtenir les étapes de suivi d'un colis
     * @return Liste des étapes de suivi
     */
    public List<EtapeSuivi> getEtapesSuivi() {
        List<EtapeSuivi> etapes = new ArrayList<>();

        if (colis == null) {
            return etapes;
        }

        // Étape 1: Colis enregistré
        etapes.add(new EtapeSuivi(
            "Colis enregistré",
            "Votre colis a été enregistré dans notre système",
            colis.getDateEnvoi(),
            colis.getStatus() == StatusColis.EN_ATTENTE
        ));

        // Étape 2: En transit (si applicable)
        if (colis.getStatus() == StatusColis.EN_TRANSIT || 
            colis.getStatus() == StatusColis.LIVRE || 
            colis.getStatus() == StatusColis.RETOURNE) {

            // Date estimée en transit (1 jour après l'envoi)
            LocalDateTime dateTransit = colis.getDateEnvoi().plusDays(1);

            etapes.add(new EtapeSuivi(
                "En transit",
                "Votre colis est en cours d'acheminement",
                dateTransit,
                colis.getStatus() == StatusColis.EN_TRANSIT
            ));
        }

        // Étape 3: Livré (si applicable)
        if (colis.getStatus() == StatusColis.LIVRE) {
            etapes.add(new EtapeSuivi(
                "Colis livré",
                "Votre colis a été livré à destination",
                colis.getDateLivraison() != null ? colis.getDateLivraison() : LocalDateTime.now(),
                true
            ));
        }

        // Étape 3 alternative: Retourné (si applicable)
        if (colis.getStatus() == StatusColis.RETOURNE) {
            etapes.add(new EtapeSuivi(
                "Colis retourné",
                "Votre colis a été retourné à l'expéditeur",
                colis.getDateLivraison() != null ? colis.getDateLivraison() : LocalDateTime.now(),
                true
            ));
        }

        // Étape 3 alternative: Annulé (si applicable)
        if (colis.getStatus() == StatusColis.ANNULE) {
            etapes.add(new EtapeSuivi(
                "Colis annulé",
                "L'envoi de votre colis a été annulé",
                colis.getDateEnvoi(),
                true
            ));
        }

        return etapes;
    }

    // Getters et setters
    public String getNumeroSuivi() {
        return numeroSuivi;
    }

    public void setNumeroSuivi(String numeroSuivi) {
        this.numeroSuivi = numeroSuivi;
    }

    public boolean isSearchAttempted() {
        return searchAttempted;
    }

    public void setSearchAttempted(boolean searchAttempted) {
        this.searchAttempted = searchAttempted;
    }

    public boolean isColisFound() {
        return colisFound;
    }

    public void setColisFound(boolean colisFound) {
        this.colisFound = colisFound;
    }

    public Colis getColis() {
        return colis;
    }

    public void setColis(Colis colis) {
        this.colis = colis;
    }
}
