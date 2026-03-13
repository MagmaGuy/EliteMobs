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
                  context.scheduler:run_after(20, function(context)
                    local task_id
                    local function do_dialog()
                      if not context.boss:is_alive() then
                        if reinforcement_dad ~= nil and reinforcement_dad:is_alive() then
                          say_random_line(context, reinforcement_dad, death_message)
                        end
                        if reinforcement_mom ~= nil and reinforcement_mom:is_alive() then
                          say_random_line(context, reinforcement_mom, death_message)
                        end
                        if task_id ~= nil then
                          context.scheduler:cancel_task(task_id)
                        end
                        return false
                      end

                      if math.random() < 0.5 then
                        say_random_line(context, context.boss, boss_entity_dialog)
                      end

                      if reinforcement_dad ~= nil and reinforcement_dad:is_alive() and math.random() < 0.5 then
                        say_random_line(context, reinforcement_dad, zombie_dad)
                      end

                      if reinforcement_mom ~= nil and reinforcement_mom:is_alive() and math.random() < 0.5 then
                        say_random_line(context, reinforcement_mom, zombie_mom)
                      end

                      return true
                    end

                    if do_dialog() then
                      task_id = context.scheduler:run_every(20 * 8, function(context)
                        do_dialog()
                      end)
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if math.random() > 0.01 then
                      return
                    end

                    context.state.zombie_parents_firing = false

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
