package pe.zuncode.ticketing.event.exception;

public class InvalidRequestException extends RuntimeException{
    public InvalidRequestException (String message){
        super  (message);
    }
}
