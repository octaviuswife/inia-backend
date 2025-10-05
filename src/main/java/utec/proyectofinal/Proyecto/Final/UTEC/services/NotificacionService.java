package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.AnalisisHistorial;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Notificacion;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Usuario;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisHistorialRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.AnalisisRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.NotificacionRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.UsuarioRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.NotificacionRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.NotificacionDTO;

@Service
@Transactional
public class NotificacionService {

    @Autowired
    private NotificacionRepository notificacionRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AnalisisRepository analisisRepository;

    @Autowired
    private AnalisisHistorialRepository analisisHistorialRepository;

    // Crear notificación manual
    public NotificacionDTO crearNotificacion(NotificacionRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(request.getUsuarioId().intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Notificacion notificacion = new Notificacion();
        notificacion.setNombre(request.getNombre());
        notificacion.setMensaje(request.getMensaje());
        notificacion.setUsuario(usuario);
        notificacion.setAnalisisId(request.getAnalisisId());
        notificacion.setTipo(request.getTipo());

        notificacion = notificacionRepository.save(notificacion);
        return convertToDTO(notificacion);
    }

    // Notificación cuando se registra un nuevo usuario
    public void notificarNuevoUsuario(Long usuarioId) {
        // Buscar todos los administradores
        List<Usuario> administradores = usuarioRepository.findByEstado(utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario.ACTIVO)
                .stream()
                .filter(u -> u.getRol() == utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .collect(Collectors.toList());
        
        Usuario usuarioNuevo = usuarioRepository.findById(usuarioId.intValue())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        for (Usuario admin : administradores) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("Nuevo usuario registrado");
            notificacion.setMensaje("El usuario " + usuarioNuevo.getNombres() + " " + usuarioNuevo.getApellidos() + 
                                  " se ha registrado y requiere aprobación.");
            notificacion.setUsuario(admin);
            notificacion.setTipo(utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion.USUARIO_REGISTRO);
            
            notificacionRepository.save(notificacion);
        }
    }

    // Notificación cuando se finaliza un análisis
    public void notificarAnalisisFinalizado(Long analisisId) {
        // Buscar todos los administradores
        List<Usuario> administradores = usuarioRepository.findByEstado(utec.proyectofinal.Proyecto.Final.UTEC.enums.EstadoUsuario.ACTIVO)
                .stream()
                .filter(u -> u.getRol() == utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .collect(Collectors.toList());
        
        Analisis analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado"));

        for (Usuario admin : administradores) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("Análisis finalizado");
            notificacion.setMensaje("El análisis ID " + analisisId + " del lote " + analisis.getLote().getFicha() + 
                                  " ha sido finalizado y requiere aprobación.");
            notificacion.setUsuario(admin);
            notificacion.setAnalisisId(analisisId);
            notificacion.setTipo(utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion.ANALISIS_FINALIZADO);
            
            notificacionRepository.save(notificacion);
        }
    }

    // Notificación cuando se aprueba un análisis
    public void notificarAnalisisAprobado(Long analisisId) {
        // Buscar usuarios que modificaron el análisis (excluyendo administradores)
        List<AnalisisHistorial> historial = analisisHistorialRepository.findByAnalisisIdOrderByFechaHoraDesc(analisisId);
        List<Usuario> usuariosInvolucrados = historial.stream()
                .map(AnalisisHistorial::getUsuario)
                .filter(usuario -> usuario.getRol() != utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .distinct()
                .collect(Collectors.toList());

        Analisis analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado"));

        for (Usuario usuario : usuariosInvolucrados) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("Análisis aprobado");
            notificacion.setMensaje("El análisis ID " + analisisId + " del lote " + analisis.getLote().getFicha() + 
                                  " ha sido aprobado por el administrador.");
            notificacion.setUsuario(usuario);
            notificacion.setAnalisisId(analisisId);
            notificacion.setTipo(utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion.ANALISIS_APROBADO);
            
            notificacionRepository.save(notificacion);
        }
    }

    // Notificación cuando se marca un análisis para repetir
    public void notificarAnalisisRepetir(Long analisisId) {
        // Buscar usuarios que modificaron el análisis (excluyendo administradores)
        List<AnalisisHistorial> historial = analisisHistorialRepository.findByAnalisisIdOrderByFechaHoraDesc(analisisId);
        List<Usuario> usuariosInvolucrados = historial.stream()
                .map(AnalisisHistorial::getUsuario)
                .filter(usuario -> usuario.getRol() != utec.proyectofinal.Proyecto.Final.UTEC.enums.Rol.ADMIN)
                .distinct()
                .collect(Collectors.toList());

        Analisis analisis = analisisRepository.findById(analisisId)
                .orElseThrow(() -> new RuntimeException("Análisis no encontrado"));

        for (Usuario usuario : usuariosInvolucrados) {
            Notificacion notificacion = new Notificacion();
            notificacion.setNombre("Análisis marcado para repetir");
            notificacion.setMensaje("El análisis ID " + analisisId + " del lote " + analisis.getLote().getFicha() + 
                                  " ha sido marcado para repetir por el administrador.");
            notificacion.setUsuario(usuario);
            notificacion.setAnalisisId(analisisId);
            notificacion.setTipo(utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoNotificacion.ANALISIS_REPETIR);
            
            notificacionRepository.save(notificacion);
        }
    }

    // Obtener notificaciones de un usuario
    public Page<NotificacionDTO> obtenerNotificacionesPorUsuario(Long usuarioId, Pageable pageable) {
        Page<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdAndActivoTrueOrderByFechaCreacionDesc(usuarioId, pageable);
        return notificaciones.map(this::convertToDTO);
    }

    // Obtener notificaciones no leídas de un usuario
    public List<NotificacionDTO> obtenerNotificacionesNoLeidas(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdAndLeidoFalseAndActivoTrueOrderByFechaCreacionDesc(usuarioId);
        return notificaciones.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    // Marcar notificación como leída
    public NotificacionDTO marcarComoLeida(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        notificacion.setLeido(true);
        notificacion = notificacionRepository.save(notificacion);
        
        return convertToDTO(notificacion);
    }

    // Marcar todas las notificaciones de un usuario como leídas
    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> notificaciones = notificacionRepository.findByUsuarioIdAndLeidoFalseAndActivoTrue(usuarioId);
        notificaciones.forEach(n -> n.setLeido(true));
        notificacionRepository.saveAll(notificaciones);
    }

    // Eliminar notificación (marcar como inactiva)
    public void eliminarNotificacion(Long notificacionId) {
        Notificacion notificacion = notificacionRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        
        notificacion.setActivo(false);
        notificacionRepository.save(notificacion);
    }

    // Contar notificaciones no leídas
    public Long contarNotificacionesNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidoFalseAndActivoTrue(usuarioId);
    }

    private NotificacionDTO convertToDTO(Notificacion notificacion) {
        NotificacionDTO dto = new NotificacionDTO();
        dto.setId(notificacion.getId());
        dto.setNombre(notificacion.getNombre());
        dto.setMensaje(notificacion.getMensaje());
        dto.setLeido(notificacion.getLeido());
        dto.setActivo(notificacion.getActivo());
        dto.setFechaCreacion(notificacion.getFechaCreacion());
        dto.setUsuarioId(notificacion.getUsuario().getUsuarioID().longValue());
        dto.setUsuarioNombre(notificacion.getUsuario().getNombres() + " " + notificacion.getUsuario().getApellidos());
        dto.setAnalisisId(notificacion.getAnalisisId());
        dto.setTipo(notificacion.getTipo());
        return dto;
    }
}