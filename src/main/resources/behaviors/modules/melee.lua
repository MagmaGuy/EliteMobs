local REACH_SQUARED = 4
local COOLDOWN = 20
return ai.module {
    id = 'elitemobs:behavior/melee', revision = 1,
    memories = { next_attack = { type = 'integer', persistent = false } },
    behaviors = {
        ai.behavior {
            id = 'elitemobs:behavior/attack', priority = 10,
            controls = { ai.controls.attack },
            can_start = function(c) return c.perception:current_target() ~= nil end,
            can_continue = function(c) return c.perception:current_target() ~= nil end,
            tick = function(c)
                if c.tick < (c.memory:get('next_attack') or 0) then return end
                local target = c.perception:current_target()
                if not target or not target.is_valid or target.is_dead then return end
                local at, own = target.current_location, c.entity.current_location
                if not at or not own or at.world ~= own.world then return end
                local dx, dy, dz = at.x - own.x, at.y - own.y, at.z - own.z
                if dx * dx + dy * dy + dz * dz <= REACH_SQUARED and c.perception:can_see(target) then
                    c.actuator:attack(target)
                    c.memory:set('next_attack', c.tick + COOLDOWN)
                end
            end
        }
    }
}
