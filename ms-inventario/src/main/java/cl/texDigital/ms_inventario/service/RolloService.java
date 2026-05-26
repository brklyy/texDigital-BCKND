package cl.texDigital.ms_inventario.service;

import cl.texDigital.ms_inventario.dto.RolloRequestDTO;
import cl.texDigital.ms_inventario.dto.RolloResponseDTO;
import cl.texDigital.ms_inventario.dto.UsarMetrosRequestDTO;
import cl.texDigital.ms_inventario.exception.MetrosInsuficientesException;
import cl.texDigital.ms_inventario.exception.ResourceNotFoundException;
import cl.texDigital.ms_inventario.model.Rollo;
import cl.texDigital.ms_inventario.model.Textil;
import cl.texDigital.ms_inventario.repository.RolloRepository;
import cl.texDigital.ms_inventario.repository.TextilRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RolloService {

    private final RolloRepository rolloRepository;
    private final TextilRepository textilRepository;

    public List<RolloResponseDTO> findAll() {
        log.debug("Consultando todos los rollos");
        return rolloRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public RolloResponseDTO findById(Long id) {
        log.debug("Buscando rollo con id={}", id);
        Rollo rollo = rolloRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Rollo no encontrado con id={}", id);
                    return new ResourceNotFoundException("Rollo no encontrado con id: " + id);
                });
        return toResponseDTO(rollo);
    }

    public RolloResponseDTO create(RolloRequestDTO dto) {
        log.info("Creando rollo para textilId={}, metrosTotales={}", dto.getTextilId(), dto.getMetrosTotales());
        Textil textil = textilRepository.findById(dto.getTextilId())
                .orElseThrow(() -> {
                    log.warn("Textil no encontrado al crear rollo, textilId={}", dto.getTextilId());
                    return new ResourceNotFoundException("Textil no encontrado con id: " + dto.getTextilId());
                });
        Rollo rollo = new Rollo();
        rollo.setTextil(textil);
        rollo.setMetrosTotales(dto.getMetrosTotales());
        rollo.setMetrosRestantes(dto.getMetrosTotales());
        rollo.setMetrosUsados(0.0);
        rollo.setFechaIngreso(dto.getFechaIngreso());
        rollo.setEstado("ACTIVO");
        Rollo saved = rolloRepository.save(rollo);
        log.info("Rollo creado con id={}", saved.getId());
        return toResponseDTO(saved);
    }

    public RolloResponseDTO update(Long id, RolloRequestDTO dto) {
        log.info("Actualizando rollo id={}", id);
        Rollo rollo = rolloRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Rollo no encontrado para actualizar, id={}", id);
                    return new ResourceNotFoundException("Rollo no encontrado con id: " + id);
                });
        Textil textil = textilRepository.findById(dto.getTextilId())
                .orElseThrow(() -> new ResourceNotFoundException("Textil no encontrado con id: " + dto.getTextilId()));
        rollo.setTextil(textil);
        rollo.setMetrosTotales(dto.getMetrosTotales());
        rollo.setFechaIngreso(dto.getFechaIngreso());
        Rollo saved = rolloRepository.save(rollo);
        log.info("Rollo actualizado id={}", saved.getId());
        return toResponseDTO(saved);
    }

    public void delete(Long id) {
        log.info("Eliminando rollo id={}", id);
        if (!rolloRepository.existsById(id)) {
            log.warn("Rollo no encontrado para eliminar, id={}", id);
            throw new ResourceNotFoundException("Rollo no encontrado con id: " + id);
        }
        rolloRepository.deleteById(id);
        log.info("Rollo eliminado id={}", id);
    }

    public RolloResponseDTO usarMetros(Long id, UsarMetrosRequestDTO dto) {
        log.info("Usando {} metros del rollo id={}", dto.getMetros(), id);
        Rollo rollo = rolloRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rollo no encontrado con id: " + id));
        if (rollo.getMetrosRestantes() < dto.getMetros()) {
            log.warn("Metros insuficientes en rollo id={}. Disponibles: {}, Solicitados: {}",
                    id, rollo.getMetrosRestantes(), dto.getMetros());
            throw new MetrosInsuficientesException(
                    String.format("Metros insuficientes. Disponibles: %.2f, Solicitados: %.2f",
                            rollo.getMetrosRestantes(), dto.getMetros()));
        }
        rollo.setMetrosRestantes(rollo.getMetrosRestantes() - dto.getMetros());
        rollo.setMetrosUsados(rollo.getMetrosUsados() + dto.getMetros());
        if (rollo.getMetrosRestantes() <= 0) {
            rollo.setEstado("AGOTADO");
            log.info("Rollo id={} marcado como AGOTADO", id);
        }
        Rollo saved = rolloRepository.save(rollo);
        log.info("Metros actualizados en rollo id={}. Restantes: {}", id, saved.getMetrosRestantes());
        return toResponseDTO(saved);
    }

    public List<RolloResponseDTO> findByTextilId(Long textilId) {
        log.debug("Buscando rollos por textilId={}", textilId);
        if (!textilRepository.existsById(textilId)) {
            throw new ResourceNotFoundException("Textil no encontrado con id: " + textilId);
        }
        return rolloRepository.findByTextilId(textilId).stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    private RolloResponseDTO toResponseDTO(Rollo rollo) {
        return new RolloResponseDTO(
                rollo.getId(),
                rollo.getTextil().getId(),
                rollo.getTextil().getNombre(),
                rollo.getMetrosTotales(),
                rollo.getMetrosRestantes(),
                rollo.getMetrosUsados(),
                rollo.getFechaIngreso(),
                rollo.getEstado()
        );
    }
}
