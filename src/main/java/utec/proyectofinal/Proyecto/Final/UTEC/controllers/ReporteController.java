package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.*;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ReporteService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    @GetMapping("/general")
    public ResponseEntity<ReporteGeneralDTO> obtenerReporteGeneral(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        ReporteGeneralDTO reporte = reporteService.obtenerReporteGeneral(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/germinacion")
    public ResponseEntity<ReporteGerminacionDTO> obtenerReporteGerminacion(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        ReporteGerminacionDTO reporte = reporteService.obtenerReporteGerminacion(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/pms")
    public ResponseEntity<ReportePMSDTO> obtenerReportePms(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        ReportePMSDTO reporte = reporteService.obtenerReportePms(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/pureza")
    public ResponseEntity<ReportePurezaDTO> obtenerReportePureza(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        ReportePurezaDTO reporte = reporteService.obtenerReportePureza(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/tetrazolio")
    public ResponseEntity<ReporteTetrazolioDTO> obtenerReporteTetrazolio(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        ReporteTetrazolioDTO reporte = reporteService.obtenerReporteTetrazolio(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/dosn")
    public ResponseEntity<ReportePurezaDTO> obtenerReporteDosn(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        ReportePurezaDTO reporte = reporteService.obtenerReporteDosn(fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }

    @GetMapping("/pureza/contaminantes/{especie}")
    public ResponseEntity<java.util.Map<String, Double>> obtenerContaminantesPureza(
            @PathVariable String especie,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        java.util.Map<String, Double> contaminantes = reporteService.obtenerContaminantesPorEspeciePureza(especie, fechaInicio, fechaFin);
        return ResponseEntity.ok(contaminantes);
    }

    @GetMapping("/dosn/contaminantes/{especie}")
    public ResponseEntity<java.util.Map<String, Double>> obtenerContaminantesDosn(
            @PathVariable String especie,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin
    ) {
        java.util.Map<String, Double> contaminantes = reporteService.obtenerContaminantesPorEspecieDosn(especie, fechaInicio, fechaFin);
        return ResponseEntity.ok(contaminantes);
    }
}
