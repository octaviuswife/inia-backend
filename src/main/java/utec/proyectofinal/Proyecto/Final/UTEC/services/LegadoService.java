package utec.proyectofinal.Proyecto.Final.UTEC.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Legado;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.LegadoRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LegadoSimpleDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.LoteDTO;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LegadoService {

    private final LegadoRepository legadoRepository;
    private final LoteService loteService;

    /**
     * Obtener todos los registros legados activos (versión simple)
     */
    @Transactional(readOnly = true)
    public List<LegadoSimpleDTO> obtenerTodosSimple() {
        return legadoRepository.findByActivoTrue().stream()
                .map(this::convertirASimpleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener un legado por ID con información completa
     */
    @Transactional(readOnly = true)
    public LegadoDTO obtenerPorId(Long id) {
        Legado legado = legadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Legado no encontrado con ID: " + id));
        
        return convertirADTO(legado);
    }

    /**
     * Obtener legados por archivo origen
     */
    @Transactional(readOnly = true)
    public List<LegadoSimpleDTO> obtenerPorArchivo(String archivoOrigen) {
        return legadoRepository.findByArchivoOrigen(archivoOrigen).stream()
                .map(this::convertirASimpleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener legados por ficha
     */
    @Transactional(readOnly = true)
    public List<LegadoSimpleDTO> obtenerPorFicha(String ficha) {
        return legadoRepository.findByFicha(ficha).stream()
                .map(this::convertirASimpleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Desactivar un registro legado
     */
    @Transactional
    public void desactivar(Long id) {
        Legado legado = legadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Legado no encontrado con ID: " + id));
        
        legado.setActivo(false);
        legadoRepository.save(legado);
    }

    /**
     * Convertir entidad a DTO simple
     */
    private LegadoSimpleDTO convertirASimpleDTO(Legado legado) {
        LegadoSimpleDTO dto = new LegadoSimpleDTO();
        dto.setLegadoID(legado.getLegadoID());
        dto.setNomLote(legado.getLote() != null ? legado.getLote().getNomLote() : null);
        dto.setFicha(legado.getLote() != null ? legado.getLote().getFicha() : null);
        dto.setCodDoc(legado.getCodDoc());
        dto.setNomDoc(legado.getNomDoc());
        dto.setFamilia(legado.getFamilia());
        dto.setActivo(legado.getActivo());
        return dto;
    }

    /**
     * Convertir entidad a DTO completo
     */
    private LegadoDTO convertirADTO(Legado legado) {
        LegadoDTO dto = new LegadoDTO();
        dto.setLegadoID(legado.getLegadoID());
        
        // Obtener información completa del lote
        if (legado.getLote() != null) {
            LoteDTO loteDTO = loteService.obtenerLotePorId(legado.getLote().getLoteID());
            dto.setLote(loteDTO);
        }
        
        // Datos del documento
        dto.setCodDoc(legado.getCodDoc());
        dto.setNomDoc(legado.getNomDoc());
        dto.setNroDoc(legado.getNroDoc());
        dto.setFechaDoc(legado.getFechaDoc());
        dto.setFamilia(legado.getFamilia());
        
        // Tipo de semilla y tratamiento
        dto.setTipoSemilla(legado.getTipoSemilla());
        dto.setTratada(legado.getTratada());
        dto.setTipoTratGerm(legado.getTipoTratGerm());
        
        // Precios y montos
        dto.setPrecioUnit(legado.getPrecioUnit());
        dto.setUnidad(legado.getUnidad());
        dto.setMoneda(legado.getMoneda());
        dto.setImporteMN(legado.getImporteMN());
        dto.setImporteMO(legado.getImporteMO());
        
        // Datos de germinación
        dto.setGermC(legado.getGermC());
        dto.setGermSC(legado.getGermSC());
        dto.setPeso1000(legado.getPeso1000());
        
        // Datos de pureza
        dto.setPura(legado.getPura());
        dto.setOc(legado.getOc());
        dto.setPorcOC(legado.getPorcOC());
        dto.setMaleza(legado.getMaleza());
        dto.setMalezaTol(legado.getMalezaTol());
        dto.setMatInerte(legado.getMatInerte());
        
        // Datos de pureza inicial
        dto.setPuraI(legado.getPuraI());
        dto.setOcI(legado.getOcI());
        dto.setMalezaI(legado.getMalezaI());
        dto.setMalezaTolI(legado.getMalezaTolI());
        dto.setMatInerteI(legado.getMatInerteI());
        
        // Otros datos
        dto.setPesoHEC(legado.getPesoHEC());
        dto.setNroTrans(legado.getNroTrans());
        dto.setCtaMov(legado.getCtaMov());
        dto.setCaCC(legado.getCaCC());
        dto.setFf(legado.getFf());
        dto.setTitular(legado.getTitular());
        dto.setCtaArt(legado.getCtaArt());
        dto.setProveedor(legado.getProveedor());
        dto.setDocAfect(legado.getDocAfect());
        dto.setNroAfect(legado.getNroAfect());
        dto.setStk(legado.getStk());
        dto.setReferencia(legado.getReferencia());
        
        // Fechas adicionales
        dto.setFechaSC_I(legado.getFechaSC_I());
        dto.setFechaC_I(legado.getFechaC_I());
        dto.setGermTotalSC_I(legado.getGermTotalSC_I());
        dto.setGermTotalC_I(legado.getGermTotalC_I());
        
        // Observaciones
        dto.setObsTrans(legado.getObsTrans());
        dto.setOtrasSemillasObser(legado.getOtrasSemillasObser());
        dto.setSemillaPura(legado.getSemillaPura());
        dto.setSemillaOtrosCultivos(legado.getSemillaOtrosCultivos());
        dto.setSemillaMalezas(legado.getSemillaMalezas());
        dto.setSemillaMalezasToleradas(legado.getSemillaMalezasToleradas());
        dto.setMateriaInerte(legado.getMateriaInerte());
        
        // Metadatos
        dto.setFechaImportacion(legado.getFechaImportacion());
        dto.setArchivoOrigen(legado.getArchivoOrigen());
        dto.setFilaExcel(legado.getFilaExcel());
        dto.setActivo(legado.getActivo());
        
        return dto;
    }
}
