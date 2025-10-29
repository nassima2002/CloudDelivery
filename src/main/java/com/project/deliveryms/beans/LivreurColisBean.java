package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Colis;
import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.StatusColis;
import com.project.deliveryms.services.ColisService;
import com.project.deliveryms.services.LivreurService;
import com.project.deliveryms.services.UtilisateurService;
import jakarta.annotation.PostConstruct;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
@ViewScoped
public class LivreurColisBean implements Serializable {

    private int totalColis;
    private int colisEnTransit;
    private List<Colis> troisDerniersColis;
    private long colisTermines;

    private List<Livreur> livreursIndisponibles;
    private List<Colis> colisNonAffectes;

    private Long idLivreurSelectionne;
    private Long idColisAffectation;

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

    @PostConstruct
    public void init() {
        livreursIndisponibles = livreurService.getLivreursIndisponibles();
        colisNonAffectes = colisService.getColisNonAffectes();

         livreur = utilisateurService.getLivreurConnecte();
        if (livreur != null) {

            // ✅ Ajoute ces 3 lignes ici
            totalColis = colisService.countColisByLivreur(livreur.getId());
            colisEnTransit = colisService.countColisByLivreurEtStatus(livreur.getId(), StatusColis.EN_TRANSIT);
            troisDerniersColis = colisService.findDerniersColisByLivreur(livreur.getId(), 3);
            colisTermines = colisService.countColisByLivreurEtStatus(livreur.getId(), StatusColis.LIVRE);

            if (statusFiltre == null || statusFiltre.isEmpty()) {
                colisLivreur = colisService.getColisByLivreur(livreur);
            } else {
                colisLivreur = colisService.getColisByLivreurEtStatus(livreur, StatusColis.valueOf(statusFiltre));
            }

            System.out.println("Colis récupérés pour le livreur : " + colisLivreur.size());
            nouveauxStatus.clear();

            for (Colis c : colisLivreur) {
                System.out.println("→ Colis ID " + c.getId() + " statut = " + c.getStatus().name());
                nouveauxStatus.put(c.getId(), c.getStatus().name());
            }
        }
    }
    public int getTotalColis() {
        return totalColis;
    }

    public int getColisEnTransit() {
        return colisEnTransit;
    }

    public List<Colis> getTroisDerniersColis() {
        return troisDerniersColis;
    }


    public Livreur getLivreurConnecte() {
        return livreur;
    }
    public void loadColisDetail(ComponentSystemEvent event) {
        if (colisDetail == null && idColis != null) {
            colisDetail = colisService.getColisById(idColis);
        }
    }


    public void filtrerParStatus() {
        init(); // recharge avec filtre
    }

    private Map<Long, String> nouveauxStatus = new HashMap<>();


    public void setNouveauxStatus(Map<Long, String> nouveauxStatus) {
        this.nouveauxStatus = nouveauxStatus;
    }

    public List<Colis> getColisLivreur() {
        return colisLivreur;
    }

    public void affecterColisAuLivreur() {
        try {
            System.out.println(">>> Méthode affecterColisAuLivreur() appelée");

            System.out.println("ID du livreur sélectionné : " + idLivreurSelectionne);
            System.out.println("ID du colis à affecter : " + idColisAffectation);

            if (idLivreurSelectionne != null && idColisAffectation != null) {
                colisService.affecterColis(idColisAffectation, idLivreurSelectionne);

                System.out.println("✅ Colis affecté avec succès !");

                // Recharger les listes après affectation
                livreursIndisponibles = livreurService.getLivreursIndisponibles();
                colisNonAffectes = colisService.getColisNonAffectes();
            } else {
                System.out.println("⚠️ Les identifiants sont nuls. Affectation annulée.");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors de l'affectation du colis au livreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<Long, String> getNouveauxStatus() {
        return nouveauxStatus;
    }


    public String formatDateLivraison(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }

    public String formatDateEnvoi(LocalDateTime date) {
        if (date == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }

    // Getters et Setters
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

    private String statusFiltre;

    public String getStatusFiltre() {
        return statusFiltre;
    }

    public void setStatusFiltre(String statusFiltre) {
        this.statusFiltre = statusFiltre;
    }

    //    private Colis colisDetail;
//
//    public Colis getColisDetail() {
//        return colisDetail;
//    }
//
//    public void setColisDetail(Colis colisDetail) {
//        this.colisDetail = colisDetail;
//    }
//    public String afficherDetails(Long colisId) {
//        Colis colis = colisService.getColisById(colisId);
//        if (colis != null) {
//            this.colisDetail = colis;
//            return "colisDetails.xhtml?faces-redirect=true";
//        }
//        return null;
//    }
    public String editerColis(Long idColis) {
        this.colisDetail = colisService.getColisById(idColis);
        return "editColis.xhtml?faces-redirect=true"; // Exemple de redirection vers page édition
    }

    public Map<String, String> getListeStatus() {
        Map<String, String> liste = new LinkedHashMap<>();
        for (StatusColis s : StatusColis.values()) {
            // Utilise le nom exact de l'enum comme clé (à passer à valueOf())
            liste.put(s.name(), s.toString().replace("_", " "));
        }
        return liste;
    }

    // ➕ AJOUTE LA MÉTHODE ICI :
    public void mettreAJourStatus(Long colisId) {
        String nouveauStatus = nouveauxStatus.get(colisId);
        System.out.println("📦 Mise à jour du colis ID " + colisId + " vers statut : " + nouveauStatus);

        if (nouveauStatus != null) {
            try {
                // Convertit en majuscule + remplace espace par underscore
                StatusColis statutEnum = StatusColis.valueOf(nouveauStatus.toUpperCase().replace(" ", "_"));
                colisService.mettreAJourStatusColis(colisId, statutEnum);
                init(); // Rafraîchir la liste après mise à jour
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Erreur : statut inconnu → " + nouveauStatus);
            }
        }
    }

    public String afficherDetails(Long colisId) {
        return "colisDetails.xhtml?faces-redirect=true&colisId=" + colisId;
    }


    // La méthode loadColisDetail, avec import jakarta.faces.event.ComponentSystemEvent
//    public void loadColisDetail(ComponentSystemEvent event) {
//        if (colisDetail == null) { // éviter de recharger plusieurs fois
//            Map<String, String> params = FacesContext.getCurrentInstance()
//                    .getExternalContext()
//                    .getRequestParameterMap();
//
//            String idColisStr = params.get("colisId");
//            if (idColisStr != null) {
//                try {
//                    Long idColis = Long.parseLong(idColisStr);
//                    this.colisDetail = colisService.getColisById(idColis);
//                } catch (NumberFormatException e) {
//                    System.out.println("Paramètre colisId invalide: " + idColisStr);
//                }
//            }
//        }
//
//
//    }}
// Getters
    public String formatDate(LocalDateTime date) {
        if (date == null) return "";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return date.format(formatter);
    }


    public long getColisTermines() {
        return colisTermines;
    }
}