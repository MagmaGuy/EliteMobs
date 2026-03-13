package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class ShieldWallLuaConfig extends InlineLuaPowerConfig {
    public ShieldWallLuaConfig() {
        super("shield_wall", Material.SHIELD.toString(), PowersConfigFields.PowerType.DEFENSIVE, """
                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if context.boss:shield_wall_is_active() then
                      if context.player ~= nil and context.boss:shield_wall_absorb_damage(context.player, context.event.damage_amount) then
                        context.event.cancel_event()
                      end
                      return
                    end

                    if not context.cooldowns:local_ready("shield_wall")
                      or not context.cooldowns:global_ready()
                      or math.random() >= 0.1 then
                      return
                    end

                    context.cooldowns:set_local(20 * 60, "shield_wall")
                    context.cooldowns:set_global(20 * 7)
                    context.boss:initialize_shield_wall(math.random(1, 4))
                  end,
                  on_exit_combat = function(context)
                    context.boss:deactivate_shield_wall()
                  end
                }
                """);
    }
}
