package com.project.deliveryms;

import java.io.IOException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "RedirectServlet", urlPatterns = {"/hello-servlet", "/hello"})
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String path = request.getServletPath();

        switch (path) {
            case "/hello-servlet":
                response.sendRedirect(request.getContextPath() + "/pages/Livreur.xhtml");
                break;
            case "/hello":
                response.sendRedirect(request.getContextPath() + "/pages/affectation.xhtml");
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
