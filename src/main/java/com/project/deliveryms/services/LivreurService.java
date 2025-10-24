// 2. LivreurService.java - Updated updateLivreur method

package com.project.deliveryms.services;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.repositories.LivreureRepository;
import com.project.deliveryms.repositories.UserRepository;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.mail.MessagingException;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

@Stateless
public class LivreurService implements Serializable {

    private static final long serialVersionUID = 1L;
    @PersistenceContext(unitName = "default")
    private EntityManager entityManager;

    @Inject
    private UserRepository userRepository;

    @Inject
    private LivreureRepository livreurRepository;

    @Inject
    private EmailService emailService;

    public LivreurService() {
        // Constructeur vide requis pour EJB
    }

    /**
     * Crée un nouveau livreur avec un compte utilisateur associé
     */
    @Transactional
    public Livreur createLivreur(String email, String nom, String prenom,
                                 Double latitude, Double longitude, String disponibilite) {
        try {
            String generatedPassword = generateRandomPassword();

            // Envoyer le mot de passe au livreur
            String subject = "Votre compte Livreur - Mot de passe";
            String message = "Bonjour " + prenom + ",\n\n" +
                    "Votre compte livreur a été créé.\n" +
                    "Votre mot de passe est : " + generatedPassword;
            emailService.sendEmail(email, subject, message);

            // Hacher le mot de passe
            String hashedPassword = BCrypt.hashpw(generatedPassword, BCrypt.gensalt());

            // Créer l'utilisateur
            Utilisateur user = new Utilisateur();
            user.setNom(nom);
            user.setPrenom(prenom);
            user.setEmail(email);
            user.setMotDePasse(hashedPassword);
            user.setRole(Role.LIVREUR);

            entityManager.persist(user);
            entityManager.flush(); // Génère l'ID

            // Créer le livreur
            Livreur livreur = new Livreur();
            livreur.setLatitude(latitude);
            livreur.setLongitude(longitude);
            livreur.setDisponibiliter(disponibilite);
            livreur.setUser(user);

            entityManager.persist(livreur);

            return livreur;

        } catch (MessagingException e) {
            throw new RuntimeException("Erreur lors de l'envoi de l'email : " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la création du livreur : " + e.getMessage(), e);
        }
    }

    /**
     * Met à jour un livreur et son utilisateur associé
     * Méthode améliorée pour une mise à jour plus robuste
     */
    private static final Logger LOG = Logger.getLogger(LivreurService.class.getName());

    @Transactional
    public void updateLivreur(Livreur livreur) {
        try {
            // Vérification que le livreur et son ID sont valides
            if (livreur == null || livreur.getId() == null) {
                throw new RuntimeException("Données de livreur invalides");
            }

            // Appel au repository pour mettre à jour le livreur
            livreurRepository.update(livreur);

            // Pour forcer la mise à jour en base de données
            // EntityManager.flush() force la persistance et valide la transaction
            livreurRepository.getEntityManager().flush();

            LOG.info("Livreur mis à jour avec succès - ID: " + livreur.getId());
        } catch (Exception e) {
            LOG.severe("Erreur lors de la mise à jour: " + e.getMessage());
            throw new RuntimeException("Erreur lors de la mise à jour du livreur: " + e.getMessage(), e);
        }
    }

    /**
     * Supprime un livreur et son utilisateur associé
     */
    @Transactional
    public void deleteLivreur(Long id) {
        Livreur livreur = entityManager.find(Livreur.class, id);
        if (livreur != null) {
            Utilisateur user = livreur.getUser();
            entityManager.remove(livreur);

            if (user != null) {
                entityManager.remove(entityManager.contains(user) ? user : entityManager.merge(user));
            }
        }
    }

    /**
     * Récupère tous les livreurs
     */
    public List<Livreur> getAllLivreurs() {
        return livreurRepository.findAll();
    }

    /**
     * Récupère les livreurs non disponibles
     */
    public List<Livreur> getLivreursIndisponibles() {
        return livreurRepository.findLivreursIndisponibles();
    }

    /**
     * Génère un mot de passe aléatoire
     */
    private String generateRandomPassword() {
        int length = 10;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }


    /**
     * Récupère un livreur par son ID
     * @param id L'identifiant du livreur
     * @return Le livreur ou null s'il n'existe pas
     */
    public Livreur getLivreurById(Long id) {
        try {
            if (id == null) {
                return null;
            }

            System.out.println("Récupération du livreur avec ID: " + id);

            // Utiliser l'entity manager pour récupérer le livreur
            Livreur livreur = entityManager.find(Livreur.class, id);

            // Forcer le chargement des associations pour éviter les erreurs LazyInitialization
            if (livreur != null && livreur.getUser() != null) {
                // Accéder aux propriétés pour forcer le chargement
                livreur.getUser().getNom();
                livreur.getUser().getEmail();
            }

            return livreur;
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du livreur: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }


    public Livreur findByEmail(String email) {
        return livreurRepository.findLivreurByEmail(email);
    }





}