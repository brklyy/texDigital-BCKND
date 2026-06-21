package cl.texDigital.ms_envios.repository;

import cl.texDigital.ms_envios.model.Envio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {

    List<Envio> findByPedidoId(Long pedidoId);
    List<Envio> findByClienteId(Long clienteId);
    List<Envio> findByEstado(String estado);
}
