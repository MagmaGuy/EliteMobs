package com.magmaguy.elitemobs.config.luapowers.premade;

public class EnderDragonEnderFireballBombardmentLuaConfig extends EnderDragonBombardmentLuaConfig {
    public EnderDragonEnderFireballBombardmentLuaConfig() {
        super("ender_dragon_ender_fireball_bombardment", 30, 5, """
                if math.random() > 0.1 then
                  return
                end

                local boss_location = context.boss:get_location()
                local players = get_box_players(context, 200, 100, 200)
                for index = 1, #players do
                  context.boss:summon_projectile(
                    "DRAGON_FIREBALL",
                    clone_location(boss_location),
                    players[index]:get_location(),
                    1,
                    {
                      spawn_at_origin = true,
                      direction_only = true,
                      track = false
                    }
                  )
                end
                """);
    }
}
