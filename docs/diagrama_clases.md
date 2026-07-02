# Diagrama de Clases - Modelo de Dominio (api-backend)

```mermaid
classDiagram
    class Usuario {
        +Integer id
        +String dni
        +String nombres
        +String apellidos
        +String telefono
        +String username
        +String password
        +String email
        +Boolean estado
        +Boolean eliminado
        +LocalDateTime fechaCreacion
    }

    class Rol {
        +Integer id
        +String nombreRol
    }

    class Departamento {
        +Integer id
        +String bloqueTorre
        +String numeroDepa
    }

    class Visitante {
        +String dni
        +String nombre
        +String apellidos
        +String telefono
        +String empresaDelivery
        +Boolean esFrecuente
        +Boolean bloqueado
        +String motivoBloqueo
    }

    class Invitacion {
        +Integer id
        +String codigoQrHash
        +LocalDate fechaProgramada
        +LocalDateTime horaExpiracion
        +String estado
        +LocalDateTime fechaCreacion
    }

    class RegistroAcceso {
        +Integer id
        +String tipoIngreso
        +String tipoVisita
        +LocalDateTime horaIngreso
        +LocalDateTime horaSalida
        +String placaVehiculo
        +String observaciones
        +String estadoAcceso
    }

    class ObjetoRegistrado {
        +Integer id
        +String descripcion
        +String marcaModelo
        +String numeroSerie
        +LocalDateTime fechaRegistro
    }

    class EventoSeguridad {
        +Integer id
        +String tipoEvento
        +String codigoQrIntentado
        +String dniVisitante
        +String detalle
        +LocalDateTime fechaHora
    }

    class DispositivoPush {
        +Integer id
        +String tokenPush
        +String plataforma
        +LocalDateTime fechaRegistro
    }

    Usuario "many" --> "1" Rol : rol
    Usuario "many" --> "0..1" Departamento : departamento
    Departamento "1" --> "many" Usuario : residentes

    Invitacion "many" --> "1" Usuario : anfitrion
    Invitacion "many" --> "1" Visitante : visitante
    Invitacion "many" --> "1" Departamento : departamentoDestino

    RegistroAcceso "many" --> "1" Visitante : visitante
    RegistroAcceso "many" --> "1" Departamento : departamentoDestino
    RegistroAcceso "many" --> "0..1" Usuario : residenteQueAutoriza
    RegistroAcceso "many" --> "1" Usuario : conserjeEnTurno
    RegistroAcceso "many" --> "0..1" Invitacion : invitacion

    ObjetoRegistrado "many" --> "1" RegistroAcceso : registroAcceso

    EventoSeguridad "many" --> "0..1" Usuario : conserje

    DispositivoPush "many" --> "1" Usuario : usuario
```

## Versión de negocio (asociaciones con verbos)

Misma estructura, pero con las relaciones nombradas según la acción que representan en el dominio (más legible para personas no técnicas):

```mermaid
classDiagram
    class Usuario
    class Rol
    class Departamento
    class Visitante
    class Invitacion
    class RegistroAcceso
    class ObjetoRegistrado
    class EventoSeguridad
    class DispositivoPush

    Usuario "many" --> "1" Rol : tiene
    Usuario "many" --> "0..1" Departamento : reside en

    Usuario "1" --> "many" Invitacion : crea (anfitrión)
    Invitacion "many" --> "1" Visitante : invita a
    Invitacion "many" --> "1" Departamento : tiene como destino

    RegistroAcceso "many" --> "1" Visitante : registra el ingreso de
    RegistroAcceso "many" --> "1" Departamento : registra el acceso a
    Usuario "1" --> "many" RegistroAcceso : autoriza (residente)
    Usuario "1" --> "many" RegistroAcceso : registra (conserje)
    Invitacion "0..1" --> "0..1" RegistroAcceso : genera

    RegistroAcceso "1" --> "many" ObjetoRegistrado : declara

    Usuario "1" --> "many" EventoSeguridad : reporta (conserje)

    Usuario "1" --> "many" DispositivoPush : posee
```

Cada relación se lee en el sentido de la flecha, sujeto → verbo → objeto:
- Un Usuario tiene un Rol.
- Un Usuario reside en un Departamento.
- Un Usuario (anfitrión) crea muchas Invitaciones.
- Una Invitación invita a un Visitante.
- Una Invitación tiene como destino un Departamento.
- Un RegistroAcceso registra el ingreso de un Visitante.
- Un RegistroAcceso registra el acceso a un Departamento.
- Un Usuario (residente) autoriza muchos RegistroAcceso.
- Un Usuario (conserje) registra muchos RegistroAcceso.
- Una Invitación genera (opcionalmente) un RegistroAcceso.
- Un RegistroAcceso declara muchos ObjetoRegistrado.
- Un Usuario (conserje) reporta muchos EventoSeguridad.
- Un Usuario posee muchos DispositivoPush.
