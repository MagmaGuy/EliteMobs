package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.luapowers.LuaPowersConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.List;

public class ZombieFriendsLuaConfig extends LuaPowersConfigFields {

    private static final List<String> FRIEND_DEATH_MESSAGE = List.of(
            "Noooo!",
            "Mediocre!",
            "Zacharias!",
            "He's deader than before!",
            "Vengeance!",
            "Revenge!",
            "I can't believe you've done this.");

    private static final List<String> ORIGINAL_ENTITY_DIALOG = List.of(
            "Let's play ZombieCraft later!",
            "Feel the power of friendship!",
            "El pueblo, unido!",
            "I called my friends over!",
            "BFF power!",
            "One for all!",
            "Get him!",
            "Screw you guys, I'm going home!");

    private static final List<String> REINFORCEMENT_DIALOG = List.of(
            "Don't mess with our friends!",
            "We got your back Zach!",
            "Backup has arrived!",
            "One for all, one for all!",
            "This is going to be easy!",
            "Give up we have the high ground!",
            "You wanna go bruv?",
            "Worldstaaaaaaaaar!",
            "What are you doing to our friend",
            "Feel the power of friendship!",
            "Friendship power at 100%!",
            "Zombies, assemble!",
            "We got your back mate!",
            "Together we are better!",
            "The more the merrier!",
            "I got you fam!",
            "All for one!");

    public ZombieFriendsLuaConfig() {
        super("zombie_friends", Material.ZOMBIE_HEAD.toString(), PowersConfigFields.PowerType.MAJOR_ZOMBIE);
    }

    @Override
    public String getSource() {
        return """
                local friend_death_message = %s
                local original_entity_dialog = %s
                local reinforcement_dialog = %s

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

                local function start_dialog_task(context, reinforcement_one, reinforcement_two)
                  context.scheduler:run_after(20, function(context)
                    local task_id
                    local function run_dialog()
                      local boss_alive = context.boss:is_alive()
                      local reinforcement_one_alive = reinforcement_one ~= nil and reinforcement_one:is_alive()
                      local reinforcement_two_alive = reinforcement_two ~= nil and reinforcement_two:is_alive()

                      if not boss_alive or (not reinforcement_one_alive and not reinforcement_two_alive) then
                        if reinforcement_one_alive then
                          say_random_line(context, reinforcement_one, friend_death_message)
                        end
                        if reinforcement_two_alive then
                          say_random_line(context, reinforcement_two, friend_death_message)
                        end
                        if task_id ~= nil then
                          context.scheduler:cancel_task(task_id)
                        end
                        return false
                      end

                      if math.random() < 0.5 then
                        say_random_line(context, context.boss, original_entity_dialog)
                      end

                      if reinforcement_one_alive and math.random() < 0.5 then
                        say_random_line(context, reinforcement_one, reinforcement_dialog)
                      end

                      if reinforcement_two_alive and math.random() < 0.5 then
                        say_random_line(context, reinforcement_two, reinforcement_dialog)
                      end

                      return true
                    end

                    if run_dialog() then
                      task_id = context.scheduler:run_every(20 * 8, function(context)
                        run_dialog()
                      end)
                    end
                  end)
                end

                return {
                  api_version = 1,
                  on_boss_damaged_by_player = function(context)
                    if context.state.zombie_friends_firing then
                      return
                    end

                    if math.random() > 0.01 then
                      return
                    end

                    context.state.zombie_friends_firing = true

                    local boss_location = context.boss:get_location()
                    local reinforcement_one = context.world:spawn_custom_boss_at_location(
                      "zombie_friends_friend.yml",
                      boss_location,
                      { level = context.boss.level, silent = false }
                    )
                    local reinforcement_two = context.world:spawn_custom_boss_at_location(
                      "zombie_friends_friend.yml",
                      boss_location,
                      { level = context.boss.level, silent = false }
                    )

                    start_dialog_task(context, reinforcement_one, reinforcement_two)
                  end
                }
                """.formatted(
                luaList(FRIEND_DEATH_MESSAGE),
                luaList(ORIGINAL_ENTITY_DIALOG),
                luaList(REINFORCEMENT_DIALOG));
    }
}
