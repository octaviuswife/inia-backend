package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.NotificacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.services.NotificacionService;

@RestController
@RequestMapping("/api/notificaciones")
@CrossOrigin(origins = "*")
public class NotificacionController {

    @Autowired
    private NotificacionService notificacionService;

    // Crear notificación manual
    @PostMapping
    public ResponseEntity<NotificacionDTO> crearNotificacion(@RequestBody NotificacionRequestDTO request) {
        NotificacionDTO notificacion = notificacionService.crearNotificacion(request);
        return ResponseEntity.ok(notificacion);
    }

    // Obtener notificaciones de un usuario con paginación
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Page<NotificacionDTO>> obtenerNotificacionesPorUsuario(
            @PathVariable Long usuarioId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesPorUsuario(usuarioId, pageable);
        return ResponseEntity.ok(notificaciones);
    }

    // Obtener notificaciones no leídas de un usuario
    @GetMapping("/usuario/{usuarioId}/no-leidas")
    public ResponseEntity<List<NotificacionDTO>> obtenerNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        List<NotificacionDTO> notificaciones = notificacionService.obtenerNotificacionesNoLeidas(usuarioId);
        return ResponseEntity.ok(notificaciones);
    }

    // Contar notificaciones no leídas
    @GetMapping("/usuario/{usuarioId}/contar-no-leidas")
    public ResponseEntity<Long> contarNotificacionesNoLeidas(@PathVariable Long usuarioId) {
        Long count = notificacionService.contarNotificacionesNoLeidas(usuarioId);
        return ResponseEntity.ok(count);
    }

    // Marcar notificación como leída
    @PutMapping("/{notificacionId}/marcar-leida")
    public ResponseEntity<NotificacionDTO> marcarComoLeida(@PathVariable Long notificacionId) {
        NotificacionDTO notificacion = notificacionService.marcarComoLeida(notificacionId);
        return ResponseEntity.ok(notificacion);
    }

    // Marcar todas las notificaciones de un usuario como leídas
    @PutMapping("/usuario/{usuarioId}/marcar-todas-leidas")
    public ResponseEntity<Void> marcarTodasComoLeidas(@PathVariable Long usuarioId) {
        notificacionService.marcarTodasComoLeidas(usuarioId);
        return ResponseEntity.ok().build();
    }

    // Eliminar notificación (marcar como inactiva)
    @DeleteMapping("/{notificacionId}")
    public ResponseEntity<Void> eliminarNotificacion(@PathVariable Long notificacionId) {
        notificacionService.eliminarNotificacion(notificacionId);
        return ResponseEntity.ok().build();
    }

    // Endpoints internos para generar notificaciones automáticas
    @PostMapping("/interno/nuevo-usuario/{usuarioId}")
    public ResponseEntity<Void> notificarNuevoUsuario(@PathVariable Long usuarioId) {
        notificacionService.notificarNuevoUsuario(usuarioId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/interno/analisis-finalizado/{analisisId}")
    public ResponseEntity<Void> notificarAnalisisFinalizado(@PathVariable Long analisisId) {
        notificacionService.notificarAnalisisFinalizado(analisisId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/interno/analisis-aprobado/{analisisId}")
    public ResponseEntity<Void> notificarAnalisisAprobado(@PathVariable Long analisisId) {
        notificacionService.notificarAnalisisAprobado(analisisId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/interno/analisis-repetir/{analisisId}")
    public ResponseEntity<Void> notificarAnalisisRepetir(@PathVariable Long analisisId) {
        notificacionService.notificarAnalisisRepetir(analisisId);
        return ResponseEntity.ok().build();
    }
}