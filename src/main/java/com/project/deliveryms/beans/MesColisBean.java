package com.project.deliveryms.beans;

import com.project.deliveryms.entities.BordereauExpedition;
import com.project.deliveryms.repositories.ColisRepository;
import com.project.deliveryms.entities.Colis;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.StatusColis;
import com.project.deliveryms.services.BordereauService;
import com.project.deliveryms.services.ColisService;
import com.itextpdf.text.DocumentException;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class MesColisBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<Colis> userColis = new ArrayList<>();
    private List<Colis> filteredColis = new ArrayList<>();

    // Filtres
    private String filtreTypeStatus = "";
    private LocalDateTime filtreDateDebut;
    private LocalDateTime filtreDateFin;

    // Pagination
    private int currentPage = 1;
    private int pageSize = 10;
    private int totalItems;

    @Inject
    private ColisService colisService;

    @Inject
    private BordereauService bordereauService;

    @Inject
    private LoginBean loginBean;
    @Inject
    private ColisRepository colisRepository;

    @PostConstruct
    public void init() {
        loadUserColis();
        filteredColis = new ArrayList<>(userColis);
        calculateTotalItems();
    }

    private void loadUserColis() {
        Utilisateur utilisateur = loginBean.getUtilisateur();
        if (utilisateur != null) {
            List<Colis> allColis = colisService.getAllColisWithDetails();
            userColis = allColis.stream()
                    .filter(c -> c.getUtilisateur() != null &&
                            c.getUtilisateur().getId().equals(utilisateur.getId()) &&
                            !c.getDeleted())
                    .collect(Collectors.toList());
        }
    }

    public String filtrer() {
        filteredColis = userColis;

        if (filtreTypeStatus != null && !filtreTypeStatus.isEmpty()) {
            filteredColis = filteredColis.stream()
                    .filter(c -> c.getStatus() != null && c.getStatus().toString().equals(filtreTypeStatus))
                    .collect(Collectors.toList());
        }

        if (filtreDateDebut != null) {
            filteredColis = filteredColis.stream()
                    .filter(c -> c.getDateEnvoi() != null && c.getDateEnvoi().isAfter(filtreDateDebut))
                    .collect(Collectors.toList());
        }

        if (filtreDateFin != null) {
            filteredColis = filteredColis.stream()
                    .filter(c -> c.getDateEnvoi() != null && c.getDateEnvoi().isBefore(filtreDateFin))
                    .collect(Collectors.toList());
        }

        calculateTotalItems();
        currentPage = 1;

        return null;
    }

    public String reinitialiserFiltres() {
        filtreTypeStatus = "";
        filtreDateDebut = null;
        filtreDateFin = null;
        filteredColis = new ArrayList<>(userColis);
        calculateTotalItems();
        currentPage = 1;

        return null;
    }

    private void calculateTotalItems() {
        this.totalItems = filteredColis != null ? filteredColis.size() : 0;
    }

    public List<Colis> getCurrentPageItems() {
        if (filteredColis == null || filteredColis.isEmpty()) {
            return new ArrayList<>();
        }

        int fromIndex = (currentPage - 1) * pageSize;
        if (fromIndex >= filteredColis.size()) {
            currentPage = 1;
            fromIndex = 0;
        }

        int toIndex = Math.min(fromIndex + pageSize, filteredColis.size());

        return filteredColis.subList(fromIndex, toIndex);
    }

    public String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return dateTime.format(formatter);
    }

    public String getStatusClass(StatusColis status) {
        if (status == null) {
            return "";
        }

        switch (status) {
            case EN_TRANSIT:
                return "bg-yellow-100 text-yellow-800";
            case LIVRE:
                return "bg-green-100 text-green-800";
            case EN_ATTENTE:
                return "bg-blue-100 text-blue-800";
            case ANNULE:
                return "bg-red-100 text-red-800";
            case RETOURNE:
                return "bg-gray-100 text-gray-800";
            default:
                return "";
        }
    }

    public int getTotalPages() {
        if (totalItems == 0) {
            return 1;
        }
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    public int getFirstItemIndex() {
        if (totalItems == 0) {
            return 0;
        }
        return (currentPage - 1) * pageSize + 1;
    }

    public int getLastItemIndex() {
        if (totalItems == 0) {
            return 0;
        }
        return Math.min(getFirstItemIndex() + pageSize - 1, totalItems);
    }

    // **Méthode ajoutée** : Sauvegarder colis (update ou persist)
    public void saveColis(Colis colis) {
        colisRepository.update(colis);
    }
    // --- Méthode pour générer le PDF ---

    public void genererBordereauPDF(Long colisId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse response = (HttpServletResponse) facesContext.getExternalContext().getResponse();

        try {
            Colis colis = colisService.getColisById(colisId);
            if (colis == null) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Colis non trouvé"));
                return;
            }

            BordereauExpedition bordereau = colis.getBordereauExpedition();

            if (bordereau == null) {
                bordereau = new BordereauExpedition();
                bordereau.setColis(colis);
                bordereau.setDateGeneration(LocalDateTime.now());

                colis.setBordereauExpedition(bordereau);

                colisService.saveColis(colis);
            }

            response.reset();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=\"bordereau_" + colis.getNumeroSuivi() + ".pdf\"");

            bordereauService.generateBordereauPdf(bordereau, response);

            facesContext.responseComplete();

        } catch (DocumentException | IOException e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur", "Erreur lors de la génération du PDF"));
            e.printStackTrace();
        }
    }

    // --- Getters & setters ---

    public List<Colis> getUserColis() {
        return userColis;
    }

    public String getFiltreTypeStatus() {
        return filtreTypeStatus;
    }

    public void setFiltreTypeStatus(String filtreTypeStatus) {
        this.filtreTypeStatus = filtreTypeStatus;
    }

    public LocalDateTime getFiltreDateDebut() {
        return filtreDateDebut;
    }

    public void setFiltreDateDebut(LocalDateTime filtreDateDebut) {
        this.filtreDateDebut = filtreDateDebut;
    }

    public LocalDateTime getFiltreDateFin() {
        return filtreDateFin;
    }

    public void setFiltreDateFin(LocalDateTime filtreDateFin) {
        this.filtreDateFin = filtreDateFin;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getTotalItems() {
        return totalItems;
    }
}