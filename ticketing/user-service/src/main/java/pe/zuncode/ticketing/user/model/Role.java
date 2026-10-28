package pe.zuncode.ticketing.user.model;

/**
 * Roles del sistema. Viajan en el claim "roles" del JWT y cada servicio decide qué permite a cada uno.
 */
public enum Role {
    /** Compra entradas: crea, paga y cancela SUS reservas */
    CUSTOMER,
    /** Organiza eventos: crea recintos, eventos y zonas, y los publica */
    ORGANIZER,
    /** Administra la plataforma: todo lo anterior, gestionar usuarios y eliminar eventos */
    ADMIN
}
