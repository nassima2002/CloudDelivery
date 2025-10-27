package com.project.deliveryms.beans;

import com.project.deliveryms.entities.Utilisateur;
import com.project.deliveryms.enums.Role;
import com.project.deliveryms.services.ClientService;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@SessionScoped
public class ClientBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private ClientService clientService;

    private List<Utilisateur> clients;
    private Utilisateur nouveauClient = new Utilisateur();
    private Utilisateur clientSelectionne = new Utilisateur();

    @PostConstruct
    public void init() {
        chargerClients();
    }

    public void chargerClients() {
        clients = clientService.getAllClients();
    }

    public void ajouterClient() {
        try {
            // S'assurer que le rôle est CLIENT lors de l'ajout
            nouveauClient.setRole(Role.CLIENT);

            clientService.addClient(nouveauClient);
            nouveauClient = new Utilisateur();
            chargerClients();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("✅ Client ajouté avec succès"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur : " + e.getMessage(), null));
        }
    }

    public void preparerModification(Utilisateur client) {
        // Copier les données dans clientSelectionne pour éviter d'éditer la liste directement
        clientSelectionne = new Utilisateur();
        clientSelectionne.setId(client.getId());
        clientSelectionne.setNom(client.getNom());
        clientSelectionne.setPrenom(client.getPrenom());
        clientSelectionne.setEmail(client.getEmail());
        clientSelectionne.setMotDePasse(client.getMotDePasse());

        // IMPORTANT: Toujours définir le rôle à CLIENT
        clientSelectionne.setRole(Role.CLIENT);
    }

    public void modifierClient() {
        try {
            // SÉCURITÉ: Forcer le rôle à CLIENT avant la modification
            // pour éviter toute tentative de changement de rôle
            clientSelectionne.setRole(Role.CLIENT);

            clientService.updateClient(clientSelectionne);
            chargerClients();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("✅ Client modifié avec succès"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur : " + e.getMessage(), null));
        }
    }

    public void supprimerClient(Long id) {
        try {
            clientService.deleteClient(id);
            chargerClients();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage("🗑️ Client supprimé avec succès"));
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erreur : " + e.getMessage(), null));
        }
    }

    // Getters / Setters
    public List<Utilisateur> getClients() {
        return clients;
    }

    public void setClients(List<Utilisateur> clients) {
        this.clients = clients;
    }

    public Utilisateur getNouveauClient() {
        return nouveauClient;
    }

    public void setNouveauClient(Utilisateur nouveauClient) {
        this.nouveauClient = nouveauClient;
    }

    public Utilisateur getClientSelectionne() {
        return clientSelectionne;
    }

    public void setClientSelectionne(Utilisateur clientSelectionne) {
        this.clientSelectionne = clientSelectionne;
    }
}