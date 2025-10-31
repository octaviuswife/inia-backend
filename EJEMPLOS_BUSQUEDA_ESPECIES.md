# Ejemplos de Búsqueda Inteligente de Especies

Este documento muestra cómo funciona el sistema de búsqueda inteligente de especies durante la importación de archivos Excel.

## 🎯 Objetivo

Evitar la creación de especies duplicadas cuando se importan datos desde Excel, incluso cuando los nombres tienen diferencias en:
- Mayúsculas/minúsculas
- Acentos
- Nombres parciales vs completos

## 📊 Casos de Uso Reales

### Caso 1: Diferencias de Mayúsculas

**Catálogo contiene:**
```
Raigrás
```

**Excel tiene:**
```
RAIGRAS
raigras
RaiGras
```

**Resultado:**
✅ Todas las variantes encuentran "Raigrás" existente  
❌ NO se crean duplicados

**Método usado:** Nivel 1 - Búsqueda exacta case-insensitive

---

### Caso 2: Nombres Compuestos vs Parciales

**Catálogo contiene:**
```
Avena blanca / Avena amarilla
Pasto ovillo / Pasto azul
Lotus uliginosus / L. pedunculatus
```

**Excel tiene:**
```
AVENA BLANCA
Avena amarilla
PASTO OVILLO
Pasto azul
Lotus uliginosus
```

**Resultado:**
✅ Todos encuentran la especie compuesta existente  
❌ NO se crean duplicados

**Método usado:** Nivel 2 - Búsqueda por inicio de nombre

---

### Caso 3: Con/Sin Acentos

**Catálogo contiene:**
```
Trébol rojo
Trébol blanco
Trébol alejandrino
Cáñamo
Maíz
```

**Excel tiene:**
```
TREBOL ROJO
trebol blanco
Trebol alejandrino
CANAMO
MAIZ
```

**Resultado:**
✅ Todas las variantes encuentran la especie con acentos  
❌ NO se crean duplicados

**Método usado:** Nivel 3 - Búsqueda flexible sin acentos

---

### Caso 4: Nombres Científicos Múltiples

**Catálogo contiene:**
```
Especie: Vicia forrajera
Nombre científico: Vicia benghalensis, Vicia villosa, Vicia sativa
```

**Excel tiene:**
```
VICIA FORRAJERA
Vicia
```

**Resultado:**
✅ "VICIA FORRAJERA" encuentra la especie existente  
⚠️ "Vicia" (solo) podría encontrarla por búsqueda flexible  
❓ Si no coincide, crea "Vicia" como nueva especie

**Método usado:** Niveles 1-3

---

### Caso 5: Abreviaturas y Variaciones

**Catálogo contiene:**
```
Colza, Nabo, Nabo forrajero, Canola
```

**Excel tiene:**
```
COLZA
Nabo
Canola
Nabo forrajero
```

**Resultado:**
✅ "COLZA" encuentra la especie (por inicio)  
✅ "Nabo" encuentra la especie (contenido)  
✅ "Canola" encuentra la especie (contenido)  
✅ "Nabo forrajero" encuentra la especie (por inicio)

**Método usado:** Niveles 2 y 3

---

## 🔍 Flujo de Búsqueda Detallado

```
┌─────────────────────────────────────┐
│ Importar Excel: "AVENA BLANCA"      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ Nivel 1: Búsqueda Exacta            │
│ findByNombreComunIgnoreCase()       │
│ ❌ No encuentra "AVENA BLANCA"      │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ Nivel 2: Búsqueda por Inicio        │
│ WHERE nombreComun LIKE 'AVENA%'     │
│ ✅ Encuentra:                        │
│    "Avena blanca / Avena amarilla"  │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│ ✅ RETORNA especie existente        │
│ NO crea duplicado                   │
└─────────────────────────────────────┘
```

---

## 📝 Logs del Sistema

Durante la importación, verás estos logs:

```
DEBUG - Especie encontrada (búsqueda exacta): Maíz
DEBUG - Especie encontrada (búsqueda por inicio): Avena blanca -> Avena blanca / Avena amarilla
DEBUG - Especie encontrada (búsqueda flexible): TREBOL ROJO -> Trébol rojo
INFO  - Creando nueva especie: Algodon
```

**Interpretación:**
- `búsqueda exacta`: Coincidencia perfecta (ignorando case)
- `búsqueda por inicio`: El nombre buscado está al inicio del nombre catalogado
- `búsqueda flexible`: Encontrado removiendo acentos y buscando contenido
- `Creando nueva especie`: No existe en catálogo, se crea por primera vez

---

## 🛠️ Casos Especiales

### ¿Qué pasa si el Excel tiene errores?

**Excel tiene:**
```
MAZ (error tipográfico)
```

**Resultado:**
❌ NO encuentra "Maíz" en ningún nivel  
✅ Crea nueva especie "MAZ"  
⚠️ **Acción requerida:** Revisar y corregir/fusionar manualmente

### ¿Qué pasa con "spp."?

**Catálogo contiene:**
```
Brassica spp.
Cannabis spp.
```

**Excel tiene:**
```
BRASSICA
Cannabis
```

**Resultado:**
✅ "BRASSICA" encuentra "Brassica spp." (búsqueda flexible)  
✅ "Cannabis" encuentra "Cannabis spp." (búsqueda flexible)

### ¿Qué pasa con nombres muy genéricos?

**Catálogo contiene:**
```
Sorgo - Sorghum bicolor
Sorgo - Sorghum bicolor x Sorghum drummondii
```

**Excel tiene:**
```
SORGO
```

**Resultado:**
✅ Encuentra el primero que coincida  
⚠️ **Nota:** Si hay múltiples "Sorgo", retorna el primero encontrado

---

## 🎓 Mejores Prácticas

### Para Importación de Excel

1. **Usar nombres completos**: "Avena blanca" es mejor que "Avena"
2. **Respetar formato del catálogo**: Si el catálogo tiene "Raigrás", usa "Raigrás" (aunque el sistema tolere "RAIGRAS")
3. **Revisar logs**: Después de importar, revisar que las especies se hayan asociado correctamente
4. **Corregir errores tipográficos ANTES de importar**: "MAZ" → "MAIZ"

### Para Mantenimiento del Catálogo

1. **Nombres descriptivos**: Incluir variantes comunes: "Avena blanca / Avena amarilla"
2. **Consistencia**: Decidir un formato y mantenerlo
3. **Documentar variantes**: Si "Canola" = "Colza", ponerlo en el nombre común
4. **Actualizar periódicamente**: Agregar especies nuevas cuando aparezcan en importaciones

---

## 🔧 Configuración Técnica

### Métodos de Repository

```java
// Búsqueda exacta case-insensitive
Optional<Especie> findByNombreComunIgnoreCase(String nombreComun);

// Búsqueda por inicio
@Query("SELECT e FROM Especie e WHERE LOWER(e.nombreComun) LIKE LOWER(CONCAT(:nombreComun, '%'))")
List<Especie> buscarPorNombreComunInicio(@Param("nombreComun") String nombreComun);

// Búsqueda flexible
@Query("SELECT e FROM Especie e WHERE LOWER(e.nombreComun) LIKE LOWER(CONCAT('%', :nombreComun, '%'))")
List<Especie> buscarPorNombreComunFlexible(@Param("nombreComun") String nombreComun);
```

### Lógica de Normalización

```java
// Remover acentos
String normalized = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD);
return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

// Ejemplo:
"Trébol" → "Trebol"
"Maíz" → "Maiz"
```

---

## ✅ Resumen

| Escenario | Encuentra Duplicado | Crea Nuevo |
|-----------|---------------------|------------|
| "MAIZ" vs "Maíz" | ✅ Sí | ❌ No |
| "Avena blanca" vs "Avena blanca / Avena amarilla" | ✅ Sí | ❌ No |
| "TREBOL ROJO" vs "Trébol rojo" | ✅ Sí | ❌ No |
| "Raigras" vs "Raigrás" | ✅ Sí | ❌ No |
| "MAZ" vs "Maíz" | ❌ No | ✅ Sí (error) |
| "Algodon" (no existe) | ❌ No | ✅ Sí (correcto) |

---

**Última actualización:** Octubre 2025  
**Versión:** 1.1
