# Experimental Lua Power Files

EliteMobs now supports experimental standalone Lua power files in the `powers` config tree.

The old `eliteScript` DSL remains YAML-only. Lua is not used as an alternate authoring format for that DSL.

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
  - `on_zone_enter`
  - `on_zone_leave`

## Runtime helpers

The runtime exposes a safe `context` object with helpers for:

- boss state and actions
- player queries such as `current_target()`, `nearby_players(radius)`, and `all_players_in_world()`
- scheduler ownership
- cooldown inspection and shared cooldowns
- event mutation on supported damage events
- particles, sounds, lightning, and boss spawning
- simple zone queries and `watch_zone(zone_definition, { on_enter = ..., on_leave = ... })`

Unsafe globals such as `os`, `io`, `debug`, `package`, `require`, and Java interop are not exposed.
