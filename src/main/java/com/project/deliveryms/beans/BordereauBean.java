package com.project.deliveryms.beans;

import com.project.deliveryms.entities.BordereauExpedition;
import com.project.deliveryms.services.BordereauService;
import com.project.deliveryms.services.ColisService;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import java.io.Serializable;

@Named
@ViewScoped
public class BordereauBean implements Serializable {

    @Inject
    private BordereauService bordereauService;

    @Inject
    private ColisService colisService;

    public void genererBordereau(Long colisId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();

        try {
            // Récupérer le bordereau lié au colis
            BordereauExpedition bordereau = colisService.getBordereauByColisId(colisId);
            if (bordereau == null) {
                facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Erreur", "Bordereau non trouvé pour ce colis"));
                return;
            }

            // Préparer la réponse HTTP
            HttpServletResponse response = (HttpServletResponse) externalContext.getResponse();
            response.reset();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "attachment; filename=bordereau_" + colisId + ".pdf");

            // Générer le PDF dans la réponse
            bordereauService.generateBordereauPdf(bordereau, response);

            // Signaler à JSF que la réponse est terminée
            facesContext.responseComplete();

        } catch (Exception e) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Erreur", "Impossible de générer le bordereau"));
            e.printStackTrace();
        }
    }
}