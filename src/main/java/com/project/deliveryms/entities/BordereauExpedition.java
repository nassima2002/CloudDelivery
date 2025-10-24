package com.project.deliveryms.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class BordereauExpedition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private Colis colis;

    private LocalDateTime dateGeneration;  // ✅ Add this field

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Colis getColis() {
        return colis;
    }

    public void setColis(Colis colis) {
        this.colis = colis;
    }

    public LocalDateTime getDateGeneration() {
        return dateGeneration;
    }

    public void setDateGeneration(LocalDateTime dateGeneration) {
        this.dateGeneration = dateGeneration;
    }
}