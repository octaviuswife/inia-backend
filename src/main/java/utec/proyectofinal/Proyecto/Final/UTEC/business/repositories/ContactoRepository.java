package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Contacto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactoRepository extends JpaRepository<Contacto, Long> {
    
    // Buscar contactos activos
    List<Contacto> findByActivoTrue();
    
    // Buscar contacto activo por ID
    Optional<Contacto> findByContactoIDAndActivoTrue(Long contactoID);
    
    // Buscar contactos activos por tipo
    List<Contacto> findByTipoAndActivoTrue(TipoContacto tipo);
    
    // Buscar contactos inactivos por tipo
    List<Contacto> findByTipoAndActivoFalse(TipoContacto tipo);
    
    // Buscar contactos por tipo (todos los estados)
    List<Contacto> findByTipo(TipoContacto tipo);
    
    // Buscar contactos por nombre conteniendo texto y tipo
    @Query("SELECT c FROM Contacto c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.tipo = :tipo AND c.activo = true")
    List<Contacto> findByNombreContainingIgnoreCaseAndTipoAndActivoTrue(@Param("nombre") String nombre, @Param("tipo") TipoContacto tipo);
    
    // Buscar contactos por nombre conteniendo texto (todos los tipos activos)
    @Query("SELECT c FROM Contacto c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) AND c.activo = true")
    List<Contacto> findByNombreContainingIgnoreCaseAndActivoTrue(@Param("nombre") String nombre);
    
    // Verificar si existe nombre para un tipo específico (excluyendo un ID)
    @Query("SELECT COUNT(c) > 0 FROM Contacto c WHERE LOWER(c.nombre) = LOWER(:nombre) AND c.tipo = :tipo AND c.contactoID != :contactoID")
    boolean existsByNombreIgnoreCaseAndTipoAndContactoIDNot(@Param("nombre") String nombre, @Param("tipo") TipoContacto tipo, @Param("contactoID") Long contactoID);
    
    // Verificar si existe nombre para un tipo específico
    @Query("SELECT COUNT(c) > 0 FROM Contacto c WHERE LOWER(c.nombre) = LOWER(:nombre) AND c.tipo = :tipo")
    boolean existsByNombreIgnoreCaseAndTipo(@Param("nombre") String nombre, @Param("tipo") TipoContacto tipo);
    
    // Buscar por nombre exacto, tipo y activo
    Optional<Contacto> findByNombreAndTipoAndActivoTrue(String nombre, TipoContacto tipo);
    
    // Métodos paginados para listado
    Page<Contacto> findByActivoTrueOrderByNombreAsc(Pageable pageable);
    
    Page<Contacto> findByActivoFalseOrderByNombreAsc(Pageable pageable);
    
    Page<Contacto> findAllByOrderByNombreAsc(Pageable pageable);
    
    Page<Contacto> findByTipoAndActivoTrueOrderByNombreAsc(TipoContacto tipo, Pageable pageable);
    
    Page<Contacto> findByTipoAndActivoFalseOrderByNombreAsc(TipoContacto tipo, Pageable pageable);
    
    Page<Contacto> findByTipoOrderByNombreAsc(TipoContacto tipo, Pageable pageable);
}