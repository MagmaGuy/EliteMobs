package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackLightningLuaConfig extends LuaPowersConfigFields {
    public AttackLightningLuaConfig() {
        super("attack_lightning", Material.HORN_CORAL.toString(), PowersConfigFields.PowerType.OFFENSIVE);
    }

    @Override
    public String getSource() {
        return """
                local function strike_after_warning(context, location)
                  local task_id
                  local counter = 0
                  task_id = context.scheduler:run_every(1, function()
                    counter = counter + 1
                    if counter > 60 then
                      context.scheduler:cancel_task(task_id)
                      context.world:strike_lightning_at_location(location)
                      return
                    end

                    context.world:spawn_particle_at_location(location, {
                      particle = "CRIT",
                      amount = 10,
                      x = 0.5,
                      y = 1.5,
                      z = 0.5,
                      speed = 0.3
                    }, 10)
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if not context.cooldowns.global_ready() then
                      return
                    end

                    context.cooldowns.set_global(300)
                    for _, player in ipairs(context.boss:get_nearby_players(20)) do
                      strike_after_warning(context, player:get_location())
                    end
                  end
                }
                """;
    }
}
