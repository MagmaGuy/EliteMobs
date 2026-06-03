[![Crowdin](https://badges.crowdin.net/elitemobs/localized.svg)](https://crowdin.com/project/elitemobs)
[![BStats](https://bstats.org/signatures/bukkit/EliteMobs.svg)](https://bstats.org/plugin/bukkit/EliteMobs/1081)

# EliteMobs

EliteMobs is a Spigot/Paper plugin built around custom bosses. On top of the boss system it adds an interlocking set of
RPG-style features: quests, arenas, dungeons, custom and procedurally generated items and enchantments, an in-game
economy, player progression and skills, NPCs, and shops.

- Feature and configuration documentation: https://github.com/MagmaGuy/EliteMobs/wiki
- Discord support channel: https://discord.gg/QSA2wgh
- Webapp for creating content: https://magmaguy.com/webapp/webapp.html

## Features

- **Custom bosses** — Regional, instanced and event bosses defined in config, with a scriptable power system.
- **Dungeons** — Installable, packageable instanced content with kill-percentage and kill-target objectives.
- **Arenas** — Wave-based combat instances (optional MythicMobs integration for arena mobs).
- **Quests** — Static and dynamic quests with objectives, tracking, rewards and quest NPCs.
- **Items & enchantments** — Custom items plus procedurally generated gear, custom enchantments, and scrolls.
- **Economy & shops** — Elite currency with custom, dynamic and sell shops; Vault integration.
- **Progression** — Player ranks, level scaling, a skills system and the Adventurer's Guild hub.
- **NPCs** — Configurable NPCs for shops, quests, ranks, repairs and more.
- **Wormholes**, treasure chests, timed/custom world events, and an explosion-regeneration system.

## Requirements

- Java 21 (the plugin builds on a Java 21 toolchain).
- A Spigot or Paper server. `plugin.yml` declares `api-version: 1.21.4`; the build compiles against the Spigot
  `1.21.11` API.
- No hard dependencies. All integrations are optional (`softdepend`): Multiverse-Core, WorldGuard, Vault,
  PlaceholderAPI, HolographicDisplays, DiscordSRV, LibsDisguises, ModelEngine, Geyser-Spigot, MythicMobs, LevelledMobs,
  InfernalMobs, FreeMinecraftModels. Vault is required for economy features; the others enable the corresponding
  integration when present.

## Installation

1. Drop `EliteMobs.jar` into your server's `plugins/` folder.
2. (Optional) Install any of the soft-dependency plugins above to enable their integrations.
3. Start the server, then run the first-time setup (`/em setup`, requires `elitemobs.initialize`).

## Commands

Two commands are registered (see `plugin.yml`):

| Command            | Aliases | Description    |
|--------------------|---------|----------------|
| `/elitemobs`       | `/em`   | Main command   |
| `/adventurersguild`| `/ag`   | Main command   |

Both are dispatched through MagmaCore's `CommandManager` (see `commands/CommandHandler.java`), which exposes a large set
of player and admin subcommands. Run `/em help` in-game for the full list, or consult the wiki.

## Permissions

Permissions are defined in `plugin.yml`. The high-level nodes:

- `elitemobs.*` — all commands (default: op).
- `elitemobs.user` — recommended player permission set; bundles the player-facing nodes (shops, quests, ranks, repair,
  scrap, teleports, NPC interactions, etc.) (default: true).
- Individual nodes follow the `elitemobs.<area>.<action>` convention. Admin actions (setup, spawning, killing, loot
  debug, currency manipulation, reloads, packaging) default to `op`; player actions default to `true`.

See `plugin.yml` for the authoritative, per-node list.

## Configuration

EliteMobs is config-heavy. On first run it generates its files under `plugins/EliteMobs/`. Configuration is split by
domain, mirroring the `com.magmaguy.elitemobs.config` package — including custom bosses, custom items, enchantments,
quests, events, arenas, treasure chests, NPCs, spawns, powers (including Lua powers), mob properties, menus, potion
effects, skill bonuses and wormholes. Translations are managed via Crowdin. See the
[wiki](https://github.com/MagmaGuy/EliteMobs/wiki) for the configuration reference.

## Building from source

The project builds with Gradle (wrapper included) and shades its runtime dependencies into a single jar:

```
./gradlew shadowJar
```

On Windows:

```
gradlew.bat shadowJar
```

The shaded jar is written to `testbed/plugins/EliteMobs.jar`. The build relocates bStats and `easyminecraftgoals`, and
shades MagmaCore and commons-io.

# Dev notes

## Repository

Maven:
```xml
<repository>
  <id>magmaguy-repo-releases</id>
  <name>MagmaGuy's Repository</name>
  <url>https://repo.magmaguy.com/releases</url>
</repository>

<dependency>
  <groupId>com.magmaguy</groupId>
  <artifactId>EliteMobs</artifactId>
  <version>10.4.0</version>
</dependency>
```

Gradle:
```kotlin
repositories {
    //EliteMobs
    maven {
        url = uri("https://repo.magmaguy.com/releases")
    }
}

dependencies {
    //EliteMobs
    compileOnly("com.magmaguy:EliteMobs:10.4.0")
}
```

## Important classes

The following is a list of key classes for the plugin:

### EliteItemManager

`com.magmaguy.elitemobs.api.utils.EliteItemManager`

This class is specifically made to be the easiest way to interface with Elite Items.

### EliteEntity, CustomBossEntity, RegionalBossEntity

These are the key classes for the boss system. RegionalBossEntity extends CustomBossEntity which extends EliteEntity.

### EntityTracker

This is used to check if an entity is from EliteMobs and to get the specific instance of that entity.

---

`Note: This documentation is updated as requested. If you feel like it is incomplete and should further expand on some aspects, request it and it will be updated.`

---

## API Events

EliteMobs has a few basic APIs to interface with in the `com.magmaguy.elitemobs.api` package. Here's the breakdown:

### ArenaCompleteEvent

Fires when an arena is successfully beaten by players.

### CustomEventStartEvent

Fires when a Custom Event starts. Note that in this instance Custom Events refers to the mechanic in which bosses
randomly spawn in the Overworld.

- Can be cancelled

### DungeonInstallEvent

Fires when a dungeon is installed by an admin.

- Can be cancelled

### DungeonUninstallEvent

Fires when a dungeon is uninstalled by an admin.

- Can be cancelled

### EliteExplosionEvent

Fires when an Elite Explosion happens. Note that elite explosions have a custom visual effect and regenerate the damage
done to terrain after 2 minutes.

- Can be cancelled

### EliteMobDamagedByEliteMobEvent

Used for listening to moments when one Elite damages another Elite. Uses:

- Same as Bukkit's EntityDamagedByEntity event but for elites specifically
- Cancelling it might not work. Report if it doesn't.

### EliteMobDamagedByPlayerAntiExploitEvent

Used for listening to events which trigger an antiexploit **check** - doesn't necessarily mean that it detected an
exploit. Uses:

- Same as Bukkit's EntityDamagedByEntity event but for elites specifically
- Can be cancelled

### EliteMobDamagedByPlayerEvent

Used for listening to moments when a player damages an Elite. Uses:

- Same as Bukkit's EntityDamagedByEntity event but for players damaging elites specifically
- ***Important:*** can't be cancelled as it only fires after applying the damage

### EliteMobDamagedEvent

Used for listening to moments when an elite is damaged in general. Uses:

- Same as Bukkit's EntityDamageEvent
- ***Important*** Cancelling this event might not 100% work, report if it doesn't

### EliteMobDeathEvent

Used to listening to moments when an elite is killed. Uses:

- Same as Bukkit's EntityDeathEvent.

### EliteMobEnterCombatEvent

Used for listening to moments when an elite enters combat against a player. Note that bosses only enter combat after
either striking a player or being struck, and not at the moment of targetting. Uses:

- Get the target (player only) of the Elite
- Get the elite which entered in combat

### EliteMobExitCombatEvent

Used for listening to moments when an elite leaves combat against a player. Uses:

- Get the elite which just let combat.

### EliteMobHealEvent

Used when an elite gets healed.

- Can be cancelled.

### EliteMobRemoveEvent

Used when an elite mob gets removed. Please note that not all removals are permanent as bosses can be removed because
the chunks unload while still being persistent.

### EliteMobsItemDetector

Used for detecting whether an ItemStack is an EliteMobs ItemStack (like a custom item or a procedurally generated item).
Uses:

- Detect if an ItemStack is an EliteMobs custom or dynamic item.

### EliteMobSpawnEvent

Used for detecting when an Elite spawns. Uses:

- Detect when an Elite spawns.
- Detect which Elite spawned.

### EliteMobTargetPlayerEvent

Used for detecting when an Elite has targetted a player. Uses:

- Detect when an Elite targets a player.
- Cancel an Elite's detection of a player.

### GenericAntiExploitEvent

Used for listening to moments when the antiexploit runs a check but no players damaged it. Uses:

- Detect when the antiexploit is doing a non-player based exploit check.
- Cancel an antiexploit check.

### NPCEntityRemoveEvent

Used when an npc gets removed. This removal may not be permanent, as it might just be a chunk unload.

### NPCEntitySpawnEvent

Used when an npc gets spawned.

### PlayerDamagedByEliteMobEvent

Used for listening to moments when players are damaged by an Elite. Uses:

- Same as Bukkit's EntityDamageEvent.

### PlayerPreTeleportEvent

Used when a player starts teleporting through EliteMobs features. There is a 3 second timer before the teleportation
actually happens.

- Can be cancelled.

### PlayerTeleportEvent

Used when a player actually teleports through EliteMobs features.

- Can be cancelled.

### QuestAcceptEvent

Used when a player accepts a quest.

- Can be cancelled.

### QuestCompleteEvent

Used when a player completes a quest.

- Can be cancelled.

### QuestLeaveEvent

Used when a player leaves a quest.

### QuestObjectivesCompletedEvent

Used when all quest objectives for a quest are completed.

### QuestProgressionEvent

Used when a player progresses in a quest, such as by killing a quest mob or collecting a quest item.

### QuestRewardEvent

Used when a player gets the reward from a quest.

### SuperMobDamageEvent

Used when a Super Mob gets damaged.

- Can be cancelled.

### SuperMobDeathEvent

Used when a Super Mob dies.

### SuperMobRemoveEvent

Used when a Super Mob gets removed. Note that removals might be temporary due to chunk unloads.

### SuperMobSpawnEvent

Used when a Super Mob spawns.

- Can be cancelled.

---

## To add new item enchantments:

1) Add new enchantment class to the `com.magmaguy.elitemobs.config.enchantments.premade` **
   extending** `EnchantmentsConfigFields` to initialize create its config file (naming convention: [EnchantmentName]
   Config)
2) Initialize enchantment in `CustomEnchantment` to initialize the config file
3) Add enchantment class to `com.magmaguy.elitemobs.items.customenchantments` **extending** `CustomEnchantment` to write
   the logic for the enchantment (naming convention [EnchantmentName]Enchantment)
4) Add a public static String called "key" to register using the ItemTagger class for persistent enchantment tracking
5) Add an entry to the parseEnchantments() method in `CustomItem` so custom items detect it correctly
6) (Alternative) Add an entry to `generateCustomEnchantments()` method in `EnchantmentGenerator` if the enchantment
   should appear in procedurally generated items

Note:

- Don't forget to register events in `EventsRegistrer` if the part with logic in it requires events.
- Don't forget to use the damage bypass if the power is supposed to deal custom damage. Damage dealt by the player to an
  elite can be overwritten in `CombatSystem` through the static "bypass" boolean field - it makes the next damage dealt
  to the elite use the raw damage value. For correctly assigning damage, use
  Bukkit's `Damageable#damage(double amount, Entity source)` and assign the source to your player.

## To add new powers to elites:

to be documented

## To add new events:

to be documented

## To add new default NPCs:

to be documented

## To add new mob types:

to be documented

## Special thanks:

Special Thanks to Illusion for spending a few hours proselytizing enums that are also have anonymous method
implementations on their constructor.

## License

EliteMobs is licensed under the GNU General Public License v3.0. See [LICENSE](LICENSE) for the full text.
