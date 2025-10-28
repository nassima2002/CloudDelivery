package com.project.deliveryms.repositories;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.StatusColis;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.logging.Logger;

@Stateless
public class LivreureRepository {



    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    private static final Logger LOG = Logger.getLogger(LivreureRepository.class.getName());

    // Insérer un livreur
    public Livreur save(Livreur livreur) {
        entityManager.persist(livreur);
        return livreur;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    // Méthode pour mettre à jour un livreur
    public void update(Livreur livreur) {
        if (livreur.getId() == null) {
            throw new IllegalArgumentException("ID du livreur est requis pour la mise à jour");
        }

        // Récupérer le livreur existant
        Livreur existingLivreur = entityManager.find(Livreur.class, livreur.getId());
        if (existingLivreur == null) {
            throw new RuntimeException("Livreur avec ID " + livreur.getId() + " non trouvé");
        }

        // Mettre à jour les données du livreur
        existingLivreur.setDisponibiliter(livreur.getDisponibiliter());
        if (livreur.getLatitude() != null) {
            existingLivreur.setLatitude(livreur.getLatitude());
        }
        if (livreur.getLongitude() != null) {
            existingLivreur.setLongitude(livreur.getLongitude());
        }

        // Mettre à jour les données de l'utilisateur associé
        Utilisateur existingUser = existingLivreur.getUser();
        if (existingUser != null && livreur.getUser() != null) {
            if (livreur.getUser().getNom() != null) {
                existingUser.setNom(livreur.getUser().getNom());
            }
            if (livreur.getUser().getPrenom() != null) {
                existingUser.setPrenom(livreur.getUser().getPrenom());
            }
            if (livreur.getUser().getEmail() != null) {
                existingUser.setEmail(livreur.getUser().getEmail());
            }

            // Persister les changements de l'utilisateur
            entityManager.merge(existingUser);
        }

        // Mettre à jour les données du livreur
        entityManager.merge(existingLivreur);
    }
    // Supprimer un livreur par ID
    public void delete(Long id) {
        Livreur livreur = find(id);
        if (livreur != null) {
            entityManager.remove(livreur);
        }
    }

    // Trouver un livreur par ID
    public Livreur find(Long id) {
        return entityManager.find(Livreur.class, id);
    }

    // Lister tous les livreurs
    public List<Livreur> findAll() {
        TypedQuery<Livreur> query = entityManager.createQuery("SELECT l FROM Livreur l", Livreur.class);
        return query.getResultList();
    }

    // Trouver un user par email (utile pour vérifier si email existe)
    public Utilisateur findByEmail(String email) {
        TypedQuery<Utilisateur> query = entityManager.createQuery(
                "SELECT u FROM Utilisateur u WHERE u.email = :email", Utilisateur.class);
        query.setParameter("email", email);
        return query.getResultStream().findFirst().orElse(null);
    }

    // Liste des livreurs indisponibles
    public List<Livreur> findLivreursIndisponibles() {
        TypedQuery<Livreur> query = entityManager.createQuery(
                "SELECT DISTINCT l FROM Livreur l " +
                        "LEFT JOIN Colis c ON c.livreur = l " +
                        "WHERE l.disponibiliter = 'oui'",
                Livreur.class
        );
        return query.getResultList();
    }

    public Livreur findLivreurByEmail(String email) {
        TypedQuery<Livreur> query = entityManager.createQuery(
                "SELECT l FROM Livreur l WHERE l.user.email = :email", Livreur.class);
        query.setParameter("email", email);
        return query.getResultStream().findFirst().orElse(null);
    }


}