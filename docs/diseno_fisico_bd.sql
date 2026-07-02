-- ============================================================
-- Diseño Físico de la Base de Datos - Proyecto Los Robles
-- Motor: SQL Server (Azure SQL Database)
-- Generado a partir del modelo JPA en api-backend
-- ============================================================

-- ============================================================
-- 1. tbl_roles
-- ============================================================
CREATE TABLE tbl_roles (
    rol_id      INT IDENTITY(1,1) NOT NULL,
    nombre_rol  VARCHAR(50) NOT NULL,
    CONSTRAINT PK_tbl_roles PRIMARY KEY (rol_id)
);

-- ============================================================
-- 2. tbl_departamentos
-- ============================================================
CREATE TABLE tbl_departamentos (
    id_departamento INT IDENTITY(1,1) NOT NULL,
    bloque_torre    VARCHAR(50) NULL,
    numero_depa     VARCHAR(20) NULL,
    CONSTRAINT PK_tbl_departamentos PRIMARY KEY (id_departamento)
);

-- ============================================================
-- 3. tbl_usuarios
-- ============================================================
CREATE TABLE tbl_usuarios (
    id_usuario      INT IDENTITY(1,1) NOT NULL,
    dni             VARCHAR(15) NOT NULL,
    nombres         VARCHAR(100) NOT NULL,
    apellidos       VARCHAR(100) NOT NULL,
    telefono        VARCHAR(15) NULL,
    username        VARCHAR(50) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    email           VARCHAR(100) NULL,
    estado          BIT NOT NULL CONSTRAINT DF_tbl_usuarios_estado DEFAULT (1),
    eliminado       BIT NOT NULL CONSTRAINT DF_tbl_usuarios_eliminado DEFAULT (0),
    fecha_creacion  DATETIME2 NOT NULL CONSTRAINT DF_tbl_usuarios_fecha_creacion DEFAULT (SYSUTCDATETIME()),
    rol_id          INT NULL,
    id_departamento INT NULL,
    CONSTRAINT PK_tbl_usuarios PRIMARY KEY (id_usuario),
    CONSTRAINT UQ_tbl_usuarios_dni UNIQUE (dni),
    CONSTRAINT UQ_tbl_usuarios_username UNIQUE (username),
    CONSTRAINT UQ_tbl_usuarios_email UNIQUE (email),
    CONSTRAINT FK_usuarios_rol FOREIGN KEY (rol_id)
        REFERENCES tbl_roles (rol_id),
    CONSTRAINT FK_usuarios_departamento FOREIGN KEY (id_departamento)
        REFERENCES tbl_departamentos (id_departamento)
);

CREATE NONCLUSTERED INDEX IX_usuarios_rol_id ON tbl_usuarios (rol_id);
CREATE NONCLUSTERED INDEX IX_usuarios_id_departamento ON tbl_usuarios (id_departamento);

-- ============================================================
-- 4. tbl_visitantes
-- ============================================================
CREATE TABLE tbl_visitantes (
    dni_visitante     VARCHAR(15) NOT NULL,
    nombre            VARCHAR(100) NULL,
    apellidos         VARCHAR(100) NULL,
    telefono          VARCHAR(15) NULL,
    empresa_delivery  VARCHAR(100) NULL,
    es_frecuente      BIT NOT NULL CONSTRAINT DF_tbl_visitantes_es_frecuente DEFAULT (0),
    bloqueado         BIT NOT NULL CONSTRAINT DF_tbl_visitantes_bloqueado DEFAULT (0),
    motivo_bloqueo    VARCHAR(255) NULL,
    CONSTRAINT PK_tbl_visitantes PRIMARY KEY (dni_visitante)
);

-- ============================================================
-- 5. tbl_invitaciones
-- ============================================================
CREATE TABLE tbl_invitaciones (
    id_invitacion           INT IDENTITY(1,1) NOT NULL,
    codigo_qr_hash          VARCHAR(255) NOT NULL,
    id_residente_anfitrion  INT NOT NULL,
    dni_visitante           VARCHAR(15) NOT NULL,
    id_departamento_destino INT NOT NULL,
    fecha_programada        DATE NOT NULL,
    hora_expiracion         DATETIME2 NULL,
    estado                  VARCHAR(20) NOT NULL CONSTRAINT DF_tbl_invitaciones_estado DEFAULT ('PENDIENTE'),
    fecha_creacion          DATETIME2 NOT NULL CONSTRAINT DF_tbl_invitaciones_fecha_creacion DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_tbl_invitaciones PRIMARY KEY (id_invitacion),
    CONSTRAINT UQ_tbl_invitaciones_codigo_qr_hash UNIQUE (codigo_qr_hash),
    CONSTRAINT FK_invitaciones_anfitrion FOREIGN KEY (id_residente_anfitrion)
        REFERENCES tbl_usuarios (id_usuario),
    CONSTRAINT FK_invitaciones_visitante FOREIGN KEY (dni_visitante)
        REFERENCES tbl_visitantes (dni_visitante),
    CONSTRAINT FK_invitaciones_departamento FOREIGN KEY (id_departamento_destino)
        REFERENCES tbl_departamentos (id_departamento),
    CONSTRAINT CK_tbl_invitaciones_estado CHECK (estado IN ('PENDIENTE', 'USADA', 'EXPIRADA', 'CANCELADA'))
);

CREATE NONCLUSTERED INDEX IX_invitaciones_anfitrion ON tbl_invitaciones (id_residente_anfitrion);
CREATE NONCLUSTERED INDEX IX_invitaciones_visitante ON tbl_invitaciones (dni_visitante);
CREATE NONCLUSTERED INDEX IX_invitaciones_departamento ON tbl_invitaciones (id_departamento_destino);

-- ============================================================
-- 6. tbl_registro_accesos
-- ============================================================
CREATE TABLE tbl_registro_accesos (
    id_registro               INT IDENTITY(1,1) NOT NULL,
    dni_visitante             VARCHAR(15) NOT NULL,
    id_departamento_destino   INT NOT NULL,
    id_residente_que_autoriza INT NULL,
    id_conserje_en_turno      INT NOT NULL,
    id_invitacion             INT NULL,
    tipo_ingreso              VARCHAR(20) NOT NULL,
    tipo_visita               VARCHAR(20) NULL,
    hora_ingreso              DATETIME2 NOT NULL CONSTRAINT DF_tbl_registro_accesos_hora_ingreso DEFAULT (SYSUTCDATETIME()),
    hora_salida               DATETIME2 NULL,
    placa_vehiculo            VARCHAR(15) NULL,
    observaciones             VARCHAR(MAX) NULL,
    estado_acceso             VARCHAR(20) NOT NULL CONSTRAINT DF_tbl_registro_accesos_estado DEFAULT ('ACTIVO'),
    CONSTRAINT PK_tbl_registro_accesos PRIMARY KEY (id_registro),
    CONSTRAINT FK_registro_visitante FOREIGN KEY (dni_visitante)
        REFERENCES tbl_visitantes (dni_visitante),
    CONSTRAINT FK_registro_departamento FOREIGN KEY (id_departamento_destino)
        REFERENCES tbl_departamentos (id_departamento),
    CONSTRAINT FK_registro_residente_autoriza FOREIGN KEY (id_residente_que_autoriza)
        REFERENCES tbl_usuarios (id_usuario),
    CONSTRAINT FK_registro_conserje FOREIGN KEY (id_conserje_en_turno)
        REFERENCES tbl_usuarios (id_usuario),
    CONSTRAINT FK_registro_invitacion FOREIGN KEY (id_invitacion)
        REFERENCES tbl_invitaciones (id_invitacion),
    CONSTRAINT CK_tbl_registro_accesos_tipo_ingreso CHECK (tipo_ingreso IN ('QR', 'MANUAL', 'VEHICULAR')),
    CONSTRAINT CK_tbl_registro_accesos_tipo_visita CHECK (tipo_visita IN ('NORMAL', 'FRECUENTE', 'DELIVERY')),
    CONSTRAINT CK_tbl_registro_accesos_estado CHECK (estado_acceso IN ('ACTIVO', 'FINALIZADO'))
);

CREATE NONCLUSTERED INDEX IX_registro_visitante ON tbl_registro_accesos (dni_visitante);
CREATE NONCLUSTERED INDEX IX_registro_departamento ON tbl_registro_accesos (id_departamento_destino);
CREATE NONCLUSTERED INDEX IX_registro_residente ON tbl_registro_accesos (id_residente_que_autoriza);
CREATE NONCLUSTERED INDEX IX_registro_conserje ON tbl_registro_accesos (id_conserje_en_turno);
CREATE NONCLUSTERED INDEX IX_registro_invitacion ON tbl_registro_accesos (id_invitacion);
CREATE NONCLUSTERED INDEX IX_registro_estado ON tbl_registro_accesos (estado_acceso);

-- ============================================================
-- 7. tbl_objetos_registrados
-- ============================================================
CREATE TABLE tbl_objetos_registrados (
    id_objeto       INT IDENTITY(1,1) NOT NULL,
    id_registro     INT NOT NULL,
    descripcion     VARCHAR(255) NOT NULL,
    marca_modelo    VARCHAR(100) NULL,
    numero_serie    VARCHAR(100) NULL,
    fecha_registro  DATETIME2 NOT NULL CONSTRAINT DF_tbl_objetos_registrados_fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_tbl_objetos_registrados PRIMARY KEY (id_objeto),
    CONSTRAINT FK_objetos_registro FOREIGN KEY (id_registro)
        REFERENCES tbl_registro_accesos (id_registro)
);

CREATE NONCLUSTERED INDEX IX_objetos_registro ON tbl_objetos_registrados (id_registro);

-- ============================================================
-- 8. tbl_eventos_seguridad
-- ============================================================
CREATE TABLE tbl_eventos_seguridad (
    id_evento            INT IDENTITY(1,1) NOT NULL,
    tipo_evento          VARCHAR(30) NOT NULL,
    codigo_qr_intentado  VARCHAR(255) NULL,
    dni_visitante        VARCHAR(15) NULL,
    id_conserje          INT NULL,
    detalle              VARCHAR(MAX) NULL,
    fecha_hora           DATETIME2 NOT NULL CONSTRAINT DF_tbl_eventos_seguridad_fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_tbl_eventos_seguridad PRIMARY KEY (id_evento),
    CONSTRAINT FK_eventos_conserje FOREIGN KEY (id_conserje)
        REFERENCES tbl_usuarios (id_usuario),
    CONSTRAINT CK_tbl_eventos_seguridad_tipo CHECK (tipo_evento IN
        ('QR_INVALIDO', 'QR_EXPIRADO', 'QR_YA_USADO', 'VISITANTE_BLOQUEADO', 'RESIDENTE_NO_VINCULADO'))
);

CREATE NONCLUSTERED INDEX IX_eventos_conserje ON tbl_eventos_seguridad (id_conserje);
CREATE NONCLUSTERED INDEX IX_eventos_fecha_hora ON tbl_eventos_seguridad (fecha_hora);

-- ============================================================
-- 9. tbl_dispositivos_push
-- ============================================================
CREATE TABLE tbl_dispositivos_push (
    id_dispositivo  INT IDENTITY(1,1) NOT NULL,
    id_usuario      INT NOT NULL,
    token_push      VARCHAR(255) NOT NULL,
    plataforma      VARCHAR(10) NULL,
    fecha_registro  DATETIME2 NOT NULL CONSTRAINT DF_tbl_dispositivos_push_fecha DEFAULT (SYSUTCDATETIME()),
    CONSTRAINT PK_tbl_dispositivos_push PRIMARY KEY (id_dispositivo),
    CONSTRAINT UQ_tbl_dispositivos_push_token UNIQUE (token_push),
    CONSTRAINT FK_dispositivos_usuario FOREIGN KEY (id_usuario)
        REFERENCES tbl_usuarios (id_usuario),
    CONSTRAINT CK_tbl_dispositivos_push_plataforma CHECK (plataforma IN ('ANDROID', 'IOS'))
);

CREATE NONCLUSTERED INDEX IX_dispositivos_usuario ON tbl_dispositivos_push (id_usuario);

-- ============================================================
-- Datos semilla (catálogo de roles)
-- ============================================================
INSERT INTO tbl_roles (nombre_rol) VALUES
    ('ADMIN'),
    ('RESIDENTE'),
    ('CONSERJE');
