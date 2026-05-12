# PRD — NetworkPlatform

**Producto:** NetworkPlatform  
**Tipo:** Librería/SDK interno para desarrollo de plugins PaperMC  
**Estado:** Propuesta oficial para desarrollo  
**Versión del PRD:** 1.0  
**Última actualización:** 2026-05-11  
**Autor:** Equipo de arquitectura / desarrollo Minecraft Network

---

## 1. Resumen ejecutivo

NetworkPlatform será una librería interna reutilizable para acelerar y estandarizar el desarrollo de plugins de Minecraft en PaperMC.

Su objetivo principal es eliminar boilerplate repetitivo entre plugins, por ejemplo:

- manejo de archivos de configuraciones potente;
- comandos;
- feedback al jugador;
- schedulers;
- traducciones;
- lifecycle/shutdown;
- database;
- Redis;
- hooks con dependencias externas para por ejemplo Scoreboards, menus, placeholderapi .

La plataforma no será un plugin instalado en `/plugins`. Será una dependencia Gradle consumida por plugins independientes y empaquetada dentro del JAR final del plugin consumidor mediante Shadow.

Cada plugin consumidor podrá usar únicamente los módulos que necesite. La platform debe sentirse como código propio disponible dentro del proyecto, sin limitar al desarrollador ni imponer lógica de modalidad.

Para todo el tema de testing vamos a estar utilizando JUNIT
---

## 2. Problema

Actualmente, cada nuevo plugin o modalidad tiende a repetir las mismas piezas de infraestructura:

```text
PracticePlugin
├─ ConfigManager
├─ CommandManager
├─ DatabaseManager
├─ SchedulerUtils
├─ MessageUtils
├─ MenuManager
└─ Hooks

UHCPlugin
├─ ConfigManager
├─ CommandManager
├─ DatabaseManager
├─ SchedulerUtils
├─ MessageUtils
├─ MenuManager
└─ Hooks
```

Esto genera varios problemas:

1. **Duplicación de código:** cada plugin reimplementa la misma infraestructura.
2. **Mantenimiento costoso:** un bug en commands/config/database debe corregirse en varios proyectos.
3. **Inconsistencia:** cada plugin termina usando patrones distintos.
4. **Arranque lento de nuevos proyectos:** se pierde tiempo preparando boilerplate antes de programar la lógica real.
5. **Mayor probabilidad de errores:** cada implementación propia puede tener bugs distintos.
6. **Escalabilidad limitada:** al crecer la network, mantener muchos plugins independientes se vuelve más difícil.

---

## 3. Objetivos

### 3.1 Objetivo principal

Crear una librería/SDK interna llamada `NetworkPlatform` que permita iniciar nuevos plugins con infraestructura base ya resuelta, sin copiar código entre proyectos.

### 3.2 Objetivos específicos

- Reducir boilerplate en plugins nuevos.
- Estandarizar patrones comunes de desarrollo.
- Permitir que cada plugin consumidor use solo lo que necesita.
- Mantener bajo acoplamiento entre la platform y la lógica de cada modalidad.
- Facilitar correcciones centralizadas de bugs.
- Permitir versionado y evolución controlada de la infraestructura común.
- Mantener una experiencia de desarrollo flexible, sin convertir la platform en una jaula.

---

## 4. No objetivos

NetworkPlatform **no** debe:

- ser un plugin runtime obligatorio instalado en `/plugins`;
- contener lógica específica de modalidades;
- decidir reglas de negocio;
- ser un framework gigante que controle todo;
- compartir estado global en memoria entre plugins;
- forzar database/Redis/scoreboards/menus en plugins que no los necesitan;
- reemplazar la arquitectura interna de cada plugin consumidor;
- ocultar tanto la implementación que el desarrollador pierda control.

Ejemplos de lógica que NO debe vivir en NetworkPlatform:

```text
Practice
├─ duelos
├─ ranked queue
├─ ELO
├─ kits
└─ arenas 1v1

UHC
├─ border
├─ scenarios
├─ deathmatch
└─ teams

DestroyTheNexus
├─ nexus health
├─ generators
├─ teams
└─ bases
```

---

## 5. Principios de diseño

### 5.1 Infraestructura, no reglas de negocio

NetworkPlatform debe abstraer infraestructura técnica común, no comportamiento específico de juego.

Correcto:

```text
configs
commands
feedback
scheduler
database base
redis base
menus base
scoreboards base
translations
hooks
```

Incorrecto:

```text
showLobbyScoreboard específico
createRankedDuel específico
startUHCBorder específico
damageNexus específico
```

---

### 5.2 Base fuerte + addons opcionales

La plataforma se dividirá en una base obligatoria y módulos opcionales.

```text
network-platform-paper      ← base obligatoria
network-platform-database   ← addon opcional
network-platform-redis      ← addon opcional
network-platform-scoreboard ← addon opcional
network-platform-menus      ← addon opcional
network-platform-hooks      ← addon opcional
```

No se usará una única librería gigante para todo ni micro-módulos excesivos.

---

### 5.3 El plugin consumidor mantiene el control

El plugin consumidor sigue siendo dueño de:

- `onEnable()`;
- `onDisable()`;
- registro de lógica propia;
- inicialización explícita de módulos opcionales;
- flujo de arranque;
- reglas del dominio.

NetworkPlatform se inicializa desde el plugin consumidor:

```java
public final class PracticePlugin extends JavaPlugin {

    private NetworkPlatform platform;

    @Override
    public void onEnable() {
        this.platform = NetworkPlatform.create(this);
    }

    @Override
    public void onDisable() {
        if (platform != null) {
            platform.shutdown();
        }
    }
}
```

---

### 5.4 Nada pesado inicia automáticamente

`NetworkPlatform.create(this)` no debe conectar MySQL, Redis, hooks externos ni servicios costosos.

Correcto:

```text
NetworkPlatform.create(this)
├─ guarda contexto del plugin
├─ prepara servicios base
└─ queda lista para usarse
```

Incorrecto:

```text
NetworkPlatform.create(this)
├─ conecta MySQL
├─ conecta Redis
├─ carga PlaceholderAPI
├─ crea scoreboards
└─ registra lógica no solicitada
```

Los módulos pesados se instalan explícitamente:

```java
DatabaseService database = DatabaseModule.install(platform, config.database());
RedisService redis = RedisModule.install(platform, config.redis());
ScoreboardService scoreboards = ScoreboardModule.install(platform);
```

---

### 5.5 APIs cómodas + escape hatches avanzados

NetworkPlatform debe ofrecer APIs simples para el 80% de casos, pero permitir acceso avanzado para casos especiales.

La platform no debe limitar al equipo a una sola forma rígida de trabajar.

Los desarrolladores de los plugins consumidores pueden crear sus propias cosas sin necesidad de modificar NetworkPlatform y que NetworkPlatform no los limite.

---

## 6. Usuarios del producto

### 6.1 Developer de modalidades

Desarrolla plugins como:

- Practice;
- UHC;
- Hunger Games;
- Battle Royale;
- Destroy The Nexus;
- Pillars of Fortune.

Necesita enfocarse en lógica de juego, no en infraestructura repetida.

### 6.2 Developer de plugins de network

Desarrolla plugins no necesariamente ligados a modalidades:

- Lobby;
- Punishments;
- Profiles;
- Friends;
- Parties;
- Announcer;
- Ranks;
- Rewards.

Necesita una base común para commands, configs, feedback, database y Redis.

### 6.3 Maintainer de infraestructura

Mantiene NetworkPlatform, corrige bugs y publica nuevas versiones para todos los plugins consumidores.

---

## 7. Arquitectura de alto nivel

### 7.1 Estructura general

(OJO MUCHO CUIDADO ALGO PORQUE ESTO ES ALGO REFERENCIAL QUE SE DEICIDIO CON EL EQUIPO PARA MAPEAR EL PRODUCTO EN NUESTRAS CABEZAS ASI QUE COMO TE DIGO ES ALGO QUE ES SOLO REFERENCIAL QUE REALIZAMOS PARA LOGRAR ENTENDER EL PRODUCTO DE FORMA PRACTICA Y PLASMADA )

```text
network-platform-paper
├─ NetworkPlatform
├─ PluginContext
├─ ConfigService
├─ CommandService
├─ FeedbackService
├─ SchedulerService
├─ TranslationService
└─ Lifecycle

network-platform-database
├─ DatabaseModule
├─ DatabaseService
├─ DatabaseConfig
└─ HikariDatabaseService

network-platform-redis
├─ RedisModule
├─ RedisService
├─ RedisConfig
└─ RedisService implementation

network-platform-scoreboard
├─ ScoreboardModule
├─ ScoreboardService
└─ PaperScoreboardService

network-platform-menus
├─ MenuModule
├─ MenuService
└─ PaperMenuService

network-platform-hooks
├─ PlaceholderAPIHook
├─ LuckPermsHook
├─ VaultHook
```

---

### 7.2 Relación de dependencias

La dirección correcta de dependencias será:

```text
network-platform-database   → network-platform-paper
network-platform-redis      → network-platform-paper
network-platform-scoreboard → network-platform-paper
network-platform-menus      → network-platform-paper
network-platform-hooks      → network-platform-paper
```

La base `network-platform-paper` no debe depender de addons opcionales.

Incorrecto:

```text
network-platform-paper → database
network-platform-paper → redis
network-platform-paper → scoreboard
```

Esto evitará que la base se convierta en un monstruo acoplado.

---

## 8. Modelo de consumo en plugins

### 8.1 Plugin simple

Ejemplo: `AnnouncerPlugin`

```kotlin
val platformVersion = "1.0.0"

dependencies {
    implementation("com.tunetwork:network-platform-paper:$platformVersion")
}
```

Uso:

```java
public final class AnnouncerPlugin extends JavaPlugin {

    private NetworkPlatform platform;

    @Override
    public void onEnable() {
        this.platform = NetworkPlatform.create(this);

    }

    @Override
    public void onDisable() {
        if (platform != null) {
            platform.shutdown();
        }
    }
}
```

Resultado esperado:

```text
El plugin tiene configs, commands y feedback.
No tiene database.
No tiene Redis.
No tiene scoreboard.
No carga módulos innecesarios.
```

---

### 8.2 Plugin con scoreboard

Ejemplo: `LobbyPlugin`

```kotlin
val platformVersion = "1.0.0"

dependencies {
    implementation("com.tunetwork:network-platform-paper:$platformVersion")
    implementation("com.tunetwork:network-platform-scoreboard:$platformVersion")
}
```

Uso:

```java
public final class LobbyPlugin extends JavaPlugin {

    private NetworkPlatform platform;
    private ScoreboardService scoreboards;

    @Override
    public void onEnable() {
        this.platform = NetworkPlatform.create(this);

        this.scoreboards = ScoreboardModule.install(platform);

    }

    @Override
    public void onDisable() {
        if (platform != null) {
            platform.shutdown();
        }
    }
}
```

---

### 8.3 Plugin con database

Ejemplo: `PunishmentsPlugin`

```kotlin
val platformVersion = "1.0.0"

dependencies {
    implementation("com.tunetwork:network-platform-paper:$platformVersion")
    implementation("com.tunetwork:network-platform-database:$platformVersion")
}
```

Uso:

```java
public final class PunishmentsPlugin extends JavaPlugin {

    private NetworkPlatform platform;
    private DatabaseService database;

    @Override
    public void onEnable() {
        this.platform = NetworkPlatform.create(this);

        this.database = DatabaseModule.install(platform, config.database());

    }

    @Override
    public void onDisable() {
        if (platform != null) {
            platform.shutdown();
        }
    }
}
```

---

### 8.4 Plugin pesado tipo Practice

```kotlin
val platformVersion = "1.0.0"

dependencies {
    implementation("com.tunetwork:network-platform-paper:$platformVersion")
    implementation("com.tunetwork:network-platform-database:$platformVersion")
    implementation("com.tunetwork:network-platform-redis:$platformVersion")
    implementation("com.tunetwork:network-platform-scoreboard:$platformVersion")
    implementation("com.tunetwork:network-platform-menus:$platformVersion")
}
```

Uso:

```java
public final class PracticePlugin extends JavaPlugin {

    private NetworkPlatform platform;

    private DatabaseService database;
    private RedisService redis;
    private ScoreboardService scoreboards;
    private MenuService menus;

    @Override
    public void onEnable() {
        this.platform = NetworkPlatform.create(this);


        this.database = DatabaseModule.install(platform, config.database());
        this.redis = RedisModule.install(platform, config.redis());
        this.scoreboards = ScoreboardModule.install(platform);
        this.menus = MenuModule.install(platform);

    }

    @Override
    public void onDisable() {
        if (platform != null) {
            platform.shutdown();
        }
    }
}
```

---

## 9. Componentes principales

## 9.1 network-platform-paper

### Responsabilidad

Base común para todos los plugins PaperMC.


(OJO MUCHO CUIDADO ALGO PORQUE ESTO ES ALGO REFERENCIAL QUE SE DEICIDIO CON EL EQUIPO PARA MAPEAR EL PRODUCTO EN NUESTRAS CABEZAS ASI QUE COMO TE DIGO ES ALGO QUE ES SOLO REFERENCIAL QUE REALIZAMOS PARA LOGRAR ENTENDER EL PRODUCTO DE FORMA PRACTICA Y PLASMADA )

### Incluye



```text
NetworkPlatform
PluginContext
ConfigService
CommandService
FeedbackService
SchedulerService
TranslationService
Lifecycle
```

### Requisitos funcionales

#### NP-PAPER-001 — Crear platform desde JavaPlugin

La platform debe poder inicializarse desde el plugin consumidor:

```java
NetworkPlatform platform = NetworkPlatform.create(this);
```

#### NP-PAPER-002 — Exponer contexto del plugin

La platform debe exponer el plugin/contexto actual para uso interno de servicios:

```java
JavaPlugin plugin = platform.plugin();
```

#### NP-PAPER — Lifecycle

Debe registrar acciones de apagado para que el plugin consumidor solo llame:

```java
platform.shutdown();
```

Ejemplo interno:

```java
platform.lifecycle().onShutdown(database::shutdown);
platform.lifecycle().onShutdown(redis::shutdown);
```

---

### Requisitos funcionales

#### NP-HOOKS-001 — Hooks opcionales

Un hook no debe romper el plugin si la dependencia externa no está instalada, salvo que el plugin consumidor lo marque como obligatorio.

---

## 10. Lifecycle

### 10.1 Concepto

Lifecycle será el sistema que centraliza acciones de apagado.

Ejemplo:

```java
public interface Lifecycle {

    void onShutdown(Runnable action);

    void shutdown();
}
```

### 10.2 Comportamiento esperado

Cuando un addon abre recursos, debe registrar su cierre:

```java
platform.lifecycle().onShutdown(database::shutdown);
platform.lifecycle().onShutdown(redis::shutdown);
platform.lifecycle().onShutdown(scoreboards::shutdown);
```

El plugin consumidor solo debe llamar:

```java
platform.shutdown();
```

### 10.3 Orden de apagado

Las acciones de shutdown deben ejecutarse en orden inverso al registro.

Ejemplo:

```text
Registro:
1. database
2. redis
3. scoreboards

Shutdown:
1. scoreboards
2. redis
3. database
```

---

## 11. Packaging y distribución

### 11.1 NetworkPlatform como dependencia Gradle

Cada módulo se publicará como artifact Gradle/Maven.

Ejemplo:

```kotlin
val platformVersion = "1.0.0"

dependencies {
    implementation("com.tunetwork:network-platform-paper:$platformVersion")
}
```

### 11.2 Publicación inicial

Fase inicial:

```bash
./gradlew publishToMavenLocal
```

Fase posterior:

```text
GitHub Packages
Nexus privado
Artifactory privado
```

### 11.3 Shadow en plugins consumidores

Los plugins consumidores deben usar Shadow para empaquetar NetworkPlatform dentro del JAR final.

Ejemplo:

```kotlin
plugins {
    java
    id("com.gradleup.shadow") version "9.0.1"
}

tasks {
    shadowJar {
        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar)
    }
}
```

Resultado:

```text
PracticePlugin.jar
├─ código de Practice
├─ código de network-platform-paper
├─ código de network-platform-database
└─ código de network-platform-scoreboard
```

---

## 12. Versionado

Todos los módulos de NetworkPlatform deben compartir la misma versión.

Ejemplo:

```text
network-platform-paper:1.0.0
network-platform-database:1.0.0
network-platform-redis:1.0.0
network-platform-scoreboard:1.0.0
```

Uso recomendado en plugins:

```kotlin
val platformVersion = "1.0.0"

dependencies {
    implementation("com.tunetwork:network-platform-paper:$platformVersion")
    implementation("com.tunetwork:network-platform-database:$platformVersion")
}
```

### 12.1 Política semántica

Se usará versionado semántico:

```text
MAJOR.MINOR.PATCH
```

Ejemplo:

```text
1.0.0 → primera versión estable
1.0.1 → bugfix compatible
1.1.0 → nueva funcionalidad compatible
2.0.0 → cambio incompatible
```

---

## 13. Flujo de actualización de bugs

Ejemplo: bug en commands.

1. Se corrige el bug en `network-platform-paper`.
2. Se publica nueva versión:

```text
1.0.0 → 1.0.1
```

3. Cada plugin consumidor actualiza:

```kotlin
val platformVersion = "1.0.1"
```

4. Se recompila el plugin consumidor.
5. El JAR final ya contiene el fix.

Resultado:

```text
PracticePlugin.jar actualizado
UHCPlugin.jar actualizado
LobbyPlugin.jar actualizado
```

---
### 18.2 Inicialización

Base:

```java
NetworkPlatform platform = NetworkPlatform.create(this);
```

Addon:

```java
DatabaseService database = DatabaseModule.install(platform, config.database());
```

### 18.3 Shutdown

Siempre:

```java
@Override
public void onDisable() {
    if (platform != null) {
        platform.shutdown();
    }
}
```

---

## 20. Decisión final

La decisión oficial es construir NetworkPlatform como una librería/SDK interno con base Paper y addons opcionales.

Arquitectura aprobada:

```text
Base obligatoria:
└─ network-platform-paper

Addons opcionales:
├─ network-platform-database
├─ network-platform-redis
├─ network-platform-scoreboard
├─ network-platform-menus
└─ network-platform-hooks
```

Este enfoque se considera correcto porque:

- elimina boilerplate real;
- no obliga a usar módulos innecesarios;
- permite corregir bugs de infraestructura una sola vez;
- mantiene cada plugin independiente;
- evita un plugin runtime central obligatorio;
- preserva control total en el plugin consumidor;
- permite crecer sin convertir la platform en una jaula.

Este enfoque se considera eficiente porque:

- los plugins incluyen solo los módulos que usan;
- los servicios pesados se inicializan explícitamente;
- no se abren conexiones innecesarias;
- no se cargan hooks innecesarios;
- se reduce duplicación y mantenimiento repetido.

La regla principal del producto será:

> NetworkPlatform debe darle herramientas poderosas al plugin consumidor, no decidir por él.
