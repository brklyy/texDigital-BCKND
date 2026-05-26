package cl.texDigital.ms_pedidos.controller;

import cl.texDigital.ms_pedidos.dto.PedidoRequestDTO;
import cl.texDigital.ms_pedidos.dto.PedidoResponseDTO;
import cl.texDigital.ms_pedidos.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @GetMapping
    public ResponseEntity<List<PedidoResponseDTO>> getAll() {
        return ResponseEntity.ok(pedidoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pedidoService.findById(id));
    }

    @GetMapping("/cliente/{clienteId}")
    public ResponseEntity<List<PedidoResponseDTO>> getByClienteId(@PathVariable Long clienteId) {
        return ResponseEntity.ok(pedidoService.findByClienteId(clienteId));
    }

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> create(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoService.create(dto));
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<PedidoResponseDTO> updateEstado(@PathVariable Long id,
                                                          @RequestParam String estado) {
        return ResponseEntity.ok(pedidoService.updateEstado(id, estado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        pedidoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
