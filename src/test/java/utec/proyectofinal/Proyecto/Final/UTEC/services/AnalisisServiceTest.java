package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Analisis;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Germinacion;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.Estado;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de AnalisisService")
class AnalisisServiceTest {

    @Mock
    private AnalisisHistorialService analisisHistorialService;

    @Mock
    private NotificacionService notificacionService;

    @InjectMocks
    private AnalisisService analisisService;

    private Analisis analisis;

    @BeforeEach
    void setUp() {
        analisis = new Germinacion();
        analisis.setAnalisisID(1L);
        analisis.setEstado(Estado.EN_PROCESO);
        analisis.setActivo(true);
    }

    private void setRolAnalista(boolean esAnalista) {
        SecurityContext context = mock(SecurityContext.class);
        Authentication auth = mock(Authentication.class);
        lenient().when(context.getAuthentication()).thenReturn(auth);
        // Usar wildcard para que coincida con la firma de Mockito (Collection<? extends GrantedAuthority>)
        Collection<? extends GrantedAuthority> autoridades = esAnalista
            ? java.util.Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANALISTA"))
            : java.util.Collections.emptyList();
        lenient().doReturn(autoridades).when(auth).getAuthorities();
        SecurityContextHolder.setContext(context);
    }

    @Test
    @DisplayName("Establecer fecha inicio - debe asignar fecha actual")
    void establecerFechaInicio_debeAsignarFechaActual() {
        analisisService.establecerFechaInicio(analisis);

        assertNotNull(analisis.getFechaInicio());
        assertTrue(analisis.getFechaInicio().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Finalizar análisis como analista - debe dejar PENDIENTE_APROBACION y setear fechaFin, con notificación en catch")
    void finalizarAnalisis_analista_pendienteAprobacion_yNotificacionEnCatch() {
        setRolAnalista(true);
        doNothing().when(analisisHistorialService).registrarModificacion(any());
        doThrow(new RuntimeException("error notif")).when(notificacionService).notificarAnalisisFinalizado(1L);

        Analisis res = analisisService.finalizarAnalisis(analisis);
        assertEquals(Estado.PENDIENTE_APROBACION, res.getEstado());
        assertNotNull(res.getFechaFin());
        verify(analisisHistorialService, times(1)).registrarModificacion(analisis);
    }

    @Test
    @DisplayName("Finalizar análisis como admin - debe aprobar directamente")
    void finalizarAnalisis_admin_aprobado() {
        setRolAnalista(false);
        doNothing().when(analisisHistorialService).registrarModificacion(any());

        Analisis res = analisisService.finalizarAnalisis(analisis);
        assertEquals(Estado.APROBADO, res.getEstado());
        assertNotNull(res.getFechaFin());
    }

    @Test
    @DisplayName("Finalizar análisis inactivo - debe lanzar excepción")
    void finalizarAnalisis_inactivo_lanzaExcepcion() {
        analisis.setActivo(false);
        assertThrows(RuntimeException.class, () -> analisisService.finalizarAnalisis(analisis));
    }

    @Test
    @DisplayName("Finalizar análisis ya finalizado o a repetir - debe lanzar excepción")
    void finalizarAnalisis_estadoInvalido_lanzaExcepcion() {
        analisis.setEstado(Estado.APROBADO);
        assertThrows(RuntimeException.class, () -> analisisService.finalizarAnalisis(analisis));
        analisis.setEstado(Estado.A_REPETIR);
        assertThrows(RuntimeException.class, () -> analisisService.finalizarAnalisis(analisis));
    }

    @Test
    @DisplayName("Aprobar análisis pendiente - debe cambiar a APROBADO")
    void aprobarAnalisis_pendiente_debeCambiarAAprobado() {
        analisis.setEstado(Estado.PENDIENTE_APROBACION);

        Analisis resultado = analisisService.aprobarAnalisis(analisis);

        assertEquals(Estado.APROBADO, resultado.getEstado());
        assertNotNull(resultado.getFechaFin());
        verify(analisisHistorialService, times(1)).registrarModificacion(analisis);
    }

    @Test
    @DisplayName("Aprobar análisis no pendiente - debe lanzar excepción")
    void aprobarAnalisis_noPendiente_debeLanzarExcepcion() {
        analisis.setEstado(Estado.EN_PROCESO);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analisisService.aprobarAnalisis(analisis);
        });

        assertTrue(exception.getMessage().contains("PENDIENTE_APROBACION"));
    }

    @Test
    @DisplayName("Aprobar análisis inactivo - debe lanzar excepción")
    void aprobarAnalisis_analisisInactivo_debeLanzarExcepcion() {
        analisis.setActivo(false);
        analisis.setEstado(Estado.PENDIENTE_APROBACION);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analisisService.aprobarAnalisis(analisis);
        });

        assertEquals("No se puede aprobar un análisis inactivo", exception.getMessage());
    }

    @Test
    @DisplayName("Manejar edición análisis finalizado - analista cambia a PENDIENTE_APROBACION")
    void manejarEdicionAnalisisFinalizado_analista_cambiaEstado() {
        setRolAnalista(true);
        analisis.setEstado(Estado.APROBADO);
        analisisService.manejarEdicionAnalisisFinalizado(analisis);
        assertEquals(Estado.PENDIENTE_APROBACION, analisis.getEstado());
        verify(analisisHistorialService, times(1)).registrarModificacion(analisis);
    }

    @Test
    @DisplayName("Manejar edición análisis finalizado - admin mantiene estado")
    void manejarEdicionAnalisisFinalizado_admin_noCambia() {
        setRolAnalista(false);
        analisis.setEstado(Estado.APROBADO);
        analisisService.manejarEdicionAnalisisFinalizado(analisis);
        assertEquals(Estado.APROBADO, analisis.getEstado());
        verify(analisisHistorialService, never()).registrarModificacion(any());
    }

    @Test
    @DisplayName("Manejar edición análisis no aprobado - no hace nada")
    void manejarEdicionAnalisisFinalizado_noAprobado_noHaceNada() {
        setRolAnalista(true);
        analisis.setEstado(Estado.EN_PROCESO);
        analisisService.manejarEdicionAnalisisFinalizado(analisis);
        verify(analisisHistorialService, never()).registrarModificacion(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("finalizarAnalisisGenerico - ejecuta validador, finaliza y mapea")
    void finalizarAnalisisGenerico_valida_yMapea() {
        setRolAnalista(false); // admin -> estado aprobado
        JpaRepository<Germinacion, Long> repo = mock(JpaRepository.class);
        Germinacion g = new Germinacion();
        g.setAnalisisID(10L);
        g.setEstado(Estado.EN_PROCESO);
        g.setActivo(true);
        when(repo.findById(10L)).thenReturn(Optional.of(g));
        when(repo.save(any(Germinacion.class))).thenAnswer(i -> i.getArgument(0));
        AtomicBoolean valido = new AtomicBoolean(false);
        Consumer<Germinacion> validator = a -> valido.set(true);
        Function<Germinacion, String> mapper = a -> "MAPPED-" + a.getEstado();

        String res = analisisService.finalizarAnalisisGenerico(10L, repo, mapper, validator);
        assertTrue(valido.get());
        assertEquals("MAPPED-" + Estado.APROBADO, res);
        verify(repo, times(1)).save(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("aprobarAnalisisGenerico - con otro análisis válido debe fallar")
    void aprobarAnalisisGenerico_otroValido_falla() {
        JpaRepository<Germinacion, Long> repo = mock(JpaRepository.class);
        Germinacion actual = new Germinacion();
        actual.setAnalisisID(20L);
        actual.setActivo(true);
        actual.setEstado(Estado.A_REPETIR);
        actual.setLote(new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote());
        actual.getLote().setLoteID(5L);
        actual.getLote().setFicha("F-5");
        when(repo.findById(20L)).thenReturn(Optional.of(actual));

        // Otro análisis válido (activo y estado != A_REPETIR)
        Germinacion otro = new Germinacion();
        otro.setAnalisisID(21L);
        otro.setActivo(true);
        otro.setEstado(Estado.EN_PROCESO);

        Function<Long, java.util.List<Germinacion>> buscarPorLote = id -> List.of(actual, otro);

        assertThrows(RuntimeException.class, () ->
            analisisService.aprobarAnalisisGenerico(20L, repo, a -> "DTO", a -> {}, buscarPorLote)
        );
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("aprobarAnalisisGenerico - sin otros válidos aprueba y guarda")
    void aprobarAnalisisGenerico_sinOtroValido_ok() {
        JpaRepository<Germinacion, Long> repo = mock(JpaRepository.class);
        Germinacion actual = new Germinacion();
        actual.setAnalisisID(22L);
        actual.setActivo(true);
        actual.setEstado(Estado.PENDIENTE_APROBACION);
        actual.setLote(new utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Lote());
        actual.getLote().setLoteID(6L);
        when(repo.findById(22L)).thenReturn(Optional.of(actual));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Function<Long, java.util.List<Germinacion>> buscarPorLote = id -> List.of(actual);
        String dto = analisisService.aprobarAnalisisGenerico(22L, repo, a -> "DTO-OK", a -> {}, buscarPorLote);
        assertEquals("DTO-OK", dto);
        verify(repo).save(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("marcarParaRepetirGenerico - cambia estado y guarda")
    void marcarParaRepetirGenerico_ok() {
        JpaRepository<Germinacion, Long> repo = mock(JpaRepository.class);
        Germinacion g = new Germinacion();
        g.setAnalisisID(30L);
        g.setActivo(true);
        g.setEstado(Estado.APROBADO);
        when(repo.findById(30L)).thenReturn(Optional.of(g));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        String dto = analisisService.marcarParaRepetirGenerico(30L, repo, a -> "DTO", a -> {});
        assertEquals("DTO", dto);
        assertEquals(Estado.A_REPETIR, g.getEstado());
        verify(analisisHistorialService, times(1)).registrarModificacion(g);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("desactivarAnalisis - pone activo=false y guarda")
    void desactivarAnalisis_ok() {
        JpaRepository<Germinacion, Long> repo = mock(JpaRepository.class);
        Germinacion g = new Germinacion();
        g.setAnalisisID(40L);
        g.setActivo(true);
        when(repo.findById(40L)).thenReturn(Optional.of(g));
        analisisService.desactivarAnalisis(40L, repo);
        assertFalse(g.getActivo());
        verify(repo).save(g);
    }

    @SuppressWarnings("unchecked")
    @Test
    @DisplayName("reactivarAnalisis - éxito y error si ya activo")
    void reactivarAnalisis_casos() {
        JpaRepository<Germinacion, Long> repo = mock(JpaRepository.class);
        Germinacion inactivo = new Germinacion();
        inactivo.setAnalisisID(50L);
        inactivo.setActivo(false);
        when(repo.findById(50L)).thenReturn(Optional.of(inactivo));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        String dto = analisisService.reactivarAnalisis(50L, repo, a -> "DTO");
        assertEquals("DTO", dto);
        assertTrue(inactivo.getActivo());

        Germinacion activo = new Germinacion();
        activo.setAnalisisID(51L);
        activo.setActivo(true);
        when(repo.findById(51L)).thenReturn(Optional.of(activo));
        assertThrows(RuntimeException.class, () -> analisisService.reactivarAnalisis(51L, repo, a -> "X"));
    }

    @Test
    @DisplayName("Marcar para repetir - debe cambiar a A_REPETIR")
    void marcarParaRepetir_debeCambiarARepetir() {
        analisis.setEstado(Estado.APROBADO);

        Analisis resultado = analisisService.marcarParaRepetir(analisis);

        assertEquals(Estado.A_REPETIR, resultado.getEstado());
        verify(analisisHistorialService, times(1)).registrarModificacion(analisis);
    }

    @Test
    @DisplayName("Marcar para repetir análisis inactivo - debe lanzar excepción")
    void marcarParaRepetir_analisisInactivo_debeLanzarExcepcion() {
        analisis.setActivo(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            analisisService.marcarParaRepetir(analisis);
        });

        assertEquals("No se puede marcar para repetir un análisis inactivo", exception.getMessage());
    }
}
