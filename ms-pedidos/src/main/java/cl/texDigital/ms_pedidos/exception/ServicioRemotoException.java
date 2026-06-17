package cl.texDigital.ms_pedidos.exception;

/**
 * Se lanza cuando falla la comunicacion con un microservicio remoto
 * (timeout, conexion rechazada o error 5xx del servicio consultado).
 */
public class ServicioRemotoException extends RuntimeException {

    public ServicioRemotoException(String message) {
        super(message);
    }
}
