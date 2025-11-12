package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.request.ContactoRequestDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.dtos.response.ContactoDTO;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Contacto;
import utec.proyectofinal.Proyecto.Final.UTEC.enums.TipoContacto;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ContactoRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ContactoService {

    @Autowired
    private ContactoRepository contactoRepository;

    // Obtener todos los contactos activos
    public List<ContactoDTO> obtenerTodosLosContactos() {
        return contactoRepository.findByActivoTrue()
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener contactos por tipo
    public List<ContactoDTO> obtenerContactosPorTipo(TipoContacto tipo) {
        return contactoRepository.findByTipoAndActivoTrue(tipo)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Obtener contactos por tipo con filtro de estado opcional
    public List<ContactoDTO> obtenerContactosPorTipo(TipoContacto tipo, Boolean activo) {
        if (activo == null) {
            // Devolver todos (activos e inactivos)
            return contactoRepository.findByTipo(tipo)
                    .stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else if (activo) {
            return contactoRepository.findByTipoAndActivoTrue(tipo)
                    .stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        } else {
            return contactoRepository.findByTipoAndActivoFalse(tipo)
                    .stream()
                    .map(this::mapearEntidadADTO)
                    .collect(Collectors.toList());
        }
    }

    // Obtener solo clientes activos
    public List<ContactoDTO> obtenerClientes() {
        return obtenerContactosPorTipo(TipoContacto.CLIENTE);
    }

    // Obtener clientes con filtro de estado opcional
    public List<ContactoDTO> obtenerClientes(Boolean activo) {
        return obtenerContactosPorTipo(TipoContacto.CLIENTE, activo);
    }

    // Obtener solo empresas activas
    public List<ContactoDTO> obtenerEmpresas() {
        return obtenerContactosPorTipo(TipoContacto.EMPRESA);
    }

    // Obtener empresas con filtro de estado opcional
    public List<ContactoDTO> obtenerEmpresas(Boolean activo) {
        return obtenerContactosPorTipo(TipoContacto.EMPRESA, activo);
    }

    // Obtener contacto por ID
    public ContactoDTO obtenerContactoPorId(Long contactoID) {
        Contacto contacto = contactoRepository.findByContactoIDAndActivoTrue(contactoID)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con ID: " + contactoID));
        return mapearEntidadADTO(contacto);
    }

    // Crear nuevo contacto
    public ContactoDTO crearContacto(ContactoRequestDTO contactoRequestDTO) {
        validarDatosContacto(contactoRequestDTO, null);
        
        Contacto contacto = mapearSolicitudAEntidad(contactoRequestDTO, new Contacto());
        contacto.setActivo(true);
        
        Contacto contactoGuardado = contactoRepository.save(contacto);
        return mapearEntidadADTO(contactoGuardado);
    }

    // Actualizar contacto existente
    public ContactoDTO actualizarContacto(Long contactoID, ContactoRequestDTO contactoRequestDTO) {
        Contacto contacto = contactoRepository.findByContactoIDAndActivoTrue(contactoID)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con ID: " + contactoID));
        
        validarDatosContacto(contactoRequestDTO, contactoID);
        
        mapearSolicitudAEntidad(contactoRequestDTO, contacto);
        
        Contacto contactoActualizado = contactoRepository.save(contacto);
        return mapearEntidadADTO(contactoActualizado);
    }

    // Eliminar contacto (soft delete)
    public void eliminarContacto(Long contactoID) {
        Contacto contacto = contactoRepository.findByContactoIDAndActivoTrue(contactoID)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con ID: " + contactoID));
        
        contacto.setActivo(false);
        contactoRepository.save(contacto);
    }

    // Reactivar contacto
    public ContactoDTO reactivarContacto(Long contactoID) {
        Contacto contacto = contactoRepository.findById(contactoID)
                .orElseThrow(() -> new RuntimeException("Contacto no encontrado con ID: " + contactoID));
        
        if (contacto.getActivo()) {
            throw new RuntimeException("El contacto ya está activo");
        }
        
        contacto.setActivo(true);
        Contacto contactoReactivado = contactoRepository.save(contacto);
        return mapearEntidadADTO(contactoReactivado);
    }

    // Buscar contactos por nombre
    public List<ContactoDTO> buscarContactosPorNombre(String nombre) {
        return contactoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(nombre)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Buscar contactos por nombre y tipo
    public List<ContactoDTO> buscarContactosPorNombreYTipo(String nombre, TipoContacto tipo) {
        return contactoRepository.findByNombreContainingIgnoreCaseAndTipoAndActivoTrue(nombre, tipo)
                .stream()
                .map(this::mapearEntidadADTO)
                .collect(Collectors.toList());
    }

    // Buscar clientes por nombre
    public List<ContactoDTO> buscarClientes(String nombre) {
        return buscarContactosPorNombreYTipo(nombre, TipoContacto.CLIENTE);
    }

    // Buscar empresas por nombre
    public List<ContactoDTO> buscarEmpresas(String nombre) {
        return buscarContactosPorNombreYTipo(nombre, TipoContacto.EMPRESA);
    }

    // Validar datos de contacto
    private void validarDatosContacto(ContactoRequestDTO contactoRequestDTO, Long contactoIDExcluir) {
        // Validar nombre requerido
        if (contactoRequestDTO.getNombre() == null || contactoRequestDTO.getNombre().trim().isEmpty()) {
            throw new RuntimeException("El nombre del contacto es requerido");
        }

        // Validar contacto requerido
        if (contactoRequestDTO.getContacto() == null || contactoRequestDTO.getContacto().trim().isEmpty()) {
            throw new RuntimeException("La información de contacto es requerida");
        }

        // Validar tipo requerido
        if (contactoRequestDTO.getTipo() == null) {
            throw new RuntimeException("El tipo de contacto es requerido");
        }

        // Validar unicidad del nombre para el tipo específico
        if (contactoIDExcluir != null) {
            if (contactoRepository.existsByNombreIgnoreCaseAndTipoAndContactoIDNot(
                    contactoRequestDTO.getNombre(), contactoRequestDTO.getTipo(), contactoIDExcluir)) {
                throw new RuntimeException("Ya existe un " + contactoRequestDTO.getTipo().name().toLowerCase() + " con ese nombre");
            }
        } else {
            if (contactoRepository.existsByNombreIgnoreCaseAndTipo(
                    contactoRequestDTO.getNombre(), contactoRequestDTO.getTipo())) {
                throw new RuntimeException("Ya existe un " + contactoRequestDTO.getTipo().name().toLowerCase() + " con ese nombre");
            }
        }
    }

    // Mapear solicitud a entidad
    private Contacto mapearSolicitudAEntidad(ContactoRequestDTO dto, Contacto contacto) {
        contacto.setNombre(dto.getNombre());
        contacto.setContacto(dto.getContacto());
        contacto.setTipo(dto.getTipo());
        return contacto;
    }

    // Mapear entidad a DTO
    private ContactoDTO mapearEntidadADTO(Contacto contacto) {
        ContactoDTO dto = new ContactoDTO();
        dto.setContactoID(contacto.getContactoID());
        dto.setNombre(contacto.getNombre());
        dto.setContacto(contacto.getContacto());
        dto.setTipo(contacto.getTipo());
        dto.setActivo(contacto.getActivo());
        return dto;
    }

    // Obtener entidad por ID para uso interno
    public Contacto obtenerEntidadPorId(Long contactoID) {
        return contactoRepository.findByContactoIDAndActivoTrue(contactoID).orElse(null);
    }



    /**
     * Listar Contactos con paginado y filtros dinámicos
     * @param pageable Información de paginación
     * @param searchTerm Término de búsqueda (opcional)
     * @param activo Filtro por estado activo (opcional)
     * @param tipo Filtro por tipo de contacto (opcional)
     * @return Página de ContactoDTO filtrados
     */
    public Page<ContactoDTO> obtenerContactosPaginadosConFiltros(
            Pageable pageable,
            String searchTerm,
            Boolean activo,
            TipoContacto tipo) {
        
        Page<Contacto> contactoPage;
        
        // Si hay tipo específico
        if (tipo != null) {
            // Si hay búsqueda
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                List<Contacto> contactos;
                if (activo == null) {
                    contactos = contactoRepository.findByTipo(tipo);
                } else if (activo) {
                    contactos = contactoRepository.findByTipoAndActivoTrue(tipo);
                } else {
                    contactos = contactoRepository.findByTipoAndActivoFalse(tipo);
                }
                
                // Filtrar por término de búsqueda
                contactos = contactos.stream()
                    .filter(c -> (c.getNombre() != null && c.getNombre().toLowerCase().contains(searchTerm.toLowerCase())) ||
                                 (c.getContacto() != null && c.getContacto().toLowerCase().contains(searchTerm.toLowerCase())))
                    .collect(Collectors.toList());
                
                // Convertir lista a página
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), contactos.size());
                List<Contacto> pageContent = contactos.subList(start, end);
                contactoPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, contactos.size());
            } else {
                // Sin búsqueda, solo filtro de activo y tipo
                if (activo == null) {
                    contactoPage = contactoRepository.findByTipoOrderByNombreAsc(tipo, pageable);
                } else if (activo) {
                    contactoPage = contactoRepository.findByTipoAndActivoTrueOrderByNombreAsc(tipo, pageable);
                } else {
                    contactoPage = contactoRepository.findByTipoAndActivoFalseOrderByNombreAsc(tipo, pageable);
                }
            }
        } else {
            // Sin tipo específico
            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                List<Contacto> contactos;
                if (activo == null) {
                    contactos = contactoRepository.findAll();
                } else if (activo) {
                    contactos = contactoRepository.findByActivoTrue();
                } else {
                    contactos = contactoRepository.findAll().stream()
                        .filter(c -> !c.getActivo())
                        .collect(Collectors.toList());
                }
                
                // Filtrar por término de búsqueda
                contactos = contactos.stream()
                    .filter(c -> (c.getNombre() != null && c.getNombre().toLowerCase().contains(searchTerm.toLowerCase())) ||
                                 (c.getContacto() != null && c.getContacto().toLowerCase().contains(searchTerm.toLowerCase())))
                    .collect(Collectors.toList());
                
                // Convertir lista a página
                int start = (int) pageable.getOffset();
                int end = Math.min(start + pageable.getPageSize(), contactos.size());
                List<Contacto> pageContent = contactos.subList(start, end);
                contactoPage = new org.springframework.data.domain.PageImpl<>(pageContent, pageable, contactos.size());
            } else {
                // Sin búsqueda ni tipo, solo filtro de activo
                if (activo == null) {
                    contactoPage = contactoRepository.findAllByOrderByNombreAsc(pageable);
                } else if (activo) {
                    contactoPage = contactoRepository.findByActivoTrueOrderByNombreAsc(pageable);
                } else {
                    contactoPage = contactoRepository.findByActivoFalseOrderByNombreAsc(pageable);
                }
            }
        }
        
        return contactoPage.map(this::mapearEntidadADTO);
    }
}