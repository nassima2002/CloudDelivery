package com.project.deliveryms.services;

import com.project.deliveryms.enums.StatusColis;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDate;
import java.time.LocalDateTime;

@ApplicationScoped
public class StatistiqueService {

    @PersistenceContext
    private EntityManager em;

    public int getNbColisLivresJour() {
        LocalDate today = LocalDate.now();
        return ((Long) em.createQuery(
                        "SELECT COUNT(c) FROM Colis c WHERE c.status = :statut AND DATE(c.dateLivraison) = :today")
                .setParameter("statut", StatusColis.LIVRE)
                .setParameter("today", today)
                .getSingleResult()).intValue();
    }

    public int getNbColisLivresSemaine() {
        LocalDateTime startOfWeek = LocalDate.now().with(java.time.DayOfWeek.MONDAY).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return ((Long) em.createQuery(
                        "SELECT COUNT(c) FROM Colis c WHERE c.status = :statut AND c.dateLivraison BETWEEN :start AND :end")
                .setParameter("statut", StatusColis.LIVRE)
                .setParameter("start", startOfWeek)
                .setParameter("end", now)
                .getSingleResult()).intValue();
    }

    public int getNbColisEnTransit() {
        return ((Long) em.createQuery(
                        "SELECT COUNT(c) FROM Colis c WHERE c.status = :statut")
                .setParameter("statut", StatusColis.EN_TRANSIT)
                .getSingleResult()).intValue();
    }
}