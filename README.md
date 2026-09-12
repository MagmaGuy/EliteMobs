[![Crowdin](https://badges.crowdin.net/elitemobs/localized.svg)](https://crowdin.com/project/elitemobs)
[![BStats](https://bstats.org/signatures/bukkit/EliteMobs.svg)](https://bstats.org/plugin/bukkit/EliteMobs/1081)

# EliteMobs

EliteMobs is a Spigot/Paper plugin built around custom bosses. On top of the boss system it adds an interlocking set of
RPG-style features: quests, arenas, dungeons, custom and procedurally generated items and enchantments, an in-game
economy, player progression and skills, NPCs, and shops.

[Download](https://nightbreak.io/plugin/elitemobs/) · [Modrinth](https://modrinth.com/plugin/elitemobs) · [Documentation](https://wiki.nightbreak.io/) · [Support](https://discord.gg/nightbreak)

## Features

- **Custom bosses**: Regional, instanced and event bosses defined in config, with a scriptable power system.
- **Dungeons**: Installable, packageable instanced content with kill-percentage and kill-target objectives.
- **Arenas**: Wave-based combat instances (optional MythicMobs integration for arena mobs).
- **Quests**: Static and dynamic quests with objectives, tracking, rewards and quest NPCs.
- **Parties**: Five-player, session-scoped groups with shared kill credit, need/greed Elite gear, and a compact sidebar.
- **Items & enchantments**: Custom items plus procedurally generated gear, custom enchantments, and scrolls.
- **Economy & shops**: Elite currency with custom, dynamic and sell shops; Vault integration.
- **Progression**: Player ranks, level scaling, a skills system and the Adventurer's Guild hub.
- **NPCs**: Configurable NPCs for shops, quests, ranks, repairs and more.
- **Wormholes**, treasure chests, timed/custom world events, and an explosion-regeneration system.

## Requirements

- Java 21 (the plugin builds on a Java 21 toolchain).
- A Spigot or Paper server. `plugin.yml` declares `api-version: 1.21.4`; the build compiles against the Spigot
  `26.2-R0.1-SNAPSHOT` API. Use the Java version required by your server and check the download page for release compatibility.
- No hard dependencies. All integrations are optional (`softdepend`): Multiverse-Core, WorldGuard, Vault,
  PlaceholderAPI, HolographicDisplays, DiscordSRV, LibsDisguises, ModelEngine, Geyser-Spigot, MythicMobs, LevelledMobs,
  InfernalMobs, FreeMinecraftModels. Vault is required for economy features; the others enable the corresponding
  integration when present.

## [Alpha] Advanced Combat System

Release `10.9.0` includes 76 classes with progression, passives, and mobility, signature, and utility abilities. The Adventurer's Guild provides class instructors, starter training, and solo class-unlock challenges. Class-aware gear and FreeMinecraftModels staff and wand integration connect equipment to combat progression.

The system is enabled by default in `AdvancedCombatSystem.yml`. Set `enabled: false` to disable it. While the alpha baseline is being evaluated, class balance and localization settings are intentionally limited.

Press **F** to open the ability-selection window, then **F** for mobility, **left-click** for signature, or **right-click** for utility. With `allowClassAbilitiesOutsideEliteMobsWorlds` enabled, players can opt in outside EliteMobs content by double-tapping F while sneaking.

ResourcePackManager enables the graphical combat HUD. `enableCombatHud: false`, or an unavailable ResourcePackManager, uses the text display instead. Players must accept the server resource pack for the graphical HUD. Install FreeMinecraftModels when your encounters or equipment use its models.

## Skill XP permission perks

Grant `elitemobs.perks.xp.<perk>.all` for all weapon/armor skills, or replace `all`
with `armor`, `swords`, `axes`, `bows`, `crossbows`, `tridents`, `hoes`, `maces`,
`spears`, `staves`, or `wands`. These permissions default to false, including for
operators. The `.all` permissions inherit the individual nodes, so a permission
manager can explicitly deny a particular skill.

| Perk | XP multiplier |
|---|---|
| `10percentboost` | 1.1 |
| `25percentboost` | 1.25 |
| `50percentboost` | 1.5 |
| `double` | 2 |
| `150percentboost` | 2.5 |
| `triple` | 3 |
| `quadruple` | 4 |
| `customboost` | `customXpPerkMultiplier` in `skills.yml`, default 1 |

As in mcMMO, the custom perk overrides fixed perks; otherwise only the largest
granted fixed multiplier applies. Permissions are checked at each eligible award,
so temporary grants and revocations take effect without reloading EliteMobs.
For example, LuckPerms users can grant a 30-minute boost with
`/lp user PlayerName permission settemp elitemobs.perks.xp.25percentboost.all true 30m`.
Boosts apply after the native weapon/armor multiplier and round down to whole XP.
They preserve anti-farm checks, dungeon lockouts, world exclusions and skill-level
eligibility. They do not boost class XP, currency, loot, vanilla XP, or administrative
XP writes. Numeric awards saturate at the remaining `long` capacity.

## Installation

1. Drop `EliteMobs.jar` into your server's `plugins/` folder.
2. (Optional) Install any of the soft-dependency plugins above to enable their integrations.
3. Start the server, then run the first-time setup (`/em initialize`, requires `elitemobs.initialize`). Use `/em setup` afterward to manage content.

## Commands

Two commands are registered (see `plugin.yml`):

| Command            | Aliases | Description    |
|--------------------|---------|----------------|
| `/elitemobs`       | `/em`   | Main command   |
| `/adventurersguild`| `/ag`   | Main command   |

Both are dispatched through MagmaCore's `CommandManager` (see `commands/CommandHandler.java`), which exposes a large set
of player and admin subcommands. Run `/em help` in-game for the full list, or consult the wiki.

### Parties

Parties are enabled by default through `Party.yml` and last only while their members remain logged in. A party contains
one creator plus up to four invited players. Membership is never written to the player database.

Entering or creating an instanced dungeon starts a ready check for the initiating player's entire current party. The
initiator is ready automatically; the dungeon is prepared only after every other member accepts the exact dungeon,
difficulty and dynamic level shown in chat. Capacity, package permissions, active-instance state, party membership,
and cancellable join events are validated for everyone before anybody is moved. Open-world dungeon teleports start
the normal safe-teleport countdown for every available party member; an individual can still cancel their own
teleport by moving. Personal item-enchantment challenges remain solo because they escrow the owner's item.

| Command | Purpose |
|---|---|
| `/em party create` | Create a party. |
| `/em party invite <player>` | Invite an online player. Any current member may invite. |
| `/em party accept` | Accept the most recent unexpired invitation. |
| `/em party leave` | Leave the party. Leadership passes to the next member when necessary. |
| `/em party ready <token>` | Accept the clickable instanced-dungeon ready check. |
| `/em party decline <token>` | Decline and cancel the clickable ready check for the party. |

Nearby party members in the same world receive credit for matching active kill objectives, including quest-specific
boss kills. Quests remain separate per player: parties never accept quests, turn quests in, or collect rewards for
another member. Fetch items, NPC dialogue, and arena participation remain personal because their underlying item,
story, and match requirements must not be bypassed.

Normal Elite equipment and special Elite gear earned while at least two party members are nearby enter the existing
need/greed flow exposed by `/em loot`. Coins and Elite Scrolls remain personal. Each drop has an independent vote,
large pools are paginated, overlapping roll sessions can be cycled by running `/em loot` again, and dungeon boss
lockouts are applied before a player is admitted to a vote.
Party vote pools retain the normal 60-second inactivity window but have a configurable hard lifetime of 120 seconds
by default, so a steady stream of drops cannot keep one vote open forever. Players who make no selection remain in
the Greed pool.

The sidebar lists the leader and members with compact five-segment health bars and marks downed members clearly. While
the viewer is in an instanced dungeon, each participating member also shows their remaining lives. It incorporates the
currently tracked quest when space permits and alternates between the invite and leave command hints. Health colors and
glyphs, `sharedProgressRange`, invitation expiry, rotation timing, styling, and messages are configurable in `Party.yml`.
The combined sidebar itself can be disabled independently; normal EliteMobs quest scoreboards then remain in use.

## Permissions

Permissions are defined in `plugin.yml`. The high-level nodes:

- `elitemobs.*`: all commands (default: op).
- `elitemobs.user`: recommended player permission set; bundles the player-facing nodes (shops, quests, ranks, repair,
  scrap, teleports, NPC interactions, etc.) (default: true).
- Individual nodes follow the `elitemobs.<area>.<action>` convention. Admin actions (setup, spawning, killing, loot
  debug, currency manipulation, reloads, packaging) default to `op`; player actions default to `true`.

See `plugin.yml` for the authoritative, per-node list.

## Configuration

EliteMobs is config-heavy. On first run it generates its files under `plugins/EliteMobs/`. Configuration is split by
domain, mirroring the `com.magmaguy.elitemobs.config` package: including custom bosses, custom items, enchantments,
quests, events, arenas, treasure chests, NPCs, spawns, powers (including Lua powers), mob properties, menus, potion
effects, skill bonuses and wormholes. Translations are managed via Crowdin. See the
[wiki](https://wiki.nightbreak.io/) for the configuration reference.

## Building from source

The project builds with JDK 21 and Gradle and shades its runtime dependencies into a single jar. On Windows, use the included wrapper:

```powershell
.\gradlew.bat shadowJar
```

For a Unix checkout, install the Gradle version specified by [gradle-wrapper.properties](gradle/wrapper/gradle-wrapper.properties), currently `9.1.0`, and run `gradle shadowJar`. This repository currently tracks the Windows wrapper only.

The shaded jar is written to `build/libs/EliteMobs.jar`. When `MC_DIST_DIR` is set, the stable deployable copy is also
written to that directory as `EliteMobs.jar`. The build relocates bStats and `easyminecraftgoals`, and shades MagmaCore
and commons-io.


If you change MagmaCore locally, run its `publishToMavenLocal` task before rebuilding EliteMobs. Do the same for locally changed plugin API dependencies using their own build tools.

## Developer API

[Java class and method reference](https://wiki.nightbreak.io/javadoc/elitemobs/index.html).

Events, tracked entities, item helpers and Lua power services: [EliteMobs developer reference](https://wiki.nightbreak.io/developers/elitemobs). See the [Java API index](https://wiki.nightbreak.io/developers) for dependency setup and lifecycle guidance.

Maven: `com.magmaguy:EliteMobs:10.9.0` from [MagmaGuy's repository](https://repo.magmaguy.com/#/releases). Use `provided` or `compileOnly` scope for the installed plugin.

## Troubleshooting and support

Check `/em version`, the startup log, and whether the required content packs and optional plugins loaded. For missing visuals, confirm both model loading and resource-pack delivery. For custom content errors, include the affected YAML or Lua file and the complete exception, along with server and plugin versions. Remove account tokens and other credentials from anything you share.

## License

See [LICENSE](LICENSE). Downloadable content and third-party assets can have separate terms.
