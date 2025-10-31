-- Script SQL para crear la tabla de suscripciones push
-- Ejecutar en PostgreSQL después de que la aplicación esté corriendo
-- JPA/Hibernate puede crear la tabla automáticamente con spring.jpa.hibernate.ddl-auto=update
-- Si quieres crearla manualmente:
CREATE TABLE IF NOT EXISTS push_subscriptions (
    id BIGSERIAL PRIMARY KEY,
    usuario_id INTEGER NOT NULL,
    endpoint TEXT NOT NULL UNIQUE,
    p256dh TEXT NOT NULL,
    auth TEXT NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL,
    user_agent VARCHAR(500),
    FOREIGN KEY (usuario_id) REFERENCES usuarios(usuario_id) ON DELETE CASCADE
);
-- Índices para mejorar el rendimiento
CREATE INDEX IF NOT EXISTS idx_push_subscriptions_usuario_id ON push_subscriptions(usuario_id);
CREATE INDEX IF NOT EXISTS idx_push_subscriptions_activo ON push_subscriptions(activo);
CREATE INDEX IF NOT EXISTS idx_push_subscriptions_endpoint ON push_subscriptions(endpoint);
-- Comentarios
COMMENT ON TABLE push_subscriptions IS 'Almacena las suscripciones push de los usuarios para notificaciones';
COMMENT ON COLUMN push_subscriptions.endpoint IS 'URL del endpoint push del navegador';
COMMENT ON COLUMN push_subscriptions.p256dh IS 'Clave pública del cliente para encriptación';
COMMENT ON COLUMN push_subscriptions.auth IS 'Secreto de autenticación del cliente';
COMMENT ON COLUMN push_subscriptions.user_agent IS 'Navegador y dispositivo del cliente';