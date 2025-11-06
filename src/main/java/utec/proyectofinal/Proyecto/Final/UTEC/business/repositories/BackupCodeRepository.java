package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.BackupCode;

import java.util.List;

/**
 * Repositorio para códigos de respaldo de 2FA
 */
@Repository
public interface BackupCodeRepository extends JpaRepository<BackupCode, Long> {

    /**
     * Encuentra todos los códigos de respaldo no usados de un usuario
     * 
     * @param usuarioId ID del usuario
     * @return Lista de códigos disponibles
     */
    List<BackupCode> findByUsuarioIdAndUsedFalse(Integer usuarioId);

    /**
     * Encuentra todos los códigos de un usuario (usados y no usados)
     * 
     * @param usuarioId ID del usuario
     * @return Lista de todos los códigos
     */
    List<BackupCode> findByUsuarioId(Integer usuarioId);

    /**
     * Cuenta cuántos códigos no usados tiene un usuario
     * 
     * @param usuarioId ID del usuario
     * @return Cantidad de códigos disponibles
     */
    long countByUsuarioIdAndUsedFalse(Integer usuarioId);

    /**
     * Elimina todos los códigos de un usuario (usados y no usados)
     * Útil al regenerar códigos o deshabilitar 2FA
     * 
     * @param usuarioId ID del usuario
     */
    @Modifying
    @Query("DELETE FROM BackupCode b WHERE b.usuarioId = :usuarioId")
    void deleteAllByUsuarioId(Integer usuarioId);

    /**
     * Elimina solo los códigos usados de un usuario
     * Útil para limpieza periódica
     * 
     * @param usuarioId ID del usuario
     */
    @Modifying
    @Query("DELETE FROM BackupCode b WHERE b.usuarioId = :usuarioId AND b.used = true")
    void deleteUsedByUsuarioId(Integer usuarioId);
}
