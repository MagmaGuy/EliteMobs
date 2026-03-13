package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonEndermiteBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
    public EnderDragonEndermiteBombardmentLuaConfig() {
        super("ender_dragon_endermite_bombardment", 120, 30, """
                local function rotate_around_y_bug(vector, rotation)
                  local cosine = math.cos(rotation)
                  local sine = math.sin(rotation)
                  return em.create_vector(
                    (vector.x * cosine) + (vector.z * sine),
                    vector.y,
                    (-vector.x * sine) + (vector.z * cosine)
                  )
                end

                local function spawn_endermite(offset)
                  local spawned = context.world:spawn_custom_boss_at_location(
                    "binder_of_worlds_phase_1_endermite_reinforcement.yml",
                    offset_location(context.boss:get_location(), offset.x, offset.y, offset.z),
                    {
                      add_as_reinforcement = true
                    }
                  )
                  if spawned ~= nil then
                    spawned:apply_potion_effect("SLOW_FALLING", 20 * 5, 0)
                  end
                end

                local direction = context.boss:get_location().direction
                local rotation = math.atan2(direction.x, direction.z) * 180 / math.pi
                local direction1 = rotate_around_y_bug(em.create_vector(1, -4, 0), rotation)
                local direction2 = rotate_around_y_bug(em.create_vector(-1, -4, 0), rotation)

                spawn_endermite(direction1)
                spawn_endermite(direction2)
                """);
    }
}
