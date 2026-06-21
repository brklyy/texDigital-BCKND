package cl.texDigital.ms_inventario.repository;

import cl.texDigital.ms_inventario.model.Rollo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolloRepository extends JpaRepository<Rollo, Long> {

    List<Rollo> findByTextilId(Long textilId);

    boolean existsByTextilId(Long textilId);
}
