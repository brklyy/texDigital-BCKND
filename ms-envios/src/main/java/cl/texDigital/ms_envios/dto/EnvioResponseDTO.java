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
    private Long clienteId;
    private String nombreCliente;
    private String apellidoCliente;
    private String emailCliente;
    private LocalDate fechaEnvio;
    private LocalDate fechaEstimadaEntrega;
    private String estado;
    private String transportista;
    private String numeroSeguimiento;
    private String direccionDestino;
}
