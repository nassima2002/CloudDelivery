package com.project.deliveryms.services;

import com.project.deliveryms.entities.Adresse;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class AdresseService {

    @PersistenceContext
    private EntityManager em;

    public Adresse createAdresse(String rue, String ville, String codePostal, String pays) {
        Adresse adresse = new Adresse();
        adresse.setRue(rue);
        adresse.setVille(ville);
        adresse.setCodePostal(codePostal);
        adresse.setPays(pays);

        em.persist(adresse);
        return adresse;
    }

    public void updateAdresse(Adresse adresse) {
        // Cette méthode met à jour l'adresse existante dans la base de données
        if (adresse != null) {
            em.merge(adresse);  // merge() fusionne l'entité avec la base de données, ou la crée si elle n'existe pas
        } else {
            throw new IllegalArgumentException("Adresse ne peut pas être nulle");
        }
    }
}
