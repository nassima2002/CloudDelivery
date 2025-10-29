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
import java.util.logging.Logger;

@Stateless
public class ColisRepository {

    private static final Logger logger = Logger.getLogger(ColisRepository.class.getName());

    @PersistenceContext
    private EntityManager em;

    public Colis find(Long id) {
        return em.find(Colis.class, id);
    }

    public List<Colis> findColisNonAffectes() {
        logger.info("🔍 Recherche des colis non affectés");
        try {
            TypedQuery<Colis> query = em.createQuery(
                    "SELECT c FROM Colis c WHERE (c.livreur IS NULL OR c.status = :status)",
                    Colis.class
            );
            query.setParameter("status", StatusColis.EN_ATTENTE);

            List<Colis> colis = query.getResultList();
            logger.info("✅ findColisNonAffectes: " + colis.size() + " colis trouvés");

            colis.forEach(c ->
                    logger.info("   - ID=" + c.getId() + " | Description=" + c.getDescription() +
                            " | Statut=" + c.getStatus() + " | Livreur=" + c.getLivreur())
            );

            return colis;
        } catch (Exception e) {
            logger.severe("❌ Erreur findColisNonAffectes: " + e.getMessage());
            throw e;
        }
    }

    public Optional<Colis> findById(Long colisId) {
        logger.info("🔍 Recherche colis ID=" + colisId);
        try {
            // Utilisation de find() qui est plus simple et charge automatiquement les relations EAGER
            Colis colis = em.find(Colis.class, colisId);

            if (colis != null) {
                logger.info("✅ Colis trouvé: ID=" + colis.getId() + " | Description=" + colis.getDescription());

                // Force le chargement des relations lazy si nécessaire
                if (colis.getLivreur() != null) {
                    colis.getLivreur().getId(); // Force l'initialisation
                }
                if (colis.getAdresseDestinataire() != null) {
                    colis.getAdresseDestinataire().getId();
                }
            } else {
                logger.warning("⚠️ Colis introuvable: ID=" + colisId);
            }

            return Optional.ofNullable(colis);
        } catch (Exception e) {
            logger.severe("❌ Erreur findById: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public Colis findByNumeroSuivi(String numero) {
        try {
            return em.createQuery(
                            "SELECT c FROM Colis c WHERE c.numeroSuivi = :numero", Colis.class)
                    .setParameter("numero", numero)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<Colis> findAllWithDetails() {
        return em.createQuery(
                "SELECT DISTINCT c FROM Colis c " +
                        "LEFT JOIN FETCH c.adresseDestinataire " +
                        "LEFT JOIN FETCH c.utilisateur " +
                        "WHERE c.deleted = false",
                Colis.class
        ).getResultList();
    }

    public void save(Colis colis) {
        logger.info("💾 Sauvegarde colis: " + (colis.getId() != null ? "update ID=" + colis.getId() : "nouveau"));
        try {
            if (colis.getId() == null) {
                em.persist(colis);
                logger.info("✅ Colis créé avec succès");
            } else {
                em.merge(colis);
                logger.info("✅ Colis mis à jour avec succès");
            }
            em.flush(); // Force la synchronisation avec la BD
        } catch (Exception e) {
            logger.severe("❌ Erreur save: " + e.getMessage());
            throw e;
        }
    }

    public void update(Colis colis) {
        logger.info("🔄 Update colis ID=" + colis.getId());
        try {
            Colis updated = em.merge(colis);
            em.flush();
            logger.info("✅ Colis mis à jour avec succès");
        } catch (Exception e) {
            logger.severe("❌ Erreur update: " + e.getMessage());
            throw e;
        }
    }
}