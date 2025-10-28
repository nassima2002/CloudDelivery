package com.project.deliveryms.entities;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "livreurs")
public class Livreur implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "disponibiliter")
    private String disponibiliter;

    // 🔹 Propriété transient : non persistée en base de données
    // Utilisée uniquement pour transférer le mot de passe lors de la création
    @Transient
    private String password;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private Utilisateur user;

    // Constructeurs
    public Livreur() {
    }

    public Livreur(Utilisateur user, Double latitude, Double longitude, String disponibiliter) {
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
        this.disponibiliter = disponibiliter;
    }

    // Getters et Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getDisponibiliter() {
        return disponibiliter;
    }

    public void setDisponibiliter(String disponibiliter) {
        this.disponibiliter = disponibiliter;
    }

    public Utilisateur getUser() {
        return user;
    }

    public void setUser(Utilisateur user) {
        this.user = user;
    }

    /**
     * Getter pour le mot de passe (transient - non persisté)
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter pour le mot de passe (transient - non persisté)
     * ✅ CORRECTION : était "this.Password = Password" au lieu de "this.password = password"
     */
    public void setPassword(String password) {
        this.password = password;  // ✅ Correction ici !
    }

    @Override
    public String toString() {
        return "Livreur{" +
                "id=" + id +
                ", user=" + (user != null ? user.getEmail() : "null") +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", disponibiliter='" + disponibiliter + '\'' +
                '}';
    }
}