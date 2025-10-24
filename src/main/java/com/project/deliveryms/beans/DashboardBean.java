package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Colis;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.StatusColis;
import com.project.deliveryms.services.ColisService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class DashboardBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Colis> userColis = java.util.Collections.emptyList();
    private List<Colis> dernieresCommandes = java.util.Collections.emptyList();

    @Inject
    private ColisService colisService;

    @Inject
    private LoginBean loginBean;

    @PostConstruct
    public void init() {
        loadUserColis();
        loadDernieresCommandes();
    }

    /**
     * Charge tous les colis de l'utilisateur connecté
     */
    private void loadUserColis() {
        Utilisateur utilisateur = loginBean.getUtilisateur();
        if (utilisateur != null) {
            // Récupérer tous les colis et filtrer par utilisateur
            List<Colis> allColis = colisService.getAllColisWithDetails();
            userColis = allColis.stream()
                    .filter(c -> c.getUtilisateur() != null && 
                           c.getUtilisateur().getId().equals(utilisateur.getId()) &&
                           !c.getDeleted())
                    .collect(Collectors.toList());
        }
    }

    /**
     * Charge les 5 dernières commandes de l'utilisateur
     */
    private void loadDernieresCommandes() {
        if (userColis.isEmpty()) {
            dernieresCommandes = java.util.Collections.emptyList();
            return;
        }

        // Trier par date d'envoi décroissante et prendre les 5 premiers
        dernieresCommandes = userColis.stream()
                .filter(c -> c.getDateEnvoi() != null) // Ignorer les colis sans date d'envoi
                .sorted((c1, c2) -> c2.getDateEnvoi().compareTo(c1.getDateEnvoi()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * Retourne le nombre total de commandes de l'utilisateur
     * @return Nombre de commandes
     */
    public int getNombreCommandes() {
        return userColis.size();
    }

    /**
     * Retourne le nombre de commandes en cours (en attente ou en transit)
     * @return Nombre de commandes en cours
     */
    public int getCommandesEnCours() {
        return (int) userColis.stream()
                .filter(c -> c.getStatus() != null && 
                       (c.getStatus() == StatusColis.EN_ATTENTE || c.getStatus() == StatusColis.EN_TRANSIT))
                .count();
    }

    /**
     * Retourne le montant total dépensé par l'utilisateur
     * Note: Comme il n'y a pas de champ prix dans l'entité Colis, on utilise le poids comme base de calcul
     * (10€ par kg, à titre d'exemple)
     * @return Montant total dépensé
     */
    public double getTotalDepense() {
        // Calculer le total en fonction du poids (10€ par kg)
        return userColis.stream()
                .mapToDouble(c -> c.getPoids() * 7.0)
                .sum();
    }

    /**
     * Retourne les dernières commandes de l'utilisateur
     * @return Liste des dernières commandes
     */
    public List<Colis> getDernieresCommandes() {
        return dernieresCommandes;
    }

    /**
     * Formatte une date au format dd/MM/yyyy
     * @param colis Le colis dont on veut formater la date
     * @return La date formatée
     */
    public String formatDate(Colis colis) {
        if (colis == null || colis.getDateEnvoi() == null) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return colis.getDateEnvoi().format(formatter);
    }

    /**
     * Retourne le statut d'un colis sous forme de chaîne de caractères
     * @param colis Le colis dont on veut obtenir le statut
     * @return Le statut du colis
     */
    public String getStatut(Colis colis) {
        if (colis == null || colis.getStatus() == null) {
            return "";
        }

        switch (colis.getStatus()) {
            case EN_TRANSIT:
                return "En cours";
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
     * Retourne le montant d'une commande (calculé à partir du poids)
     * @param colis Le colis dont on veut calculer le montant
     * @return Le montant de la commande
     */
    public double getMontant(Colis colis) {
        if (colis == null) {
            return 0.0;
        }

        // Calculer le montant en fonction du poids (7€ par kg)
        return colis.getPoids() * 7.0;
    }
}
