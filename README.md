Thank you for using EliteMobs!

This plugin is made to both work from the very start with no additional config work and to be highly customizable.

If you want to make some deep configuration changes to the plugin, try to read the GitHub wiki I've created ahead of time or as you're making the changes so you know what the options are doing. https://github.com/MagmaGuy/EliteMobs

If you need support with this plugin, you can reach me at my discord support channel: https://discord.gg/QSA2wgh

For all things EliteMobs related, you can go to my website to find all resources I've made available, such as the github page, the wiki, the custom loot maker, so on.

Hope you enjoy! If you do, leave rating on the resource page and / or buy me a coffee ! (paypal links can be found in the spigot resource page)


# Dev notes:

## API

EliteMobs has a few basic APIs to interface with in the `com.magmaguy.elitemobs.api` package. Here's the breakdown:

### DamageEliteMob

Used for applying custom damage to Elite Mobs. Uses:
- Bypass EliteMobs' custom damage system for a specific damage event
- Apply an automatically recommended custom amount of damage which varies based on the tier of the boss.

*Note:* the variable damage takes the elite's tier into account and deals more damage to higher tiers. It does not take the health multiplier into account for custom bosses for balance reasons.

### EliteMobDamagedByEliteMobEvent

Used for listening to moments when one Elite damages another Elite. Uses:
- Same as Bukkit's EntityDamagedByEntity event but for elites specifically
- Cancelling it might not work. Report if it doesn't.

### EliteMobDamagedByPlayerAntiExploit
Used for listening to events which trigger an antiexploit **check** - doesn't necessarily mean that it detected an exploit. Uses:
- Same as Bukkit's EntityDamagedByEntity event but for elites specifically
- Can be cancelled

### EliteMobDamagedByPlayerEvent:
Used for listening to moments when a player damages an Elite. Uses:

- Same as Bukkit's EntityDamagedByEntity event but for players damaging elites specifically
- ***Important:*** can't be cancelled as it only fires after applying the damage //todo: fix this

### EliteMobDamagedEvent:
Used for listening to moments when an elite is damaged in general. Uses:

- Same as Bukkit's EntityDamageEvent
- ***Important*** Cancelling this event might not 100% work, report if it doesn't

### EliteMobDeathEvent:
Used to listening to moments when an elite is killed. Uses:

- Same as Bukkit's EntityDeathEvent.

### EliteMobEnterCombatEvent:
Used for listening to moments when an elite enters combat against a player. Uses:

- Get the target (player only) of the Elite
- Get the elite which entered in combat

#### EliteMobExitCombatEvent:
Used for listening to moments when an elite leaves combat against a player. Uses:

- Get the elite which just let combat.

### EliteMobsItemDetector:
Used for detecting whether an ItemStack is an EliteMobs ItemStack (like a custom item or a procedurally generated item). Uses:

- Detect if an ItemStack is an EliteMobs custom or dynamic item.

### EliteMobSpawnEvent:
Used for detecting when an Elite spawns. USes:

- Detect when an Elite spawns
- Detect which Elite spawned

### EliteMobTargetPlayerEvent:
Used for detecting when an Elite has targetted a player. Uses:

- Detect when an Elite targets a player.
- Cancel an Elite's detection of a player.

### GenericAntiExploitEvent

Used for listening to moments when the antiexploit runs a check but no players damaged it. Uses:

- Detect when the antiexploit is doing a non-player based exploit check
- Cancel an antiexploit check

### PlayerDamagedByEliteMobEvent

Used for listening to moments when players are damaged by an Elite. Uses:

- Same as Bukkit's EntityDamageEvent

---


## To add new item enchantments:
1) Add new enchantment class to the `com.magmaguy.elitemobs.config.enchantments.premade` **extending** `EnchantmentsConfigFields` to initialize create its config file (naming convention: [EnchantmentName]Config)
2) Initialize enchantment in `com.magmaguy.elitemobs.items.customenchantments.CustomEnchantment` to initialize the config file
3) Add enchantment class to `com.magmaguy.elitemobs.items.customenchantments` **extending** `CustomEnchantment` to write the logic for the enchantment (naming convention [EnchantmentName]Enchantment)
4) Add a public static String called "key" to register using the ItemTagger class for persistent enchantment tracking
5) Add an entry to the parseEnchantments() method in `com.magmaguy.elitemobs.items.customitems.CustomItem` so custom items detect it correctly
6) (Alternative) Add an entry to `generateCustomEnchantments()` method in `com.magmaguy.elitemobs.items.itemconstructor.EnchantmentGenerator` if the enchantment should appear in procedurally generated items

Note:
- Don't forget to register events in `com.magmaguy.elitemobs.EventsRegistrer` if the part with logic in it requires events.
- Don't forget to use the damage bypass if the power is supposed to deal custom damage. Damage dealt by the player to an elite can be overwritten in `com.magmaguy.elitemobs.combatsystem.CombatSystem` through the static "bypass" boolean field - it makes the next damage dealt to the elite use the raw damage value. For correctly assigning damage, use Bukkit's `Damageable#damage(double amount, Entity source)` and assign the source to your player.

## To add new powers to elites:

to be documented

## To add new events:

to be documented

## To add new default NPCs:

to be documented

## To add new mob types:

to be documented
