# Integration Hooks for Update-6.2

Extract this archive into your repository root on a new branch:

```bash
git checkout Update-6.2
git checkout -b feat/warframe-abilities-and-parkour
```

None of these files overwrite existing `Update-6.2` sources. Wire them in with these 4 hooks:

## 1. Payload Registration (`ModBusEvents.java` or `BinderNetwork.java`)
Inside your `RegisterPayloadHandlersEvent` listener:
```java
com.jax.destiny2mc.tenno.parkour.ParkourNetwork.register(event);
```

## 2. Common Gameplay Events (`CommonForgeEvents.java` or mod constructor on `NeoForge.EVENT_BUS`)
```java
NeoForge.EVENT_BUS.addListener(com.jax.destiny2mc.tenno.parkour.WarframeParkourEvents::onPlayerTickPost);
NeoForge.EVENT_BUS.addListener(com.jax.destiny2mc.tenno.parkour.WarframeParkourEvents::onLivingFall);
NeoForge.EVENT_BUS.addListener(com.jax.destiny2mc.tenno.parkour.WarframeParkourEvents::onLivingIncomingDamage);
NeoForge.EVENT_BUS.addListener(com.jax.destiny2mc.tenno.parkour.WarframeParkourEvents::onPlayerLoggedOut);
```

## 3. Client Tick Input (`BinderClient.java` or client event subscriber)
Inside your `ClientTickEvent.Post` listener:
```java
com.jax.destiny2mc.tenno.parkour.ParkourClientInput.onClientTick();
```
And on client disconnect (`ClientPlayerNetworkEvent.LoggingOut`):
```java
com.jax.destiny2mc.tenno.parkour.ParkourClientState.clear();
```

## 4. Mixin Registration (`src/main/resources/ageless_arsenals.mixins.json`)
Add `"PlayerParkourPoseMixin"` to the `"client"` array:
```json
"client": [
  "PlayerParkourPoseMixin"
]
```

## 5. Casting an Ability from Your Existing Keybind / HUD
```java
PacketDistributor.sendToServer(
    new com.jax.destiny2mc.tenno.parkour.ParkourNetwork.WarframeCastPayload(abilityId)
);
```
