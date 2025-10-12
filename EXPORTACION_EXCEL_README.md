# Funcionalidad de Exportación a Excel

## Descripción General

Se ha implementado una funcionalidad completa para exportar los datos de análisis de semillas a archivos Excel, siguiendo la estructura de la planilla de ejemplo proporcionada. Esta funcionalidad permite exportar datos de los diferentes tipos de análisis (Pureza, Germinación, PMS, Tetrazolio, DOSN) con sus respectivos datos INIA e INASE.

## Estructura del Archivo Excel

El archivo Excel generado incluye las siguientes columnas basadas en la planilla de ejemplo:

### Datos Básicos
- Especie
- Variedad (Cultivar)
- Lote
- Deposito (de lote)
- N° de artículo (de lote)
- N° análisis
- Nro. Ficha (de lote)
- Kilos (de lote)
- H% (Humedad) (Desplegable de todos los conjuntos ingresados de datosHumedad en lote)

### Pureza INIA
- SP% (Semilla Pura)
- MI% (Materia Inerte)
- OC% (Otros Cultivos)
- M% (Malezas)
- MT.% (Malezas Toleradas)
- M.T.C% (Materia Total)

### Pureza INASE
- SP-I% (Semilla Pura INASE)
- MI-I% (Materia Inerte INASE)
- OC-I% (Otros Cultivos INASE)
- M% (Malezas INASE)
- M.T-I% (Malezas Toleradas INASE)
- M.T.C% (Materia Total INASE)

### Descripción
- MI (Descripción Materia Inerte)
- OC (Descripción Otros Cultivos)
- MT (Descripción Malezas Toleradas)
- MTC (Descripción Materia Total)

### DOSN e DOSN-I
- Datos de DOSN (INIA)
- Datos de DOSN-I (INASE)
(OC, M, MT, MTC, DB)

### Otros Datos
- PMS (peso de mil semillas)
- Fecha Análisis (fecha de ingresado)
- TS (Tratamiento de Semillas)
- Datos de Germinación (valores INIA e INASE (normales, anormales, frescas, duras y germinacion))

- V% y V-I% (Viabilidad - tetrazolio)

## Endpoints Disponibles

### 1. Exportación General
```
GET /api/exportaciones/excel
```
Exporta todos los lotes activos o una lista específica de lotes.

**Parámetros:**
- `loteIds` (opcional): Lista de IDs de lotes a exportar

**Ejemplo:**
```
GET /api/exportaciones/excel?loteIds=1,2,3
```

### 2. Exportación de Lote Específico
```
GET /api/exportaciones/excel/lote/{loteId}
```
Exporta los datos de un lote específico.

**Ejemplo:**
```
GET /api/exportaciones/excel/lote/1
```

### 3. Exportación Personalizada
```
POST /api/exportaciones/excel/personalizado
```
Exporta una lista específica de lotes enviada en el cuerpo de la petición.

**Body:**
```json
[1, 2, 3, 4, 5]
```

### 4. Exportación Avanzada con Filtros
```
POST /api/exportaciones/excel/avanzado
```
Exporta datos con filtros avanzados.

**Body:**
```json
{
  "loteIds": [1, 2, 3],
  "fechaDesde": "2024-01-01",
  "fechaHasta": "2024-12-31",
  "especieIds": [1, 2],
  "cultivarIds": [1, 2, 3],
  "incluirInactivos": false,
  "tiposAnalisis": ["PUREZA", "GERMINACION", "PMS"],
  "incluirEncabezados": true,
  "incluirColoresEstilo": true,
  "formatoFecha": "dd/MM/yyyy"
}
```

## Campos del DTO de Filtros (ExportacionRequestDTO)

- **loteIds**: Lista de IDs de lotes específicos a exportar
- **fechaDesde/fechaHasta**: Rango de fechas para filtrar lotes
- **especieIds**: Lista de IDs de especies a incluir
- **cultivarIds**: Lista de IDs de cultivares a incluir
- **incluirInactivos**: Si incluir lotes inactivos (por defecto: false)
- **tiposAnalisis**: Tipos de análisis a incluir ("PUREZA", "GERMINACION", "PMS", "TETRAZOLIO", "DOSN")
- **incluirEncabezados**: Si incluir encabezados en el Excel (por defecto: true)
- **incluirColoresEstilo**: Si aplicar estilos y colores (por defecto: true)
- **formatoFecha**: Formato de fecha para el Excel (por defecto: "dd/MM/yyyy")

## Estructura de Datos Exportados (DatosExportacionExcelDTO)

El DTO principal contiene todos los campos necesarios para representar los datos de la planilla:

```java
public class DatosExportacionExcelDTO {
    // Datos básicos del lote
    private String especie;
    private String variedad;
    private String lote;
    // ... más campos
    
    // Datos de Pureza INIA
    private BigDecimal purezaSemillaPura;
    private BigDecimal purezaMateriaInerte;
    // ... más campos
    
    // Datos de Pureza INASE
    private BigDecimal purezaInaseSemillaPura;
    // ... más campos
    
    // Datos de Germinación, PMS, Tetrazolio, etc.
}
```

## Permisos y Seguridad

Todos los endpoints requieren autenticación JWT y los siguientes roles:
- `ADMIN`
- `ANALISTA`
- `OBSERVADOR`

## Características del Excel Generado

1. **Formato Visual**: Replica el estilo de la planilla original con colores y bordes
2. **Encabezados Combinados**: Las secciones principales tienen encabezados combinados
3. **Estilos Diferenciados**: 
   - Gris para datos INIA
   - Amarillo para datos INASE
   - Bordes y separaciones claras
4. **Nombre Automático**: Los archivos se generan con timestamp automático
5. **Formato de Descarga**: Se entregan como attachment con el tipo MIME correcto

## Ejemplo de Uso desde Frontend

```javascript
// Exportar todos los lotes activos
fetch('/api/exportaciones/excel', {
  method: 'GET',
  headers: {
    'Authorization': 'Bearer ' + token
  }
})
.then(response => response.blob())
.then(blob => {
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = 'analisis_semillas.xlsx';
  a.click();
});

// Exportar con filtros avanzados
fetch('/api/exportaciones/excel/avanzado', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
    'Authorization': 'Bearer ' + token
  },
  body: JSON.stringify({
    fechaDesde: '2024-01-01',
    fechaHasta: '2024-12-31',
    tiposAnalisis: ['PUREZA', 'GERMINACION'],
    incluirEncabezados: true
  })
})
.then(response => response.blob())
.then(blob => {
  // Manejar descarga del archivo
});
```

## Archivos Modificados

### Nuevos Archivos:
1. `DatosExportacionExcelDTO.java` - DTO para los datos exportados
2. `ExportacionExcelService.java` - Servicio principal de exportación
3. `ExportacionController.java` - Controlador REST
4. `ExportacionRequestDTO.java` - DTO para filtros de exportación

### Archivos Modificados:
1. `pom.xml` - Agregadas dependencias de Apache POI
2. Varios repositorios - Agregados métodos `findByLoteLoteID`

## Dependencias Agregadas

```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi</artifactId>
    <version>5.3.0</version>
</dependency>
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.3.0</version>
</dependency>
```

## Notas Técnicas

1. **Memoria**: Para grandes volúmenes de datos, considerar implementar exportación por chunks
2. **Rendimiento**: Los archivos se generan en memoria, adecuado para reportes de hasta 10,000 registros
3. **Extensibilidad**: La estructura permite agregar fácilmente nuevos campos o tipos de análisis
4. **Compatibilidad**: Genera archivos .xlsx compatibles con Excel 2007+