package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad para trackear dispositivos de confianza
 * 
 * Permite que los usuarios no tengan que ingresar el código 2FA
 * en dispositivos que ya fueron verificados previamente.
 * 
 * SEGURIDAD:
 * - El fingerprint se genera en el cliente combinando:
 *   User-Agent, IP, idioma del navegador, zona horaria, etc.
 * - Se hashea en el servidor para evitar almacenar datos sensibles
 * - Cada dispositivo expira después de 30 días de inactividad
 */
@Entity
@Table(name = "trusted_devices", indexes = {
    @Index(name = "idx_user_device", columnList = "usuario_id,device_fingerprint_hash")
})
@Data
public class TrustedDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID del usuario al que pertenece este dispositivo
     */
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    /**
     * Hash del fingerprint del dispositivo
     * El fingerprint original nunca se almacena, solo su hash SHA-256
     */
    @Column(name = "device_fingerprint_hash", nullable = false, length = 64)
    private String deviceFingerprintHash;

    /**
     * Nombre descriptivo del dispositivo (opcional)
     * Ej: "Chrome en Windows", "Safari en iPhone"
     */
    @Column(name = "device_name", length = 100)
    private String deviceName;

    /**
     * User-Agent completo del navegador (para auditoría)
     */
    @Column(name = "user_agent", length = 500)
    private String userAgent;

    /**
     * IP desde donde se registró este dispositivo
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    /**
     * Fecha de creación (primera vez que se confió en este dispositivo)
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /**
     * Última vez que se usó este dispositivo
     * Se actualiza en cada login exitoso
     */
    @Column(name = "last_used_at", nullable = false)
    private LocalDateTime lastUsedAt;

    /**
     * Fecha de expiración del dispositivo de confianza
     * Por defecto: 60 días desde última vez usado
     */
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Si el dispositivo está activo o fue revocado manualmente
     */
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUsedAt = LocalDateTime.now();
        if (expiresAt == null) {
            expiresAt = LocalDateTime.now().plusDays(60);
        }
    }

    /**
     * Actualiza la fecha de último uso y extiende la expiración
     */
    public void updateLastUsed() {
        this.lastUsedAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusDays(60);
    }

    /**
     * Verifica si el dispositivo está expirado
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * Verifica si el dispositivo es válido (activo y no expirado)
     */
    public boolean isValid() {
        return active && !isExpired();
    }
}
