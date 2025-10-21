package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPendienteDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.AnalisisPorAprobarDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.DashboardStatsDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.DashboardService;

// CORS configurado globalmente en WebSecurityConfig
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> obtenerEstadisticas() {
        try {
            DashboardStatsDTO stats = dashboardService.obtenerEstadisticas();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error al obtener estadísticas del dashboard: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @GetMapping("/analisis-pendientes")
    public ResponseEntity<List<AnalisisPendienteDTO>> obtenerAnalisisPendientes() {
        try {
            List<AnalisisPendienteDTO> pendientes = dashboardService.listarAnalisisPendientes();
            return ResponseEntity.ok(pendientes);
        } catch (Exception e) {
            System.err.println("Error al obtener análisis pendientes: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/analisis-por-aprobar")
    public ResponseEntity<List<AnalisisPorAprobarDTO>> obtenerAnalisisPorAprobar() {
        try {
            List<AnalisisPorAprobarDTO> porAprobar = dashboardService.listarAnalisisPorAprobar();
            return ResponseEntity.ok(porAprobar);
        } catch (Exception e) {
            System.err.println("Error al obtener análisis por aprobar: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
