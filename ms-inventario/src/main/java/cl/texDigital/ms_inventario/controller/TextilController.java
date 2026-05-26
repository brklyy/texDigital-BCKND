package cl.texDigital.ms_inventario.controller;

import cl.texDigital.ms_inventario.dto.TextilRequestDTO;
import cl.texDigital.ms_inventario.dto.TextilResponseDTO;
import cl.texDigital.ms_inventario.service.TextilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/textiles")
@RequiredArgsConstructor
public class TextilController {

    private final TextilService textilService;

    @GetMapping
    public ResponseEntity<List<TextilResponseDTO>> getAll() {
        return ResponseEntity.ok(textilService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<TextilResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(textilService.findById(id));
    }

    @PostMapping
    public ResponseEntity<TextilResponseDTO> create(@Valid @RequestBody TextilRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(textilService.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TextilResponseDTO> update(@PathVariable Long id,
                                                     @Valid @RequestBody TextilRequestDTO dto) {
        return ResponseEntity.ok(textilService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        textilService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
