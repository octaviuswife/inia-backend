# 📚 Guía Completa: Tests Unitarios en el Proyecto INIA

## 🎯 ¿Qué son los Tests y por qué son importantes?

Los **tests unitarios** son código que prueba automáticamente nuestro código de producción para verificar que funciona correctamente. Son como tener un inspector que revisa cada pieza de tu aplicación antes de que llegue a producción.

### ✅ Beneficios

1. **Detectar errores temprano**: Encuentras bugs antes de que lleguen a producción
2. **Documentación viva**: Los tests muestran cómo se debe usar cada clase/método
3. **Refactorización segura**: Puedes cambiar código con confianza sabiendo que los tests te avisarán si algo se rompe
4. **Calidad del código**: Fuerzan a escribir código más limpio y modular

---

## 🏗️ Estructura de un Test: Patrón AAA

Todos nuestros tests siguen el patrón **AAA** (Arrange-Act-Assert):

```java
@Test
void crearLote_debeAsignarActivoTrue() {
    // 1. ARRANGE (Preparar)
    // Configuramos los datos de prueba y mocks
    LoteRequestDTO solicitud = new LoteRequestDTO();
    solicitud.setNomLote("LOTE-001");
    when(loteRepository.save(any())).thenReturn(lote);
    
    // 2. ACT (Actuar)
    // Ejecutamos el método que queremos probar
    LoteDTO resultado = loteService.crearLote(solicitud);
    
    // 3. ASSERT (Afirmar)
    // Verificamos que el resultado sea el esperado
    assertNotNull(resultado);
    assertEquals("LOTE-001", resultado.getNomLote());
}
```

---

## 🔧 Tecnologías Utilizadas

### 1. **JUnit 5 (Jupiter)** - Framework de testing
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Anotaciones principales:**
- `@Test`: Marca un método como test
- `@BeforeEach`: Ejecuta código antes de cada test (setup)
- `@DisplayName`: Nombre descriptivo del test

### 2. **Mockito** - Framework para crear objetos simulados (mocks)
```java
@Mock
private LoteRepository loteRepository; // Repositorio simulado

@InjectMocks
private LoteService loteService; // Servicio real con mocks inyectados
```

**¿Por qué Mockito?**
- Permite probar una clase en **aislamiento** sin depender de la base de datos
- Simula el comportamiento de dependencias externas
- Más rápido que tests de integración

### 3. **H2 Database** - Base de datos en memoria para tests
```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

**Ventajas:**
- Se crea y destruye automáticamente en cada ejecución
- No afecta la base de datos de producción (PostgreSQL)
- Muy rápida (todo en RAM)

---

## 📁 Estructura de Tests en el Proyecto

```
src/test/
├── java/
│   └── utec/proyectofinal/.../
│       ├── services/
│       │   ├── LoteServiceTest.java          (5 tests)
│       │   ├── PurezaServiceTest.java        (6 tests)
│       │   └── TetrazolioServiceTest.java    (6 tests)
│       └── ProyectoFinalUtecApplicationTests.java (1 test)
└── resources/
    └── application.properties (configuración H2)
```

---

## 🧪 Tests Creados

### 1. **LoteServiceTest** (5 tests)

#### Test 1: Crear lote asigna activo=true
```java
@Test
void crearLote_debeAsignarActivoTrue()
```
**¿Qué valida?**
- Al crear un lote, el campo `activo` debe ser `true` automáticamente
- El lote se guarda correctamente en el repositorio

#### Test 2: Obtener lote existente
```java
@Test
void obtenerLotePorId_cuandoExiste_debeRetornarLote()
```
**¿Qué valida?**
- El método `obtenerLotePorId` devuelve el lote correcto
- Usa el método especial `findByIdWithCultivarAndEspecie` (trae datos relacionados)

#### Test 3: Obtener lote inexistente
```java
@Test
void obtenerLotePorId_cuandoNoExiste_debeLanzarExcepcion()
```
**¿Qué valida?**
- Lanza `RuntimeException` cuando el lote no existe
- El mensaje de error es claro

#### Test 4: Eliminar lote
```java
@Test
void eliminarLote_debeCambiarActivoAFalse()
```
**¿Qué valida?**
- Eliminación lógica (cambia `activo` a `false`)
- No se borran datos físicamente de la BD

#### Test 5: Reactivar lote
```java
@Test
void reactivarLote_debeCambiarActivoATrue()
```
**¿Qué valida?**
- Un lote inactivo puede volver a activarse
- Devuelve el DTO actualizado

---

### 2. **PurezaServiceTest** (6 tests)

#### Test 1: Crear pureza asigna estado EN_PROCESO
```java
@Test
void crearPureza_debeAsignarEstadoEnProceso()
```
**¿Qué valida?**
- Al crear un análisis de pureza, el estado inicial es `EN_PROCESO`
- Se registra automáticamente en el historial

#### Test 2: Validación de lote inexistente
```java
@Test
void crearPureza_conLoteInexistente_debeLanzarExcepcion()
```
**¿Qué valida?**
- No se puede crear pureza sin un lote válido
- Lanza excepción si el lote no existe

#### Test 3: Validación de pesos
```java
@Test
void validarPesos_pesoTotalMenorQuePesoInicial_debeLanzarExcepcion()
```
**¿Qué valida?**
- Regla de negocio: `pesoTotal` ≥ `pesoInicial`
- Evita datos inconsistentes

#### Test 4: Obtener pureza existente
```java
@Test
void obtenerPurezaPorId_cuandoExiste_debeRetornarPureza()
```
**¿Qué valida?**
- Recupera correctamente un análisis por ID

#### Test 5: Desactivar pureza
```java
@Test
void desactivarPureza_debeCambiarActivoAFalse()
```
**¿Qué valida?**
- Usa el servicio genérico `AnalisisService.desactivarAnalisis`
- Eliminación lógica (no física)

---

### 3. **TetrazolioServiceTest** (6 tests)

#### Test 1: Crear tetrazolio asigna estado REGISTRADO
```java
@Test
void crearTetrazolio_debeAsignarEstadoRegistrado()
```
**¿Qué valida?**
- Estado inicial correcto: `REGISTRADO`
- Se registra en el historial

#### Test 2: Validación de repeticiones esperadas (null)
```java
@Test
void crearTetrazolio_sinRepeticionesEsperadas_debeLanzarExcepcion()
```
**¿Qué valida?**
- Campo `numRepeticionesEsperadas` es obligatorio

#### Test 3: Validación de repeticiones esperadas (cero)
```java
@Test
void crearTetrazolio_conRepeticionesCero_debeLanzarExcepcion()
```
**¿Qué valida?**
- `numRepeticionesEsperadas` debe ser > 0
- Regla de negocio para garantizar datos válidos

#### Test 4: Validación de lote inactivo
```java
@Test
void crearTetrazolio_conLoteInactivo_debeLanzarExcepcion()
```
**¿Qué valida?**
- No se puede crear análisis sobre lotes inactivos
- Mantiene integridad de datos

#### Test 5: Obtener tetrazolio existente
```java
@Test
void obtenerTetrazolioPorId_cuandoExiste_debeRetornarTetrazolio()
```
**¿Qué valida?**
- Recupera correctamente el análisis
- Incluye todos los campos (repeticiones esperadas, etc.)

#### Test 6: Desactivar tetrazolio
```java
@Test
void desactivarTetrazolio_debeCambiarActivoAFalse()
```
**¿Qué valida?**
- Usa servicio genérico de análisis
- Consistencia con otros tipos de análisis

---

## 🚀 Cómo Ejecutar los Tests

### Opción 1: Desde la terminal (Maven)
```bash
# Ejecutar todos los tests
mvn test

# Ejecutar solo un test específico
mvn test -Dtest=LoteServiceTest

# Ver más detalles
mvn test -X
```

### Opción 2: Desde IntelliJ IDEA
1. Clic derecho en la clase de test (ej: `LoteServiceTest.java`)
2. Seleccionar **"Run 'LoteServiceTest'"**
3. Ver resultados en el panel inferior

### Opción 3: Desde VS Code
1. Instalar extensión "Test Runner for Java"
2. Clic en el ícono de "play" verde junto al test
3. Ver resultados en el panel de tests

---

## 📊 Interpretando Resultados

### ✅ Test Exitoso (Exit Code: 0)
```
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
**Significado:** Todos los tests pasaron. El código funciona como se espera.

### ❌ Test Fallido
```
[ERROR] Failures: 1
[ERROR] LoteServiceTest.crearLote_debeAsignarActivoTrue:65
Expected: true but was: false
```
**Significado:** Un test no pasó. Revisar el código en la línea indicada.

### 🔥 Error de Compilación
```
[ERROR] Compilation failure
[ERROR] cannot find symbol: method desactivarLote(long)
```
**Significado:** El código de test no compila. El método no existe o tiene nombre diferente.

---

## 🧩 Comandos Mockito Explicados

### when().thenReturn()
```java
when(loteRepository.findById(1L)).thenReturn(Optional.of(lote));
```
**Significado:** "Cuando se llame a `findById(1L)`, devuelve este lote"

### verify()
```java
verify(loteRepository, times(1)).save(any(Lote.class));
```
**Significado:** "Verifica que se llamó a `save()` exactamente 1 vez con cualquier lote"

### any()
```java
when(repository.save(any(Lote.class))).thenReturn(lote);
```
**Significado:** "Acepta cualquier instancia de Lote como parámetro"

### doNothing()
```java
doNothing().when(service).desactivar(anyLong());
```
**Significado:** "Cuando se llame a este método, no hagas nada (para métodos void)"

---

## 🎓 Aserciones Comunes (JUnit)

| Aserción | Significado |
|----------|-------------|
| `assertNotNull(objeto)` | El objeto no debe ser null |
| `assertEquals(esperado, real)` | Los valores deben ser iguales |
| `assertTrue(condicion)` | La condición debe ser verdadera |
| `assertThrows(Exception.class, () -> {...})` | Debe lanzar esa excepción |
| `assertAll(...)` | Agrupa múltiples aserciones |

---

## 🔍 Configuración para Tests

### application.properties (test)
```properties
# Base de datos H2 en memoria
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# JWT para tests
jwt.secret=test-secret-key-for-testing-only

# Email simulado
spring.mail.username=test@test.com
spring.mail.password=test
```

**¿Por qué esta configuración?**
- H2 reemplaza PostgreSQL (más rápido)
- Esquema se crea/destruye automáticamente
- Credenciales de prueba (no se envían emails reales)

### pom.xml - Configuración Mockito
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.5.3</version>
    <configuration>
        <argLine>-XX:+EnableDynamicAgentLoading</argLine>
    </configuration>
</plugin>
```

**¿Para qué sirve?**
- Permite que Mockito funcione con Java 21
- Evita warnings sobre carga dinámica de agentes

---

## 📝 Mejores Prácticas

### ✅ Hacer
1. **Nombres descriptivos:** `crearLote_debeAsignarActivoTrue` (qué hace y qué espera)
2. **Un concepto por test:** Cada test valida UNA cosa
3. **AAA Pattern:** Arrange, Act, Assert siempre
4. **Tests independientes:** No dependen del orden de ejecución
5. **Mocks para dependencias:** Aislar la clase bajo prueba

### ❌ Evitar
1. ❌ Tests que dependen de base de datos real
2. ❌ Tests que dependen de otros tests
3. ❌ Lógica compleja dentro de los tests
4. ❌ Tests sin aserciones (no validan nada)
5. ❌ Código duplicado (usar `@BeforeEach`)

---

## 🐛 Problemas Comunes y Soluciones

### Problema 1: "Cannot find symbol: method X"
**Causa:** El método no existe o tiene otro nombre en el servicio
**Solución:** Verificar con `grep` qué métodos públicos tiene el servicio

### Problema 2: "Wanted but not invoked"
**Causa:** El mock no coincide con el método real llamado
**Solución:** Leer el mensaje de error, muestra qué método se llamó realmente

### Problema 3: Test pasa localmente pero falla en CI/CD
**Causa:** Dependencia de estado externo (fecha, hora, archivos)
**Solución:** Usar datos fijos en los tests, no dependencias externas

### Problema 4: Tests muy lentos
**Causa:** Tests de integración que levantan todo Spring
**Solución:** Usar tests unitarios con mocks cuando sea posible

---

## 📈 Cobertura de Código

### ¿Qué es?
Porcentaje de código cubierto por tests. Ejemplo: 80% significa que el 80% del código tiene al menos un test.

### ¿Cómo medirla?
```bash
# Generar reporte de cobertura con JaCoCo
mvn test jacoco:report
```

### Meta Recomendada
- **Servicios críticos:** 80-90%
- **Controladores:** 70-80%
- **DTOs/Entities:** 50-60%

---

## 🎯 Próximos Pasos

### 1. **Agregar más tests**
- Tests para `GerminacionService`
- Tests para `PmsService`
- Tests para `DosnService`

### 2. **Tests de integración**
- Probar controladores completos con `@WebMvcTest`
- Probar endpoints con `MockMvc`

### 3. **Tests de repositorio**
- Queries personalizadas con `@DataJpaTest`

---

## 💡 Conclusión

**Los tests no son opcionales, son parte del código de producción.**

- ✅ Detectan bugs antes de producción
- ✅ Documentan cómo funciona el código
- ✅ Dan confianza para refactorizar
- ✅ Mejoran la calidad general del proyecto

**Exit Code: 0 = Proyecto sano y confiable** 🎉

---

## 📚 Recursos Adicionales

- **JUnit 5 Docs:** https://junit.org/junit5/docs/current/user-guide/
- **Mockito Docs:** https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html
- **Spring Boot Testing:** https://spring.io/guides/gs/testing-web/
- **AAA Pattern:** https://martinfowler.com/bliki/GivenWhenThen.html

---

## ❓ Preguntas Frecuentes

**Q: ¿Por qué usar mocks en lugar de la base de datos real?**
A: Mocks son más rápidos, no requieren configuración externa, y aíslan la lógica de negocio.

**Q: ¿Cuándo usar tests unitarios vs tests de integración?**
A: Tests unitarios para lógica de negocio. Tests de integración para verificar que todo funciona junto.

**Q: ¿Debo testear getters/setters?**
A: No, son generados automáticamente (Lombok). Testea lógica de negocio.

**Q: ¿Qué hacer si un test falla después de un cambio?**
A: Si el cambio es intencional, actualiza el test. Si no, corrige el código.

---

**Creado por el equipo INIA - 2025**
**Versión Java: 21 | Spring Boot: 3.5.5 | JUnit: 5**
