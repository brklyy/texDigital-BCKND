package cl.texDigital.ms_pedidos.client;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductoResponse {

    private Long id;
    private String nombre;
    private Double precioBase;
}
