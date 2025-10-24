package com.project.deliveryms.entities;

import com.project.deliveryms.enums.StatusColis;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Colis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numeroSuivi;

    private String description;
    private double poids;

    private LocalDateTime dateEnvoi;
    private LocalDateTime dateLivraison;

    @ManyToOne
    private Adresse adresseDestinataire;

    @Enumerated(EnumType.STRING)
    private StatusColis status;

    @ManyToOne
    private Livreur livreur;

    @ManyToOne
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    private boolean deleted ;

    // Ajout de la relation OneToOne avec BordereauExpedition
    @OneToOne(mappedBy = "colis", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private BordereauExpedition bordereauExpedition;

    public BordereauExpedition getBordereauExpedition() {
        return bordereauExpedition;
    }

    public void setBordereauExpedition(BordereauExpedition bordereauExpedition) {
        this.bordereauExpedition = bordereauExpedition;
    }

    // Getters et setters pour les autres attributs

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Utilisateur getUtilisateur() {
        return utilisateur;
    }
    public void setUtilisateur(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
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
    public LocalDateTime getDateLivraison() {
        return dateLivraison;
    }
    public void setDateLivraison(LocalDateTime dateLivraison) {
        this.dateLivraison = dateLivraison;
    }
    public Adresse getAdresseDestinataire() {
        return adresseDestinataire;
    }
    public void setAdresseDestinataire(Adresse adresse) {
        this.adresseDestinataire = adresse;
    }
    public Livreur getLivreur() {
        return livreur;
    }
    public void setLivreur(Livreur livreur) {
        this.livreur = livreur;
    }

    public StatusColis getStatus() {
        return status;
    }

    public void setStatus(StatusColis status) {
        this.status = status;
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
}