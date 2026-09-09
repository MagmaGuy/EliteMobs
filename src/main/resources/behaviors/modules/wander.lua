-- A short local walk, with a rest between attempts. Native navigation rejects unreachable paths.
return ai.module {
    id = 'elitemobs:behavior/wander', revision = 1,
    memories = {
        next_wander = { type = 'integer', persistent = false },
        wander_until = { type = 'integer', persistent = false }
    },
    behaviors = {
        ai.behavior {
            id = 'elitemobs:behavior/idle_walk', priority = 50,
            controls = { ai.controls.move, ai.controls.look },
            can_start = function(c)
                return c.perception:current_target() == nil and c.tick >= (c.memory:get('next_wander') or 0)
            end,
            can_continue = function(c)
                return c.perception:current_target() == nil and c.tick < (c.memory:get('wander_until') or 0)
            end,
            start = function(c)
                local at = c.entity.current_location
                if not at then return end
                local angle = math.random() * math.pi * 2
                local radius = 2 + math.random() * 4
                local destination = { world = at.world, x = at.x + math.cos(angle) * radius,
                    y = at.y, z = at.z + math.sin(angle) * radius }
                c.actuator:look_at(destination)
                c.actuator:move_to(destination, 0.7)
                c.memory:set('wander_until', c.tick + 60)
                c.memory:set('next_wander', c.tick + 100 + math.random(0, 60))
            end,
            tick = function(c) end,
            stop = function(c) c.actuator:stop_moving() end
        }
    }
}
