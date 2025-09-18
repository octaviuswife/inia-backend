package utec.proyectofinal.Proyecto.Final.UTEC.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ConfiguracionDatos;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.ConfiguracionDatosRepository;

@Service
public class ConfiguracionDatosService {

    @Autowired
    private ConfiguracionDatosRepository configuracionDatosRepository;

    // Obtener valores de humedad disponibles
    public List<String> obtenerValoresHumedad() {
        Optional<ConfiguracionDatos> config = configuracionDatosRepository.findByTipoAndActivoTrue("HUMEDAD");
        return config.map(ConfiguracionDatos::getValores).orElse(List.of());
    }

    // Obtener valores de número de artículo disponibles
    public List<String> obtenerValoresNumeroArticulo() {
        Optional<ConfiguracionDatos> config = configuracionDatosRepository.findByTipoAndActivoTrue("NUMERO_ARTICULO");
        return config.map(ConfiguracionDatos::getValores).orElse(List.of());
    }

    // Actualizar configuración de humedad (solo admin)
    public ConfiguracionDatos actualizarConfiguracionHumedad(List<String> nuevosValores) {
        Optional<ConfiguracionDatos> configOpt = configuracionDatosRepository.findByTipoAndActivoTrue("HUMEDAD");
        ConfiguracionDatos config;
        
        if (configOpt.isPresent()) {
            config = configOpt.get();
            config.setValores(nuevosValores);
        } else {
            config = new ConfiguracionDatos();
            config.setTipo("HUMEDAD");
            config.setValores(nuevosValores);
            config.setActivo(true);
        }
        
        return configuracionDatosRepository.save(config);
    }

    // Actualizar configuración de número de artículo (solo admin)
    public ConfiguracionDatos actualizarConfiguracionNumeroArticulo(List<String> nuevosValores) {
        Optional<ConfiguracionDatos> configOpt = configuracionDatosRepository.findByTipoAndActivoTrue("NUMERO_ARTICULO");
        ConfiguracionDatos config;
        
        if (configOpt.isPresent()) {
            config = configOpt.get();
            config.setValores(nuevosValores);
        } else {
            config = new ConfiguracionDatos();
            config.setTipo("NUMERO_ARTICULO");
            config.setValores(nuevosValores);
            config.setActivo(true);
        }
        
        return configuracionDatosRepository.save(config);
    }

    // Obtener todas las configuraciones activas
    public List<ConfiguracionDatos> obtenerTodasLasConfiguraciones() {
        return configuracionDatosRepository.findByActivoTrue();
    }
}