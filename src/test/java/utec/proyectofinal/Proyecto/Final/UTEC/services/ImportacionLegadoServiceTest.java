package utec.proyectofinal.Proyecto.Final.UTEC.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.*;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de ImportacionLegadoService")
class ImportacionLegadoServiceTest {

    @Mock
    private LegadoRepository legadoRepository;

    @Mock
    private LoteRepository loteRepository;

    @Mock
    private CultivarRepository cultivarRepository;

    @Mock
    private EspecieRepository especieRepository;

    @Mock
    private ContactoRepository contactoRepository;

    @Mock
    private CatalogoCrudRepository catalogoRepository;

    @Mock
    private MultipartFile archivo;

    @InjectMocks
    private ImportacionLegadoService importacionLegadoService;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("Importar archivo con error IO - debe manejar correctamente")
    void importarDesdeExcel_errorIO_debeManejarCorrectamente() throws IOException {
        when(archivo.getInputStream()).thenThrow(new IOException("Error al leer archivo"));

        var resultado = importacionLegadoService.importarDesdeExcel(archivo, false);

        assertNotNull(resultado);
        assertFalse(resultado.getExitoso());
        assertTrue(resultado.getMensaje().contains("Error al leer"));
    }
}
