# Hallazgos y Diagnóstico — Ejercicio 1 (SupplierController)

## Fallo 1 — Contexto de prueba no levanta por falta de SupplierWebMapper
- **Síntoma**: Los 5 tests fallan con `NoSuchBeanDefinitionException: No qualifying bean of type 'com.indra.catalog.suppliers.web.SupplierWebMapper' available`.
- **Causa raíz**: `@WebMvcTest(SupplierController.class)` es un slice test que solo escanea componentes específicos de la capa MVC (`@Controller`, `@RestController`, `@ControllerAdvice`, `Converter`, etc.). No carga componentes genéricos marcados con `@Component` (como `SupplierWebMapper`), a pesar de residir en el paquete `web`. Al instanciar `SupplierController`, el constructor requiere `SupplierWebMapper` y el ApplicationContext falla.
- **Corrección**: Se agregó `@Import(SupplierWebMapper.class)` en `SupplierControllerTest` para incorporar explícitamente el mapper en el contexto del slice.
- **Prevención**: Comprender la delimitación de beans que carga `@WebMvcTest`. Si un controller tiene dependencias utilitarias anotadas con `@Component`, se deben importar con `@Import` o simular con `@MockBean`/`@SpyBean`.

## Fallo 2 — Validación en POST no ejecutada por ausencia de @Valid
- **Síntoma**: `test4` esperaba `400 Bad Request` ante un nombre vacío, pero retornaba `500 Internal Server Error` (NullPointerException).
- **Causa raíz**: El parámetro `request` en el método `create` de `SupplierController` carecía de la anotación `@Valid`. Sin ella, Spring no activa el validador de Jakarta Bean Validation (`MethodArgumentNotValidException`), dejando pasar payloads inválidos a la lógica interna.
- **Corrección**: Se añadió `@Valid` antes de `@RequestBody CreateSupplierRequest request` en `SupplierController`.
- **Prevención**: Aplicar siempre `@Valid` en todo endpoint de mutación (`POST`/`PUT`/`PATCH`) que reciba un DTO con restricciones de Bean Validation.

## Fallo 3 — Header Location mal formado en la respuesta de creación
- **Síntoma**: `test3` fallaba en la aserción del header `Location`. Se esperaba `http://localhost/api/suppliers/SUP-001` pero se recibía `http://localhost/api/suppliersSUP-001`.
- **Causa raíz**: En `SupplierController`, se construía el path con `ServletUriComponentsBuilder.fromCurrentRequest().path("{id}")` sin la barra inclinada `/`, concatenando el ID directamente al final de la ruta base.
- **Corrección**: Se corrigió a `.path("/{id}")`.
- **Prevención**: Verificar siempre mediante tests de integración/slice la sintaxis de las cabeceras HTTP autogeneradas como `Location`.

## Fallo 4 — Código HTTP incorrecto en eliminación (200 en lugar de 204)
- **Síntoma**: `test5` esperaba status `204 No Content`, pero recibía `200 OK`.
- **Causa raíz**: `SupplierController.delete` retornaba `ResponseEntity.ok().build()`. La convención REST para operaciones de borrado exitosas sin cuerpo en la respuesta dicta el uso de `204 No Content`.
- **Corrección**: Se cambió el retorno a `ResponseEntity.noContent().build()`.
- **Prevención**: Estandarizar las convenciones REST del equipo (POST -> 201, DELETE exitoso sin cuerpo -> 204, etc.) y asegurarlas mediante contratos en tests.

## Refactors aplicados

| Principio / práctica | Antes | Después | Test que lo respalda |
|---|---|---|---|
| **DIP (Dependency Inversion Principle)** | `SupplierController` dependía directamente de `SupplierServiceImpl` (clase concreta) y `SupplierServiceImpl` instanciaba `InMemorySupplierRepository` con `new`. | Se creó la interfaz `SupplierService`. El controller y los tests dependen de la interfaz; la implementación recibe el repositorio por constructor. | `SupplierControllerTest` |
| **SRP (Single Responsibility Principle) & Dominio** | La normalización (mayúsculas y remoción de puntos en el NIT) y asignación de notas internas se realizaban en `SupplierController`. | Se encapsuló la normalización en la entidad de dominio `Supplier.normalize()` y se orquesta en `SupplierServiceImpl`. El controller solo traduce HTTP. | `SupplierTest` (3 tests unitarios) |
| **Seguridad (OWASP / Information Leak)** | `ApiExceptionHandler` retornaba `exception.toString()` en el cuerpo de errores 500 (`INTERNAL_ERROR`), exponiendo detalles internos del sistema. | Se reemplazó por un mensaje genérico seguro: `"Ha ocurrido un error interno inesperado"`. | `SupplierControllerTest` |
| **Calidad de Testing** | Nombres genéricos (`test1` a `test5`) y aserción frágil de JSON mediante String plano en `test1`. | Nombres descriptivos bajo convención Given/When/Then, `@DisplayName`, aserciones precisas con `jsonPath` y verificación de `verifyNoInteractions`. | `SupplierControllerTest` |

