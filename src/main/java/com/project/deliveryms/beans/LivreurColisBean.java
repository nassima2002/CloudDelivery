package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Colis;
import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.StatusColis;
import com.project.deliveryms.services.ColisService;
import com.project.deliveryms.services.LivreurService;
import com.project.deliveryms.services.UtilisateurService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class LivreurColisBean implements Serializable {

    private static final Logger LOG = Logger.getLogger(LivreurColisBean.class.getName());

    private int totalColis;
    private int colisEnTransit;
    private List<Colis> troisDerniersColis;
    private long colisTermines;

    private List<Livreur> livreursIndisponibles;
    private List<Colis> colisNonAffectes;

    private Long idLivreurSelectionne;
    private Long idColisAffectation;

    // ✅ Map pour gérer les sélections par livreur
    private Map<Long, Long> colisSelectionParLivreur = new HashMap<>();

    @Inject
    private LivreurService livreurService;

    private Livreur livreur;

    @Inject
    private ColisService colisService;

    @Inject
    private UtilisateurService utilisateurService;

    private List<Colis> colisLivreur;

    private Long idColis;
    private Colis colisDetail;

    private String statusFiltre;
    private Map<Long, String> nouveauxStatus = new HashMap<>();

    @PostConstruct
    public void init() {
        LOG.info("========================================");
        LOG.info("🚀 INITIALISATION LivreurColisBean");
        LOG.info("========================================");

        loadData();

        // Charger les données du livreur connecté
        livreur = utilisateurService.getLivreurConnecte();
        if (livreur != null) {
            totalColis = colisService.countColisByLivreur(livreur.getId());
            colisEnTransit = colisService.countColisByLivreurEtStatus(livreur.getId(), StatusColis.EN_TRANSIT);
            troisDerniersColis = colisService.findDerniersColisByLivreur(livreur.getId(), 3);
            colisTermines = colisService.countColisByLivreurEtStatus(livreur.getId(), StatusColis.LIVRE);

            if (statusFiltre == null || statusFiltre.isEmpty()) {
                colisLivreur = colisService.getColisByLivreur(livreur);
            } else {
                colisLivreur = colisService.getColisByLivreurEtStatus(livreur, StatusColis.valueOf(statusFiltre));
            }

            LOG.info("📦 Colis récupérés pour le livreur : " + colisLivreur.size());
            nouveauxStatus.clear();

            for (Colis c : colisLivreur) {
                LOG.info("   → Colis ID " + c.getId() + " statut = " + c.getStatus().name());
                nouveauxStatus.put(c.getId(), c.getStatus().name());
            }
        }
    }

    /**
     * ✅ MÉTHODE CORRIGÉE: Charge les données pour l'affectation
     */
    public void loadData() {
        try {
            LOG.info("========================================");
            LOG.info("🔄 CHARGEMENT DES DONNÉES");
            LOG.info("========================================");

            // Charger les livreurs indisponibles
            livreursIndisponibles = livreurService.getLivreursIndisponibles();
            LOG.info("   Livreurs indisponibles: " + (livreursIndisponibles != null ? livreursIndisponibles.size() : 0));

            // Charger les colis non affectés
            colisNonAffectes = colisService.getColisNonAffectes();
            LOG.info("   Colis non affectés: " + (colisNonAffectes != null ? colisNonAffectes.size() : 0));

            LOG.info("========================================");
            LOG.info("✅ DONNÉES CHARGÉES");
            LOG.info("========================================");

        } catch (Exception e) {
            LOG.severe("❌ Erreur lors du chargement des données: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * ✅ NOUVELLE MÉTHODE: Affecte avec le Map
     */
    public void affecterColisAvecMap(Long livreurId) {
        try {
            Long colisId = colisSelectionParLivreur.get(livreurId);

            LOG.info("========================================");
            LOG.info("🔄 DÉBUT AFFECTATION (Map)");
            LOG.info("   Livreur ID: " + livreurId);
            LOG.info("   Colis ID: " + colisId);
            LOG.info("========================================");

            if (colisId == null) {
                LOG.warning("⚠️ Aucun colis sélectionné pour le livreur " + livreurId);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Attention", "Veuillez sélectionner un colis"));
                return;
            }

            // Appel du service
            colisService.affecterColis(colisId, livreurId);

            LOG.info("✅ Affectation réussie");

            // Nettoyer la sélection
            colisSelectionParLivreur.remove(livreurId);

            // Recharger
            loadData();

            // Message
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succès", "Colis " + colisId + " affecté au livreur " + livreurId));

            LOG.info("========================================");
            LOG.info("✅ AFFECTATION TERMINÉE");
            LOG.info("========================================");

        } catch (Exception e) {
            LOG.severe("❌ ERREUR: " + e.getMessage());
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", "Impossible d'affecter: " + e.getMessage()));
        }
    }

    /**
     * ✅ MÉTHODE CORRIGÉE: Affecte un colis à un livreur
     */
    public void affecterColisAuLivreur() {
        try {
            LOG.info("========================================");
            LOG.info("🔄 DÉBUT AFFECTATION (Bean)");
            LOG.info("   Livreur sélectionné: " + idLivreurSelectionne);
            LOG.info("   Colis sélectionné: " + idColisAffectation);
            LOG.info("========================================");

            // Validation
            if (idLivreurSelectionne == null || idColisAffectation == null) {
                LOG.warning("⚠️ Sélection invalide - livreur=" + idLivreurSelectionne + ", colis=" + idColisAffectation);
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN,
                                "Attention", "Veuillez sélectionner un livreur et un colis"));
                return;
            }

            LOG.info("✅ Validation OK - Appel du service");
            LOG.info("   Avant affectation - Colis non affectés: " + (colisNonAffectes != null ? colisNonAffectes.size() : "null"));

            // Appel du service
            colisService.affecterColis(idColisAffectation, idLivreurSelectionne);

            LOG.info("✅ Affectation réussie dans le service");

            // ✅ IMPORTANT: Réinitialiser les sélections
            Long oldLivreurId = idLivreurSelectionne;
            Long oldColisId = idColisAffectation;

            idLivreurSelectionne = null;
            idColisAffectation = null;

            LOG.info("   Variables réinitialisées");

            // ✅ IMPORTANT: Recharger les données
            LOG.info("   Rechargement des données...");
            loadData();

            LOG.info("   Après rechargement - Colis non affectés: " + (colisNonAffectes != null ? colisNonAffectes.size() : "null"));

            // Message de succès
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Succès", "Le colis " + oldColisId + " a été affecté au livreur " + oldLivreurId));

            LOG.info("========================================");
            LOG.info("✅ AFFECTATION TERMINÉE (Bean)");
            LOG.info("========================================");

        } catch (Exception e) {
            LOG.severe("========================================");
            LOG.severe("❌ ERREUR AFFECTATION (Bean): " + e.getMessage());
            LOG.severe("========================================");
            e.printStackTrace();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR,
                            "Erreur", "Impossible d'affecter le colis: " + e.getMessage()));
        }
    }

    public void loadColisDetail(ComponentSystemEvent event) {
        if (colisDetail == null && idColis != null) {
            colisDetail = colisService.getColisById(idColis);
        }
    }

    public void filtrerParStatus() {
        init(); // recharge avec filtre
    }

    public void mettreAJourStatus(Long colisId) {
        String nouveauStatus = nouveauxStatus.get(colisId);
        LOG.info("📦 Mise à jour du colis ID " + colisId + " vers statut : " + nouveauStatus);

        if (nouveauStatus != null) {
            try {
                StatusColis statutEnum = StatusColis.valueOf(nouveauStatus.toUpperCase().replace(" ", "_"));
                colisService.mettreAJourStatusColis(colisId, statutEnum);
                init();
            } catch (IllegalArgumentException e) {
                LOG.severe("❌ Erreur : statut inconnu → " + nouveauStatus);
            }
        }
    }

    public String afficherDetails(Long colisId) {
        return "colisDetails.xhtml?faces-redirect=true&colisId=" + colisId;
    }

    public String editerColis(Long idColis) {
        this.colisDetail = colisService.getColisById(idColis);
        return "editColis.xhtml?faces-redirect=true";
    }

    public Map<String, String> getListeStatus() {
        Map<String, String> liste = new LinkedHashMap<>();
        for (StatusColis s : StatusColis.values()) {
            liste.put(s.name(), s.toString().replace("_", " "));
        }
        return liste;
    }

    public String formatDate(LocalDateTime date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }

    public String formatDateLivraison(LocalDateTime date) {
        return formatDate(date);
    }

    public String formatDateEnvoi(LocalDateTime date) {
        return formatDate(date);
    }

    // ============================================
    // GETTERS ET SETTERS
    // ============================================

    public int getTotalColis() {
        return totalColis;
    }

    public int getColisEnTransit() {
        return colisEnTransit;
    }

    public List<Colis> getTroisDerniersColis() {
        return troisDerniersColis;
    }

    public long getColisTermines() {
        return colisTermines;
    }

    public Livreur getLivreurConnecte() {
        return livreur;
    }

    public List<Colis> getColisLivreur() {
        return colisLivreur;
    }

    public List<Livreur> getLivreursIndisponibles() {
        return livreursIndisponibles;
    }

    public List<Colis> getColisNonAffectes() {
        return colisNonAffectes;
    }

    public Long getIdLivreurSelectionne() {
        return idLivreurSelectionne;
    }

    public void setIdLivreurSelectionne(Long idLivreurSelectionne) {
        this.idLivreurSelectionne = idLivreurSelectionne;
    }

    public Long getIdColisAffectation() {
        return idColisAffectation;
    }

    public void setIdColisAffectation(Long idColisAffectation) {
        this.idColisAffectation = idColisAffectation;
    }

    public String getStatusFiltre() {
        return statusFiltre;
    }

    public void setStatusFiltre(String statusFiltre) {
        this.statusFiltre = statusFiltre;
    }

    public Map<Long, String> getNouveauxStatus() {
        return nouveauxStatus;
    }

    public void setNouveauxStatus(Map<Long, String> nouveauxStatus) {
        this.nouveauxStatus = nouveauxStatus;
    }

    public Long getIdColis() {
        return idColis;
    }

    public void setIdColis(Long idColis) {
        this.idColis = idColis;
    }

    public Colis getColisDetail() {
        return colisDetail;
    }

    public void setColisDetail(Colis colisDetail) {
        this.colisDetail = colisDetail;
    }

    public Map<Long, Long> getColisSelectionParLivreur() {
        return colisSelectionParLivreur;
    }

    public void setColisSelectionParLivreur(Map<Long, Long> colisSelectionParLivreur) {
        this.colisSelectionParLivreur = colisSelectionParLivreur;
    }

    /**
     * ✅ Récupère les colis affectés à un livreur
     */
    public List<Colis> getColisParLivreur(Long livreurId) {
        try {
            Livreur livreur = livreurService.getLivreurById(livreurId);
            if (livreur != null) {
                return colisService.getColisByLivreur(livreur);
            }
        } catch (Exception e) {
            LOG.severe("Erreur récupération colis pour livreur " + livreurId + ": " + e.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * ✅ Récupère les numéros de suivi des colis d'un livreur (formaté)
     */
    public String getNumerosSuiviParLivreur(Long livreurId) {
        List<Colis> colisDuLivreur = getColisParLivreur(livreurId);

        if (colisDuLivreur == null || colisDuLivreur.isEmpty()) {
            return "Aucun colis";
        }

        // Limiter à 3 colis + "..." si plus
        if (colisDuLivreur.size() > 3) {
            return colisDuLivreur.stream()
                    .limit(3)
                    .map(Colis::getNumeroSuivi)
                    .collect(Collectors.joining(", ")) + " (+" + (colisDuLivreur.size() - 3) + " autres)";
        }

        return colisDuLivreur.stream()
                .map(Colis::getNumeroSuivi)
                .collect(Collectors.joining(", "));
    }

    /**
     * ✅ Compte le nombre de colis affectés à un livreur
     */
    public int getNombreColisParLivreur(Long livreurId) {
        List<Colis> colis = getColisParLivreur(livreurId);
        return colis != null ? colis.size() : 0;
    }
}