package utec.proyectofinal.Proyecto.Final.UTEC.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.ConfiguracionDatos;
import utec.proyectofinal.Proyecto.Final.UTEC.services.ConfiguracionDatosService;

@RestController
@RequestMapping("/api/configuracion")
@CrossOrigin(origins = "*")
public class ConfiguracionDatosController {

    @Autowired
    private ConfiguracionDatosService configuracionDatosService;

    // Obtener valores de humedad disponibles
    @GetMapping("/humedad")
    public ResponseEntity<List<String>> obtenerValoresHumedad() {
        try {
            List<String> valores = configuracionDatosService.obtenerValoresHumedad();
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener valores de número de artículo disponibles
    @GetMapping("/numero-articulo")
    public ResponseEntity<List<String>> obtenerValoresNumeroArticulo() {
        try {
            List<String> valores = configuracionDatosService.obtenerValoresNumeroArticulo();
            return new ResponseEntity<>(valores, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar configuración de humedad (solo admin)
    @PutMapping("/humedad")
    public ResponseEntity<ConfiguracionDatos> actualizarConfiguracionHumedad(@RequestBody List<String> nuevosValores) {
        try {
            ConfiguracionDatos config = configuracionDatosService.actualizarConfiguracionHumedad(nuevosValores);
            return new ResponseEntity<>(config, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Actualizar configuración de número de artículo (solo admin)
    @PutMapping("/numero-articulo")
    public ResponseEntity<ConfiguracionDatos> actualizarConfiguracionNumeroArticulo(@RequestBody List<String> nuevosValores) {
        try {
            ConfiguracionDatos config = configuracionDatosService.actualizarConfiguracionNumeroArticulo(nuevosValores);
            return new ResponseEntity<>(config, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Obtener todas las configuraciones
    @GetMapping
    public ResponseEntity<List<ConfiguracionDatos>> obtenerTodasLasConfiguraciones() {
        try {
            List<ConfiguracionDatos> configuraciones = configuracionDatosService.obtenerTodasLasConfiguraciones();
            return new ResponseEntity<>(configuraciones, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}