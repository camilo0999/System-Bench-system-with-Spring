package com.sistema.banco.service;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sistema.banco.models.Cliente;
import com.sistema.banco.models.Compras;
import com.sistema.banco.models.Servicio;
import com.sistema.banco.repository.ComprasRepository;

@Service
public class ComprasServiceImp implements ComprasService {

    private static Logger logger = LoggerFactory.getLogger(ComprasServiceImp.class);

    private final ComprasRepository comprasRepository;

    public ComprasServiceImp(ComprasRepository comprasRepository) {
        this.comprasRepository = comprasRepository;
    }

    @Override
    public void guardarCompras(Cliente cliente, Servicio servicio, String detalle) {

        try {

            Date fechaActual = new Date();

            Compras compras = new Compras();

            Set<Servicio> servicios = new HashSet<>();

            servicios.add(servicio);

            compras.setServicios(servicios);

            compras.setClienteNombre(cliente.getNombre() + " " + cliente.getApellido());

            compras.setFecha(fechaActual);

            compras.setDetalle(
                    "SE REALIZO LA COMPRA DEL SERVICIO " + servicio.getNombre() + " EL CUAL PERTENECE A LA CATEGORIA "
                            + servicio.getCategoira() + ", SERVICIO BENEFICIARIO PARA " + detalle);

            comprasRepository.save(compras);

            logger.info("SE GUARDO CORRECTAMENTE LA COMPRA ");

        } catch (Exception e) {

            logger.error("OCURRIO UN ERROR AL GUARDAR LA COMPRA: ", e);

        }

    }

}