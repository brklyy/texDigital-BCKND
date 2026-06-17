package cl.texDigital.ms_pagos.repository;

import cl.texDigital.ms_pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {

    List<Pago> findByPedidoId(Long pedidoId);

    List<Pago> findByEstado(String estado);

    boolean existsByPedidoIdAndEstado(Long pedidoId, String estado);
}
