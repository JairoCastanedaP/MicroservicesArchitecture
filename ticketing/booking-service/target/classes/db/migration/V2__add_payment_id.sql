-- Sesión 3: la reserva guarda el id del pago aprobado en payment-service.
-- No es clave foránea: el pago vive en otra base de datos (payment_db).
-- Nueva migración V2 (nunca se modifica una migración ya aplicada como la V1).

ALTER TABLE bookings ADD COLUMN payment_id BIGINT;
