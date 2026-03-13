package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonFireballBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
    public EnderDragonFireballBombardmentLuaConfig() {
        super("ender_dragon_fireball_bombardment", 30, 5, """
                if context.state.firing_timer % 10 ~= 0 then
                  return
                end

                local function rotate_around_y_bug(vector, rotation)
                  local cosine = math.cos(rotation)
                  local sine = math.sin(rotation)
                  return em.create_vector(
                    (vector.x * cosine) + (vector.z * sine),
                    vector.y,
                    (-vector.x * sine) + (vector.z * cosine)
                  )
                end

                local function generate_fireball(vector)
                  local boss_location = context.boss:get_location()
                  local origin = offset_location(boss_location, vector.x, vector.y, vector.z)
                  context.boss:summon_projectile(
                    "FIREBALL",
                    origin,
                    offset_location(origin, 0, -0.1, 0),
                    0.1,
                    {
                      spawn_at_origin = true,
                      direction_only = true,
                      yield = 5
                    }
                  )
                end

                local direction = context.boss:get_location().direction
                local rotation = math.atan2(direction.x, direction.z) * 180 / math.pi
                local direction1 = rotate_around_y_bug(em.create_vector(3, -4, 0), rotation)
                local direction2 = rotate_around_y_bug(em.create_vector(-3, -4, 0), rotation)

                generate_fireball(direction1)
                generate_fireball(direction2)
                """);
    }
}
