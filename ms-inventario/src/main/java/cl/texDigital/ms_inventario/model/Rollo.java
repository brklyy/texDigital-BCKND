package cl.texDigital.ms_inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "rollo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rollo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "textil_id", nullable = false)
    private Textil textil;

    @Column(name = "metros_totales", nullable = false)
    private Double metrosTotales;

    @Column(name = "metros_restantes", nullable = false)
    private Double metrosRestantes;

    @Column(name = "metros_usados", nullable = false)
    private Double metrosUsados;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(nullable = false, length = 20)
    private String estado;
}
