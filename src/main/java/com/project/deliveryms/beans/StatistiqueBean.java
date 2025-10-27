package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Colis;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

/**
 * Bean managé pour afficher les statistiques du tableau de bord
 * @author Delivery Management System
 */
@Named
@SessionScoped
public class StatistiqueBean implements Serializable {

    private static final long serialVersionUID = 1L;

    // ============================================
    // INJECTION DE DÉPENDANCES
    // ============================================

    @PersistenceContext
    private EntityManager em;

    // ============================================
    // ATTRIBUTS - STATISTIQUES
    // ============================================

    private long nbTotalColis;
    private long nbTotalClients;
    private long nbTotalLivreurs;

    // ============================================
    // ATTRIBUTS - DONNÉES
    // ============================================

    private List<Colis> derniersColis;

    // ============================================
    // INITIALISATION
    // ============================================

    /**
     * Méthode d'initialisation appelée après la construction du bean
     * Charge toutes les statistiques au démarrage
     */
    @PostConstruct
    public void init() {
        chargerStatistiques();
        chargerDerniersColis();
    }

    // ============================================
    // MÉTHODES DE CHARGEMENT DES DONNÉES
    // ============================================

    /**
     * Charge les statistiques depuis la base de données
     */
    @Transactional
    public void chargerStatistiques() {
        try {
            // Nombre total de colis
            nbTotalColis = em.createQuery(
                            "SELECT COUNT(c) FROM Colis c", Long.class)
                    .getSingleResult();

            // Nombre total de clients (utilisateurs avec role = 'CLIENT')
            nbTotalClients = em.createQuery(
                            "SELECT COUNT(u) FROM Utilisateur u WHERE u.role = 'CLIENT'", Long.class)
                    .getSingleResult();

            // Nombre total de livreurs (utilisateurs avec role = 'LIVREUR')
            nbTotalLivreurs = em.createQuery(
                            "SELECT COUNT(u) FROM Utilisateur u WHERE u.role = 'LIVREUR'", Long.class)
                    .getSingleResult();

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des statistiques : " + e.getMessage());
            e.printStackTrace();

            // Valeurs par défaut en cas d'erreur
            nbTotalColis = 0;
            nbTotalClients = 0;
            nbTotalLivreurs = 0;
        }
    }

    /**
     * Charge les 5 derniers colis depuis la base de données
     * Triés par date d'envoi décroissante
     */
    @Transactional
    public void chargerDerniersColis() {
        try {
            derniersColis = em.createQuery(
                            "SELECT c FROM Colis c ORDER BY c.dateEnvoi DESC", Colis.class)
                    .setMaxResults(5)
                    .getResultList();

            System.out.println("✅ " + derniersColis.size() + " derniers colis chargés");

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des derniers colis : " + e.getMessage());
            e.printStackTrace();

            // Liste vide en cas d'erreur
            derniersColis = List.of();
        }
    }

    /**
     * Rafraîchit toutes les données (statistiques + derniers colis)
     * Utilisé par le bouton "Rafraîchir" dans l'interface
     */
    public void rafraichir() {
        System.out.println("🔄 Rafraîchissement des statistiques...");
        chargerStatistiques();
        chargerDerniersColis();
        System.out.println("✅ Rafraîchissement terminé");
    }

    // ============================================
    // MÉTHODES UTILITAIRES - FORMATAGE
    // ============================================

    /**
     * Formate une date au format "dd/MM/yyyy HH:mm"
     * @param date La date à formater
     * @return La date formatée ou une chaîne vide si date nulle
     */
    public String formaterDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        return sdf.format(date);
    }

    /**
     * Retourne la classe CSS Tailwind appropriée pour le badge de statut
     * @param statut Le statut du colis
     * @return La classe CSS pour le badge
     */
    public String getStatutBadgeClass(String statut) {
        if (statut == null) {
            return "bg-gray-100 text-gray-800";
        }

        switch (statut.toUpperCase()) {
            case "EN_ATTENTE":
            case "NOUVEAU":
                return "bg-blue-100 text-blue-800";

            case "EN_COURS":
            case "EN_TRANSIT":
                return "bg-amber-100 text-amber-800";

            case "LIVRE":
            case "LIVRÉ":
                return "bg-emerald-100 text-emerald-800";

            case "ANNULE":
            case "ANNULÉ":
                return "bg-red-100 text-red-800";

            case "EN_PREPARATION":
                return "bg-purple-100 text-purple-800";

            default:
                return "bg-gray-100 text-gray-800";
        }
    }

    /**
     * Retourne le libellé français du statut
     * @param statut Le statut du colis
     * @return Le libellé formaté
     */
    public String getStatutLibelle(String statut) {
        if (statut == null) {
            return "Inconnu";
        }

        switch (statut.toUpperCase()) {
            case "EN_ATTENTE":
                return "En attente";
            case "NOUVEAU":
                return "Nouveau";
            case "EN_COURS":
                return "En cours";
            case "EN_TRANSIT":
                return "En transit";
            case "LIVRE":
                return "Livré";
            case "ANNULE":
                return "Annulé";
            case "EN_PREPARATION":
                return "En préparation";
            default:
                return statut.replace("_", " ");
        }
    }

    // ============================================
    // GETTERS
    // ============================================

    public long getNbTotalColis() {
        return nbTotalColis;
    }

    public long getNbTotalClients() {
        return nbTotalClients;
    }

    public long getNbTotalLivreurs() {
        return nbTotalLivreurs;
    }

    public List<Colis> getDerniersColis() {
        return derniersColis;
    }

    public String formaterDate(LocalDateTime date) {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

}