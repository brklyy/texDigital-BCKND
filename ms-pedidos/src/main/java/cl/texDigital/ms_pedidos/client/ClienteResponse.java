package cl.texDigital.ms_pedidos.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ClienteResponse {

    private Long id;
    private String nombre;
    private String apellido;
    private String email;
    private String estado;
}
