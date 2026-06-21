package cl.texDigital.ms_envios.client;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class PedidoResponse {

    private Long id;
    private Long clienteId;
    private LocalDate fecha;
    private String estado;
    private Double total;
}
