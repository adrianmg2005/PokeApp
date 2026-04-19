# PokeApp

Pokédex completa en español con explorador de movimientos, habilidades, objetos y constructor de equipos competitivos.

## Acceso Web

**[Abrir PokeApp](https://pokeapp.onrender.com)** *(el primer acceso puede tardar ~1 min mientras el servidor arranca)*

## Funcionalidades

- **Pokédex** — Busca y explora los 1025 Pokémon con estadísticas, tipos, evoluciones y sets competitivos de Smogon
- **Comparador** — Compara dos Pokémon lado a lado
- **Movimientos** — Explora todos los movimientos con filtros por tipo, categoría y potencia
- **Habilidades** — Busca habilidades y los Pokémon que las tienen
- **Objetos** — Explora objetos con sprites, categorías y precios
- **Team Builder** — Construye equipos de 6 Pokémon con análisis de coberturas y sinergias

## Ejecución Local

### Requisitos
- Java 21+
- Maven (o usa el wrapper `mvnw` incluido)

### Ejecutar
```bash
./mvnw spring-boot:run
```
Abre http://localhost:8080 en tu navegador.

### Docker
```bash
docker build -t pokeapp .
docker run -p 8080:8080 pokeapp
```

## Despliegue en Render

1. Haz fork o conecta este repositorio en [render.com](https://render.com)
2. Crea un nuevo **Web Service**
3. Selecciona **Docker** como entorno
4. Render detectará automáticamente el `Dockerfile`
5. La app estará disponible en `https://tu-servicio.onrender.com`

## Stack Tecnológico

| Componente | Tecnología |
|-----------|-----------|
| Backend | Java 21, Spring Boot 3.4.4 |
| Frontend | HTML5, CSS3, JavaScript (SPA) |
| API de datos | [PokéAPI](https://pokeapi.co/) |
| Sets competitivos | [Smogon/pkmn](https://pkmn.github.io/smogon/) |
| Despliegue | Docker, Render |
