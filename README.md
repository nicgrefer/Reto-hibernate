# Sede Electrónica GF — Resumen de cambios y funcionamiento
Este README explica los cambios recientes y cómo funciona la aplicación a nivel de persistencia y flujo de registro. Está pensado para que puedas explicarlo al profesor.

## Resumen rápido
- Corregidos los mappings para apuntar al paquete real `com.sede.entities`.
- El servlet de registro ahora persiste realmente los objetos `Registros` en la base de datos.
- Se aplicaron defensas temporales para facilitar el arranque al depurar dependencias (NoClassDefFoundError).

## Cambios principales
- Configuración / mappings:
  - `src/main/java/hibernate.cfg.xml`: mappings actualizados a `com.sede.entities.Entidad` y `com.sede.entities.Registros`.
  - `src/main/java/com/sede/entities/Entidad.hbm.xml` y `Registros.hbm.xml`: nombres de clase corregidos.

- Lógica y persistencia:
  - `src/main/java/com/sede/servlet/ServletRegistro.java`: ahora asigna `numRegistro` y solicita persistencia.
  - `src/main/java/com/sede/ln/RegistrosLN.java`: genera `numRegistro` y delega la persistencia a `RegistrosDAO`.
  - `src/main/java/com/sede/dao/RegistrosDAO.java` y `EntidadDAO.java`: operaciones con `Session`/`Transaction`.
  - `src/main/java/com/sede/dao/HibernateUtil.java`: gestión de `SessionFactory`.

## Flujo de ejecución (pasos)
1. Usuario envía formulario en `Registro.jsp` al `ServletRegistro`.
2. El servlet valida y recupera la `Entidad` mediante `EntidadDAO`.
3. `RegistrosLN.generarNumeroRegistro()` obtiene `COUNT(*)` de la tabla y genera `REG_XXXXXX`.
4. Se asigna `numRegistro` al objeto `Registros` y se llama a `RegistrosLN.procesarRegistro(registro)`.
5. `RegistrosDAO.guardarRegistro(registro)` abre sesión, persiste con `session.persist(registro)` y hace `commit()`.
6. El servlet muestra la página de confirmación con `numRegistro` y `fechaRegistro` si la persistencia fue exitosa.

## Archivos modificados (resumen)
- `src/main/java/hibernate.cfg.xml`
- `src/main/java/com/sede/entities/Entidad.hbm.xml`
- `src/main/java/com/sede/entities/Registros.hbm.xml`
- `src/main/java/com/sede/servlet/ServletRegistro.java`
- `src/main/java/com/sede/ln/RegistrosLN.java`
- `src/main/java/com/sede/dao/RegistrosDAO.java`
- `src/main/java/com/sede/dao/EntidadDAO.java`
- `src/main/java/com/sede/dao/HibernateUtil.java`

## Comandos útiles para comprobar el WAR y dependencias
```powershell
mvn clean package
jar tf target\SedeElectronicaGF.war | Select-String 'hibernate'
Get-ChildItem -Path target\SedeElectronicaGF\WEB-INF\lib | Where-Object { $_.Name -like '*hibernate*' }
```

En Eclipse: `Project -> Maven -> Update Project`, luego `Project -> Clean` y redeploy en el servidor.

## Pruebas rápidas
- Inserta varios registros desde la UI y usa la búsqueda por número para verificar persistencia.
- Revisa la tabla `registros` en MySQL: `SELECT * FROM registros ORDER BY fecha_registro DESC;`.

## Notas técnicas y recomendaciones (para explicar al profesor)
- Generación de `numRegistro`: actualmente usa `COUNT(*)` y `contador + 1`. Funciona pero no es seguro en alta concurrencia — recomendar usar una secuencia/autoincrement o generación en BD.
- `NoClassDefFoundError`: indica que `hibernate-core` no estaba en el classpath del WAR. Asegúrate de que las dependencias de Hibernate estén en `WEB-INF/lib` o disponibles en el servidor.
- Cambios temporales: se sustituyó `catch (HibernateException)` por `catch (Exception)` en algunos lugares para facilitar el diagnóstico. Esto no reemplaza la inclusión correcta de las dependencias.

## Sugerencia para la demo
- Inserta 3 registros y muéstralos desde la UI y desde la BD.
- Explica la limitación de concurrencia y cómo solucionarla (secuencia/autoincrement).

---
Si quieres, genero una versión corta orientada al profesor (`README_resumen_para_profesor.md`) con 1 página y respuestas rápidas. ¿La añado? 

**Notas técnicas y recomendaciones (para preguntas del profesor)**
- Sobre generación de `numRegistro`: se usa `COUNT(*)` sobre la tabla y luego `contador + 1` para formar `REG_%06d`. Esto es sencillo pero no es seguro en entornos con alta concurrencia: dos peticiones simultáneas podrían generar el mismo número antes de que una de las dos haga commit. Para producción se recomienda usar:
  - una secuencia/autoincrement en la BD, o
  - una tabla/generador de números con bloqueo, o
  - valores generados por la propia BD (por ejemplo, `AUTO_INCREMENT`) y luego formatear.

- Sobre el error `NoClassDefFoundError: org/hibernate/...`: indicaba que las librerías de Hibernate no estaban en el classpath del WAR en el servidor. La solución es asegurarse de que `hibernate-core` y dependencias estén en `WEB-INF/lib` (o disponibles en el classpath del servidor). En Maven esto significa no marcar `hibernate-core` como `provided` si quieres empaquetarlo, o configurar el servidor.

- Medida temporal aplicada: en varios `catch` se cambió `catch (HibernateException)` por `catch (Exception)` para evitar fallos en el arranque al no encontrar la clase. Esto es una ayuda para detectar y manejar errores, pero no sustituye a la inclusión correcta de dependencias.

**Pruebas sugeridas para la demo**
- Mostrar inserción de 3 registros seguidos y usar la búsqueda por número para cada uno.
- Abrir la BD y mostrar que las filas se insertaron con `num_registro` y `fecha_registro`.
- Comentar que en producción hay que reemplazar la generación por `COUNT(*)` por un mecanismo transaccional/BD.

---
Si quieres, genero también un `slides.md` o un `README_resumen_para_profesor.md` más corto (1 página) con los puntos clave y respuestas preparadas para preguntas comunes. ¿Lo dejo ya guardado en el repo o prefieres primero revisar algo más?