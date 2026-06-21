package cl.texDigital.ms_envios.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EnvioResponseDTO {

    private Long id;
    private Long pedidoId;
    private String direccionEntrega;
    private String transportista;
    private String codigoSeguimiento;
    private String estado;
    private LocalDate fechaCreacion;
    private LocalDate fechaEstimadaEntrega;
    private LocalDate fechaEntregaReal;
}
