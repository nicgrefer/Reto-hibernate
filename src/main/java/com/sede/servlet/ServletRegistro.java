package com.sede.servlet;

import java.io.IOException;

import com.sede.dao.EntidadDAO;
import com.sede.dao.RegistrosDAO;
import com.sede.ln.RegistrosLN;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(name = "ServletRegistro", urlPatterns = {"/ServletRegistro", "/grabar", "/buscar"})
public class ServletRegistro extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private EntidadDAO entidadDAO;
    private RegistrosDAO registrosDAO;
    private RegistrosLN registrosLN;

    @Override
    public void init() throws ServletException {
        super.init();
        entidadDAO = new EntidadDAO();
        registrosDAO = new RegistrosDAO();
        registrosLN = new RegistrosLN();
        System.out.println("ServletRegistro inicializado");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        try {
            if ("buscar".equals(accion)) {
                buscarRegistro(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/Buscar.jsp");
            }
        } catch (Exception e) {
            System.err.println("Error en doGet: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
            request.getRequestDispatcher("/Error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String accion = request.getParameter("accion");

        try {
            if ("grabar".equals(accion) || accion == null) {
                grabarRegistro(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/Registro.jsp");
            }
        } catch (Exception e) {
            System.err.println("Error en doPost: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("error", "Error al procesar la solicitud: " + e.getMessage());
            request.getRequestDispatcher("/Error.jsp").forward(request, response);
        }
    }

    private void grabarRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String dni = request.getParameter("dni");
            String nombre = request.getParameter("nombre");
            String apellidos = request.getParameter("apellidos");
            String tramite = request.getParameter("tramite");
            String idEntidadStr = request.getParameter("idEntidad");

            System.out.println("Procesando registro: DNI=" + dni + ", Nombre=" + nombre);

            if (dni == null || nombre == null || apellidos == null || 
                tramite == null || idEntidadStr == null) {
                request.setAttribute("exito", false);
                request.setAttribute("mensaje", "Todos los campos son obligatorios");
                request.getRequestDispatcher("/Mensaje.jsp").forward(request, response);
                return;
            }

            int idEntidad = 0;
            try {
                idEntidad = Integer.parseInt(idEntidadStr);
            } catch (NumberFormatException e) {
                request.setAttribute("exito", false);
                request.setAttribute("mensaje", "ID de entidad inválido");
                request.getRequestDispatcher("/Mensaje.jsp").forward(request, response);
                return;
            }

            if (!registrosLN.validarDatos(dni, nombre, apellidos, tramite, idEntidad)) {
                request.setAttribute("exito", false);
                request.setAttribute("mensaje", "Datos inválidos. Verifique el formato del DNI (8 dígitos + letra)");
                request.getRequestDispatcher("/Mensaje.jsp").forward(request, response);
                return;
            }

            com.sede.entities.Entidad entidad = entidadDAO.obtenerPorId(idEntidad);
            if (entidad == null) {
                request.setAttribute("exito", false);
                request.setAttribute("mensaje", "Entidad no encontrada");
                request.getRequestDispatcher("/Mensaje.jsp").forward(request, response);
                return;
            }

            com.sede.entities.Registros registro = new com.sede.entities.Registros();
            registro.setDniSolicitante(dni.trim().toUpperCase());
            registro.setNombreSolicitante(nombre.trim());
            registro.setApellidosSolicitante(apellidos.trim());
            registro.setTramite(tramite.trim());
            registro.setEntidad(entidad);

            String numRegistro = registrosLN.generarNumeroRegistro();
            registro.setNumRegistro(numRegistro);

            // Guardar en la base de datos
            String resultado = registrosLN.procesarRegistro(registro);

            if (resultado != null) {
                request.setAttribute("exito", true);
                request.setAttribute("numRegistro", resultado);
                request.setAttribute("fechaRegistro", registro.getFechaRegistro());
            } else {
                request.setAttribute("exito", false);
                request.setAttribute("mensaje", "No se pudo guardar el registro en la base de datos");
            }

            request.getRequestDispatcher("/Mensaje.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("Error al grabar registro: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("exito", false);
            request.setAttribute("mensaje", "Error al realizar la grabación");
            request.getRequestDispatcher("/Mensaje.jsp").forward(request, response);
        }
    }

    private void buscarRegistro(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            String numRegistro = request.getParameter("numRegistro");

            System.out.println("Buscando registro: " + numRegistro);

            if (numRegistro == null || numRegistro.trim().isEmpty()) {
                request.setAttribute("encontrado", false);
                request.setAttribute("mensaje", "Debe proporcionar un número de registro");
                request.getRequestDispatcher("/Consultar.jsp").forward(request, response);
                return;
            }

            com.sede.entities.Registros registro = registrosDAO.buscarPorNumero(numRegistro.trim());

            if (registro != null) {
                request.setAttribute("encontrado", true);
                request.setAttribute("registro", registro);
            } else {
                request.setAttribute("encontrado", false);
                request.setAttribute("mensaje", "El trámite de registro no existe");
            }

            request.getRequestDispatcher("/Consultar.jsp").forward(request, response);

        } catch (Exception e) {
            System.err.println("Error al buscar registro: " + e.getMessage());
            e.printStackTrace();
            request.setAttribute("encontrado", false);
            request.setAttribute("mensaje", "Error al buscar el registro");
            request.getRequestDispatcher("/Consultar.jsp").forward(request, response);
        }
    }
}
