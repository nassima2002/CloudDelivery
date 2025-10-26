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
@RequestScoped
public class UtilisateurService {

    @Inject
    private UtilisateurRepository utilisateurRepository;

    @Inject
    private LivreureRepository livreurRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Authentifier un utilisateur avec validation du format BCrypt
    public String authentifier(String email, String motDePasse) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);

        if (utilisateur == null) {
            return "Utilisateur non trouvé";
        }

        String storedPassword = utilisateur.getMotDePasse();

        // ✅ Vérification 1 : Mot de passe null ou vide
        if (storedPassword == null || storedPassword.isEmpty()) {
            System.err.println("❌ ERREUR : Mot de passe vide pour l'utilisateur : " + email);
            return "Erreur de configuration du mot de passe";
        }

        // ✅ Vérification 2 : Format BCrypt valide
        // Format attendu : $2a$10$xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx (60 caractères)
        if (!storedPassword.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$")) {
            System.err.println("❌ ERREUR : Format BCrypt invalide pour : " + email);
            System.err.println("   Hash stocké (début) : " + storedPassword.substring(0, Math.min(20, storedPassword.length())));
            System.err.println("   Longueur du hash : " + storedPassword.length() + " (attendu: 60)");

            // ⚠️ MODE TEMPORAIRE : Si le mot de passe est en texte clair
            if (storedPassword.equals(motDePasse)) {
                System.err.println("⚠️ ATTENTION : Mot de passe en TEXTE CLAIR détecté pour : " + email);
                System.err.println("   URGENT : Ce mot de passe doit être hashé immédiatement !");
                return "Connexion réussie"; // Permet la connexion mais LOG l'erreur
            }

            return "Format de mot de passe invalide. Contactez l'administrateur.";
        }

        // ✅ Vérification 3 : Validation BCrypt avec gestion d'erreur
        try {
            if (!BCrypt.checkpw(motDePasse, storedPassword)) {
                return "Mot de passe incorrect";
            }
        } catch (IllegalArgumentException e) {
            System.err.println("❌ ERREUR BCrypt pour : " + email);
            System.err.println("   Message d'erreur : " + e.getMessage());
            e.printStackTrace();
            return "Erreur lors de la vérification du mot de passe";
        }

        return "Connexion réussie";
    }

    public String inscrire(Utilisateur utilisateur) {
        Utilisateur utilisateurExistant = utilisateurRepository.findByEmail(utilisateur.getEmail());

        if (utilisateurExistant != null) {
            return "Email déjà utilisé. Veuillez en choisir un autre.";
        }

        utilisateur.setCreationDate(java.time.LocalDateTime.now());
        utilisateur.setLastConnectionDate(java.time.LocalDateTime.now());

        // Hashing du mot de passe avant de l'enregistrer
        String motDePasseHash = BCrypt.hashpw(utilisateur.getMotDePasse(), BCrypt.gensalt());
        utilisateur.setMotDePasse(motDePasseHash);

        utilisateurRepository.save(utilisateur);
        return "Inscription réussie";
    }

    public Utilisateur findUserByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Transactional
    public void update(Utilisateur utilisateur) {
        if (utilisateur == null || utilisateur.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur ou son ID ne peut pas être null");
        }

        entityManager.merge(utilisateur);
        entityManager.flush();
    }

    // ✅ Méthode pour hasher un mot de passe existant (migration)
    @Transactional
    public void hashUserPassword(String email, String plainPassword) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
        if (utilisateur != null) {
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
            utilisateur.setMotDePasse(hashedPassword);
            entityManager.merge(utilisateur);
            entityManager.flush();
            System.out.println("✅ Mot de passe hashé avec succès pour : " + email);
        }
    }

    // ✅ Méthode pour migrer tous les mots de passe en texte clair
    @Transactional
    public int migrateAllPlainPasswords() {
        int count = 0;
        // Récupérer tous les utilisateurs
        // Note: Vous devrez peut-être ajouter findAll() dans votre repository
        try {
            var users = entityManager.createQuery("SELECT u FROM Utilisateur u", Utilisateur.class).getResultList();

            for (Utilisateur user : users) {
                String password = user.getMotDePasse();

                // Si le mot de passe n'est pas au format BCrypt
                if (password != null && !password.matches("^\\$2[aby]?\\$\\d{2}\\$.{53}$")) {
                    String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
                    user.setMotDePasse(hashedPassword);
                    entityManager.merge(user);
                    count++;
                    System.out.println("✅ Mot de passe migré pour : " + user.getEmail());
                }
            }

            entityManager.flush();
            System.out.println("✅ Migration terminée : " + count + " mot(s) de passe migré(s)");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la migration : " + e.getMessage());
            e.printStackTrace();
        }

        return count;
    }

    public Utilisateur getUtilisateurConnecte() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        return (Utilisateur) session.getAttribute("utilisateurConnecte");
    }

    public Livreur getLivreurConnecte() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) context.getExternalContext().getSession(false);

        if (session == null) {
            return null;
        }

        Object utilisateurSession = session.getAttribute("utilisateurConnecte");

        if (utilisateurSession instanceof Livreur) {
            return (Livreur) utilisateurSession;
        } else if (utilisateurSession instanceof Utilisateur) {
            Utilisateur utilisateur = (Utilisateur) utilisateurSession;
            return livreurRepository.findLivreurByEmail(utilisateur.getEmail());
        }
        return null;
    }
}