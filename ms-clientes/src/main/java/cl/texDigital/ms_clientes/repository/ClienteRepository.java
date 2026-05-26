package cl.texDigital.ms_clientes.repository;

import cl.texDigital.ms_clientes.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    boolean existsByEmail(String email);
}
