package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Colis;
import com.project.deliveryms.entities.Adresse;
import com.project.deliveryms.enums.StatusColis;
import com.project.deliveryms.services.ColisService;
import com.project.deliveryms.services.AdresseService;
import jakarta.annotation.PostConstruct;
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
public class ColisEnTransitBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Colis colisAModifier;
    private Colis colisASuivre;

    // Propriétés du formulaire
    private String rue;
    private String ville;
    private String codePostal;
    private String pays;
    private String description;
    private double poids;
    private List<Colis> listeColis;
    private List<Colis> listeColisFiltree;
    private String numeroSuivi;
    private LocalDateTime dateEnvoi;

    // Variable pour la recherche
    private String searchQuery;

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
        colisAModifier = new Colis();
        colisASuivre = new Colis();
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
        numeroSuivi = "";
        dateEnvoi = null;
    }

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(formatter) : "";
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
            this.numeroSuivi = colis.getNumeroSuivi();
            this.dateEnvoi = colis.getDateEnvoi();

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
     * Méthode pour charger les informations de suivi d'un colis
     * @param colis Le colis à suivre
     */
    public void chargerSuivi(Colis colis) {
        if (colis != null) {
            this.colisASuivre = colis;
            this.colisId = colis.getId();
        }
    }

    /**
     * Méthode pour marquer un colis comme livré
     */
    public String marquerCommeLivre() {
        try {
            // Récupérer le colis
            Colis colis = colisService.getColisByNumeroSuivi(numeroSuivi);
            if (colis == null) {
                throw new EntityNotFoundException("Colis non trouvé");
            }
            
            // Mettre à jour le statut et la date de livraison
            colis.setStatus(StatusColis.LIVRE);
            colis.setDateLivraison(LocalDateTime.now());
            
            // Mettre à jour le colis
            colisService.updateColis(
                    colisId,
                    colis.getDescription(),
                    colis.getPoids(),
                    StatusColis.LIVRE,
                    colis.getAdresseDestinataire().getRue(),
                    colis.getAdresseDestinataire().getVille(),
                    colis.getAdresseDestinataire().getCodePostal(),
                    colis.getAdresseDestinataire().getPays()
            );

            // Recharger la liste après modification
            chargerListeColis();
            listeColisFiltree = new ArrayList<>(listeColis);
            calculateTotalItems();

            // Afficher un message de succès
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Succès", "Le colis a été marqué comme livré"));

            return null;
        } catch (EntityNotFoundException e) {
            // Gérer l'exception si le colis n'a pas été trouvé
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Le colis n'a pas été trouvé"));

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
            // Si la recherche est vide, afficher tous les colis en transit
            listeColisFiltree = new ArrayList<>(listeColis);
        }

        calculateTotalItems();
        currentPage = 1; // Revenir à la première page après une recherche
    }

    public void chargerListeColis() {
        List<Colis> allColis = colisService.getAllColisWithDetails();
        // Filtrer pour ne garder que les colis en transit
        this.listeColis = allColis.stream()
                .filter(c -> c.getStatus() == StatusColis.EN_TRANSIT)
                .collect(Collectors.toList());
    }

    private void calculateTotalItems() {
        this.totalItems = listeColisFiltree != null ? listeColisFiltree.size() : 0;
    }

    private Long colisId;

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
    public Colis getColisAModifier() {
        return colisAModifier;
    }

    public void setColisAModifier(Colis colisAModifier) {
        this.colisAModifier = colisAModifier;
    }

    public Colis getColisASuivre() {
        return colisASuivre;
    }

    public void setColisASuivre(Colis colisASuivre) {
        this.colisASuivre = colisASuivre;
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

    public String getNumeroSuivi() {
        return numeroSuivi;
    }

    public void setNumeroSuivi(String numeroSuivi) {
        this.numeroSuivi = numeroSuivi;
    }

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