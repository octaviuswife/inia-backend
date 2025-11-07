# 🧪 GUÍA COMPLETA DE JUNIT PARA TU PROYECTO INIA

## 📖 ¿Qué es JUnit?

**JUnit** es un framework que te permite escribir **pruebas automáticas** para tu código Java. Es como tener un asistente que verifica que tu código funcione correctamente cada vez que haces cambios.

### 🎯 ¿Qué hace JUnit?

✅ **Ejecuta tus tests automáticamente**  
✅ **Te dice qué pasó y qué falló**  
✅ **Se integra con Maven/Gradle**  
✅ **Muestra resultados en color (verde = pasó, rojo = falló)**  

❌ **NO escribe los tests por ti** (tú los escribes)  
❌ **NO testea automáticamente todo** (tú decides qué testear)  

---

## 🏗️ Tipos de Tests

### 1️⃣ **Test Unitario** (Unit Test)
- Prueba **UN SOLO método** de forma aislada
- **Simula** (mockea) las dependencias
- **Rápido** (milisegundos)
- **NO usa base de datos real**

```java
@Test
void calcularPorcentaje_debeRetornar80() {
    // Solo prueba la lógica de cálculo
    double resultado = service.calcular(80, 100);
    assertEquals(80.0, resultado);
}
```

**Cuándo usarlo:**
- Probar lógica de negocio (cálculos, validaciones)
- Servicios que solo procesan datos
- Métodos que no dependen de BD

### 2️⃣ **Test de Integración** (Integration Test)
- Prueba **varios componentes juntos**
- Usa **base de datos real** (H2 o Testcontainers)
- **Más lento** (segundos)
- Verifica el **flujo completo**

```java
@Test
void crearGerminacion_debeGuardarEnBD() {
    // Prueba Controller → Service → Repository → BD
    mockMvc.perform(post("/api/germinacion")...)
           .andExpect(status().isCreated());
}
```

**Cuándo usarlo:**
- Probar endpoints de API
- Verificar que se guarda en BD
- Probar autenticación JWT

### 3️⃣ **Test End-to-End** (E2E)
- Prueba **toda la aplicación completa**
- Incluye frontend + backend + BD
- **Muy lento** (minutos)
- Se hace con herramientas como Selenium o Cypress

---

## 🔧 Cómo Ejecutar Tests

### Opción 1: Desde IntelliJ IDEA
1. Click derecho en el archivo de test
2. "Run 'NombreDelTest'"
3. Ver resultados en panel inferior

### Opción 2: Desde Visual Studio Code
1. Instalar extensión "Java Test Runner"
2. Click en el ícono de flask (testing)
3. Click en "Run Test"

### Opción 3: Desde Terminal (Maven)
```bash
# Ejecutar TODOS los tests
mvn test

# Ejecutar solo una clase de test
mvn test -Dtest=GerminacionServiceTest

# Ejecutar un test específico
mvn test -Dtest=GerminacionServiceTest#crearGerminacion_debeAsignarEstadoRegistrado

# Ver resultados con más detalle
mvn test -X

# Saltar tests (cuando haces build)
mvn clean package -DskipTests
```

---

## 📊 Interpretando Resultados

### ✅ Test Pasó (Verde)
```
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```
**Significado:** Todos los tests funcionaron correctamente

### ❌ Test Falló (Rojo)
```
[ERROR] Tests run: 5, Failures: 1, Errors: 0, Skipped: 0

Expected :REGISTRADO
Actual   :PENDIENTE
```
**Significado:** 
- 5 tests se ejecutaron
- 1 falló porque esperaba `REGISTRADO` pero obtuvo `PENDIENTE`
- Debes revisar tu código

### ⚠️ Test con Error
```
[ERROR] Tests run: 5, Failures: 0, Errors: 1, Skipped: 0

NullPointerException at line 45
```
**Significado:**
- No es un fallo de lógica, es un **error de código**
- Algo está null que no debería estarlo

---

## 🎨 Anotaciones Principales

### Anotaciones de JUnit 5

| Anotación | Qué Hace | Ejemplo |
|-----------|----------|---------|
| `@Test` | Marca un método como test | `@Test void miTest() {}` |
| `@BeforeEach` | Se ejecuta **antes de cada test** | Preparar datos |
| `@AfterEach` | Se ejecuta **después de cada test** | Limpiar datos |
| `@BeforeAll` | Se ejecuta **una vez al inicio** | Configuración global |
| `@AfterAll` | Se ejecuta **una vez al final** | Cerrar conexiones |
| `@DisplayName("...")` | Nombre legible del test | Para reportes |
| `@Disabled` | Desactiva un test temporalmente | Cuando está en desarrollo |
| `@Tag("integracion")` | Etiqueta tests para ejecutar grupos | `mvn test -Dgroups=integracion` |

### Anotaciones de Spring Testing

| Anotación | Qué Hace | Velocidad | Cuándo Usarla |
|-----------|----------|-----------|---------------|
| `@SpringBootTest` | Levanta **toda** la app | 🐢 Lento | Tests de integración completos |
| `@WebMvcTest(Controller.class)` | Solo el **controller** | 🐇 Rápido | Tests de API sin BD |
| `@DataJpaTest` | Solo **JPA y BD** | 🐰 Medio | Tests de repositorios |
| `@MockBean` | Crea un **mock** de un bean | - | Simular dependencias |
| `@Autowired` | Inyecta bean **real** | - | Usar componente real |

---

## 🧩 Mockito: Simulando Dependencias

**Mockito** te permite crear "**mocks**" (simulaciones) de objetos.

### ¿Por qué usar mocks?

❌ **Sin mock:** El test llama a la BD real → lento y complejo  
✅ **Con mock:** El test simula la BD → rápido y simple  

### Sintaxis Básica

```java
// 1. Crear el mock
@Mock
private GerminacionRepository repository;

// 2. Configurar su comportamiento
when(repository.findById(1L)).thenReturn(Optional.of(germinacion));

// 3. Usar el servicio que lo necesita
GerminacionDTO resultado = service.obtener(1L);

// 4. Verificar que se llamó
verify(repository, times(1)).findById(1L);
```

### Comandos Útiles de Mockito

```java
// WHEN: Configurar qué devuelve
when(mock.metodo()).thenReturn(valor);
when(mock.metodo()).thenThrow(new RuntimeException());
when(mock.metodo(anyLong())).thenReturn(valor);  // Acepta cualquier Long

// VERIFY: Verificar que se llamó
verify(mock).metodo();                    // Se llamó exactamente 1 vez
verify(mock, times(3)).metodo();          // Se llamó 3 veces
verify(mock, never()).metodo();           // NUNCA se llamó
verify(mock, atLeastOnce()).metodo();     // Al menos 1 vez

// ARGUMENT MATCHERS: Para cualquier parámetro
any()           // Cualquier objeto
anyLong()       // Cualquier Long
anyString()     // Cualquier String
eq(valor)       // Valor exacto
```

---

## ✅ Assertions: Verificando Resultados

**Assertions** son las verificaciones que haces en los tests.

### Assertions Comunes

```java
// Igualdad
assertEquals(esperado, actual);
assertNotEquals(valor1, valor2);

// Booleanos
assertTrue(condicion);
assertFalse(condicion);

// Nulos
assertNull(objeto);
assertNotNull(objeto);

// Excepciones
assertThrows(RuntimeException.class, () -> {
    service.metodoQueDebeFallar();
});

// Colecciones
assertArrayEquals(esperado, actual);

// Múltiples assertions
assertAll(
    () -> assertEquals(1, resultado.getId()),
    () -> assertEquals("Juan", resultado.getNombre()),
    () -> assertTrue(resultado.isActivo())
);
```

### Mensajes Personalizados

```java
assertEquals(80, resultado, "El porcentaje debe ser 80");
// Si falla, muestra: "El porcentaje debe ser 80"
```

---

## 📁 Estructura de Archivos

```
inia-backend/
├── src/
│   ├── main/
│   │   └── java/
│   │       └── services/
│   │           └── GerminacionService.java    ← Código real
│   │
│   └── test/
│       ├── java/
│       │   └── services/
│       │       └── GerminacionServiceTest.java    ← Tests del servicio
│       │
│       └── resources/
│           └── application.properties    ← Configuración para tests
```

**Convención:** El test se llama igual que la clase + `Test`
- `GerminacionService.java` → `GerminacionServiceTest.java`
- `AuthController.java` → `AuthControllerTest.java`

---

## 🎯 Patrón AAA (Arrange-Act-Assert)

Estructura **recomendada** para todos los tests:

```java
@Test
void nombreDescriptivoDelTest() {
    // ========== ARRANGE (Preparar) ==========
    // Configurar datos de prueba
    GerminacionRequestDTO request = new GerminacionRequestDTO();
    request.setLoteId(1L);
    
    when(repository.save(any())).thenReturn(germinacion);
    
    // ========== ACT (Actuar) ==========
    // Ejecutar el método que queremos probar
    GerminacionDTO resultado = service.crear(request);
    
    // ========== ASSERT (Verificar) ==========
    // Verificar que el resultado es correcto
    assertNotNull(resultado);
    assertEquals(Estado.REGISTRADO, resultado.getEstado());
    verify(repository, times(1)).save(any());
}
```

---

## 🚀 Ejemplo Completo Paso a Paso

### Paso 1: Escribir el Test PRIMERO (TDD)

```java
@Test
void calcularPorcentajeGerminacion_con80De100_debeRetornar80() {
    // ARRANGE
    int germinadas = 80;
    int totales = 100;
    
    // ACT
    double resultado = calculadora.calcularPorcentaje(germinadas, totales);
    
    // ASSERT
    assertEquals(80.0, resultado);
}
```

### Paso 2: Ejecutar el Test (Falla 🔴)
```
Error: Method calcularPorcentaje not found
```

### Paso 3: Escribir el Código Mínimo
```java
public double calcularPorcentaje(int germinadas, int totales) {
    return (germinadas * 100.0) / totales;
}
```

### Paso 4: Ejecutar el Test (Pasa ✅)
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

### Paso 5: Agregar Más Tests
```java
@Test
void calcularPorcentaje_con0Totales_debeLanzarExcepcion() {
    assertThrows(ArithmeticException.class, () -> {
        calculadora.calcularPorcentaje(0, 0);
    });
}
```

---

## 🎬 Ciclo de Vida de un Test

```java
@BeforeAll
static void inicializar() {
    System.out.println("1. Se ejecuta UNA VEZ al inicio");
}

@BeforeEach
void prepararCadaTest() {
    System.out.println("2. Se ejecuta ANTES de cada test");
}

@Test
void test1() {
    System.out.println("3. Ejecuta test1");
}

@Test
void test2() {
    System.out.println("3. Ejecuta test2");
}

@AfterEach
void limpiarCadaTest() {
    System.out.println("4. Se ejecuta DESPUÉS de cada test");
}

@AfterAll
static void finalizar() {
    System.out.println("5. Se ejecuta UNA VEZ al final");
}
```

**Salida en consola:**
```
1. Se ejecuta UNA VEZ al inicio
2. Se ejecuta ANTES de cada test
3. Ejecuta test1
4. Se ejecuta DESPUÉS de cada test
2. Se ejecuta ANTES de cada test
3. Ejecuta test2
4. Se ejecuta DESPUÉS de cada test
5. Se ejecuta UNA VEZ al final
```

---

## 🎨 Buenas Prácticas

### ✅ HACER

1. **Nombres descriptivos:**
   ```java
   @Test
   void crearGerminacion_conLoteInexistente_debeLanzarExcepcion()
   ```

2. **Un test = una cosa:**
   ```java
   @Test
   void debeValidarEmail()  // ✅ Solo valida email
   
   @Test
   void debeValidarEmailYPassword()  // ❌ Hace dos cosas
   ```

3. **Tests independientes:**
   ```java
   @Test
   void test1() {
       // No debe depender de test2
   }
   ```

4. **Usar @DisplayName:**
   ```java
   @Test
   @DisplayName("Crear germinación con lote inexistente debe lanzar excepción")
   void test() {}
   ```

5. **Tests rápidos:**
   - Test unitario: < 100ms
   - Test integración: < 1s

### ❌ EVITAR

1. **Tests que fallan aleatoriamente** (flaky tests)
2. **Tests que dependen del orden de ejecución**
3. **Tests que dependen de datos externos**
4. **Tests con sleeps/waits**
5. **Tests sin assertions**

---

## 🔍 Debugging Tests

### Si un test falla:

1. **Lee el mensaje de error:**
   ```
   Expected :80
   Actual   :75
   ```

2. **Usa `System.out.println()`:**
   ```java
   @Test
   void miTest() {
       System.out.println("Valor: " + resultado);
       assertEquals(80, resultado);
   }
   ```

3. **Ejecuta en modo debug:**
   - Pon un breakpoint
   - Click derecho → "Debug 'miTest'"

4. **Verifica los mocks:**
   ```java
   verify(repository).save(any());  // ¿Se llamó?
   ```

---

## 📦 Tests con Base de Datos

### Opción 1: H2 en Memoria (Rápido)
```properties
# src/test/resources/application.properties
spring.datasource.url=jdbc:h2:mem:testdb
```

✅ **Ventajas:** Súper rápido, no necesita Docker  
❌ **Desventajas:** No es PostgreSQL exacto, puede haber diferencias

### Opción 2: Testcontainers (PostgreSQL Real)
```java
@Testcontainers
@SpringBootTest
class MiTest {
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");
}
```

✅ **Ventajas:** PostgreSQL real, 100% igual a producción  
❌ **Desventajas:** Más lento, necesita Docker

---

## 🎓 Flujo de Trabajo Recomendado

### Para tu proyecto INIA:

1. **Tests Unitarios** (70% de cobertura)
   - Servicios con lógica de negocio
   - Cálculos, validaciones
   - Mockear repositorios

2. **Tests de Integración** (20% de cobertura)
   - Controllers principales (Auth, Germinacion)
   - Endpoints críticos
   - Flujos completos

3. **Tests E2E** (10% de cobertura)
   - Flujos críticos de usuario
   - Se pueden hacer con Postman/Newman

---

## 📊 Cobertura de Tests

### Ver cobertura con JaCoCo:

1. **Agregar al pom.xml:**
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
</plugin>
```

2. **Ejecutar:**
```bash
mvn clean test jacoco:report
```

3. **Ver reporte:**
```
target/site/jacoco/index.html
```

---

## 🎯 Próximos Pasos

1. ✅ **Ya creamos:**
   - `GerminacionServiceTest.java` (test unitario)
   - `AuthControllerIntegrationTest.java` (test integración)
   - `application.properties` para tests

2. 📝 **Puedes crear:**
   - Tests para `LoteService`
   - Tests para `UsuarioService`
   - Tests para `JwtUtil`

3. 🚀 **Ejecuta los tests:**
   ```bash
   cd inia-backend
   mvn test
   ```

---

## 💡 Resumen Final

| Concepto | Qué Es | Cuándo Usar |
|----------|--------|-------------|
| **JUnit** | Framework de testing | Siempre |
| **Test Unitario** | Prueba un método aislado | Lógica de negocio |
| **Test Integración** | Prueba varios componentes | Endpoints, flujos |
| **Mockito** | Simula dependencias | Tests unitarios |
| **MockMvc** | Simula peticiones HTTP | Tests de controllers |
| **@SpringBootTest** | Levanta toda la app | Tests integración |
| **Assertions** | Verificaciones | En todos los tests |

---

## 🤔 Preguntas Frecuentes

**P: ¿Debo testear todo mi código?**  
R: No es necesario 100%. Apunta a 70-80% de cobertura en código crítico.

**P: ¿Cuántos tests debo escribir?**  
R: Al menos 1 test por cada método público importante.

**P: ¿Los tests hacen mi código más lento?**  
R: Los tests NO afectan la velocidad de tu app en producción.

**P: ¿Puedo ejecutar la app sin tests?**  
R: Sí: `mvn spring-boot:run` (no ejecuta tests)

**P: ¿Qué hago si los tests fallan?**  
R: Revisa el código, no desactives el test.

---

## 📚 Recursos Adicionales

- [JUnit 5 Docs](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Docs](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Testing Docs](https://docs.spring.io/spring-framework/reference/testing.html)
