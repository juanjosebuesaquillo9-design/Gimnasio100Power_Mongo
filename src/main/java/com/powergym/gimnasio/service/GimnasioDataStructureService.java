package com.powergym.gimnasio.service;

import com.powergym.gimnasio.model.*;
import com.powergym.gimnasio.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class GimnasioDataStructureService {

    private final ClienteRepository clienteRepo;
    private final AsistenciaRepository asistenciaRepo;

    // 1. ESTRUCTURA LINEAL: Lista Dinamica (ArrayList)
    private ArrayList<Cliente> listaClientesCache = new ArrayList<>();

    // 2. ESTRUCTURA LINEAL FIFO: Cola de Accesos en Recepcion (Queue / LinkedList)
    private Queue<Asistencia> colaRecepcion = new LinkedList<>();

    // 3. ESTRUCTURA NO LINEAL / ASOCIATIVA: Diccionario de Pagos Embebidos por Cedula (HashMap)
    private HashMap<String, List<Pago>> mapaPagosPorCedula = new HashMap<>();

    public GimnasioDataStructureService(ClienteRepository clienteRepo, AsistenciaRepository asistenciaRepo) {
        this.clienteRepo = clienteRepo;
        this.asistenciaRepo = asistenciaRepo;
    }

    public void sincronizarEstructurasEnMemoria() {
        List<Cliente> todos = clienteRepo.findAll();
        listaClientesCache = new ArrayList<>(todos);
        mapaPagosPorCedula.clear();

        for (Cliente c : todos) {
            if (c.getHistorialPagos() != null) {
                for (Pago p : c.getHistorialPagos()) {
                    p.setCliente(c);
                }
                mapaPagosPorCedula.put(c.getCedula(), new ArrayList<>(c.getHistorialPagos()));
            }
        }
    }

    public ArrayList<Cliente> obtenerListaClientes() {
        if (listaClientesCache.isEmpty()) sincronizarEstructurasEnMemoria();
        return listaClientesCache;
    }

    public List<Pago> obtenerPagosClienteHashMap(String cedula) {
        if (mapaPagosPorCedula.isEmpty()) sincronizarEstructurasEnMemoria();
        return mapaPagosPorCedula.getOrDefault(cedula, new ArrayList<>());
    }

    public double sumarTotalIngresosEmb() {
        if (mapaPagosPorCedula.isEmpty()) sincronizarEstructurasEnMemoria();
        return mapaPagosPorCedula.values().stream()
                .flatMap(List::stream)
                .mapToDouble(p -> p.getMonto() != null ? p.getMonto() : 0.0)
                .sum();
    }

    public long contarTotalPagosEmb() {
        if (mapaPagosPorCedula.isEmpty()) sincronizarEstructurasEnMemoria();
        return mapaPagosPorCedula.values().stream().mapToLong(List::size).sum();
    }

    public List<Pago> obtenerUltimosPagosEmb(int limite) {
        if (mapaPagosPorCedula.isEmpty()) sincronizarEstructurasEnMemoria();
        return mapaPagosPorCedula.values().stream()
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Pago::getFechaPago, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limite)
                .toList();
    }

    public Asistencia encolarYProcesarAcceso(String cedula) {
        Optional<Cliente> opt = clienteRepo.findByCedula(cedula.trim());
        if (opt.isEmpty()) return null;

        Cliente cliente = opt.get();
        Asistencia registro = new Asistencia();
        registro.setCliente(cliente);
        registro.setFechaHora(LocalDateTime.now());
        registro.setTorniquete("Torniquete Principal #1");

        boolean vigente = cliente.getFechaVencimiento() != null && !cliente.getFechaVencimiento().isBefore(LocalDate.now());
        registro.setEstadoAcceso(vigente ? "AUTORIZADO" : "DENEGADO");

        colaRecepcion.offer(registro);
        Asistencia procesada = colaRecepcion.poll();
        return asistenciaRepo.save(procesada);
    }

    public int getTamanoColaActual() {
        return colaRecepcion.size();
    }
}