package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

public class AttackArrowLuaConfig extends LuaPowersConfigFields {
    public AttackArrowLuaConfig() {
        super("attack_arrow", Material.DISPENSER.toString(), PowersConfigFields.PowerType.OFFENSIVE);
    }

    @Override
    public String getSource() {
        return """
                local function valid_player(player)
                  return player ~= nil and (player.game_mode == "SURVIVAL" or player.game_mode == "ADVENTURE")
                end

                local function clear_task(context)
                  if context.state.attack_arrow_task ~= nil then
                    context.scheduler:cancel_task(context.state.attack_arrow_task)
                    context.state.attack_arrow_task = nil
                  end
                end

                return {
                  api_version = 1,
                  on_boss_target_changed = function(context)
                    if context.state.attack_arrow_task ~= nil then
                      return
                    end

                    local task_id
                    task_id = context.scheduler:run_every(160, function()
                      local target = context.boss:get_target_player()
                      if target == nil then
                        clear_task(context)
                        return
                      end

                      for _, player in ipairs(context.boss:get_nearby_players(20)) do
                        if valid_player(player) then
                          context.boss:summon_projectile("ARROW", context.boss:get_location(), player:get_location(), 2.0)
                        end
                      end
                    end)

                    context.state.attack_arrow_task = task_id
                  end
                }
                """;
    }
}
