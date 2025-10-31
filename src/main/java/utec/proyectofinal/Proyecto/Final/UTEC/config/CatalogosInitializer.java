package utec.proyectofinal.Proyecto.Final.UTEC.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.Especie;
import utec.proyectofinal.Proyecto.Final.UTEC.business.entities.MalezasCatalogo;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.EspecieRepository;
import utec.proyectofinal.Proyecto.Final.UTEC.business.repositories.MalezasCatalogoRepository;

import java.util.Arrays;
import java.util.List;

/**
 * Inicializador de catálogos base (Malezas y Especies).
 * Se ejecuta automáticamente al iniciar la aplicación si los catálogos están vacíos.
 */
@Component
@Order(2) // Se ejecuta después del DatabaseInitializer (Order 1)
public class CatalogosInitializer implements CommandLineRunner {

    @Autowired
    private MalezasCatalogoRepository malezasRepository;

    @Autowired
    private EspecieRepository especieRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeMalezas();
        initializeEspecies();
    }

    /**
     * Inicializa el catálogo de malezas si está vacío
     */
    private void initializeMalezas() {
        try {
            // Verificar si ya existen malezas en la base de datos
            long count = malezasRepository.count();
            if (count > 0) {
                System.out.println("ℹ️  Catálogo de Malezas ya inicializado (" + count + " registros)");
                return;
            }

            System.out.println("📋 Inicializando catálogo de Malezas...");

            // Lista de malezas prioritarias
            List<MalezaData> malezasPrioritarias = Arrays.asList(
                new MalezaData("Coleostephus myconis", "margarita de piria"),
                new MalezaData("Cuscuta spp.", "cúscuta"),
                new MalezaData("Eragrostis plana", "capin annoni"),
                new MalezaData("Senecio madagascariensis", "senecio"),
                new MalezaData("Sorghum halepense", "sorgo de Alepo"),
                new MalezaData("Xanthium spp.", "abrojo")
            );

            // Lista de otras malezas
            List<MalezaData> otrasMalezas = Arrays.asList(
                new MalezaData("Ammi majus", "biznaguilla"),
                new MalezaData("Ammi visnaga", "biznaga"),
                new MalezaData("Anthemis cotula", "manzanilla"),
                new MalezaData("Avena fatua", "balango"),
                new MalezaData("Brassica spp.", "nabo"),
                new MalezaData("Carduus spp.", "cardos"),
                new MalezaData("Carthamus lanatus", "cardo de la cruz"),
                new MalezaData("Centaurea spp.", "abrepuño"),
                new MalezaData("Cirsium vulgare", "cardo negro"),
                new MalezaData("Convolvulus spp.", "corrigüela"),
                new MalezaData("Cyclospermum leptophyllum", "apio cimarrón"),
                new MalezaData("Cynara cardunculus", "cardo de castilla"),
                new MalezaData("Cyperus rotundus", "pasto bolita"),
                new MalezaData("Echium plantagineum", "flor morada"),
                new MalezaData("Lolium temulentum", "joyo"),
                new MalezaData("Melilotus indicus", "trébol de olor"),
                new MalezaData("Phalaris paradoxa", "alpistillo"),
                new MalezaData("Plantago lanceolata", "llantén"),
                new MalezaData("Polygonum convolvulus", "enredadera anual"),
                new MalezaData("Raphanus spp.", "rábano"),
                new MalezaData("Rapistrum rugosum", "mostacilla"),
                new MalezaData("Rumex spp.", "lengua de vaca"),
                new MalezaData("Sylibum marianum", "cardo asnal")
            );

            int count_inserted = 0;
            
            // Insertar malezas prioritarias
            for (MalezaData data : malezasPrioritarias) {
                MalezasCatalogo maleza = new MalezasCatalogo();
                maleza.setNombreCientifico(data.nombreCientifico);
                maleza.setNombreComun(data.nombreComun);
                maleza.setActivo(true);
                malezasRepository.save(maleza);
                count_inserted++;
            }

            // Insertar otras malezas
            for (MalezaData data : otrasMalezas) {
                MalezasCatalogo maleza = new MalezasCatalogo();
                maleza.setNombreCientifico(data.nombreCientifico);
                maleza.setNombreComun(data.nombreComun);
                maleza.setActivo(true);
                malezasRepository.save(maleza);
                count_inserted++;
            }

            System.out.println("✅ Catálogo de Malezas inicializado exitosamente");
            System.out.println("   Total de malezas registradas: " + count_inserted);
            
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar catálogo de Malezas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Inicializa el catálogo de especies si está vacío
     */
    private void initializeEspecies() {
        try {
            // Verificar si ya existen especies en la base de datos
            long count = especieRepository.count();
            if (count > 0) {
                System.out.println("ℹ️  Catálogo de Especies ya inicializado (" + count + " registros)");
                return;
            }

            System.out.println("📋 Inicializando catálogo de Especies...");

            // Lista de especies - Cereales y Oleaginosas
            List<EspecieData> cerealesOleaginosas = Arrays.asList(
                new EspecieData("Achicoria", "Cichorium intybus"),
                new EspecieData("Arroz", "Oryza sativa"),
                new EspecieData("Cebada", "Hordeum vulgare subsp. vulgare"),
                new EspecieData("Cáñamo", "Cannabis spp."),
                new EspecieData("Centeno", "Secale cereale"),
                new EspecieData("Colza, Nabo, Nabo forrajero, Canola", "Brassica napus"),
                new EspecieData("Girasol", "Helianthus annuus"),
                new EspecieData("Lino", "Linum usitatissimum L."),
                new EspecieData("Maíz", "Zea mays"),
                new EspecieData("Soja", "Glycine max"),
                new EspecieData("Sorgo granífero", "Sorghum bicolor"),
                new EspecieData("Sorgo forrajero", "Sorghum bicolor x Sorghum drummondii"),
                new EspecieData("Sudangrás", "Sorghum x drummondii"),
                new EspecieData("Trigo", "Triticum aestivum subsp. aestivum"),
                new EspecieData("Triticale", "x Triticosecale")
            );

            // Lista de especies - Forrajeras (Gramíneas)
            List<EspecieData> forragerasGramineas = Arrays.asList(
                new EspecieData("Avena blanca / Avena amarilla", "Avena sativa / Avena byzantina"),
                new EspecieData("Avena negra", "Avena strigosa"),
                new EspecieData("Cebadilla", "Bromus catharticus"),
                new EspecieData("Pasto ovillo / Pasto azul", "Dactylis glomerata"),
                new EspecieData("Falaris", "Phalaris aquatica"),
                new EspecieData("Festuca", "Festuca arundinacea"),
                new EspecieData("Festulolium", "x Festulolium"),
                new EspecieData("Holcus", "Holcus lanatus"),
                new EspecieData("Moha", "Setaria italica"),
                new EspecieData("Raigrás", "Lolium multiflorum / Lolium perenne")
            );

            // Lista de especies - Forrajeras (Leguminosas)
            List<EspecieData> forragerasLeguminosas = Arrays.asList(
                new EspecieData("Alfalfa", "Medicago sativa"),
                new EspecieData("Lotononis", "Lotononis bainesii"),
                new EspecieData("Lotus angustissimus", "Lotus angustissimus"),
                new EspecieData("Lotus corniculatus", "Lotus corniculatus"),
                new EspecieData("Lotus subbiflorus", "Lotus subbiflorus"),
                new EspecieData("Lotus tenuis", "Lotus tenuis"),
                new EspecieData("Lotus uliginosus / L. pedunculatus", "Lotus uliginosus / L. pedunculatus"),
                new EspecieData("Trébol alejandrino", "Trifolium alexandrinum"),
                new EspecieData("Trébol blanco", "Trifolium repens"),
                new EspecieData("Trébol persa", "Trifolium resupinatum"),
                new EspecieData("Trébol rojo", "Trifolium pratense"),
                new EspecieData("Vicia forrajera", "Vicia benghalensis, Vicia villosa, Vicia sativa")
            );

            int count_inserted = 0;

            // Insertar cereales y oleaginosas
            for (EspecieData data : cerealesOleaginosas) {
                Especie especie = new Especie();
                especie.setNombreComun(data.nombreComun);
                especie.setNombreCientifico(data.nombreCientifico);
                especie.setActivo(true);
                especieRepository.save(especie);
                count_inserted++;
            }

            // Insertar forrajeras gramíneas
            for (EspecieData data : forragerasGramineas) {
                Especie especie = new Especie();
                especie.setNombreComun(data.nombreComun);
                especie.setNombreCientifico(data.nombreCientifico);
                especie.setActivo(true);
                especieRepository.save(especie);
                count_inserted++;
            }

            // Insertar forrajeras leguminosas
            for (EspecieData data : forragerasLeguminosas) {
                Especie especie = new Especie();
                especie.setNombreComun(data.nombreComun);
                especie.setNombreCientifico(data.nombreCientifico);
                especie.setActivo(true);
                especieRepository.save(especie);
                count_inserted++;
            }

            System.out.println("✅ Catálogo de Especies inicializado exitosamente");
            System.out.println("   Total de especies registradas: " + count_inserted);
            
        } catch (Exception e) {
            System.err.println("❌ Error al inicializar catálogo de Especies: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Clase auxiliar para almacenar datos de malezas
     */
    private static class MalezaData {
        String nombreCientifico;
        String nombreComun;

        MalezaData(String nombreCientifico, String nombreComun) {
            this.nombreCientifico = nombreCientifico;
            this.nombreComun = nombreComun;
        }
    }

    /**
     * Clase auxiliar para almacenar datos de especies
     */
    private static class EspecieData {
        String nombreComun;
        String nombreCientifico;

        EspecieData(String nombreComun, String nombreCientifico) {
            this.nombreComun = nombreComun;
            this.nombreCientifico = nombreCientifico;
        }
    }
}
