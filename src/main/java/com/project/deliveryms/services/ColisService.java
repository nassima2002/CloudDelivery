package com.project.deliveryms.services;

import com.project.deliveryms.entities.*;
import com.project.deliveryms.enums.StatusColis;
import com.project.deliveryms.repositories.ColisRepository;
import com.project.deliveryms.repositories.LivreureRepository;
import com.project.deliveryms.repositories.UtilisateurRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Stateless
public class ColisService {


    @PersistenceContext
    private EntityManager em;

    @Inject
    private ColisRepository colisRepository;

    @Inject
    private UtilisateurRepository utilisateurRepository;

    @Inject
    private AdresseService adresseService;

    @Inject
    private LivreureRepository livreurRepository;

    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;
    // Création d'un colis sans utilisateur
    public Colis createColis(String description, double poids, Adresse adresseDestinataire) {
        Colis colis = new Colis();
        colis.setNumeroSuivi(UUID.randomUUID().toString()); // Génération du numéro de suivi unique
        colis.setDescription(description);
        colis.setPoids(poids);
        colis.setDateEnvoi(LocalDateTime.now());
        colis.setStatus(StatusColis.EN_ATTENTE); // Le colis est en attente initialement
        colis.setAdresseDestinataire(adresseDestinataire);

        em.persist(colis);
        return colis;
    }

    public Colis associerColisAUtilisateur(Long colisId, Long utilisateurId) {
        Colis colis = colisRepository.findById(colisId);
        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId);

        if (colis == null || utilisateur == null) {
            throw new IllegalArgumentException("Colis ou utilisateur non trouvé");
        }

        colis.setUtilisateur(utilisateur);
        colisRepository.update(colis);
        return colis;
    }

    public Colis getColisByNumeroSuivi(String numeroSuivi) {
        return colisRepository.findByNumeroSuivi(numeroSuivi);
    }

    public List<Colis> getAllColisWithDetails() {
        return colisRepository.findAllWithDetails();
    }

    public void deleteColis(Long colisId) {
        // Recherche le colis par ID
        Colis colis = colisRepository.findById(colisId);

        if (colis == null) {
            // Si colis non trouvé, lancer une exception
            throw new EntityNotFoundException("Colis avec l'ID " + colisId + " non trouvé");
        }

        // Marquer le colis comme supprimé
        colis.setDeleted(true);

        // Sauvegarder le colis avec l'attribut 'deleted' mis à jour
        colisRepository.save(colis);
    }

    /**
     * Méthode pour mettre à jour les informations d'un colis
     * @param colisId ID du colis à mettre à jour
     * @param description Nouvelle description
     * @param poids Nouveau poids
     * @param status Nouveau statut
     * @param rue Nouvelle rue de l'adresse
     * @param ville Nouvelle ville
     * @param codePostal Nouveau code postal
     * @param pays Nouveau pays
     * @return Le colis mis à jour
     * @throws EntityNotFoundException si le colis n'est pas trouvé
     */
    public Colis updateColis(Long colisId, String description, double poids, StatusColis status,
                             String rue, String ville, String codePostal, String pays) {

        // Recherche le colis par ID
        Colis colis = colisRepository.findById(colisId);

        if (colis == null) {
            throw new EntityNotFoundException("Colis avec l'ID " + colisId + " non trouvé");
        }

        // Mise à jour des informations du colis
        colis.setDescription(description);
        colis.setPoids(poids);
        colis.setStatus(status);

        // Mise à jour de l'adresse
        Adresse adresse = colis.getAdresseDestinataire();
        if (adresse != null) {
            // Mise à jour de l'adresse existante
            adresse.setRue(rue);
            adresse.setVille(ville);
            adresse.setCodePostal(codePostal);
            adresse.setPays(pays);

            // Enregistrer les modifications de l'adresse
            adresseService.updateAdresse(adresse);
        } else {
            // Création d'une nouvelle adresse si elle n'existe pas
            adresse = adresseService.createAdresse(rue, ville, codePostal, pays);
            colis.setAdresseDestinataire(adresse);
        }

        // Sauvegarde du colis mis à jour
        colisRepository.update(colis);

        return colis;
    }

    public BordereauExpedition getBordereauByColisId(Long colisId) {
        Colis colis = em.find(Colis.class, colisId);
        if (colis != null) {
            return colis.getBordereauExpedition();
        }
        return null;
    }
    @Transactional
    public void affecterColisALivreur(Long idColis, Long idLivreur) {
        Colis colis = entityManager.find(Colis.class, idColis);
        Livreur livreur = entityManager.find(Livreur.class, idLivreur);

        if (colis != null && livreur != null) {
            colis.setLivreur(livreur);
            entityManager.merge(colis);
        }
    }




    public List<Livreur> getLivreursIndisponibles() {
        return livreurRepository.findLivreursIndisponibles();
    }


    public void affecterColis(Long idColis, Long idLivreur) {
        Colis colis = colisRepository.find(idColis);
        if (colis == null) {
            throw new RuntimeException("Colis introuvable");
        }

        Livreur livreur = livreurRepository.find(idLivreur);
        if (livreur == null) {
            throw new RuntimeException("Livreur introuvable");
        }

        colis.setLivreur(livreur);
        colis.setStatus(StatusColis.EN_TRANSIT); // Utilise l'enum correctement
        livreur.setDisponibiliter("non");
        colisRepository.save(colis);
    }
    public List<Colis> getColisNonAffectes() {
        try {
            return colisRepository.findColisNonAffectes();
        } catch (Exception e) {
            // Log or rethrow the exception as needed
            throw new RuntimeException("Erreur lors de la récupération des colis non affectés.", e);
        }
    }

    public List<Colis> getColisByLivreur(Livreur livreur) {
        return em.createQuery("SELECT c FROM Colis c WHERE c.livreur = :livreur", Colis.class)
                .setParameter("livreur", livreur)
                .getResultList();
    }
    public void update(Colis colis) {
        em.merge(colis);
    }


    public void updateStatusColis(Long colisId, StatusColis nouveauStatus) {
        Colis colis = colisRepository.findById(colisId);
        if (colis == null) {
            throw new EntityNotFoundException("Colis non trouvé");
        }
        colis.setStatus(nouveauStatus);
        colisRepository.update(colis);
    }

    public List<Colis> getColisByLivreurEtStatus(Livreur livreur, StatusColis status) {
        return em.createQuery("SELECT c FROM Colis c WHERE c.livreur = :livreur AND c.status = :status", Colis.class)
                .setParameter("livreur", livreur)
                .setParameter("status", status)
                .getResultList();
    }
    public Colis getColisById(Long id) {
        return colisRepository.findById(id);
    }
    public void mettreAJourStatusColis(Long colisId, StatusColis nouveauStatut) {
        Optional<Colis> colisOptional = colisRepository.findByIdcolis(colisId);

        if (colisOptional.isPresent()) {
            Colis colis = colisOptional.get();
            colis.setStatus(nouveauStatut);
            colisRepository.save(colis);
            System.out.println("✅ Colis ID " + colisId + " mis à jour avec le statut : " + nouveauStatut);
        } else {
            System.out.println("❌ Colis ID " + colisId + " non trouvé !");
        }
    }

    public int countColisByLivreur(Long livreurId) {
        String jpql = "SELECT COUNT(c) FROM Colis c WHERE c.livreur.id = :livreurId";
        return em.createQuery(jpql, Long.class)
                .setParameter("livreurId", livreurId)
                .getSingleResult()
                .intValue();
    }

    public int countColisByLivreurEtStatus(Long livreurId, StatusColis status) {
        String jpql = "SELECT COUNT(c) FROM Colis c WHERE c.livreur.id = :livreurId AND c.status = :status";
        return em.createQuery(jpql, Long.class)
                .setParameter("livreurId", livreurId)
                .setParameter("status", status)
                .getSingleResult()
                .intValue();
    }

    public List<Colis> findDerniersColisByLivreur(Long livreurId, int limit) {
        String jpql = "SELECT c FROM Colis c WHERE c.livreur.id = :livreurId ORDER BY c.dateEnvoi DESC";
        return em.createQuery(jpql, Colis.class)
                .setParameter("livreurId", livreurId)
                .setMaxResults(limit)
                .getResultList();
    }

    public void saveColis(Colis colis) {
        colisRepository.update(colis);
    }
}