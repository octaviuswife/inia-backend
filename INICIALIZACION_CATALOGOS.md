# Inicialización Automática de Catálogos

## Descripción

El sistema incluye scripts de inicialización automática que se ejecutan al arrancar la aplicación Spring Boot. Estos scripts pueblan la base de datos con datos iniciales necesarios para el funcionamiento del sistema.

## Scripts de Inicialización

### 1. DatabaseInitializer (Order 1)
**Archivo:** `config/DatabaseInitializer.java`

**Función:** Crea el usuario administrador predeterminado si no existe ningún administrador en el sistema.

**Datos creados:**
- Usuario: `admin`
- Contraseña: `admin123`
- Email: `admin@inia.gub.uy`
- Rol: ADMIN
- Estado: ACTIVO

**⚠️ IMPORTANTE:** Se recomienda cambiar la contraseña del administrador después del primer login.

### 2. CatalogosInitializer (Order 2)
**Archivo:** `config/CatalogosInitializer.java`

**Función:** Inicializa los catálogos de Malezas y Especies si están vacíos.

#### Catálogo de Malezas (29 registros)

**Malezas Prioritarias:**
1. Coleostephus myconis - margarita de piria
2. Cuscuta spp. - cúscuta
3. Eragrostis plana - capin annoni
4. Senecio madagascariensis - senecio
5. Sorghum halepense - sorgo de Alepo
6. Xanthium spp. - abrojo

**Otras Malezas:**
7. Ammi majus - biznaguilla
8. Ammi visnaga - biznaga
9. Anthemis cotula - manzanilla
10. Avena fatua - balango
11. Brassica spp. - nabo
12. Carduus spp. - cardos
13. Carthamus lanatus - cardo de la cruz
14. Centaurea spp. - abrepuño
15. Cirsium vulgare - cardo negro
16. Convolvulus spp. - corrigüela
17. Cyclospermum leptophyllum - apio cimarrón
18. Cynara cardunculus - cardo de castilla
19. Cyperus rotundus - pasto bolita
20. Echium plantagineum - flor morada
21. Lolium temulentum - joyo
22. Melilotus indicus - trébol de olor
23. Phalaris paradoxa - alpistillo
24. Plantago lanceolata - llantén
25. Polygonum convolvulus - enredadera anual
26. Raphanus spp. - rábano
27. Rapistrum rugosum - mostacilla
28. Rumex spp. - lengua de vaca
29. Sylibum marianum - cardo asnal

#### Catálogo de Especies (37 registros)

**Cereales y Oleaginosas (15 especies):**
1. Achicoria - Cichorium intybus
2. Arroz - Oryza sativa
3. Cebada - Hordeum vulgare subsp. vulgare
4. Cáñamo - Cannabis spp.
5. Centeno - Secale cereale
6. Colza, Nabo, Nabo forrajero, Canola - Brassica napus
7. Girasol - Helianthus annuus
8. Lino - Linum usitatissimum L.
9. Maíz - Zea mays
10. Soja - Glycine max
11. Sorgo granífero - Sorghum bicolor
12. Sorgo forrajero - Sorghum bicolor x Sorghum drummondii
13. Sudangrás - Sorghum x drummondii
14. Trigo - Triticum aestivum subsp. aestivum
15. Triticale - x Triticosecale

**Forrajeras - Gramíneas (10 especies):**
16. Avena blanca / Avena amarilla - Avena sativa / Avena byzantina
17. Avena negra - Avena strigosa
18. Cebadilla - Bromus catharticus
19. Pasto ovillo / Pasto azul - Dactylis glomerata
20. Falaris - Phalaris aquatica
21. Festuca - Festuca arundinacea
22. Festulolium - x Festulolium
23. Holcus - Holcus lanatus
24. Moha - Setaria italica
25. Raigrás - Lolium multiflorum / Lolium perenne

**Forrajeras - Leguminosas (12 especies):**
26. Alfalfa - Medicago sativa
27. Lotononis - Lotononis bainesii
28. Lotus angustissimus - Lotus angustissimus
29. Lotus corniculatus - Lotus corniculatus
30. Lotus subbiflorus - Lotus subbiflorus
31. Lotus tenuis - Lotus tenuis
32. Lotus uliginosus / L. pedunculatus - Lotus uliginosus / L. pedunculatus
33. Trébol alejandrino - Trifolium alexandrinum
34. Trébol blanco - Trifolium repens
35. Trébol persa - Trifolium resupinatum
36. Trébol rojo - Trifolium pratense
37. Vicia forrajera - Vicia benghalensis, Vicia villosa, Vicia sativa

## Funcionamiento

### Ejecución Automática
Los scripts se ejecutan automáticamente cuando:
1. La aplicación Spring Boot se inicia
2. La tabla correspondiente está vacía

### Orden de Ejecución
Los scripts se ejecutan en el siguiente orden:
1. **DatabaseInitializer** (Order 1) - Usuario Admin
2. **CatalogosInitializer** (Order 2) - Malezas y Especies

### Prevención de Duplicados

#### En la Inicialización (CatalogosInitializer)
El script **solo se ejecuta si las tablas están completamente vacías**:
- Verifica `count()` de la tabla antes de ejecutar
- Si `count > 0`, muestra mensaje informativo y sale
- Si `count == 0`, inserta todos los registros del catálogo
- **No verifica duplicados internos** porque son datos controlados y únicos

**⚠️ Importante:** Si necesitas re-ejecutar el script, debes vaciar completamente las tablas de `Malezas` y `Especie`.

#### En la Importación de Excel (ImportacionLegadoService)
Aquí es donde está la **búsqueda inteligente multinivel** para prevenir duplicados cuando importas datos externos:

**Nivel 1 - Búsqueda Exacta (case-insensitive):**
```
Busca: "RAIGRAS" → Encuentra: "Raigrás" ✅
```

**Nivel 2 - Búsqueda por Inicio:**
```
Busca: "Avena blanca" → Encuentra: "Avena blanca / Avena amarilla" ✅
```

**Nivel 3 - Búsqueda Flexible (sin acentos, contenido):**
```
Busca: "RAIGRAS" → Encuentra: "Raigrás" ✅
Busca: "Trebol rojo" → Encuentra: "Trébol rojo" ✅
Busca: "AVENA" → Encuentra: "Avena blanca / Avena amarilla" ✅
```

**Nivel 4 - Crear si no existe:**
```
Solo crea una nueva especie si ninguna búsqueda anterior tuvo éxito
```

### Ventajas del Sistema de Búsqueda Inteligente

✅ **Tolerante a mayúsculas/minúsculas**: "MAIZ" = "Maíz" = "maíz"  
✅ **Tolerante a acentos**: "Raigras" = "Raigrás"  
✅ **Maneja nombres compuestos**: "Avena blanca" coincide con "Avena blanca / Avena amarilla"  
✅ **Evita duplicados**: Si existe cualquier variante, la reutiliza  
✅ **Registra en logs**: Cada coincidencia se registra para auditoría

### Mensajes de Consola

**Al iniciar con catálogos vacíos:**
```
✅ Administrador predeterminado creado:
   Usuario: admin
   Contraseña: admin123
   Email: admin@inia.gub.uy
   ⚠️  IMPORTANTE: Cambiar la contraseña después del primer login
📋 Inicializando catálogo de Malezas...
✅ Catálogo de Malezas inicializado exitosamente
   Total de malezas registradas: 29
📋 Inicializando catálogo de Especies...
✅ Catálogo de Especies inicializado exitosamente
   Total de especies registradas: 37
```

**Al iniciar con catálogos ya poblados:**
```
ℹ️  Ya existe un administrador en el sistema
ℹ️  Catálogo de Malezas ya inicializado (29 registros)
ℹ️  Catálogo de Especies ya inicializado (37 registros)
```

**Al importar Excel con especies existentes:**
```
DEBUG - Especie encontrada (búsqueda exacta): RAIGRAS
DEBUG - Especie encontrada (búsqueda por inicio): Avena blanca -> Avena blanca / Avena amarilla
DEBUG - Especie encontrada (búsqueda flexible): TREBOL ROJO -> Trébol rojo
INFO  - Creando nueva especie: ESPECIE_NUEVA_NO_CATALOGADA
```

## Estructura de Datos

### MalezasCatalogo
```java
- catalogoID: Long (autogenerado)
- nombreCientifico: String
- nombreComun: String
- activo: Boolean (default: true)
```

### Especie
```java
- especieID: Long (autogenerado)
- nombreCientifico: String
- nombreComun: String
- activo: Boolean (default: true)
```

## Mantenimiento

### Agregar Nuevas Malezas o Especies
Para agregar nuevos registros iniciales:

1. Abrir `CatalogosInitializer.java`
2. Agregar el nuevo registro a la lista correspondiente
3. Reiniciar la aplicación (solo si la base de datos está vacía)

**Nota:** Si la base de datos ya tiene datos, el script no se ejecutará. Para forzar la re-inicialización:
- Vaciar la tabla correspondiente manualmente
- O agregar los registros directamente vía API/SQL

### Modificar Registros Existentes
Los scripts de inicialización solo **crean** datos, no los modifican. Para actualizar registros existentes:
- Usar la API REST de catalogos
- Ejecutar SQL UPDATE directamente en la base de datos

## Consideraciones

### Deshabilitación de Scripts
Si necesitas deshabilitar temporalmente algún script:

**Opción 1:** Comentar la anotación `@Component`
```java
//@Component
@Order(2)
public class CatalogosInitializer implements CommandLineRunner {
```

**Opción 2:** Agregar un condicional al inicio del método `run()`
```java
@Override
public void run(String... args) throws Exception {
    // return; // Descomentar para deshabilitar
    initializeMalezas();
    initializeEspecies();
}
```

### Rendimiento
Los scripts están optimizados para:
- Verificar existencia de datos antes de insertar
- Insertar en lotes cuando es posible
- Mostrar progreso en consola

### Errores
En caso de error durante la inicialización:
- El error se registra en consola
- La aplicación continúa ejecutándose
- Los datos parcialmente insertados se mantienen

## Historial

- **Versión 1.1** (Oct 2025): Mejoras en prevención de duplicados
  - Búsqueda inteligente multinivel en importación de Excel
  - Tolerancia a mayúsculas/minúsculas y acentos
  - Manejo de nombres compuestos (ej: "Avena blanca / Avena amarilla")
  - Verificación de duplicados antes de insertar en inicialización
  - Logs detallados de búsqueda y creación
  
- **Versión 1.0** (Oct 2025): Creación inicial del script de inicialización de catálogos
  - 29 malezas
  - 37 especies
  - Integración con DatabaseInitializer
