package utec.proyectofinal.Proyecto.Final.UTEC.business.entities;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Entidad para códigos de respaldo (backup codes) de 2FA
 * 
 * Permite a los usuarios acceder a su cuenta si pierden su dispositivo 2FA.
 * 
 * SEGURIDAD:
 * - Cada código se puede usar UNA SOLA VEZ
 * - Los códigos se hashean con BCrypt antes de almacenar
 * - Formato: XXXX-XXXX-XXXX (12 caracteres alfanuméricos)
 * - Se generan 10 códigos al activar 2FA
 * - Se pueden regenerar (invalida códigos anteriores)
 */
@Entity
@Table(name = "backup_codes", indexes = {
    @Index(name = "idx_backup_codes_user_unused", columnList = "usuario_id,used")
})
@Data
public class BackupCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ID del usuario al que pertenece este código
     */
    @Column(name = "usuario_id", nullable = false)
    private Integer usuarioId;

    /**
     * Hash BCrypt del código de respaldo
     * El código original nunca se almacena, solo su hash
     */
    @Column(name = "code_hash", nullable = false, length = 60)
    private String codeHash;

    /**
     * Si el código ya fue utilizado
     * Los códigos son de un solo uso
     */
    @Column(name = "used", nullable = false)
    private Boolean used = false;

    /**
     * Fecha y hora en que se usó el código
     */
    @Column(name = "used_at")
    private LocalDateTime usedAt;

    /**
     * Fecha de creación del código
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (used == null) {
            used = false;
        }
    }

    /**
     * Marca el código como usado
     */
    public void markAsUsed() {
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Verifica si el código está disponible (no usado)
     */
    public boolean isAvailable() {
        return !used;
    }
}
