package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Colis;
import com.project.deliveryms.entities.Adresse;
import com.project.deliveryms.enums.StatusColis;
import com.project.deliveryms.services.ColisService;
import com.project.deliveryms.services.AdresseService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.persistence.EntityNotFoundException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class ColisBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Colis nouveauColis;
    private Colis colisAModifier;

    // Propriétés du formulaire
    private String rue;
    private String ville;
    private String codePostal;
    private String pays;
    private String description;
    private double poids;
    private StatusColis status;
    private List<Colis> listeColis;
    private List<Colis> listeColisFiltree;
    private String numeroSuivi;
    private LocalDateTime dateEnvoi; // Ajout de la propriété dateEnvoi

    // Variable pour la recherche
    private String searchQuery;

    // Variable pour le filtrage par statut
    private String filtreStatut = "TOUS";

    // Variables pour la pagination
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalItems;

    @Inject
    private ColisService colisService;

    @Inject
    private AdresseService adresseService;

    @PostConstruct
    public void init() {
        nouveauColis = new Colis();
        colisAModifier = new Colis();
        chargerListeColis();
        listeColisFiltree = new ArrayList<>(listeColis);
        calculateTotalItems();
        resetFields();
    }

    public void resetFields() {
        description = "";
        poids = 0.0;
        rue = "";
        ville = "";
        codePostal = "";
        pays = "";
        status = StatusColis.EN_ATTENTE;
        numeroSuivi = "";
        dateEnvoi = null; // Réinitialiser la date d'envoi
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(formatter) : "";
    }

    // Méthode pour créer un colis
    public String ajouterColis() {
        // Créer une adresse à partir des informations du formulaire
        Adresse adresse = adresseService.createAdresse(rue, ville, codePostal, pays);

        // Utiliser le service pour créer le colis
        Colis colis = colisService.createColis(description, poids, adresse);

        // Recharger la liste après ajout
        chargerListeColis();
        listeColisFiltree = new ArrayList<>(listeColis);
        calculateTotalItems();
        resetFields();

        // Afficher un message de succès
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Le colis a été ajouté avec succès"));

        return null;
    }

    /**
     * Méthode pour charger les informations d'un colis pour la vue détaillée
     * @param colis Le colis à afficher
     */
    public void chargerColisAModifier(Colis colis) {
        if (colis != null) {
            this.colisId = colis.getId();
            this.description = colis.getDescription();
            this.poids = colis.getPoids();
            this.status = colis.getStatus();
            this.numeroSuivi = colis.getNumeroSuivi();
            this.dateEnvoi = colis.getDateEnvoi(); // Charger la date d'envoi

            // Chargement des informations d'adresse
            if (colis.getAdresseDestinataire() != null) {
                Adresse adresse = colis.getAdresseDestinataire();
                this.rue = adresse.getRue();
                this.ville = adresse.getVille();
                this.codePostal = adresse.getCodePostal();
                this.pays = adresse.getPays();
            }
        }
    }

    /**
     * Méthode pour charger les informations d'un colis pour le formulaire de modification
     * @param colis Le colis à modifier
     */
    public void chargerColisAModifierA(Colis colis) {
        chargerColisAModifier(colis);
    }

    /**
     * Méthode pour modifier un colis
     */
    public String modifierColis() {
        try {
            // Appeler le service pour mettre à jour le colis
            colisService.updateColis(
                    colisId,
                    description,
                    poids,
                    status,
                    rue,
                    ville,
                    codePostal,
                    pays
            );

            // Recharger la liste après modification
            chargerListeColis();
            appliquerFiltres();
            calculateTotalItems();
            resetFields();

            // Afficher un message de succès
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Le colis a été modifié avec succès"));

            return null;
        } catch (EntityNotFoundException e) {
            // Gérer l'exception si le colis n'a pas été trouvé
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le colis n'a pas été trouvé"));

            return null;
        }
    }

    /**
     * Méthode pour générer un bordereau d'expédition
     * @param colisId L'ID du colis pour lequel générer le bordereau
     */
    public String genererBordereau(Long colisId) {
        try {
            // Logique pour générer le bordereau d'expédition (PDF)
            // Cette méthode devrait appeler un service qui génère le PDF

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Le bordereau a été généré avec succès"));

            return null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Impossible de générer le bordereau"));

            return null;
        }
    }

    /**
     * Méthode pour rechercher des colis
     */
    public void rechercher() {
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            // Filtrer la liste des colis en mémoire
            String query = searchQuery.toLowerCase().trim();
            listeColisFiltree = listeColis.stream()
                    .filter(c -> (c.getNumeroSuivi() != null && c.getNumeroSuivi().toLowerCase().contains(query)) ||
                            (c.getDescription() != null && c.getDescription().toLowerCase().contains(query)) ||
                            (c.getAdresseDestinataire() != null &&
                                    ((c.getAdresseDestinataire().getVille() != null && c.getAdresseDestinataire().getVille().toLowerCase().contains(query)) ||
                                            (c.getAdresseDestinataire().getPays() != null && c.getAdresseDestinataire().getPays().toLowerCase().contains(query)))))
                    .collect(Collectors.toList());
        } else {
            // Si la recherche est vide, appliquer seulement le filtre de statut
            filtrerParStatut(filtreStatut);
            return;
        }

        // Appliquer également le filtre de statut si nécessaire
        if (!"TOUS".equals(filtreStatut)) {
            try {
                StatusColis statusEnum = StatusColis.valueOf(filtreStatut);
                listeColisFiltree = listeColisFiltree.stream()
                        .filter(c -> c.getStatus() == statusEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Ignorer si le statut n'est pas valide
            }
        }

        calculateTotalItems();
        currentPage = 1; // Revenir à la première page après une recherche
    }

    /**
     * Méthode pour filtrer les colis par statut
     * @param statut Le statut à filtrer
     */
    public void filtrerParStatut(String statut) {
        this.filtreStatut = statut;

        if ("TOUS".equals(statut)) {
            listeColisFiltree = new ArrayList<>(listeColis);
        } else {
            try {
                StatusColis statusEnum = StatusColis.valueOf(statut);
                listeColisFiltree = listeColis.stream()
                        .filter(c -> c.getStatus() == statusEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Si le statut n'est pas valide, afficher tous les colis
                listeColisFiltree = new ArrayList<>(listeColis);
            }
        }

        // Appliquer également la recherche si nécessaire
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            rechercher();
            return;
        }

        calculateTotalItems();
        currentPage = 1; // Revenir à la première page après un filtrage
    }

    /**
     * Méthode pour appliquer à la fois la recherche et le filtre de statut
     */
    private void appliquerFiltres() {
        // D'abord appliquer le filtre de statut
        filtrerParStatut(filtreStatut);

        // Ensuite appliquer la recherche si nécessaire
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            rechercher();
        }
    }

    /**
     * Méthode pour obtenir le nombre de colis par statut
     * @param statut Le statut pour lequel compter les colis
     * @return Le nombre de colis ayant ce statut
     */
    public int getColisCountByStatus(String statut) {
        try {
            StatusColis statusEnum = StatusColis.valueOf(statut);
            return (int) listeColis.stream()
                    .filter(c -> c.getStatus() == statusEnum)
                    .count();
        } catch (IllegalArgumentException e) {
            return 0;
        }
    }

    /**
     * Méthode pour obtenir le nombre total de colis
     * @return Le nombre total de colis
     */
    public int getTotalColis() {
        return listeColis.size();
    }

    /**
     * Méthode pour vérifier si un colis a un statut spécifique
     * @param statut Le statut à vérifier
     * @return true si le colis a ce statut, false sinon
     */
    public boolean hasStatus(String statut) {
        if (status == null) {
            return false;
        }

        try {
            StatusColis statusEnum = StatusColis.valueOf(statut);
            return status.equals(statusEnum) || status.ordinal() > statusEnum.ordinal();
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Méthode pour obtenir la date d'un statut spécifique
     * @param statut Le statut dont on veut la date
     * @return La date formatée ou une chaîne vide
     */
    public String getStatusDate(String statut) {
        if (hasStatus(statut)) {
            return formatDate(LocalDateTime.now());
        }
        return "";
    }

    public void chargerListeColis() {
        this.listeColis = colisService.getAllColisWithDetails();
    }

    private void calculateTotalItems() {
        this.totalItems = listeColisFiltree != null ? listeColisFiltree.size() : 0;
    }

    private Long colisId;

    public void supprimerColis() {
        try {
            colisService.deleteColis(colisId);
            chargerListeColis();
            appliquerFiltres();
            calculateTotalItems();

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Colis supprimé avec succès"));
        } catch (EntityNotFoundException e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le colis n'a pas été trouvé"));
        }
    }

    // Méthodes de pagination
    public void firstPage() {
        currentPage = 1;
    }

    public void lastPage() {
        currentPage = getTotalPages();
    }

    public void nextPage() {
        if (currentPage < getTotalPages()) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 1) {
            currentPage--;
        }
    }

    public void goToPage(int page) {
        if (page >= 1 && page <= getTotalPages()) {
            currentPage = page;
        }
    }

    public int getTotalPages() {
        if (totalItems == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    public List<Integer> getPageNumbers() {
        List<Integer> pages = new ArrayList<>();
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(getTotalPages(), currentPage + 2);

        for (int i = startPage; i <= endPage; i++) {
            pages.add(i);
        }

        return pages;
    }

    public List<Colis> getCurrentPageItems() {
        return getListeColis();
    }

    public int getItemsPerPage() {
        return pageSize;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.pageSize = itemsPerPage;
        this.currentPage = 1;
    }

    public List<Colis> getListeColis() {
        if (listeColisFiltree == null || listeColisFiltree.isEmpty()) {
            return new ArrayList<>();
        }

        int fromIndex = (currentPage - 1) * pageSize;
        if (fromIndex >= listeColisFiltree.size()) {
            currentPage = 1;
            fromIndex = 0;
        }

        int toIndex = Math.min(fromIndex + pageSize, listeColisFiltree.size());

        return listeColisFiltree.subList(fromIndex, toIndex);
    }

    public int getFirstItemIndex() {
        if (totalItems == 0) {
            return 0;
        }
        return (currentPage - 1) * pageSize;
    }

    public int getLastItemIndex() {
        if (totalItems == 0) {
            return 0;
        }
        return Math.min(getFirstItemIndex() + pageSize, totalItems);
    }

    public int getTotalItems() {
        return totalItems;
    }

    // Getters et setters
    public void setListeColis(List<Colis> listeColis) {
        this.listeColis = listeColis;
        this.listeColisFiltree = new ArrayList<>(listeColis);
        calculateTotalItems();
    }

    public Colis getNouveauColis() {
        return nouveauColis;
    }

    public void setNouveauColis(Colis nouveauColis) {
        this.nouveauColis = nouveauColis;
    }

    public Colis getColisAModifier() {
        return colisAModifier;
    }

    public void setColisAModifier(Colis colisAModifier) {
        this.colisAModifier = colisAModifier;
    }

    public String getRue() {
        return rue;
    }

    public void setRue(String rue) {
        this.rue = rue;
    }

    public String getVille() {
        return ville;
    }

    public void setVille(String ville) {
        this.ville = ville;
    }

    public String getCodePostal() {
        return codePostal;
    }

    public void setCodePostal(String codePostal) {
        this.codePostal = codePostal;
    }

    public String getPays() {
        return pays;
    }

    public void setPays(String pays) {
        this.pays = pays;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPoids() {
        return poids;
    }

    public void setPoids(double poids) {
        this.poids = poids;
    }

    public StatusColis getStatus() {
        return status;
    }

    public void setStatus(StatusColis status) {
        this.status = status;
    }

    public String getNumeroSuivi() {
        return numeroSuivi;
    }

    public void setNumeroSuivi(String numeroSuivi) {
        this.numeroSuivi = numeroSuivi;
    }

    // Getter et setter pour dateEnvoi
    public LocalDateTime getDateEnvoi() {
        return dateEnvoi;
    }

    public void setDateEnvoi(LocalDateTime dateEnvoi) {
        this.dateEnvoi = dateEnvoi;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getFiltreStatut() {
        return filtreStatut;
    }

    public void setFiltreStatut(String filtreStatut) {
        this.filtreStatut = filtreStatut;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        this.currentPage = 1;
    }

    public Long getColisId() {
        return colisId;
    }

    public void setColisId(Long colisId) {
        this.colisId = colisId;
    }
}