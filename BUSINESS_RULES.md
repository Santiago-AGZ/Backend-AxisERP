AUTH SERVICE — REGLAS DE NEGOCIO

=========================
USUARIOS
=========================

1. Todo usuario debe tener nombre obligatorio.

2. Todo usuario debe tener correo electrónico obligatorio.

3. Todo usuario debe tener correo electrónico único.

4. El correo electrónico debe tener formato válido.

5. No se permiten usuarios duplicados.

6. Todo usuario debe tener un rol válido.

Roles permitidos:

- ADMIN
- VENDEDOR
- INVENTARIO

7. Todo usuario nuevo debe tener un rol por defecto cuando aplique.

8. Todo usuario debe tener un estado válido.

Estados permitidos:

- PENDIENTE
- ACTIVO
- INACTIVO
- ELIMINADO

9. Los usuarios eliminados lógicamente no deben aceptar nuevas operaciones.

10. Los usuarios inactivos no deben poder autenticarse.

11. Los usuarios pendientes deben completar activación antes de acceder.

12. El sistema debe utilizar eliminación lógica para usuarios.

13. El sistema debe conservar historial de usuarios eliminados.

14. El sistema debe mantener auditoría de cambios críticos.

15. El sistema debe registrar:

- usuario creador
- usuario modificador
- fecha de creación
- fecha de actualización


=========================
CONTRASEÑAS
=========================

16. Toda contraseña debe cumplir políticas de seguridad.

17. La contraseña debe tener una longitud mínima permitida.

18. La contraseña debe tener una longitud máxima permitida.

19. La contraseña debe contener:

- al menos una letra mayúscula
- al menos una letra minúscula
- al menos un número
- al menos un carácter especial

20. La contraseña no puede contener espacios vacíos.

21. La contraseña no debe almacenarse en texto plano.

22. Las contraseñas deben almacenarse cifradas.

23. El sistema no debe exponer contraseñas en respuestas.

24. El sistema debe impedir reutilizar la contraseña actual.

25. El sistema puede restringir reutilización de contraseñas recientes.

26. El sistema debe permitir recuperación de contraseña.

27. El sistema debe permitir restablecimiento seguro de contraseña.

28. Los enlaces de recuperación deben expirar.

29. Los tokens de recuperación solo deben utilizarse una vez.


=========================
AUTENTICACIÓN
=========================

30. Toda autenticación debe validar credenciales.

31. Las credenciales inválidas deben rechazar acceso.

32. El sistema debe generar tokens seguros.

33. Los tokens deben tener tiempo de expiración.

34. Los tokens expirados no deben reutilizarse.

35. Los tokens invalidados no deben reutilizarse.

36. El sistema debe permitir cierre de sesión.

37. El cierre de sesión debe invalidar tokens activos.

38. El sistema debe permitir renovación de tokens.

39. El sistema debe validar integridad del token.

40. El sistema debe validar firma del token.

41. El sistema debe validar permisos asociados al usuario.

42. Los usuarios sin permisos suficientes deben rechazar acceso.


=========================
SEGURIDAD
=========================

43. El sistema debe limitar intentos fallidos de autenticación.

44. Múltiples intentos fallidos deben activar bloqueo temporal.

45. El sistema debe registrar intentos fallidos.

46. Las operaciones críticas deben requerir reautenticación.

47. El sistema debe validar permisos antes de ejecutar acciones críticas.

48. El sistema debe impedir acceso a recursos restringidos.

49. El sistema debe permitir revocar sesiones activas.

50. El sistema debe registrar actividad sospechosa.


=========================
AUDITORÍA
=========================

51. Toda operación crítica debe registrarse.

52. El sistema debe registrar:

- inicio de sesión
- cierre de sesión
- recuperación de contraseña
- cambio de contraseña
- actualización de usuario
- cambios de roles
- eliminación lógica

53. El sistema debe registrar:

- usuario
- fecha
- acción realizada
- dirección IP cuando aplique

54. El historial de auditoría no debe modificarse.

55. El sistema debe conservar trazabilidad histórica completa.+
=========================
CATEGORÍAS
=========================

1. Toda categoría debe tener nombre obligatorio.

2. El nombre de la categoría debe ser único.

3. El nombre no puede estar vacío.

4. La descripción es opcional.

5. Una categoría puede existir sin subcategorías.

6. Una categoría puede tener múltiples subcategorías.

7. Una subcategoría solo puede tener una categoría padre.

8. Una categoría no puede ser padre de sí misma.

9. No se permiten ciclos jerárquicos.

10. Una categoría hija debe pertenecer a una categoría existente.

11. Una categoría puede existir sin productos asociados.

12. Toda categoría debe tener estado válido.

Estados permitidos:

- ACTIVA
- INACTIVA
- ELIMINADA

13. Las categorías inactivas no deben aceptar nuevos productos.

14. Las categorías eliminadas no deben aceptar nuevas asociaciones.

15. Las categorías deben utilizar eliminación lógica.

16. Una categoría con productos asociados no puede eliminarse físicamente.

17. El sistema debe mantener historial de categorías.

18. Toda operación crítica debe registrarse en auditoría.

19. El sistema debe registrar:

- usuario creador
- usuario modificador
- fecha creación
- fecha actualización
=========================
PRODUCTOS
=========================

1. Todo producto debe tener nombre obligatorio.

2. Todo producto debe tener código único.

3. No se permiten productos con códigos duplicados.

4. Todo producto debe pertenecer a una categoría válida.

5. No se permiten categorías inexistentes.

6. No se permiten categorías inactivas.

7. El precio de venta debe ser mayor que cero.

8. El costo no puede ser negativo.

9. El precio de venta no puede ser menor que el costo cuando la política empresarial lo restrinja.

10. El sistema debe calcular margen cuando aplique.

11. El producto puede tener descripción opcional.

12. Todo producto debe tener estado válido.

Estados permitidos:

- ACTIVO
- INACTIVO
- ELIMINADO

13. Los productos eliminados no pueden venderse.

14. Los productos eliminados no pueden comprarse.

15. Los productos inactivos no pueden participar en nuevas operaciones.

16. Los productos deben utilizar eliminación lógica.

17. El sistema debe soportar múltiples códigos de barras.

18. Un producto puede tener varios códigos.

19. Solo un código de barras puede ser principal.

20. No deben existir códigos duplicados.

21. El sistema debe mantener historial de cambios.

22. El sistema debe registrar auditoría.

23. El sistema debe soportar:

- búsqueda
- filtros
- paginación
- ordenamiento
=========================
INVENTARIO
=========================

1. Todo producto inventariable debe tener un único registro de inventario.

2. El stock actual no puede ser negativo.

3. El stock mínimo no puede ser negativo.

4. El stock máximo no puede ser negativo.

5. El stock máximo debe ser mayor que el mínimo.

6. El inventario inicial solo puede registrarse una vez.

7. Las entradas aumentan stock.

8. Las salidas disminuyen stock.

9. Las salidas no pueden exceder stock disponible.

10. Las cantidades deben ser mayores que cero.

11. Todo movimiento debe registrar:

- stock anterior
- stock nuevo
- usuario
- fecha

12. Todo movimiento debe tener un tipo válido.

Tipos permitidos:

- INVENTARIO_INICIAL
- ENTRADA
- SALIDA
- AJUSTE_POSITIVO
- AJUSTE_NEGATIVO
- DEVOLUCION
- ANULACION

13. Los ajustes deben requerir justificación.

14. Las anulaciones deben generar movimientos de reversión.

15. Los movimientos históricos no deben modificarse.

16. El sistema debe generar alertas por stock bajo.

17. El sistema debe generar alertas por agotamiento.

18. Debe existir control de concurrencia.

19. La primera actualización válida debe prevalecer.

20. Toda operación crítica debe registrarse.
=========================
PROVEEDORES
=========================

1. Todo proveedor debe tener nombre obligatorio.

2. El NIT debe ser único.

3. Los proveedores deben tener estado válido.

4. Los proveedores eliminados no aceptan nuevas compras.

5. Los proveedores deben utilizar eliminación lógica.


=========================
COMPRAS
=========================

6. Toda compra debe tener proveedor válido.

7. Toda compra debe tener al menos un producto.

8. Los productos deben existir.

9. Las cantidades deben ser mayores que cero.

10. Los precios unitarios deben ser mayores que cero.

11. Un producto no puede repetirse dentro de una misma compra.

12. El subtotal debe calcularse automáticamente.

13. Los impuestos deben calcularse automáticamente.

14. El total debe calcularse automáticamente.

15. El total no puede ser negativo.

16. Toda compra debe tener estado válido.

Estados permitidos:

- BORRADOR
- PENDIENTE
- RECIBIDA
- PAGADA
- CANCELADA

17. Las compras canceladas no pueden modificarse.

18. Las compras pagadas no pueden modificarse.

19. Una compra recibida debe actualizar inventario.

20. Recepciones parciales deben mantener saldo pendiente.

21. Las anulaciones deben revertir movimientos asociados.

22. Toda operación crítica debe registrarse.
=========================
CLIENTES
=========================

1. Documento único.

2. Correo único.

3. Cliente activo para nuevas ventas.

4. Clientes eliminados usan eliminación lógica.


=========================
VENTAS
=========================

5. Toda venta debe contener al menos un producto.

6. Los productos deben existir y estar activos.

7. Las cantidades deben ser mayores que cero.

8. El stock debe validarse antes de confirmar una venta.

9. No se permiten ventas con stock insuficiente.

10. El subtotal debe calcularse automáticamente.

11. Los descuentos deben calcularse automáticamente.

12. Los descuentos superiores al límite permitido requieren autorización.

13. Los impuestos deben calcularse automáticamente.

14. El total debe calcularse automáticamente.

15. El total no puede ser negativo.

16. Estados permitidos:

- BORRADOR
- PENDIENTE
- CONFIRMADA
- PAGADA
- ANULADA

17. Las ventas anuladas no pueden modificarse.

18. Las ventas pagadas no pueden modificarse.

19. Solo usuarios autorizados pueden anular ventas.

20. La anulación debe restaurar stock.

21. Debe existir control de concurrencia.

22. Toda operación crítica debe registrarse.


=========================
FACTURAS
=========================

23. Toda venta confirmada genera factura.

24. La factura debe tener número secuencial único.

25. La factura conserva snapshot de:

- cliente
- productos
- precios

26. Las facturas emitidas no pueden modificarse.

27. Debe permitirse exportar:

- PDF
- Excel
- CSV
=========================
REPORTES
=========================

1. El sistema debe generar reportes de ventas.

2. El sistema debe generar reportes de inventario.

3. El sistema debe generar reportes de productos más vendidos.

4. El sistema debe generar reportes de clientes frecuentes.

5. El sistema debe generar dashboards.

6. Los reportes deben permitir filtros combinables.

7. Debe permitirse filtrar por:

- fechas
- vendedor
- cliente
- categoría
- estado

8. Debe permitirse exportar:

- PDF
- Excel
- CSV

9. Los reportes no deben modificar información almacenada.

10. Los reportes deben registrar auditoría.

11. Solo usuarios autorizados pueden acceder.

12. El sistema debe mantener historial de exportaciones.

13. El sistema debe mantener consistencia entre servicios externos.
=========================
IDENTIFICADORES DE NEGOCIO
=========================

1. El sistema debe mantener un identificador interno único (UUID) para cada entidad.

2. El identificador interno debe utilizarse para relaciones internas entre entidades.

3. El sistema debe permitir códigos visibles para entidades de negocio que lo requieran.

4. Los códigos visibles no reemplazan el identificador interno.

5. Los códigos visibles deben ser únicos por entidad.

6. Los códigos visibles pueden utilizar prefijos según el módulo.

Ejemplos:

- PROD-000001
- PROV-000001
- CLI-000001
- VTA-2026-000001
- COM-2026-000001
- FAC-2026-000001

7. Los códigos visibles deben poder utilizarse en búsquedas, filtros y reportes.

8. El sistema no debe depender de códigos visibles para mantener integridad referencial.

9. Las relaciones entre entidades deben utilizar identificadores internos (UUID).

10. El sistema puede ocultar identificadores internos en interfaces de usuario cuando no sean necesarios.

11. Las siguientes entidades deben soportar códigos visibles:

- productos
- proveedores
- clientes
- ventas
- compras
- facturas

12. Las siguientes entidades pueden utilizar únicamente identificadores internos:

- auditorías
- logs
- sesiones
- tokens
- eventos internos
- tablas puente

13. Las entidades de usuarios y categorías pueden utilizar códigos visibles de forma opcional según necesidades del negocio.
