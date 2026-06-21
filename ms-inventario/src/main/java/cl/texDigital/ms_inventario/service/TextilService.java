package cl.texDigital.ms_inventario.service;

import cl.texDigital.ms_inventario.dto.TextilRequestDTO;
import cl.texDigital.ms_inventario.dto.TextilResponseDTO;
import cl.texDigital.ms_inventario.exception.ResourceNotFoundException;
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
public class TextilService {

    private final TextilRepository textilRepository;
    private final RolloRepository rolloRepository;

    public List<TextilResponseDTO> findAll() {
        log.debug("Consultando todos los textiles");
        return textilRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    public TextilResponseDTO findById(Long id) {
        log.debug("Buscando textil con id={}", id);
        Textil textil = textilRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Textil no encontrado con id={}", id);
                    return new ResourceNotFoundException("Textil no encontrado con id: " + id);
                });
        return toResponseDTO(textil);
    }

    public TextilResponseDTO create(TextilRequestDTO dto) {
        log.info("Creando nuevo textil: nombre={}", dto.getNombre());
        if (textilRepository.existsByNombre(dto.getNombre())) {
            log.warn("Ya existe un textil con nombre={}", dto.getNombre());
            throw new IllegalStateException("Ya existe un textil con el nombre: " + dto.getNombre());
        }
        Textil textil = new Textil();
        textil.setNombre(dto.getNombre());
        textil.setAnchoCm(dto.getAnchoCm());
        textil.setDescripcion(dto.getDescripcion());
        Textil saved = textilRepository.save(textil);
        log.info("Textil creado con id={}", saved.getId());
        return toResponseDTO(saved);
    }

    public TextilResponseDTO update(Long id, TextilRequestDTO dto) {
        log.info("Actualizando textil id={}", id);
        Textil textil = textilRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Textil no encontrado para actualizar, id={}", id);
                    return new ResourceNotFoundException("Textil no encontrado con id: " + id);
                });
        textil.setNombre(dto.getNombre());
        textil.setAnchoCm(dto.getAnchoCm());
        textil.setDescripcion(dto.getDescripcion());
        Textil saved = textilRepository.save(textil);
        log.info("Textil actualizado id={}", saved.getId());
        return toResponseDTO(saved);
    }

    public void delete(Long id) {
        log.info("Eliminando textil id={}", id);
        if (!textilRepository.existsById(id)) {
            log.warn("Textil no encontrado para eliminar, id={}", id);
            throw new ResourceNotFoundException("Textil no encontrado con id: " + id);
        }
        if (rolloRepository.existsByTextilId(id)) {
            log.warn("No se puede eliminar textil id={} porque tiene rollos asociados", id);
            throw new IllegalStateException("No se puede eliminar el textil porque tiene rollos registrados");
        }
        textilRepository.deleteById(id);
        log.info("Textil eliminado id={}", id);
    }

    private TextilResponseDTO toResponseDTO(Textil textil) {
        return new TextilResponseDTO(
                textil.getId(),
                textil.getNombre(),
                textil.getAnchoCm(),
                textil.getDescripcion()
        );
    }
}
