package pe.zuncode.ticketing.event.exception;

/**
 * Los datos tienen el formato correcto pero no cumplen una regla que necesita
 * consultar la base de datos (p. ej. el recinto no existe) → 400 Bad Request.
 * <p>
 * Las reglas de formato (obligatorio, positivo, fecha futura...) las valida Bean Validation.
 */
public class InvalidRequestException extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}
