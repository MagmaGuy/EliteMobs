package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonPotionBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
    public EnderDragonPotionBombardmentLuaConfig() {
        super("ender_dragon_potion_bombardment", 30, 5, """
                local boss_location = context.boss:get_location()
                for _ = 1, 5 do
                  local effect_type = "SLOWNESS"
                  if math.random(0, 1) == 1 then
                    effect_type = "STRENGTH"
                  end

                  local spawn_location = offset_location(
                    boss_location,
                    -math.random(-1, 0),
                    -1,
                    -math.random(-1, 0)
                  )

                  context.world:spawn_splash_potion_at_location(spawn_location, {
                    effects = {
                      {
                        type = effect_type,
                        duration = 0,
                        amplifier = 1,
                        overwrite = true
                      }
                    },
                    velocity = em.create_vector(
                      math.random() - 0.5,
                      0,
                      math.random() - 0.5
                    )
                  })
                end
                """);
    }
}
