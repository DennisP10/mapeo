package com.lsa.mapeo.controller;

import com.lsa.mapeo.dto.CargaMasivaDTO;
import com.lsa.mapeo.model.*;
import com.lsa.mapeo.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/mapeo")


public class MapeoController {

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private DominioRepository dominioRepository;

    private String sanitizarUrl(String urlOriginal) {
        if (urlOriginal == null || urlOriginal.isBlank())
            return null;

        // 1. Limpieza inicial y minúsculas
        String url = urlOriginal.trim().toLowerCase();

        // 2. Manejo de URLs complejas (Limpieza de rutas como /app/accounts/...)
        // Si contiene "://", intentamos extraer solo hasta el final del dominio
        if (url.contains("://")) {
            try {
                // Dividimos por el protocolo
                String[] parts = url.split("://");
                String protocolo = parts[0];
                // Tomamos lo que sigue y buscamos la primera "/" después del host
                String resto = parts[1];
                int firstSlash = resto.indexOf("/");

                if (firstSlash != -1) {
                    // Recortamos para quedarnos solo con el host (ej: lsa-test.tws2.io)
                    url = protocolo + "://" + resto.substring(0, firstSlash);
                }
            } catch (Exception e) {
                // Si algo falla, continuamos con la lógica normal por seguridad
            }
        }

        // 3. Asegurar protocolo HTTPS
        if (url.startsWith("http://")) {
            url = url.replace("http://", "https://");
        } else if (!url.startsWith("https://")) {
            url = "https://" + url;
        }

        // 4. Limpieza final de redundancias
        url = url.replace("https://www.", "https://");
        url = url.replaceAll("/+$", ""); // Eliminar slashes finales si quedaron

        return url;
    }

    // SERVICIO: VINCULAR CLIENTES A INFRAESTRUCTURA EXISTENTE
    @PostMapping("/cargar-datos")
    public ResponseEntity<?> cargarDatosMasivos(@RequestBody CargaMasivaDTO dto) {

        if (dto.getEmpresa() == null || dto.getEmpresa().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de la empresa es obligatorio."));
        }

        String urlLimpia = sanitizarUrl(dto.getUrlDominio());

        // BUSCAR DOMINIO
        Optional<Dominio> dominioOpt = dominioRepository.findByUrlDominio(urlLimpia);
        if (dominioOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "El dominio no existe",
                    "mensaje", "Primero debes registrar la URL '" + urlLimpia + "' en el endpoint /dominios"));
        }
        Dominio dominio = dominioOpt.get();

        // ESTABLECER EMPRESA
        String empresaNombre = dto.getEmpresa().trim().toUpperCase();
        Empresa empresa = empresaRepository.findByNombre(empresaNombre)
                .orElseGet(() -> {
                    Empresa nueva = new Empresa();
                    nueva.setNombre(empresaNombre);
                    return empresaRepository.save(nueva);
                });

        // PROCESAR CLIENTES
        int nuevosUsuarios = 0;
        if (dto.getClientes() != null) {
            for (Map<String, Object> cliente : dto.getClientes()) {
                String email = (String) cliente.get("email");
                if (email != null) {
                    String emailLower = email.toLowerCase().trim();
                    if (usuarioRepository.findByCorreo(emailLower).isEmpty()) {
                        Usuario usuario = new Usuario();
                        usuario.setCorreo(emailLower);
                        usuario.setEmpresa(empresa);
                        usuario.setDominio(dominio);
                        usuarioRepository.save(usuario);
                        nuevosUsuarios++;
                    }
                }
            }
        }

        return ResponseEntity.ok(Map.of(
                "status", "success",
                "empresa", empresa.getNombre(),
                "dominio_utilizado", dominio.getUrlDominio(),
                "usuarios_vinculados", nuevosUsuarios));
    }

    // REGISTRO DE DOMINIOS
    @PostMapping("/dominios")
    public ResponseEntity<?> crearSoloDominio(@RequestBody Map<String, String> body) {
        String urlOriginal = body.get("urlDominio");
        if (urlOriginal == null || urlOriginal.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL requerida"));
        }

        String urlProcesada = sanitizarUrl(urlOriginal);

        Optional<Dominio> existente = dominioRepository.findByUrlDominio(urlProcesada);
        if (existente.isPresent()) {
            return ResponseEntity.ok(Map.of(
                    "status", "exists",
                    "mensaje", "El dominio ya está en el catálogo",
                    "url", existente.get().getUrlDominio()));
        }

        Dominio nuevo = new Dominio();
        nuevo.setUrlDominio(urlProcesada);
        Dominio guardado = dominioRepository.save(nuevo);

        return ResponseEntity.status(201).body(Map.of(
                "status", "created",
                "url_registrada", guardado.getUrlDominio()));
    }

    // CONSULTA
    @PostMapping("/consultar")
    public ResponseEntity<?> consultarPorCorreo(@RequestBody Map<String, String> body) {
        String correoInput = body.get("email");

        if (correoInput == null || correoInput.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El campo 'email' es obligatorio en el JSON"));
        }

        // Aplicamos la misma robustez de búsqueda (trim y minúsculas)
        String correoBusqueda = correoInput.trim().toLowerCase();

        return usuarioRepository.findByCorreo(correoBusqueda)
                .map(u -> {
                    // Preparamos la respuesta con los datos de dominio y empresa
                    Map<String, Object> res = new HashMap<>();
                    res.put("status", "success");
                    res.put("usuario", u.getCorreo());
                    res.put("empresa", u.getEmpresa().getNombre());
                    res.put("url_dominio", u.getDominio().getUrlDominio());
                    return ResponseEntity.ok(res);
                })
                .orElse(ResponseEntity.status(404).body(Map.of(
                        "status", "error",
                        "mensaje", "El correo " + correoBusqueda + " no tiene un dominio asignado")));
    }
}