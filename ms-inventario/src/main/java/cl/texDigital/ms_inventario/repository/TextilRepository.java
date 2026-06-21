package cl.texDigital.ms_inventario.repository;

import cl.texDigital.ms_inventario.model.Textil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TextilRepository extends JpaRepository<Textil, Long> {

    boolean existsByNombre(String nombre);
}
