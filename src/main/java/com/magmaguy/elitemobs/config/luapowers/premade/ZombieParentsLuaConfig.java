package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;

public class ZombieParentsLuaConfig extends LuaPowersConfigFields {

    private static final List<String> DEATH_MESSAGE = List.of(
            "You monster!",
            "My baby!",
            "What have you done!?",
            "Revenge!",
            "Nooooo!",
            "You will pay for that!",
            "Eh, he was adopted",
            "He's dead! Again!",
            "He's deader than before!",
            "You broke him!");

    private static final List<String> BOSS_ENTITY_DIALOG = List.of(
            "You're embarrassing me!",
            "He's bullying me!",
            "He's the one picking on me!",
            "I can deal with this alone!",
            "Leave me alone, I got this!",
            "Stop following me around!",
            "God this is so embarrassing!",
            "He took my lunch money!",
            "He's bullying me!");

    private static final List<String> ZOMBIE_DAD = List.of(
            "Get away from my son!",
            "Stand up for yourself son!",
            "I'll deal with him!",
            "Stop picking on my son!",
            "Why are you doing this?",
            "I'll talk to your parents!",
            "You go kiddo!",
            "Show him who's boss kiddo!",
            "Nice punch kiddo!");

    private static final List<String> ZOMBIE_MOM = List.of(
            "Hands off my child!",
            "Are you hurt sweetie?",
            "Did he hurt you sweetie?",
            "Let me see that booboo sweetie",
            "I'll talk to his parents!",
            "You forgot your jacket sweetie!",
            "Posture, sweetheart",
            "Break it up!",
            "Stop this!",
            "Did you take out the garbage?");

    public ZombieParentsLuaConfig() {
        super("zombie_parents", Material.SKELETON_SKULL.toString(), PowersConfigFields.PowerType.MAJOR_ZOMBIE);
    }

    @Override
    public String getSource() {
        return """
                local death_message = %s
                local boss_entity_dialog = %s
                local zombie_dad = %s
                local zombie_mom = %s

                local function say_random_line(context, entity, list)
                  if entity == nil or #list == 0 then
                    return
                  end
                  entity:set_custom_name(list[math.random(#list)])
                  context.scheduler:run_after(20 * 3, function()
                    if entity ~= nil and entity.is_alive ~= nil and entity:is_alive() then
                      entity:reset_custom_name()
                    end
                  end)
                end

                local function start_dialog(context, reinforcement_mom, reinforcement_dad)
                  local families = context.state.zombie_parent_families
                  if families == nil then
                    families = {}
                    context.state.zombie_parent_families = families
                  end
                  families[#families + 1] = { mom = reinforcement_mom, dad = reinforcement_dad }
                  if context.state.zombie_parents_dialog_task ~= nil then
                    return
                  end

                  context.state.zombie_parents_dialog_task = context.scheduler:run_repeating(20, 20 * 8, function(context)
                    local boss_alive = context.boss:is_alive()
                    for index = #families, 1, -1 do
                      local family = families[index]
                      local mom_alive = family.mom ~= nil and family.mom:is_alive()
                      local dad_alive = family.dad ~= nil and family.dad:is_alive()
                      if not mom_alive and not dad_alive then
                        table.remove(families, index)
                      else
                        if dad_alive and (not boss_alive or math.random() < 0.5) then
                          say_random_line(context, family.dad, boss_alive and zombie_dad or death_message)
                        end
                        if mom_alive and (not boss_alive or math.random() < 0.5) then
                          say_random_line(context, family.mom, boss_alive and zombie_mom or death_message)
                        end
                      end
                    end
                    if not boss_alive or #families == 0 then
                      context.scheduler:cancel_task(context.state.zombie_parents_dialog_task)
                      context.state.zombie_parents_dialog_task = nil
                      context.state.zombie_parent_families = nil
                      return
                    end
                    if math.random() < 0.5 then
                      say_random_line(context, context.boss, boss_entity_dialog)
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if math.random() > 0.01 then
                      return
                    end

                    local boss_location = context.boss:get_location()
                    local reinforcement_mom = context.world:spawn_custom_boss_at_location(
                      "zombie_parents_mom.yml",
                      boss_location,
                      { level = context.boss.level, silent = false }
                    )
                    if reinforcement_mom == nil then
                      return
                    end

                    local reinforcement_dad = context.world:spawn_custom_boss_at_location(
                      "zombie_parents_dad.yml",
                      boss_location,
                      { level = context.boss.level, silent = false }
                    )
                    if reinforcement_dad == nil then
                      reinforcement_mom:remove_elite()
                      return
                    end

                    start_dialog(context, reinforcement_mom, reinforcement_dad)
                  end
                }
                """.formatted(
                luaList(DEATH_MESSAGE),
                luaList(BOSS_ENTITY_DIALOG),
                luaList(ZOMBIE_DAD),
                luaList(ZOMBIE_MOM));
    }
}
