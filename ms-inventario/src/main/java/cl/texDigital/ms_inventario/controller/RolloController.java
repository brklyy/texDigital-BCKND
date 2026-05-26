package cl.texDigital.ms_inventario.controller;

import cl.texDigital.ms_inventario.dto.RolloRequestDTO;
import cl.texDigital.ms_inventario.dto.RolloResponseDTO;
import cl.texDigital.ms_inventario.dto.UsarMetrosRequestDTO;
import cl.texDigital.ms_inventario.service.RolloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rollos")
@RequiredArgsConstructor
public class RolloController {

    private final RolloService rolloService;

    @GetMapping
    public ResponseEntity<List<RolloResponseDTO>> getAll() {
        return ResponseEntity.ok(rolloService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RolloResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rolloService.findById(id));
    }

    @PostMapping
    public ResponseEntity<RolloResponseDTO> create(@Valid @RequestBody RolloRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolloService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RolloResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody RolloRequestDTO dto) {
        return ResponseEntity.ok(rolloService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        rolloService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/usar")
    public ResponseEntity<RolloResponseDTO> usarMetros(@PathVariable Long id,
                                                        @Valid @RequestBody UsarMetrosRequestDTO dto) {
        return ResponseEntity.ok(rolloService.usarMetros(id, dto));
    }

    @GetMapping("/textil/{textilId}")
    public ResponseEntity<List<RolloResponseDTO>> getByTextilId(@PathVariable Long textilId) {
        return ResponseEntity.ok(rolloService.findByTextilId(textilId));
    }
}
