package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasYCultivosCatalogo;

public interface CatalogoRepository extends JpaRepository<MalezasYCultivosCatalogo, Long> {
}
