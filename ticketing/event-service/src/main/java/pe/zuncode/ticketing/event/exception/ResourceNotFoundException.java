package pe.zuncode.ticketing.event.exception;

public class ResourceNotFoundException extends  RuntimeException{
    public ResourceNotFoundException (String resource, Long id) {
        super  (resource +" no encontrado: "+ id); }
}
