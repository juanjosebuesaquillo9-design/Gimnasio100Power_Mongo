# Gimnasio 100% Power — Sistema de Gestión NoSQL (MongoDB + Spring Boot)

Aplicación web integral para la gestión administrativa, operativa y control de acceso de afiliados del **Gimnasio 100% Power**, migrada desde una arquitectura relacional (MySQL) a una base de datos documental NoSQL (**MongoDB**).

## Equipo de Desarrollo
* Juan José Buesaquillo Ramos
* George San Juan
* Giuseppe Rinaldi

## Arquitectura de Base de Datos (`gimnasio_100_power_nosql`)
Se implementó un **modelado de datos híbrido** distribuido en 3 colecciones principales:
* **`clientes`**: Almacena los datos del afiliado junto con subdocumentos embebidos (`membresia`, `historial_pagos` y `rutina_activa`) para consultar el perfil completo en una sola lectura sin necesidad de *JOINs*.
* **`asistencias`**: Colección referenciada mediante `cliente_id` para registrar el historial de accesos al torniquete sin superar el límite de 16 MB por documento.
* **`usuarios_staff`**: Colección protegida con validación de esquema (`$jsonSchema`) e índices únicos para el personal interno (`ADMIN`, `RECEPCION`, `ENTRENADOR`).

## Requisitos y Ejecución
1. Tener **MongoDB** ejecutándose en `localhost:27017` con la base de datos `gimnasio_100_power_nosql` (los scripts de validación, índices y agregaciones se encuentran en la carpeta `/database_nosql`).
2. Abrir el proyecto en **Visual Studio Code** o cualquier IDE compatible con Maven / Java 17+.
3. Ejecutar la clase principal `GimnasioApplication.java` y abrir en el navegador: `http://localhost:8080`

## Credenciales de Acceso (Demo)
* **Administrador:** `admin@100power.com` / `admin123`
* **Recepción:** `recepcion@100power.com` / `admin123`
* **Portal del Afiliado:** Acceso directo mediante consulta de cédula en `/portal-cliente`