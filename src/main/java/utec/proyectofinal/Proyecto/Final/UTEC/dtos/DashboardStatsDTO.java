package utec.proyectofinal.Proyecto.Final.UTEC.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDTO {
    private long lotesActivos;
    private long analisisPendientes;
    private long completadosHoy;
    private long analisisPorAprobar;
}
