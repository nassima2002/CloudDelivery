package com.project.deliveryms.services;

import com.project.deliveryms.entities.Livreur;
import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.repositories.LivreureRepository;
import com.project.deliveryms.repositories.UtilisateurRepository;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.mindrot.jbcrypt.BCrypt;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@RequestScoped  // Le service est léger, donc @RequestScoped est approprié
public class UtilisateurService {
    Utilisateur trouverParEmailEtMotDePasse(String email, String motDePasse) {
        return null;
    }

    @Inject
    private UtilisateurRepository utilisateurRepository;
    @Inject
    private LivreureRepository livreurRepository;

    // Authentifier un utilisateur
    public String authentifier(String email, String motDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);

        if (utilisateur == null) {
            // Utilisateur n'existe pas
            return "Utilisateur non trouvé";
        }

        if (!BCrypt.checkpw(motDePasse, utilisateur.getMotDePasse())) {
            // Mot de passe incorrect
            return "Mot de passe incorrect";
        }

        // Connexion réussie
        return "Connexion réussie";
    }




    public String inscrire(Utilisateur utilisateur) {
        // Vérifier si un utilisateur avec cet email existe déjà
        Utilisateur utilisateurExistant = utilisateurRepository.findByEmail(utilisateur.getEmail());

        if (utilisateurExistant != null) {
            // Si l'email existe déjà
            return "Email déjà utilisé. Veuillez en choisir un autre.";
        }

        utilisateur.setCreationDate(java.time.LocalDateTime.now());
        utilisateur.setLastConnectionDate(java.time.LocalDateTime.now());

        // Hashing du mot de passe avant de l'enregistrer
        String motDePasseHash = BCrypt.hashpw(utilisateur.getMotDePasse(), BCrypt.gensalt());
        utilisateur.setMotDePasse(motDePasseHash);

        // Enregistrer l'utilisateur dans la base de données
        utilisateurRepository.save(utilisateur);
        return "Inscription réussie";
    }
    public Utilisateur findUserByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }
    @PersistenceContext
    private EntityManager entityManager;

    // ✅ Méthode pour mettre à jour un utilisateur
    @Transactional
    public void update(Utilisateur utilisateur) {
        // Recherche de l'utilisateur par son identifiant (supposons que l'utilisateur ait un ID unique)
        Utilisateur existingUser = entityManager.find(Utilisateur.class, utilisateur.getId());
        if (existingUser != null) {
            // Mise à jour des informations de l'utilisateur
            existingUser.setNom(utilisateur.getNom());
            existingUser.setPrenom(utilisateur.getPrenom());
            existingUser.setEmail(utilisateur.getEmail());

            // Si le mot de passe a changé, mettez à jour le mot de passe aussi
            if (utilisateur.getMotDePasse() != null && !utilisateur.getMotDePasse().isEmpty()) {
                existingUser.setMotDePasse(utilisateur.getMotDePasse());
            }

            // L'entité sera automatiquement mise à jour grâce à la gestion de l'EntityManager
        }
    }


    public Utilisateur getUtilisateurConnecte() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        return (Utilisateur) session.getAttribute("utilisateurConnecte");
    }
    public Livreur getLivreurConnecte() {
        // Récupérer l'objet utilisateur de la session
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session == null) {
            return null; // Si la session n'existe pas, retourner null
        }


        // Vérifier l'utilisateur connecté dans la session
        Object utilisateurSession = session.getAttribute("utilisateurConnecte");

        if (utilisateurSession instanceof Livreur) {
            // Si l'objet de session est un Livreur, le retourner directement
            return (Livreur) utilisateurSession;
        } else if (utilisateurSession instanceof Utilisateur) {
            // Si l'objet de session est un Utilisateur, rechercher le livreur par son email
            Utilisateur utilisateur = (Utilisateur) utilisateurSession;
            return livreurRepository.findLivreurByEmail(utilisateur.getEmail());
        }
        return null; // Si aucune correspondance, retourner null
    }
    // Méthode pour récupérer l'utilisateur par email
    public Utilisateur getUtilisateurByEmail(String email) {
        return livreurRepository.findByEmail(email);
    }



}
