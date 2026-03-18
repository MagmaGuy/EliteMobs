package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackFreezeLuaConfig extends LuaPowersConfigFields {
    public AttackFreezeLuaConfig() {
        super("attack_freeze", Material.PACKED_ICE.toString(), PowersConfigFields.PowerType.OFFENSIVE);
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

                    local player = context.player
                    player:apply_potion_effect("slowness", 60, 5)
                    player:place_temporary_block("PACKED_ICE", 60, true)

                    local iterations = 0
                    local task_id = nil
                    task_id = context.scheduler.run_every(1, function()
                      iterations = iterations + 1
                      player:add_visual_freeze_ticks(3)
                      if iterations >= 60 then
                        context.scheduler.cancel_task(task_id)
                      end
                    end)

                    context.cooldowns.set_local(300)
                    context.cooldowns.set_global(60)
                  end
                }
                """;
    }
}
