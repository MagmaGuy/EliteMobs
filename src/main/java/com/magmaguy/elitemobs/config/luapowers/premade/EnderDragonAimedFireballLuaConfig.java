package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonAimedFireballLuaConfig extends EnderDragonBombardmentLuaConfig {
    public EnderDragonAimedFireballLuaConfig() {
        super("ender_dragon_aimed_fireball", 30, 5, """
                if context.state.firing_timer % 20 ~= 0 then
                  return
                end

                local boss_location = context.boss:get_location()
                local players = get_box_players(context, 200, 100, 200)
                for index = 1, #players do
                  context.boss:summon_projectile(
                    "FIREBALL",
                    clone_location(boss_location),
                    players[index]:get_location(),
                    1,
                    {
                      spawn_at_origin = true,
                      direction_only = true,
                      yield = 5
                    }
                  )
                end
                """);
    }
}
