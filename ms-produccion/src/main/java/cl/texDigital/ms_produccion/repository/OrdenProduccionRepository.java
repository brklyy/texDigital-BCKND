package cl.texDigital.ms_produccion.repository;

import cl.texDigital.ms_produccion.model.OrdenProduccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrdenProduccionRepository extends JpaRepository<OrdenProduccion, Long> {

    List<OrdenProduccion> findByPedidoId(Long pedidoId);

    @Query("SELECT SUM(o.metrosUsados) FROM OrdenProduccion o")
    Double sumTotalMetrosUsados();
}
