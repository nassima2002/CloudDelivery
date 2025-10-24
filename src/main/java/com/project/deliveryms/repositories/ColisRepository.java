package com.project.deliveryms.repositories;

import com.project.deliveryms.entities.Colis;
import com.project.deliveryms.enums.StatusColis;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Optional;

@Stateless
public class ColisRepository  {
    @PersistenceContext
    private EntityManager entityManager;

    @PersistenceContext
    private EntityManager em;
    public Colis find(Long id) {
        return entityManager.find(Colis.class, id);
    }




    public List<Colis> findColisNonAffectes() {
        TypedQuery<Colis> query = entityManager.createQuery(
                "SELECT c FROM Colis c WHERE (c.livreur IS NULL OR c.status IS NULL OR c.status = :status)",
                Colis.class
        );
        query.setParameter("status", StatusColis.EN_ATTENTE); // Pour récupérer les colis avec statut "EN_ATTENTE"

        return query.getResultList();
    }


    public void save(Colis colis) {
        em.persist(colis);
    }

    public Colis findById(Long colisId) {
        try {
            return em.createQuery("SELECT c FROM Colis c WHERE c.id = :id", Colis.class)
                    .setParameter("id", colisId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Colis findByNumeroSuivi(String numero) {
        return em.createQuery(
                        "SELECT c FROM Colis c WHERE c.numeroSuivi = :numero", Colis.class)
                .setParameter("numero", numero)
                .getSingleResult();
    }

    public List<Colis> findAllWithDetails() {
        return em.createQuery(
                "SELECT c FROM Colis c LEFT JOIN FETCH c.adresseDestinataire LEFT JOIN FETCH c.utilisateur WHERE c.deleted = false",
                Colis.class
        ).getResultList();
    }

    public Optional<Colis> findByIdcolis(Long id) {
        Colis colis = em.find(Colis.class, id);
        return Optional.ofNullable(colis);
    }



    public void update(Colis colis) {
        em.merge(colis);
    }
}
