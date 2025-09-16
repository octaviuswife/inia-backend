package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Listado;

import java.util.List;

public interface ListadoRepository extends JpaRepository<Listado, Long> {
    List<Listado> findByPurezaAnalisisID(@Param("idPureza") Long idPureza);
}
