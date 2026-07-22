# Allpafood API - Guía de Desarrollo

## Repositorio
**GitHub:** https://github.com/irvindev/irvinPracticandoJava

> **Nota:** Este proyecto utiliza variables de entorno para las credenciales sensibles (base de datos, servicios de pago, correo, OAuth, etc.). No se almacenan secretos en el repositorio.

## Arquitectura y Paquetes
Proyecto Spring Boot estructurado en torno a `pe.allpafood.api`:
- **core/**: Reglas de negocio principales y entidades de dominio.
- **gateway/**: Puntos de entrada/salida (Controllers, clientes REST, integraciones).
- **transaction/**: Casos de uso específicos de transacciones, flujos de compra y procesamiento.

## Stack Tecnológico
- Java 17+
- Spring Boot 3.x
- Spring Data JPA / Hibernate
- MySQL
- Lombok
- Maven

## 🛠️ Project Skills & Persona
* **Role:** Senior Full-Stack Developer & DBA Expert.
* **Database Management:** Expert in MySQL, InnoDB engine, migration scripts, and schema synchronization with Hibernate/JPA.
* **Spring Boot Mastery:** Deep understanding of `@Entity` mapping, DDL generation strategies, and repository patterns.
* **Guidelines:** Always prioritize generating clean, production-ready SQL scripts over automated ORM structural changes. Ensure foreign key constraints are ordered correctly to prevent execution blocks.

## Reglas de Codificación (Ahorro de Tokens)

1. **Respuestas Cortas:** Dame solo el código de los métodos modificados o la clase específica que te pido. No me reescribas clases enteras si solo cambió una línea.
2. **Uso de Placeholders:** Si el resto del archivo no cambia, usa comentarios como `// ... código existente permanece igual ...` para ahorrar tokens.
3. **Manejo de Errores:** Centralizado. Prefiere lanzar excepciones personalizadas del dominio que sean capturadas por un controlador de excepciones global.