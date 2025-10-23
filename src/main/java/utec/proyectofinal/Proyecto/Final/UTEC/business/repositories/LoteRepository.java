package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    
    List<Lote> findByActivoTrue();
    
    List<Lote> findByActivoFalse();
    
    List<Lote> findByActivo(Boolean activo);
    
    @Query("SELECT l FROM Lote l WHERE l.activo = :activo")
    List<Lote> findLotesByActivo(Boolean activo);

    // Pageable for listing
    Page<Lote> findByActivo(Boolean activo, Pageable pageable);
    
    // Buscar por ficha
    Optional<Lote> findByFicha(String ficha);
    
    // Contar lotes activos
    @Query("SELECT COUNT(l) FROM Lote l WHERE l.activo = true")
    long countLotesActivos();
    
    // Contar lotes inactivos
    @Query("SELECT COUNT(l) FROM Lote l WHERE l.activo = false")
    long countLotesInactivos();
}