package com.project.deliveryms.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "livreur")
public class Livreur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private Double latitude;
    private Double longitude;
    private String disponibiliter;
    private String password;


    @OneToOne
    @JoinColumn(name = "user_id") // clé étrangère dans livreur vers user
    private Utilisateur user;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getDisponibiliter() { return disponibiliter; }
    public void setDisponibiliter(String disponibiliter) { this.disponibiliter = disponibiliter; }

    public Utilisateur getUser() { return user; }
    public void setUser(Utilisateur user) { this.user = user; }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) { this.password = password; }


}
