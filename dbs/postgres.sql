-- Tabla de departamentos
CREATE TABLE departamentos
(
    id     SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

-- Tabla de empleados
CREATE TABLE empleados
(
    id              SERIAL PRIMARY KEY,
    nombre          VARCHAR(50) NOT NULL,
    apellido        VARCHAR(50) NOT NULL,
    salario         NUMERIC(10, 2),
    id_departamento INT REFERENCES departamentos (id)
);

-- Datos de ejemplo
INSERT INTO departamentos (nombre)
VALUES ('Sistemas'),
       ('Administración'),
       ('Ventas');

INSERT INTO empleados (nombre, apellido, salario, id_departamento)
VALUES ('Juan', 'Pérez', 80000, 1),
       ('María', 'López', 95000, 1),
       ('Carlos', 'Gómez', 60000, 2),
       ('Ana', 'Martínez', 70000, 3);
