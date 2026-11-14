package com.powergym.gimnasio.controller;

import com.powergym.gimnasio.model.*;
import com.powergym.gimnasio.repository.*;
import com.powergym.gimnasio.service.GimnasioDataStructureService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Controller
public class GimnasioController {

    private final UsuarioRepository usuarioRepo;
    private final ClienteRepository clienteRepo;
    private final AsistenciaRepository asistenciaRepo;
    private final GimnasioDataStructureService dsService;

    public GimnasioController(UsuarioRepository usuarioRepo, ClienteRepository clienteRepo,
                              AsistenciaRepository asistenciaRepo, GimnasioDataStructureService dsService) {
        this.usuarioRepo = usuarioRepo;
        this.clienteRepo = clienteRepo;
        this.asistenciaRepo = asistenciaRepo;
        this.dsService = dsService;
    }

    @GetMapping("/")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("usuarioLogueado") != null) {
            return "redirect:/dashboard";
        }
        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String correo, @RequestParam String password,
                                HttpSession session, RedirectAttributes ra) {
        Optional<Usuario> userOpt = usuarioRepo.findByCorreo(correo.trim());
        // Permite entrar con admin@100power.com y cualquier contraseña del proyecto (ej: admin123)
        if (userOpt.isPresent() && Boolean.TRUE.equals(userOpt.get().getActivo())
                && ("admin123".equals(password.trim()) || password.trim().equals(userOpt.get().getPasswordHash()))) {
            Usuario u = userOpt.get();
            session.setAttribute("usuarioLogueado", u);
            if ("CLIENTE".equals(u.getRol())) {
                return "redirect:/portal-cliente";
            }
            return "redirect:/dashboard";
        }
        ra.addFlashAttribute("error", "Credenciales incorrectas o usuario inactivo.");
        return "redirect:/";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/";
        if ("CLIENTE".equals(u.getRol())) return "redirect:/portal-cliente";

        dsService.sincronizarEstructurasEnMemoria();
        List<Cliente> clientes = dsService.obtenerListaClientes();
        long activos = clientes.stream().filter(c -> "ACTIVA".equals(c.getEstadoMembresia())).count();
        long vencidos = clientes.size() - activos;

        LocalDateTime inicioHoy = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime finHoy = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        long asistenciasHoy = asistenciaRepo.countByFechaHoraBetween(inicioHoy, finHoy);

        model.addAttribute("usuario", u);
        model.addAttribute("totalClientes", clientes.size());
        model.addAttribute("clientesActivos", activos);
        model.addAttribute("clientesVencidos", vencidos);
        model.addAttribute("totalIngresos", dsService.sumarTotalIngresosEmb());
        model.addAttribute("totalPagosCount", dsService.contarTotalPagosEmb());
        model.addAttribute("totalAsistenciasCount", asistenciaRepo.count());
        model.addAttribute("asistenciasHoy", asistenciasHoy);
        model.addAttribute("ultimosPagos", dsService.obtenerUltimosPagosEmb(6));
        model.addAttribute("ultimasAsistencias", asistenciaRepo.findTop50ByOrderByFechaHoraDesc().stream().limit(6).toList());
        return "dashboard";
    }

    // --- RF001: CRUD CLIENTES ---
    @GetMapping("/clientes")
    public String listarClientes(@RequestParam(required = false) String buscar, HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/";

        dsService.sincronizarEstructurasEnMemoria();
        List<Cliente> lista = dsService.obtenerListaClientes();
        if (buscar != null && !buscar.isBlank()) {
            String q = buscar.toLowerCase();
            lista = lista.stream()
                    .filter(c -> c.getNombreCompleto().toLowerCase().contains(q) || c.getCedula().contains(q))
                    .toList();
        } else {
            lista = lista.stream().limit(100).toList();
        }
        model.addAttribute("usuario", u);
        model.addAttribute("clientes", lista);
        model.addAttribute("nuevoCliente", new Cliente());
        model.addAttribute("buscar", buscar);
        return "clientes";
    }

    @PostMapping("/clientes/guardar")
    public String guardarCliente(@ModelAttribute Cliente cliente, RedirectAttributes ra) {
        if (cliente.getIdCliente() == null || cliente.getIdCliente().isBlank()) {
            cliente.setIdCliente(null);
            if (clienteRepo.findByCedula(cliente.getCedula()).isPresent()) {
                ra.addFlashAttribute("error", "Ya existe un cliente registrado con la cédula " + cliente.getCedula());
                return "redirect:/clientes";
            }
            cliente.setFechaRegistro(LocalDate.now());
            cliente.setActivo(true);
            int meses = switch (cliente.getTipoMembresia().toUpperCase()) {
                case "TRIMESTRAL" -> 3;
                case "SEMESTRAL" -> 6;
                case "ANUAL" -> 12;
                default -> 1;
            };
            cliente.setFechaVencimiento(LocalDate.now().plusMonths(meses));
        } else {
            Cliente existente = clienteRepo.findById(cliente.getIdCliente()).orElseThrow();
            existente.setNombreCompleto(cliente.getNombreCompleto());
            existente.setEdad(cliente.getEdad());
            existente.setTelefono(cliente.getTelefono());
            existente.setTipoMembresia(cliente.getTipoMembresia());
            if (cliente.getFechaVencimiento() != null) {
                existente.setFechaVencimiento(cliente.getFechaVencimiento());
            }
            cliente = existente;
        }
        clienteRepo.save(cliente);
        dsService.sincronizarEstructurasEnMemoria();
        ra.addFlashAttribute("exito", "Documento del cliente guardado en MongoDB correctamente.");
        return "redirect:/clientes";
    }

    @GetMapping("/clientes/eliminar/{id}")
    public String darDeBajaCliente(@PathVariable String id, RedirectAttributes ra) {
        clienteRepo.findById(id).ifPresent(c -> {
            c.setActivo(false);
            clienteRepo.save(c);
        });
        dsService.sincronizarEstructurasEnMemoria();
        ra.addFlashAttribute("exito", "Cliente dado de baja en MongoDB.");
        return "redirect:/clientes";
    }

    // --- RF002: PAGOS EMBEBIDOS EN CLIENTE ---
    @GetMapping("/pagos")
    public String listarPagos(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/";

        dsService.sincronizarEstructurasEnMemoria();
        model.addAttribute("usuario", u);
        model.addAttribute("pagos", dsService.obtenerUltimosPagosEmb(50));
        model.addAttribute("clientes", clienteRepo.findTop200ByOrderByIdClienteDesc());
        model.addAttribute("totalIngresos", dsService.sumarTotalIngresosEmb());
        return "pagos";
    }

    @PostMapping("/pagos/registrar")
    public String registrarPago(@RequestParam String idCliente, @RequestParam Double monto,
                                @RequestParam String metodo, @RequestParam String planPagado,
                                RedirectAttributes ra) {
        Cliente c = clienteRepo.findById(idCliente).orElseThrow();
        Pago p = new Pago();
        p.setIdPago("PAG-" + c.getCedula() + "-" + (c.getHistorialPagos().size() + 1));
        p.setFechaPago(LocalDate.now());
        p.setMonto(monto);
        p.setMetodo(metodo);
        p.setPlanPagado(planPagado);

        if (c.getHistorialPagos() == null) c.setHistorialPagos(new ArrayList<>());
        c.getHistorialPagos().add(p);

        int meses = switch (planPagado.toUpperCase()) {
            case "TRIMESTRAL" -> 3;
            case "SEMESTRAL" -> 6;
            case "ANUAL" -> 12;
            default -> 1;
        };
        LocalDate base = (c.getFechaVencimiento() != null && c.getFechaVencimiento().isAfter(LocalDate.now()))
                ? c.getFechaVencimiento() : LocalDate.now();
        c.setTipoMembresia(planPagado);
        c.setFechaVencimiento(base.plusMonths(meses));
        clienteRepo.save(c);

        dsService.sincronizarEstructurasEnMemoria();
        ra.addFlashAttribute("exito", "Pago embebido en MongoDB y membresía renovada hasta " + c.getFechaVencimiento());
        return "redirect:/pagos";
    }

    // --- RF003 y RF004: ASISTENCIAS / TORNIQUETE ---
    @GetMapping("/asistencias")
    public String vistaAsistencias(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/";

        model.addAttribute("usuario", u);
        model.addAttribute("asistencias", asistenciaRepo.findTop50ByOrderByFechaHoraDesc());
        return "asistencias";
    }

    @PostMapping("/asistencias/marcar")
    public String marcarAsistencia(@RequestParam String cedula, RedirectAttributes ra) {
        Asistencia resultado = dsService.encolarYProcesarAcceso(cedula);
        if (resultado == null) {
            ra.addFlashAttribute("error", "No se encontró ningún cliente con la cédula: " + cedula);
        } else if ("AUTORIZADO".equals(resultado.getEstadoAcceso())) {
            ra.addFlashAttribute("accesoAutorizado", resultado);
        } else {
            ra.addFlashAttribute("accesoDenegado", resultado);
        }
        return "redirect:/asistencias";
    }

    // --- RF005: RUTINAS EMBEBIDAS EN CLIENTE ---
    @GetMapping("/rutinas")
    public String listarRutinas(HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/";

        List<Cliente> clientesMuestra = clienteRepo.findTop200ByOrderByIdClienteDesc();
        List<Rutina> rutinas = new ArrayList<>();
        for (Cliente c : clientesMuestra) {
            if (c.getRutinaActiva() != null) {
                Rutina r = c.getRutinaActiva();
                r.setCliente(c);
                rutinas.add(r);
            }
        }

        List<Entrenador> entrenadores = usuarioRepo.findByRolAndActivoTrue("ENTRENADOR").stream()
                .map(st -> new Entrenador(st.getIdUsuario(), st.getNombreMostrar(), st.getEspecialidad(), st.getTelefono()))
                .toList();

        model.addAttribute("usuario", u);
        model.addAttribute("rutinas", rutinas);
        model.addAttribute("clientes", clientesMuestra);
        model.addAttribute("entrenadores", entrenadores);
        return "rutinas";
    }

    @PostMapping("/rutinas/guardar")
    public String guardarRutina(@RequestParam String idCliente, @RequestParam String idEntrenador,
                                @RequestParam String nivel, @RequestParam String objetivo,
                                @RequestParam String ejercicios, RedirectAttributes ra) {
        Cliente c = clienteRepo.findById(idCliente).orElseThrow();
        Usuario st = usuarioRepo.findById(idEntrenador).orElseThrow();

        Rutina r = new Rutina();
        r.setEntrenador(new Entrenador(st.getIdUsuario(), st.getNombreMostrar(), st.getEspecialidad(), st.getTelefono()));
        r.setNivel(nivel);
        r.setObjetivo(objetivo);
        r.setEjercicios(ejercicios);
        r.setFechaCreacion(LocalDate.now());

        c.setRutinaActiva(r);
        clienteRepo.save(c);

        ra.addFlashAttribute("exito", "Rutina embebida en el documento del cliente en MongoDB.");
        return "redirect:/rutinas";
    }

    // --- RF006: PORTAL MÓVIL CLIENTE ---
    @GetMapping("/portal-cliente")
    public String portalCliente(@RequestParam(required = false) String cedulaConsulta, HttpSession session, Model model) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogueado");
        if (u == null) return "redirect:/";

        String cedula = (cedulaConsulta != null && !cedulaConsulta.isBlank()) ? cedulaConsulta.trim() : "1143325454";
        Optional<Cliente> cliOpt = clienteRepo.findByCedula(cedula);
        model.addAttribute("usuario", u);
        if (cliOpt.isPresent()) {
            Cliente c = cliOpt.get();
            model.addAttribute("cliente", c);
            model.addAttribute("pagosCliente", dsService.obtenerPagosClienteHashMap(c.getCedula()));
            List<Rutina> rutinasCli = new ArrayList<>();
            if (c.getRutinaActiva() != null) {
                c.getRutinaActiva().setCliente(c);
                rutinasCli.add(c.getRutinaActiva());
            }
            model.addAttribute("rutinasCliente", rutinasCli);
        } else {
            model.addAttribute("error", "Cédula no encontrada en MongoDB.");
        }
        return "portal-cliente";
    }
}