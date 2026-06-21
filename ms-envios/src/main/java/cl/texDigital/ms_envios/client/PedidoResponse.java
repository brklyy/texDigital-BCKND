package cl.texDigital.ms_envios.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PedidoResponse {
    private Long id;
    private String estado;
}
