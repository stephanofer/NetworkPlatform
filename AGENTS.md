# AGENTS.md

## Propósito del proyecto

Este proyecto desarrolla **NetworkPlatform**, una plataforma interna para plugins de Minecraft/PaperMC.

NetworkPlatform no es un plugin que se instala en el servidor. Es un conjunto de librerías reutilizables que los plugins consumidores agregan como dependencia y empaquetan dentro de su JAR final.

El objetivo es eliminar boilerplate repetitivo en plugins de la network sin quitar control al plugin consumidor.

---

## Filosofía principal

NetworkPlatform debe sentirse como si el código reutilizable hubiera sido escrito dentro del propio plugin consumidor.

Cuando un desarrollador cree un plugin como `Practice`, `Lobby`, `UHC` o `Punishments`, debe poder usar configs, comandos, feedback, scheduler, translations, database, Redis, scoreboards o menús con la misma naturalidad que si esas herramientas estuvieran dentro del proyecto.

La plataforma no debe sentirse como una caja negra, una jaula ni un framework que impone una forma única de trabajar.

La regla central es:

> NetworkPlatform ofrece herramientas poderosas; el plugin consumidor toma las decisiones.

---
## Documentación

1. Cuando se va a trabar en cualqueir funcionalidad recodar revisar la docuemtnacion que tenemos local aca mismo y ademas revisar el .jar de las dependencias para hacerlo correctamente.
2. Tenemos dentro de `docs/PaperMC` `docs/Adventure` que son como las principales entocnes tienes que consultar siempre que se va a desarrollar una funcionalidad o cualqueir cosa consultar la documetnacion ya que actualemtne han cambiado muchas cosas entocnes super imporante hacerlo. 

## Principios no negociables

1. **No lógica de modalidad en NetworkPlatform**

   NetworkPlatform no debe contener lógica específica de Practice, UHC, Hunger Games, Nexus, Lobby u otra modalidad.

   Correcto:

   - comandos genéricos;
   - configs;
   - feedback;
   - scheduler;
   - translations;
   - database base;
   - Redis base;
   - scoreboard base;
   - menus base;
   - hooks base.

   Incorrecto:

   - ELO de Practice;
   - kits de Practice;
   - scenarios de UHC;
   - vida del Nexus;
   - deathmatch de Hunger Games;
   - generadores de una modalidad específica.

2. **La base no debe conocer los addons**

   `network-platform-paper` es la base.

   La base puede exponer:

   - configs;
   - commands;
   - feedback;
   - scheduler;
   - translations;
   - lifecycle;
   - contexto del plugin.

   La base no debe depender directamente de:

   - database;
   - Redis;
   - scoreboards;
   - menus;
   - hooks pesados.

   Los addons deben enchufarse sobre la base, no al revés.

3. **Nada pesado inicia automáticamente**

   Crear la platform no debe conectar MySQL, Redis, hooks externos ni sistemas pesados.

   Correcto:

   ```java
   this.platform = NetworkPlatform.create(this);
   ```

   Eso solo prepara el contexto.

   La inicialización pesada debe ser explícita:

   ```java
   this.database = DatabaseModule.install(platform, config.database());
   this.redis = RedisModule.install(platform, config.redis());
   this.scoreboards = ScoreboardModule.install(platform);
   ```

4. **El plugin consumidor es dueño de su lifecycle**

   El plugin consumidor sigue teniendo `onEnable()` y `onDisable()`.

   NetworkPlatform solo ayuda a ordenar el inicio y apagado de recursos.

   Patrón esperado:

   ```java
   @Override
   public void onEnable() {
       this.platform = NetworkPlatform.create(this);
       // inicialización del plugin consumidor
   }

   @Override
   public void onDisable() {
       if (platform != null) {
           platform.shutdown();
       }
   }
   ```


6. **No esconder demasiado poder**

   NetworkPlatform debe reducir boilerplate, no bloquear posibilidades.

   Si una abstracción impide hacer algo que Paper/Bukkit permite hacer razonablemente, esa abstracción está mal diseñada.

7. **Evitar magia oculta**

   No crear servicios, conexiones, listeners o tareas en segundo plano sin que el plugin consumidor lo haya pedido explícitamente.

8. **No singletons globales peligrosos**

   Evitar esto:

   ```java
   public static NetworkPlatform INSTANCE;
   public static JavaPlugin PLUGIN;
   ```

   Cada plugin consumidor debe tener su propia instancia de platform.

---

## Criterios de diseño de APIs

Al diseñar una API nueva, validar estas preguntas:

1. ¿Reduce boilerplate real?
2. ¿El plugin consumidor conserva control?
3. ¿Permite casos simples y casos avanzados?
4. ¿Evita lógica específica de modalidad?
5. ¿Tiene un shutdown claro si abre recursos?
6. ¿Puede usarse sin magia oculta?
7. ¿Se sentiría natural si este código estuviera dentro del plugin consumidor?

Si alguna respuesta es negativa, rediseñar antes de implementar.

---

## Convenciones de código

- Usar Java moderno y APIs claras.
- Preferir composición sobre herencia.
- Evitar clases `Manager` gigantes.
- Mantener clases pequeñas y con una responsabilidad clara.
- Evitar utilidades globales con estado.
- Evitar acoplar módulos opcionales a la base.
- Documentar decisiones importantes en código o Markdown.

---

---

## Testing 

Vamos a estar usando JUNIT

---

## Definición de éxito

NetworkPlatform será exitoso si un nuevo plugin puede arrancar con muy poco código inicial y enfocarse en su lógica real.

Un buen plugin consumidor debería sentirse así:

```text
El plugin decide qué quiere hacer.
NetworkPlatform le da herramientas ya listas.
Nada se impone.
Nada pesado se activa sin pedirlo.
El código sigue siendo claro, poderoso y mantenible.
```

La meta no es crear un framework gigante.

La meta es crear una base profesional, flexible y reutilizable para desarrollar plugins de Minecraft más rápido, con menos errores y sin perder control.
