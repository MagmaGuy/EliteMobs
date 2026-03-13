# Experimental Lua Power Files

EliteMobs now supports experimental standalone Lua power files in the `powers` config tree.

## Availability

Standalone `.lua` power files in the `powers` tree load automatically.

## How bosses reference Lua powers

Boss configs keep using the normal `powers:` field:

```yml
powers:
  - attack_push.yml
  - mycoolpower.lua
```

Lua powers are referenced by filename exactly like YAML powers.

## Where Lua power files live

Place `.lua` files in the same powers folder tree as normal power `.yml` files.

## Lua power contract

Lua power files must `return` a flat table:

```lua
return {
  api_version = 1,
  priority = 0,

  on_spawn = function(context)
    context.boss:add_tag("awake")
  end,

  on_boss_damaged_by_player = function(context)
    if context.cooldowns.local_ready("shield") then
      context.event.multiply_damage_amount(0.5)
      context.cooldowns.set_local(60, "shield")
    end
  end
}
```

The Lua API is intentionally verbose and self-documenting. Prefer names like `context.cooldowns.set_local(...)` and `context.player:place_temporary_block(...)` over short or ambiguous helper names.

## Direct scripting helpers

`context.script` is a direct helper surface for targeting, zones, relative vectors, scripted pushes, scripted damage, facing, and particles:

```lua
local zone = context.script:zone({
  type = "sphere",
  radius = 4
})

local targets = zone:full_target()
context.script:damage(targets, 2.0, 1.0)
```

These helpers are intentionally direct. Lua does not register synthetic YAML script containers anymore.

## Native parity helpers

The native Lua runtime now exposes the bridge-only behaviors directly:

- `context.world:set_block_at_location(location, material, require_air)`
- `context.world:place_temporary_block_at_location(location, material, duration, require_air)`
- `context.world:get_block_type_at_location(location)`
- `context.world:is_air_at_location(location)`
- `context.world:is_on_floor_at_location(location)`
- `context.world:is_standing_on_material(location, material)`
- `context.world:spawn_entity_at_location(entity_type, location, { velocity = ..., duration = ..., effect = ..., on_land = function(...) ... end, max_ticks = ... })`
- `context.world:spawn_falling_block_at_location(location, material, { velocity = ..., drop_item = false, hurt_entities = false, on_land = function(...) ... end })`
- `context.boss:summon_projectile(entity_type, origin, destination, speed, { duration = ..., effect = ..., on_land = function(...) ... end, max_ticks = ... })`
- `context.world:spawn_fireworks_at_location(location, { power = 1, effects = { { type = "BALL_LARGE", colors = { "ORANGE" }, fade_colors = { "WHITE" }, flicker = true, trail = true } } })`

Landing callbacks receive:

```lua
function(landing_location, spawned_entity)
  context.world:spawn_particle_at_location(landing_location, "EXPLOSION", 1)
end
```

## Supported top-level fields

- `api_version` (required)
- `priority` (optional, defaults to `0`)
- named hook functions such as:
  - `on_spawn`
  - `on_game_tick`
  - `on_boss_damaged`
  - `on_boss_damaged_by_player`
  - `on_boss_damaged_by_elite`
  - `on_player_damaged_by_boss`
  - `on_enter_combat`
  - `on_exit_combat`
  - `on_heal`
  - `on_boss_target_changed`
  - `on_death`
  - `on_phase_switch`

## Runtime helpers

The runtime exposes a safe `context` object with helpers for:

- boss state and actions
- player queries such as `current_target()`, `nearby_players(radius)`, and `all_players_in_world()`
- scheduler ownership
- cooldown inspection and shared cooldowns
- event mutation on supported damage events
- particles, sounds, lightning, boss spawning, entity spawning, falling blocks, and block inspection helpers
- simple zone queries and `watch_zone(zone_definition, { on_enter = ..., on_leave = ... })`

Unsafe globals such as `os`, `io`, `debug`, `package`, `require`, and Java interop are not exposed.
