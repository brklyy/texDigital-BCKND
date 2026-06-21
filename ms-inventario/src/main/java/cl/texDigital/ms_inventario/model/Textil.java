package cl.texDigital.ms_inventario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "textil")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Textil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(name = "ancho_cm", nullable = false)
    private Double anchoCm;

    @Column(length = 500)
    private String descripcion;
}
