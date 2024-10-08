package com.sistema.banco.service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.sistema.banco.models.Cuenta;
import com.sistema.banco.models.Servicio;
import com.sistema.banco.models.Transaccion;
import com.sistema.banco.repository.TransaccionRepository;

import jakarta.persistence.EntityNotFoundException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class TransaccionServiceImp implements TransaccionService {

    private static Logger logger = LoggerFactory.getLogger(TransaccionServiceImp.class);

    private final TransaccionRepository transaccionRepository;
    private final ComprasService comprasService;

    public TransaccionServiceImp(TransaccionRepository transaccionRepository, @Lazy ComprasService comprasService) {
        this.transaccionRepository = transaccionRepository;
        this.comprasService = comprasService;
    }

    @Override
    public List<Transaccion> listaTransaccion() throws Exception {

        try {

            return transaccionRepository.findAll();

        } catch (Exception e) {

            logger.error("Error al cargar la lista de movimientos", e);

            return Collections.emptyList();
        }

    }

    @Override
    public Set<Transaccion> movimientosCuenta(Cuenta cuenta) throws Exception {

        return transaccionRepository.findAllByCuenta(cuenta);
    }

    @Override
    public byte[] exportPdf(Long id) throws JRException {

        try {

            Transaccion transaccion = transaccionRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Transacción no encontrada con id: " + id));

            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(
                    Collections.singletonList(transaccion));

            InputStream reportStream = getClass().getResourceAsStream("/reports/factura.jrxml");

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();

            parameters.put("Fecha",
                    transaccion.getFecha() != null
                            ? Date.from(transaccion.getFecha().atZone(ZoneId.systemDefault()).toInstant())
                            : null);
            parameters.put("Emisor",
                    transaccion.getEmisor() != null ? transaccion.getEmisor() : "Emisor no disponible");
            parameters.put("Transacion_id", transaccion.getId() != null ? transaccion.getId() : "ID no disponible");
            parameters.put("Monto", transaccion.getMonto() != null ? transaccion.getMonto() : "Monto no disponible");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

            return JasperExportManager.exportReportToPdf(jasperPrint);

        } catch (Exception e) {
            logger.error("Error al generar la factura: ", e);
        }

        throw new UnsupportedOperationException("Unimplemented method 'exportPdf'");
    }

    @Override
    public List<Transaccion> obtenerMovimientos(String tipo) throws Exception {

        return transaccionRepository.findAllByTipo(tipo);
    }

    @Override
    public void guardarRecarga(Cuenta cuenta, Double saldo, String emisor) throws Exception {
        try {

            LocalDateTime locaDate = LocalDateTime.now();

            Transaccion transaccion = new Transaccion();

            transaccion.setFecha(locaDate);

            transaccion.setMonto(saldo);

            transaccion.setEmisor(emisor);

            transaccion.setCuenta(cuenta);

            transaccion.setTipo("Recargas");

            transaccionRepository.save(transaccion);

        } catch (Exception e) {

            logger.error("Error en guardar transacion: ", e);
        }
    }

    @Override
    public void guardarCompra(Cuenta cuenta, Double saldo, Servicio servicio, String detalle)
            throws Exception {
        try {

            LocalDateTime locaDate = LocalDateTime.now();

            Transaccion transaccion = new Transaccion();

            transaccion.setFecha(locaDate);

            transaccion.setMonto(saldo);

            transaccion.setEmisor("COMPRA DEL SERRVICIO: " + servicio.getNombre());

            transaccion.setCuenta(cuenta);

            transaccion.setTipo("Compras");

            transaccionRepository.save(transaccion);

            comprasService.guardarCompras(cuenta.getCliente(), servicio, detalle);

        } catch (Exception e) {

            logger.error("Error en guardar transacion: ", e);
        }
    }

    @Override
    public void guardarTransacion(Cuenta cuenta, Double saldo, String emisor) throws Exception {
        try {
            LocalDateTime locaDate = LocalDateTime.now();
            Transaccion transaccion = new Transaccion();
            transaccion.setFecha(locaDate);
            transaccion.setMonto(saldo);
            transaccion.setEmisor(emisor);
            transaccion.setCuenta(cuenta);
            transaccion.setTipo("Envios");
            transaccionRepository.save(transaccion);
        } catch (Exception e) {
            logger.error("Error en guardar transacción: ", e);
            throw new Exception("Error al guardar la transacción de envío", e);
        }
    }

    @Override
    public void guardarEnvio(Cuenta cuenta, Double saldo, String receptor) throws Exception {
        try {
            LocalDateTime locaDate = LocalDateTime.now();
            Transaccion transaccion = new Transaccion();
            transaccion.setFecha(locaDate);
            transaccion.setMonto(saldo);
            transaccion.setEmisor(receptor);
            transaccion.setCuenta(cuenta);
            transaccion.setTipo("Recibidos");
            transaccionRepository.save(transaccion);
        } catch (Exception e) {
            logger.error("Error en guardar transacción: ", e);
            throw new Exception("Error al guardar la transacción recibida", e);
        }
    }

}
