package cl.texDigital.ms_clientes.service;

import cl.texDigital.ms_clientes.dto.ClienteRequestDTO;
import cl.texDigital.ms_clientes.dto.ClienteResponseDTO;
import cl.texDigital.ms_clientes.exception.ResourceNotFoundException;
import cl.texDigital.ms_clientes.model.Cliente;
import cl.texDigital.ms_clientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public List<ClienteResponseDTO> findAll() {
        log.debug("Obteniendo lista de todos los clientes");
        return clienteRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public ClienteResponseDTO findById(Long id) {
        log.debug("Buscando cliente con id={}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        return toResponseDTO(cliente);
    }

    public ClienteResponseDTO create(ClienteRequestDTO dto) {
        log.debug("Creando cliente con email={}", dto.getEmail());
        if (clienteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Ya existe un cliente con el email: " + dto.getEmail());
        }
        Cliente cliente = new Cliente();
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        cliente.setEstado("ACTIVO");
        Cliente guardado = clienteRepository.save(cliente);
        log.debug("Cliente creado con id={}", guardado.getId());
        return toResponseDTO(guardado);
    }

    public ClienteResponseDTO update(Long id, ClienteRequestDTO dto) {
        log.debug("Actualizando cliente con id={}", id);
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado con id: " + id));
        if (!cliente.getEmail().equals(dto.getEmail()) && clienteRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalStateException("Ya existe un cliente con el email: " + dto.getEmail());
        }
        cliente.setNombre(dto.getNombre());
        cliente.setApellido(dto.getApellido());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setDireccion(dto.getDireccion());
        return toResponseDTO(clienteRepository.save(cliente));
    }

    public void delete(Long id) {
        log.debug("Eliminando cliente con id={}", id);
        if (!clienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cliente no encontrado con id: " + id);
        }
        clienteRepository.deleteById(id);
        log.debug("Cliente eliminado con id={}", id);
    }

    private ClienteResponseDTO toResponseDTO(Cliente cliente) {
        return new ClienteResponseDTO(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getDireccion(),
                cliente.getEstado()
        );
    }
}
