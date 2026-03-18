package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackWebLuaConfig extends LuaPowersConfigFields {
    public AttackWebLuaConfig() {
        super("attack_web", Material.COBWEB.toString(), PowersConfigFields.PowerType.MISCELLANEOUS);
    }

    @Override
    public String getSource() {
        return """
                return {
                  api_version = 1,
                  on_player_damaged_by_boss = function(context)
                    if context.player == nil then
                      return
                    end
                    if not context.cooldowns.local_ready() or not context.cooldowns.global_ready() then
                      return
                    end

                    context.player:place_temporary_block("COBWEB", 60, true)
                    context.boss:apply_potion_effect("SPEED", 60, 2)
                    context.cooldowns.set_local(60)
                    context.cooldowns.set_global(20)
                  end
                }
                """;
    }
}
