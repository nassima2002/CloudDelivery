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
import java.util.logging.Logger;

@Stateless
public class ColisService {

    private static final Logger LOG = Logger.getLogger(ColisService.class.getName());

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

    // Création d'un colis sans utilisateur
    public Colis createColis(String description, double poids, Adresse adresseDestinataire) {
        Colis colis = new Colis();
        colis.setNumeroSuivi(UUID.randomUUID().toString());
        colis.setDescription(description);
        colis.setPoids(poids);
        colis.setDateEnvoi(LocalDateTime.now());
        colis.setStatus(StatusColis.EN_ATTENTE);
        colis.setAdresseDestinataire(adresseDestinataire);

        em.persist(colis);
        return colis;
    }

    public Colis associerColisAUtilisateur(Long colisId, Long utilisateurId) {
        // ✅ CORRECTION: Gérer l'Optional
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new IllegalArgumentException("Colis non trouvé: ID=" + colisId));

        Utilisateur utilisateur = utilisateurRepository.findById(utilisateurId);

        if (utilisateur == null) {
            throw new IllegalArgumentException("Utilisateur non trouvé: ID=" + utilisateurId);
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
        // ✅ CORRECTION: Gérer l'Optional
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new EntityNotFoundException("Colis avec l'ID " + colisId + " non trouvé"));

        colis.setDeleted(true);
        colisRepository.save(colis);
    }

    public Colis updateColis(Long colisId, String description, double poids, StatusColis status,
                             String rue, String ville, String codePostal, String pays) {

        // ✅ CORRECTION: Gérer l'Optional
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new EntityNotFoundException("Colis avec l'ID " + colisId + " non trouvé"));

        colis.setDescription(description);
        colis.setPoids(poids);
        colis.setStatus(status);

        Adresse adresse = colis.getAdresseDestinataire();
        if (adresse != null) {
            adresse.setRue(rue);
            adresse.setVille(ville);
            adresse.setCodePostal(codePostal);
            adresse.setPays(pays);
            adresseService.updateAdresse(adresse);
        } else {
            adresse = adresseService.createAdresse(rue, ville, codePostal, pays);
            colis.setAdresseDestinataire(adresse);
        }

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
        Colis colis = em.find(Colis.class, idColis);
        Livreur livreur = em.find(Livreur.class, idLivreur);

        if (colis != null && livreur != null) {
            colis.setLivreur(livreur);
            em.merge(colis);
        }
    }

    public List<Livreur> getLivreursIndisponibles() {
        return livreurRepository.findLivreursIndisponibles();
    }

    /**
     * ✅ MÉTHODE CORRIGÉE : Affecte un colis à un livreur et le rend indisponible
     */
    @Transactional
    public void affecterColis(Long idColis, Long idLivreur) {
        try {
            LOG.info("========================================");
            LOG.info("🔄 DÉBUT AFFECTATION");
            LOG.info("   Colis ID: " + idColis);
            LOG.info("   Livreur ID: " + idLivreur);
            LOG.info("========================================");

            // Récupérer le colis
            Colis colis = colisRepository.find(idColis);
            if (colis == null) {
                LOG.severe("❌ Colis introuvable: ID=" + idColis);
                throw new RuntimeException("Colis introuvable");
            }

            // Récupérer le livreur
            Livreur livreur = livreurRepository.find(idLivreur);
            if (livreur == null) {
                LOG.severe("❌ Livreur introuvable: ID=" + idLivreur);
                throw new RuntimeException("Livreur introuvable");
            }

            LOG.info("✅ Entités trouvées:");
            LOG.info("   Colis: " + colis.getDescription());
            LOG.info("   Livreur: " + livreur.getUser().getNom() + " " + livreur.getUser().getPrenom());
            LOG.info("   Disponibilité AVANT: " + livreur.getDisponibiliter());

            // Affecter le livreur au colis
            colis.setLivreur(livreur);

            // Changer le statut du colis
            StatusColis ancienStatut = colis.getStatus();
            colis.setStatus(StatusColis.EN_TRANSIT);
            LOG.info("   Statut colis: " + ancienStatut + " → EN_TRANSIT");

            // Sauvegarder le colis
            colisRepository.save(colis);
            LOG.info("✅ Colis sauvegardé");

            // ✅ Mise à jour de la disponibilité du livreur (méthode sûre)
            LOG.info("🔄 Mise à jour disponibilité livreur...");
            livreur.setDisponibiliter("non");

            // Forcer la mise à jour immédiate
            em.merge(livreur);
            em.flush();
            em.clear(); // Clear le cache pour forcer un rechargement

            // Recharger pour vérifier
            Livreur livreurVerif = em.find(Livreur.class, idLivreur);
            LOG.info("   Disponibilité après merge: " + livreurVerif.getDisponibiliter());

            LOG.info("========================================");
            LOG.info("✅ AFFECTATION TERMINÉE");
            LOG.info("   Colis affecté au livreur");
            LOG.info("   Disponibilité: " + livreurVerif.getDisponibiliter());
            LOG.info("========================================");

            if (!"non".equals(livreurVerif.getDisponibiliter())) {
                LOG.severe("⚠️ ATTENTION: Disponibilité non mise à jour!");
                LOG.severe("   Tentative de mise à jour forcée...");

                // Tentative ultime avec refresh
                livreurVerif.setDisponibiliter("non");
                em.merge(livreurVerif);
                em.flush();
            }

        } catch (Exception e) {
            LOG.severe("========================================");
            LOG.severe("❌ ERREUR AFFECTATION: " + e.getMessage());
            LOG.severe("========================================");
            e.printStackTrace();
            throw new RuntimeException("Erreur lors de l'affectation: " + e.getMessage(), e);
        }
    }

    public List<Colis> getColisNonAffectes() {
        try {
            return colisRepository.findColisNonAffectes();
        } catch (Exception e) {
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
        // ✅ CORRECTION: Gérer l'Optional
        Colis colis = colisRepository.findById(colisId)
                .orElseThrow(() -> new EntityNotFoundException("Colis non trouvé: ID=" + colisId));

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
        // ✅ CORRECTION: Retourner null si absent (comportement original)
        return colisRepository.findById(id).orElse(null);
    }

    public void mettreAJourStatusColis(Long colisId, StatusColis nouveauStatut) {
        // ✅ Cette méthode utilisait déjà la bonne approche avec Optional
        Optional<Colis> colisOptional = colisRepository.findById(colisId);

        if (colisOptional.isPresent()) {
            Colis colis = colisOptional.get();
            colis.setStatus(nouveauStatut);
            colisRepository.save(colis);
            LOG.info("✅ Colis ID " + colisId + " mis à jour avec le statut : " + nouveauStatut);
        } else {
            LOG.warning("❌ Colis ID " + colisId + " non trouvé !");
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

    /**
     * ✅ Méthode pour rendre un livreur disponible après livraison
     */
    @Transactional
    public void marquerLivraisonTerminee(Long colisId) {
        try {
            // ✅ CORRECTION: Gérer l'Optional
            Colis colis = colisRepository.findById(colisId)
                    .orElseThrow(() -> new RuntimeException("Colis introuvable: ID=" + colisId));

            Livreur livreur = colis.getLivreur();
            if (livreur != null) {
                // Vérifier si le livreur a d'autres colis en cours
                List<Colis> autresColis = em.createQuery(
                                "SELECT c FROM Colis c WHERE c.livreur.id = :livreurId " +
                                        "AND c.status IN (:enTransit, :enAttente) AND c.id != :colisId",
                                Colis.class)
                        .setParameter("livreurId", livreur.getId())
                        .setParameter("enTransit", StatusColis.EN_TRANSIT)
                        .setParameter("enAttente", StatusColis.EN_ATTENTE)
                        .setParameter("colisId", colisId)
                        .getResultList();

                // Si aucun autre colis en cours, rendre disponible
                if (autresColis.isEmpty()) {
                    livreur.setDisponibiliter("oui");
                    em.merge(livreur);
                    LOG.info("✅ Livreur ID=" + livreur.getId() + " redevenu disponible");
                }
            }

            // Marquer le colis comme livré
            colis.setStatus(StatusColis.LIVRE);
            colis.setDateLivraison(LocalDateTime.now());
            colisRepository.update(colis);

            LOG.info("✅ Livraison terminée pour colis ID=" + colisId);

        } catch (Exception e) {
            LOG.severe("❌ Erreur marquage livraison: " + e.getMessage());
            throw new RuntimeException("Erreur: " + e.getMessage(), e);
        }
    }
}