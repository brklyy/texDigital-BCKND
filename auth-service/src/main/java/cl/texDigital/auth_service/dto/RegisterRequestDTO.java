package cl.texDigital.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "El username no puede estar vacio")
    private String username;

    @NotBlank(message = "La contrasena no puede estar vacia")
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String password;

    @NotBlank(message = "El email no puede estar vacio")
    @Email(message = "El email no es valido")
    private String email;

    @NotBlank(message = "El rol no puede estar vacio")
    private String rol;
}
