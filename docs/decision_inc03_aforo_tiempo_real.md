# Decisión — INC-03: Aforo en tiempo real

**Incidencia:** el panel "Visitantes Activos" se actualiza por polling cada 17s
(`REFRESCO_MS` en [VisitantesActivos.jsx](../app-frontend/src/features/visitantes/VisitantesActivos.jsx)),
por lo que un ingreso/salida registrado por un conserje no se refleja de
inmediato en otros clientes conectados.

**Decisión:** se deja como **limitación conocida** para esta entrega. No se
migra a WebSockets/SSE por ahora.

**Motivo:**
- El polling de 17s ya cumple el requisito funcional (el aforo se actualiza
  sin recargar la página); el problema es solo de latencia entre clientes,
  no de exactitud del dato.
- Migrar a push (STOMP sobre Spring o SSE) implica cambios en backend
  (endpoint de eventos), frontend (cliente de suscripción) y en la
  infraestructura de despliegue (verificar que Azure/el proxy soporten
  conexiones persistentes), lo que no es viable antes de la entrega final
  sin arriesgar regresiones en un flujo crítico (control de acceso).

**Mejora futura:** migrar `VisitantesActivos` a WebSockets (STOMP) o SSE,
empujando el nuevo conteo de aforo al backend cuando se confirma un
`registrarEntradaQR` / `registrarEntradaManual` / salida, en vez de que el
cliente lo pida por polling.

**Criterio de aceptación de esta decisión:** documentada aquí; no bloquea la
entrega. Si se decide implementar el push en una iteración futura, actualizar
este documento con el nuevo diseño.
