package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.TrustedDevice;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TrustedDeviceRepository extends JpaRepository<TrustedDevice, Long> {

    /**
     * Busca un dispositivo de confianza por usuario y fingerprint hash
     */
    Optional<TrustedDevice> findByUsuarioIdAndDeviceFingerprintHashAndActiveTrue(
        Integer usuarioId, 
        String deviceFingerprintHash
    );

    /**
     * Lista todos los dispositivos de confianza activos de un usuario
     */
    List<TrustedDevice> findByUsuarioIdAndActiveTrueOrderByLastUsedAtDesc(Integer usuarioId);

    /**
     * Cuenta cuántos dispositivos activos tiene un usuario
     */
    long countByUsuarioIdAndActiveTrue(Integer usuarioId);

    /**
     * Elimina dispositivos expirados (tarea de limpieza programada)
     */
    @Modifying
    @Query("UPDATE TrustedDevice t SET t.active = false WHERE t.expiresAt < :now AND t.active = true")
    int deactivateExpiredDevices(LocalDateTime now);

    /**
     * Desactiva todos los dispositivos de un usuario (útil al deshabilitar 2FA)
     */
    @Modifying
    @Query("UPDATE TrustedDevice t SET t.active = false WHERE t.usuarioId = :usuarioId AND t.active = true")
    int deactivateAllUserDevices(Integer usuarioId);
}
