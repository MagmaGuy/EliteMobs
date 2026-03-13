package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonArrowBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
    public EnderDragonArrowBombardmentLuaConfig() {
        super("ender_dragon_arrow_bombardment", 30, 5, """
                local boss_location = context.boss:get_location()
                local players = get_box_players(context, 200, 100, 200)
                for index = 1, #players do
                  local target_location = offset_location(players[index]:get_location(), 0, 1, 0)
                  local shot_vector = context.vectors:get_vector_between_locations(boss_location, target_location)
                  shot_vector = em.create_vector(
                    shot_vector.x + (math.random() - 0.5),
                    shot_vector.y + (math.random() - 0.5),
                    shot_vector.z + (math.random() - 0.5)
                  )
                  shot_vector = context.vectors:normalize_vector(shot_vector)
                  shot_vector = em.create_vector(
                    shot_vector.x * 2,
                    shot_vector.y * 2,
                    shot_vector.z * 2
                  )

                  local origin = offset_location(boss_location, shot_vector.x, shot_vector.y + 1, shot_vector.z)
                  context.boss:summon_projectile(
                    "ARROW",
                    origin,
                    offset_location(origin, shot_vector.x, shot_vector.y, shot_vector.z),
                    vector_length(shot_vector),
                    {
                      spawn_at_origin = true,
                      gravity = false,
                      persistent = false,
                      duration = 20 * 4
                    }
                  )
                end
                """);
    }
}
