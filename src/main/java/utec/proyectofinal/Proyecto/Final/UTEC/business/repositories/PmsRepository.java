package utec.proyectofinal.Proyecto.Final.UTEC.business.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Pms;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.util.List;

public interface PmsRepository extends JpaRepository<Pms, Long> {
    List<Pms> findByEstadoNot(Estado estado);
    List<Pms> findByEstado(Estado estado);
}
