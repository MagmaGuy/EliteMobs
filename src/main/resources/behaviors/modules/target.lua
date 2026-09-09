-- Keep EliteMobs' current threat/taunt target. Acquire a visible player only when it has none.
local RANGE = 35
local function eligible(target, own)
    if not target or not target.is_valid or target.is_dead then return false end
    if target.game_mode ~= 'survival' and target.game_mode ~= 'adventure' then return false end
    local at = target.current_location
    if not at or not own or at.world ~= own.world then return false end
    local dx, dy, dz = at.x - own.x, at.y - own.y, at.z - own.z
    return dx * dx + dy * dy + dz * dz <= RANGE * RANGE
end

return ai.module {
    id = 'elitemobs:behavior/target', revision = 1,
    memories = { candidate = { type = 'uuid', persistent = false } },
    sensors = {
        ai.sensor {
            id = 'elitemobs:behavior/find_target', interval = 20,
            sense = function(c)
                local own = c.entity.current_location
                local target = c.perception:current_target()
                if not eligible(target, own) then
                    target = c.perception:nearest_player(RANGE)
                    if target and not c.perception:can_see(target) then target = nil end
                end
                if target then c.memory:set('candidate', target.uuid, 21)
                else c.memory:forget('candidate') end
            end
        }
    },
    behaviors = {
        ai.behavior {
            id = 'elitemobs:behavior/select_target', priority = 5,
            controls = { ai.controls.target },
            can_start = function(c) return true end,
            can_continue = function(c) return true end,
            tick = function(c)
                local candidate = c.memory:get('candidate')
                -- Consume this sensor result once. A later teleport or game-mode change
                -- must not reuse a stale UUID before the next sensor pass.
                c.memory:forget('candidate')
                if eligible(c.perception:current_target(), c.entity.current_location) then return end
                if candidate then c.actuator:set_target(candidate)
                else c.actuator:clear_target() end
            end
        }
    }
}
