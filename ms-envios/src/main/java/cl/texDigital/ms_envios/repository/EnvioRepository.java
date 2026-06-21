package cl.texDigital.ms_envios.repository;

import cl.texDigital.ms_envios.model.Envio;
import cl.texDigital.ms_envios.model.EstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnvioRepository extends JpaRepository<Envio, Long> {
    List<Envio> findByPedidoId(Long pedidoId);
    List<Envio> findByEstado(EstadoEnvio estado);
    Optional<Envio> findByCodigoSeguimiento(String codigoSeguimiento);
    boolean existsByPedidoId(Long pedidoId);
}
