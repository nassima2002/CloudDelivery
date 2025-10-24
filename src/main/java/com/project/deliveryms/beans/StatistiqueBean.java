package com.project.deliveryms.beans;

import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable; // <-- Manquait !

@Named
@SessionScoped
public class StatistiqueBean implements Serializable { // <-- Manquait aussi Serializable ici

    private static final long serialVersionUID = 1L; // (Bonne pratique pour Serializable)

    private int nbColisLIVRESJour = 25;
    private int nbColisLIVRESSemaine = 130;
    private int nbColisEnTransit = 42;

    // Ces valeurs peuvent venir d’un service ou d’une base de données

    public int getNbColisLIVRESJour() {
        return nbColisLIVRESJour;
    }

    public int getNbColisLIVRESSemaine() {
        return nbColisLIVRESSemaine;
    }

    public int getNbColisEnTransit() {
        return nbColisEnTransit;
    }
}