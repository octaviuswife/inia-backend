# Documentación Completa - Exportación Excel 52 Columnas

## 📋 Índice
1. [Resumen Ejecutivo](#resumen-ejecutivo)
2. [Estructura Final del Excel](#estructura-final-del-excel)
3. [Historial de Cambios](#historial-de-cambios)
4. [Mapeo de Campos](#mapeo-de-campos)
5. [Campos Pendientes](#campos-pendientes)
6. [Guía de Pruebas](#guía-de-pruebas)

---

## 🎯 Resumen Ejecutivo

### Objetivo
Generar archivos Excel (.xlsx) con datos de análisis de semillas que incluyen:
- Datos básicos del lote
- Análisis de Pureza (INIA e INASE)
- Descripciones de malezas y cultivos
- Análisis DOSN (INIA e INASE)
- Peso de Mil Semillas (PMS)
- Análisis de Germinación (INIA e INASE)
- Viabilidad por Tetrazolio

### Estructura Final
- **Total de columnas:** 52 (A-AZ)
- **Encabezados:** 2 filas (principal + subencabezados)
- **Estilos:** Gris para INIA, Amarillo para INASE
- **Formato de fecha:** dd/MM/yyyy

### Estado Actual
✅ **Compilación exitosa** sin errores  
✅ **Estructura completa** de 52 columnas  
✅ **Mapeo de datos** implementado con TipoListado enum  
⚠️ **2 campos pendientes** en entidad Pureza (ver sección [Campos Pendientes](#campos-pendientes))

---

## 📊 Estructura Final del Excel

### Vista General por Secciones

| Sección | Columnas | Rango | Total | Instituto | Color |
|---------|----------|-------|-------|-----------|-------|
| Datos Básicos | A-I | 0-8 | 9 | - | Gris |
| Pureza INIA | J-O | 9-14 | 6 | INIA | Gris |
| Pureza INASE | P-U | 15-20 | 6 | INASE | Amarillo |
| Descripción | V-Y | 21-24 | 4 | - | Gris |
| DOSN | Z-AD | 25-29 | 5 | INIA | Gris |
| DOSN-I | AE-AI | 30-34 | 5 | INASE | Amarillo |
| PMS | AJ | 35 | 1 | - | Gris |
| Fecha Análisis | AK | 36 | 1 | - | Gris |
| TS | AL | 37 | 1 | - | Gris |
| Germinación | AM-AR | 38-43 | 6 | INIA | Gris |
| Germinación-I | AS-AX | 44-49 | 6 | INASE | Amarillo |
| Viabilidad | AY-AZ | 50-51 | 2 | INIA/INASE | Gris/Amarillo |

---

## 🔍 Detalle de Columnas

### A-I: Datos Básicos (9 columnas)

| Col | Idx | Subencabezado | Campo DTO | Descripción |
|-----|-----|---------------|-----------|-------------|
| A | 0 | Especie | especie | Especie de la semilla |
| B | 1 | Variedad | variedad | Variedad |
| C | 2 | Lote | lote | Número de lote |
| D | 3 | Deposito | deposito | Depósito |
| E | 4 | N° de articulo | numeroArticulo | Número de artículo |
| F | 5 | N° análisis | numeroAnalisis | Número de análisis |
| G | 6 | Nro. Ficha | numeroFicha | Número de ficha |
| H | 7 | Kilos | kilos | Peso en kilos |
| I | 8 | H% | humedad | Porcentaje de humedad |

### J-O: Pureza INIA (6 columnas) 🔵

| Col | Idx | Subencabezado | Campo DTO | Entidad Pureza |
|-----|-----|---------------|-----------|----------------|
| J | 9 | SP% | purezaSemillaPura | redonSemillaPura |
| K | 10 | MI% | purezaMateriaInerte | redonMateriaInerte |
| L | 11 | OC% | purezaOtrosCultivos | redonOtrosCultivos |
| M | 12 | M% | purezaMalezas | redonMalezas |
| N | 13 | MT.% | purezaMalezasToleradas | redonMalezasToleradas |
| O | 14 | M.T.C% | purezaMateriaTotal | ⚠️ null (campo pendiente) |

### P-U: Pureza INASE (6 columnas) 🟡

| Col | Idx | Subencabezado | Campo DTO | Entidad Pureza |
|-----|-----|---------------|-----------|----------------|
| P | 15 | SP-I% | purezaInaseSemillaPura | inasePura |
| Q | 16 | MI-I% | purezaInaseMateriaInerte | inaseMateriaInerte |
| R | 17 | OC-I% | purezaInaseOtrosCultivos | inaseOtrosCultivos |
| S | 18 | M% | purezaInaseMalezas | inaseMalezas |
| T | 19 | M.T-I% | purezaInaseMalezasToleradas | inaseMalezasToleradas |
| U | 20 | M.T.C% | purezaInaseMateriaTotal | ⚠️ null (campo pendiente) |

### V-Y: Descripción (4 columnas) 🔵

| Col | Idx | Subencabezado | Campo DTO | Origen |
|-----|-----|---------------|-----------|--------|
| V | 21 | MI | descripcionMalezas | Listado → MalezasYCultivosCatalogo |
| W | 22 | OC | descripcionOtrosCultivos | Listado → MalezasYCultivosCatalogo |
| X | 23 | MT | descripcionMalezasToleradas | Listado → MalezasYCultivosCatalogo |
| Y | 24 | MTC | descripcionMateriaTotal | Listado → MalezasYCultivosCatalogo |

### Z-AD: DOSN (5 columnas) 🔵

| Col | Idx | Subencabezado | Campo DTO | Filtro TipoListado |
|-----|-----|---------------|-----------|-------------------|
| Z | 25 | MTC | dosnMalezasToleranciaC | MAL_TOLERANCIA_CERO + INIA |
| AA | 26 | OC | dosnOtrosCultivos | CULTIVO + INIA |
| AB | 27 | M | dosnMalezas | MALEZA + INIA |
| AC | 28 | MT | dosnMalezasToleradas | MAL_TOLERANCIA + INIA |
| AD | 29 | DB | dosnBrassica | BRASSICA + INIA |

### AE-AI: DOSN-I (5 columnas) 🟡

| Col | Idx | Subencabezado | Campo DTO | Filtro TipoListado |
|-----|-----|---------------|-----------|-------------------|
| AE | 30 | MTC | dosnInaseMalezasToleranciaC | MAL_TOLERANCIA_CERO + INASE |
| AF | 31 | OC | dosnInaseOtrosCultivos | CULTIVO + INASE |
| AG | 32 | M | dosnInaseMalezas | MALEZA + INASE |
| AH | 33 | MT | dosnInaseMalezasToleradas | MAL_TOLERANCIA + INASE |
| AI | 34 | DB | dosnInaseBrassica | BRASSICA + INASE |

### AJ-AL: PMS, Fecha y TS (3 columnas) 🔵

| Col | Idx | Subencabezado | Campo DTO | Origen |
|-----|-----|---------------|-----------|--------|
| AJ | 35 | - | pms | Pms.pmsconRedon |
| AK | 36 | - | fechaAnalisis | Formato: dd/MM/yyyy |
| AL | 37 | - | tratamientoSemillas | Lote.tratamientoSemillas |

### AM-AR: Germinación INIA (6 columnas) 🔵

| Col | Idx | Subencabezado | Campo DTO | Entidad Germinacion |
|-----|-----|---------------|-----------|---------------------|
| AM | 38 | PN% | germinacionPlantulasNormales | germinacionRedoPlantulasNormales |
| AN | 39 | AN% | germinacionPlantulasAnormales | germinacionRedoPlantulasAnormales |
| AO | 40 | D% | germinacionSemillasDeterioras | germinacionRedoSemillasDeterioras |
| AP | 41 | F% | germinacionSemillasFrescas | germinacionRedoSemillasFrescas |
| AQ | 42 | M% | germinacionSemillasMuertas | germinacionRedoSemillasMuertas |
| AR | 43 | G% | germinacionTotal | germinacionRedoTotal |

### AS-AX: Germinación INASE (6 columnas) 🟡

| Col | Idx | Subencabezado | Campo DTO | Entidad Germinacion |
|-----|-----|---------------|-----------|---------------------|
| AS | 44 | PN-I% | germinacionInasePlantulasNormales | germinacionInasePlantulasNormales |
| AT | 45 | AN-I% | germinacionInasePlantulasAnormales | germinacionInasePlantulasAnormales |
| AU | 46 | D-I% | germinacionInaseSemillasDeterioras | germinacionInaseSemillasDeterioras |
| AV | 47 | F-I% | germinacionInaseSemillasFrescas | germinacionInaseSemillasFrescas |
| AW | 48 | M-I% | germinacionInaseSemillasMuertas | germinacionInaseSemillasMuertas |
| AX | 49 | G-I% | germinacionInaseTotal | germinacionInaseTotal |

### AY-AZ: Viabilidad Tetrazolio (2 columnas)

| Col | Idx | Subencabezado | Campo DTO | Entidad Tetrazolio | Color |
|-----|-----|---------------|-----------|-------------------|-------|
| AY | 50 | - | viabilidadPorcentaje | tetrazolioViabilidadInstituto | 🔵 |
| AZ | 51 | - | viabilidadInasePorcentaje | tetrazolioViabilidadInase | 🟡 |

---

## 📝 Historial de Cambios

### Iteración 1: Creación Inicial
- ✅ Creado README_USO.md para el frontend
- ✅ Agregada sección de prueba en `/reportes`
- ✅ Estructura inicial de 50 columnas

### Iteración 2: Agregado de Columnas Faltantes
- ✅ Identificadas 2 columnas faltantes (MTC Pureza INASE, PMS)
- ✅ Agregados campos en DatosExportacionExcelDTO
- ✅ Actualizada estructura a 52 columnas

### Iteración 3: Corrección de Headers
- ✅ Corregida distribución de Pureza INASE (5→6 columnas)
- ✅ Ajustados rangos de celdas combinadas
- ✅ Actualizado método `crearEncabezados()`

### Iteración 4: Mapeo Semántico de DOSN
- ✅ Renombrados campos genéricos a nombres semánticos:
  - `dosnMateriaTotal` → `dosnMalezasToleranciaC`
  - `dosnDB` → `dosnBrassica`
- ✅ Implementado filtrado por enum `TipoListado`
- ✅ Separación correcta INIA/INASE con enum `Instituto`

### Iteración 5: Corrección de Orden
- ✅ Corregido orden de columnas DOSN (MTC, OC, M, MT, DB)
- ✅ Corregido orden de columnas DOSN-I (MTC, OC, M, MT, DB)
- ✅ Agregada columna PMS en posición correcta
- ✅ Movida Fecha Análisis a su columna propia

### Iteración 6: Descripción Completa
- ✅ Agregada columna MTC en Descripción
- ✅ Descripción ahora tiene 4 columnas (V-Y)
- ✅ Actualizados todos los índices subsiguientes

---

## 🔧 Mapeo de Campos

### Métodos de Mapeo en ExportacionExcelService

#### 1. `mapearDatosPureza()`
Extrae datos de análisis de Pureza para INIA e INASE:
```java
// INIA
dto.setPurezaSemillaPura(pureza.getRedonSemillaPura());
dto.setPurezaMateriaInerte(pureza.getRedonMateriaInerte());
// ... etc

// INASE
dto.setPurezaInaseSemillaPura(pureza.getInasePura());
dto.setPurezaInaseMateriaInerte(pureza.getInaseMateriaInerte());
// ... etc
```

**Campos pendientes:**
- `redonMateriaTotal` (columna O)
- `inaseMateriaTotal` (columna U)

#### 2. `mapearDescripcionMalezas()`
Extrae nombres de malezas y cultivos desde listados de Pureza:
```java
for (Listado listado : pureza.getListados()) {
    String nombre = listado.getCatalogo().getNombreComun();
    TipoMYCCatalogo tipo = listado.getCatalogo().getTipoMYCCatalogo();
    // Filtrado por tipo: MALEZA, CULTIVO, etc.
}
```

#### 3. `mapearDatosDosn()`
Extrae datos de DOSN filtrando por `TipoListado` e `Instituto`:
```java
for (Listado listado : dosn.getListados()) {
    TipoListado tipoListado = listado.getListadoTipo();
    Instituto instituto = listado.getListadoInsti();
    
    if (instituto == Instituto.INIA) {
        if (tipoListado == TipoListado.MAL_TOLERANCIA_CERO) {
            // dosnMalezasToleranciaC
        } else if (tipoListado == TipoListado.BRASSICA) {
            // dosnBrassica
        }
        // ... otros tipos
    } else if (instituto == Instituto.INASE) {
        // Mismo filtrado para INASE
    }
}
```

**Enums utilizados:**
- `TipoListado`: MAL_TOLERANCIA_CERO, BRASSICA, MAL_TOLERANCIA, MAL_COMUNES, OTROS
- `Instituto`: INIA, INASE
- `TipoMYCCatalogo`: MALEZA, CULTIVO

#### 4. `mapearDatosPms()`
Extrae el Peso de Mil Semillas:
```java
Pms pms = analisisPms.get(0);
dto.setPms(pms.getPmsconRedon());
```

#### 5. `mapearDatosGerminacion()`
Extrae datos de Germinación para INIA e INASE:
```java
// INIA
dto.setGerminacionPlantulasNormales(germinacion.getGerminacionRedoPlantulasNormales());
// ... etc

// INASE
dto.setGerminacionInasePlantulasNormales(germinacion.getGerminacionInasePlantulasNormales());
// ... etc
```

#### 6. `mapearDatosTetrazolio()`
Extrae datos de viabilidad por Tetrazolio:
```java
dto.setViabilidadPorcentaje(tetrazolio.getTetrazolioViabilidadInstituto());
dto.setViabilidadInasePorcentaje(tetrazolio.getTetrazolioViabilidadInase());
```

---

## ⚠️ Campos Pendientes

### Entidad Pureza

#### Campo: `redonMateriaTotal`
- **Columna afectada:** O (Pureza INIA M.T.C%)
- **Estado actual:** `null`
- **Acción requerida:** Agregar campo a la entidad `Pureza.java`:
  ```java
  @Column(name = "redon_materia_total")
  private BigDecimal redonMateriaTotal;
  ```

#### Campo: `inaseMateriaTotal`
- **Columna afectada:** U (Pureza INASE M.T.C%)
- **Estado actual:** `null`
- **Acción requerida:** Agregar campo a la entidad `Pureza.java`:
  ```java
  @Column(name = "inase_materia_total")
  private BigDecimal inaseMateriaTotal;
  ```

### Solución Temporal
Los campos se establecen en `null` en el método `mapearDatosPureza()`:
```java
// TODO: Campo 'redonMateriaTotal' no existe en entidad Pureza
dto.setPurezaMateriaTotal(null);

// TODO: Campo 'inaseMateriaTotal' no existe en entidad Pureza
dto.setPurezaInaseMateriaTotal(null);
```

---

## 🧪 Guía de Pruebas

### Probar desde el Frontend

1. **Acceder a la página de reportes:**
   ```
   http://localhost:3000/reportes
   ```

2. **Usar el botón de prueba:**
   - Hacer clic en "Exportar Todo (Sin Filtros)"
   - Verificar que se descarga el archivo Excel

3. **Verificar estructura:**
   - Abrir el Excel descargado
   - Verificar 52 columnas (A-AZ)
   - Verificar encabezados en 2 filas
   - Verificar colores (gris para INIA, amarillo para INASE)

### Verificar con Filtros

1. **Usar el diálogo de filtros:**
   - Hacer clic en "Exportar con Filtros"
   - Seleccionar rango de fechas
   - Seleccionar especies/variedades
   - Exportar

2. **Verificar datos:**
   - Columnas V-Y: Descripción con MTC
   - Columnas Z-AD: DOSN con orden correcto (MTC, OC, M, MT, DB)
   - Columnas AE-AI: DOSN-I con orden correcto
   - Columna AJ: PMS visible
   - Columna AK: Fecha con formato dd/MM/yyyy

### Validar Mapeo de TipoListado

**Verificar que:**
- Columna Z (DOSN MTC) contenga solo malezas de tolerancia cero
- Columna AD (DOSN DB) contenga solo Brassica
- Columna AE (DOSN-I MTC) contenga solo malezas de tolerancia cero INASE
- Columna AI (DOSN-I DB) contenga solo Brassica INASE

---

## 📚 Archivos del Proyecto

### Backend
- **ExportacionExcelService.java** - Servicio principal de exportación
- **DatosExportacionExcelDTO.java** - DTO con 52 campos mapeados
- **ExportacionRequestDTO.java** - DTO para filtros de exportación
- **ExportacionController.java** - Endpoint REST

### Frontend
- **app/reportes/page.tsx** - Página de reportes con botones de prueba
- **components/exportar-excel-btn.tsx** - Botón de exportación
- **components/dialog-exportar-filtros.tsx** - Diálogo de filtros
- **README_USO.md** - Guía de uso del frontend

### Documentación
- **DOCUMENTACION_EXCEL_COMPLETA.md** - Este documento

---

## 🎨 Estilos de Celdas

### Encabezados INIA (Gris)
```java
private CellStyle crearEstiloEncabezado(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    // ... bordes, alineación, fuente
}
```

### Encabezados INASE (Amarillo)
```java
private CellStyle crearEstiloEncabezadoAmarillo(Workbook workbook) {
    CellStyle style = workbook.createCellStyle();
    style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
    style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    // ... bordes, alineación, fuente
}
```

---

## 🚀 Próximos Pasos

1. **Agregar campos faltantes en Pureza**
   - `redonMateriaTotal`
   - `inaseMateriaTotal`

2. **Probar exportación con datos reales**
   - Verificar filtrado de TipoListado
   - Validar separación INIA/INASE

3. **Optimizaciones opcionales**
   - Cache de estilos de celdas
   - Procesamiento por lotes para grandes volúmenes

---

**Última actualización:** 2025-10-15  
**Versión:** 1.0  
**Estado:** ✅ Producción (con 2 campos pendientes en BD)
