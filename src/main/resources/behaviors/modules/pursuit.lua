-- Navigation and collision come from the body's native locomotion profile.
return ai.module {
    id = 'elitemobs:behavior/pursuit', revision = 1,
    behaviors = {
        ai.behavior {
            id = 'elitemobs:behavior/pursue', priority = 20,
            controls = { ai.controls.move, ai.controls.look },
            can_start = function(c) return c.perception:current_target() ~= nil end,
            can_continue = function(c) return c.perception:current_target() ~= nil end,
            tick = function(c)
                local target = c.perception:current_target()
                if not target or not target.is_valid or target.is_dead
                        or (target.game_mode ~= 'survival' and target.game_mode ~= 'adventure') then
                    c.actuator:stop_moving()
                    return
                end
                local at, own = target.current_location, c.entity.current_location
                if not at or not own or at.world ~= own.world then
                    c.actuator:stop_moving()
                    return
                end
                c.actuator:look_at(at)
                local dx, dy, dz = at.x - own.x, at.y - own.y, at.z - own.z
                if dx * dx + dy * dy + dz * dz > 2.25 then
                    c.actuator:move_to(at, 1.0)
                else c.actuator:stop_moving() end
            end,
            stop = function(c) c.actuator:stop_moving() end
        }
    }
}
