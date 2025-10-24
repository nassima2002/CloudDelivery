package com.project.deliveryms.services;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.project.deliveryms.entities.BordereauExpedition;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

@Named
@RequestScoped
public class BordereauService {

    public void generateBordereauPdf(BordereauExpedition bordereau, HttpServletResponse response) throws DocumentException, IOException {
        Document document = new Document();
        PdfWriter.getInstance(document, response.getOutputStream());
        document.open();

        // Logo
        InputStream logoStream = getClass().getClassLoader().getResourceAsStream("images/camion.png");
        if (logoStream != null) {
            try {
                Image logo = Image.getInstance(IOUtils.toByteArray(logoStream));
                logo.scaleToFit(80, 80);
                logo.setAlignment(Image.ALIGN_LEFT);
                document.add(logo);
            } catch (Exception e) {
                document.add(new Paragraph("Erreur lors du chargement du logo: " + e.getMessage()));
            }
        } else {
            document.add(new Paragraph("Logo non disponible."));
        }


        // Title
        Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
        Paragraph title = new Paragraph("BORDEAUX D'EXPEDITION", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        Font sectionFont = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD, BaseColor.BLUE);
        Font labelFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font valueFont = new Font(Font.FontFamily.HELVETICA, 11);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Section: Détails du colis
        document.add(new Paragraph("Détails du Colis", sectionFont));
        document.add(new Paragraph(" "));

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        addRow(table, "N° de suivi :", bordereau.getColis().getNumeroSuivi(), labelFont, valueFont);
        addRow(table, "Description :", bordereau.getColis().getDescription(), labelFont, valueFont);
        addRow(table, "Poids :", bordereau.getColis().getPoids() + " kg", labelFont, valueFont);
        addRow(table, "Date d’envoi :", safeDate(bordereau.getColis().getDateEnvoi(), formatter), labelFont, valueFont);
        addRow(table, "Date de livraison :", safeDate(bordereau.getColis().getDateLivraison(), formatter), labelFont, valueFont);
        addRow(table, "Statut :", safeValue(bordereau.getColis().getStatus()), labelFont, valueFont);
        addRow(table, "Date de génération :", safeDate(bordereau.getDateGeneration(), formatter), labelFont, valueFont);

        document.add(table);

        // Section: Adresse de Livraison
        document.add(new Paragraph("Adresse de Livraison", sectionFont));
        document.add(new Paragraph(" "));

        PdfPTable adresseTable = new PdfPTable(2);
        adresseTable.setWidthPercentage(100);
        adresseTable.setSpacingAfter(15);

        addRow(adresseTable, "Rue :", safeAdresse(bordereau, "rue"), labelFont, valueFont);
        addRow(adresseTable, "Ville / Pays :", safeAdresse(bordereau, "villePays"), labelFont, valueFont);
        addRow(adresseTable, "Code postal :", safeAdresse(bordereau, "codePostal"), labelFont, valueFont);

        document.add(adresseTable);

        // Footer / Signature
        document.add(new Paragraph("Commentaires : .........................................................................................."));
        document.add(new Paragraph("Dispositions à prendre : .............................................................................."));
        document.add(new Paragraph("\n\nCommande complète : ______    Reçue par : ____________    Date : ____________"));

        // Ligne horizontale + merci
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(BaseColor.LIGHT_GRAY);
        document.add(new Chunk(ls));
        document.add(new Paragraph("Merci d'avoir utilisé notre service de livraison !", new Font(Font.FontFamily.HELVETICA, 10, Font.ITALIC)));

        document.close();
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell cell1 = new PdfPCell(new Phrase(label, labelFont));
        PdfPCell cell2 = new PdfPCell(new Phrase(value, valueFont));
        cell1.setPadding(8);
        cell2.setPadding(8);
        cell1.setBorderColor(BaseColor.LIGHT_GRAY);
        cell2.setBorderColor(BaseColor.LIGHT_GRAY);
        table.addCell(cell1);
        table.addCell(cell2);
    }

    private String safeDate(java.time.LocalDateTime date, DateTimeFormatter formatter) {
        return date != null ? date.format(formatter) : "Non disponible";
    }

    private String safeValue(Object obj) {
        return obj != null ? obj.toString() : "Non disponible";
    }

    private String safeAdresse(BordereauExpedition bordereau, String type) {
        if (bordereau.getColis().getAdresseDestinataire() == null) return "Non disponible";
        switch (type) {
            case "rue":
                return safeValue(bordereau.getColis().getAdresseDestinataire().getRue());
            case "villePays":
                return safeValue(bordereau.getColis().getAdresseDestinataire().getVille()) +
                        " / " +
                        safeValue(bordereau.getColis().getAdresseDestinataire().getPays());
            case "codePostal":
                return safeValue(bordereau.getColis().getAdresseDestinataire().getCodePostal());
            default:
                return "Non disponible";
        }
    }
}