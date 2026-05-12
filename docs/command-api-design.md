# NetworkPlatform Command API

Base de comandos declarativa sobre Paper Brigadier.

## Decisiones clave

- Registro con `JavaPlugin#getLifecycleManager()` + `LifecycleEvents.COMMANDS`.
- `CommandSpec` define árbol, permisos, aliases y metadata.
- `CommandNode` define ramas literales o argumentos.
- `CommandHandler` vive en el plugin consumidor; NetworkPlatform solo adapta input y ejecuta.
- `SuggestionProviders` prioriza snapshots in-memory y cache local para autocomplete.
- `CommandService#registerRaw(...)` existe como escape hatch para Brigadier puro.
- `SenderScope` se valida en runtime para evitar magia oculta y mantener mensajes útiles.

## Patrón recomendado para plugins consumidores

```java
CommandSpec spec = CommandSpec.builder("party")
    .aliases("p")
    .permission("party.use")
    .senderScope(SenderScope.PLAYER)
    .then(CommandNode.literal("invite")
        .permission("party.invite")
        .then(CommandNode.argument(CommandArguments.onlinePlayer("target")
            .suggestions(SuggestionProviders.onlinePlayers()))
            .handler(handlers::invite)
        )
    )
    .handler(handlers::help)
    .build();

platform.commands().register(spec);
```

## Reglas operativas

- No hacer MySQL/Redis/HTTP directo en autocomplete.
- Si cambian permisos o ramas dinámicas, refrescar jugadores afectados con `CommandService#refreshPlayerCommands(...)`.
- Para lógica compleja usar clases handler separadas y services del plugin consumidor.
- Para casos avanzados o Paper-specific usar raw Brigadier.
