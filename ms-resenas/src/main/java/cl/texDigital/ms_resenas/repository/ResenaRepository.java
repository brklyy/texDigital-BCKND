package cl.texDigital.ms_resenas.repository;

import cl.texDigital.ms_resenas.model.Resena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResenaRepository extends JpaRepository<Resena, Long> {

    List<Resena> findByProductoId(Long productoId);

    List<Resena> findByClienteId(Long clienteId);

    List<Resena> findByClienteIdAndProductoId(Long clienteId, Long productoId);

    @Query("SELECT AVG(r.puntaje) FROM Resena r WHERE r.productoId = :productoId")
    Double findPromedioPuntajeByProductoId(@Param("productoId") Long productoId);
}
