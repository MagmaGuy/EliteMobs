package com.magmaguy.elitemobs.config.luapowers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;

public abstract class EnderDragonBombardmentLuaConfig extends InlineLuaPowerConfig {

    protected EnderDragonBombardmentLuaConfig(String baseFileName,
                                              int powerCooldownSeconds,
                                              int globalCooldownSeconds,
                                              String taskBehavior) {
        super(baseFileName,
                null,
                PowersConfigFields.PowerType.UNIQUE,
                buildSource(powerCooldownSeconds * 20, globalCooldownSeconds * 20, taskBehavior));
    }

    private static String buildSource(int powerCooldownTicks, int globalCooldownTicks, String taskBehavior) {
        return """
                local function clone_location(location)
                  return em.create_location(
                    location.x,
                    location.y,
                    location.z,
                    location.world,
                    location.yaw,
                    location.pitch
                  )
                end

                local function offset_location(location, x, y, z)
                  return em.create_location(
                    location.x + x,
                    location.y + y,
                    location.z + z,
                    location.world,
                    location.yaw,
                    location.pitch
                  )
                end

                local function vector_length(vector)
                  return math.sqrt(
                    (vector.x * vector.x) +
                    (vector.y * vector.y) +
                    (vector.z * vector.z)
                  )
                end

                local function is_flying_phase(phase)
                  return phase == "HOVER" or phase == "CIRCLING" or phase == "STRAFING"
                end

                local function is_blocked_phase(phase)
                  return phase == "DYING"
                    or phase == "HOVER"
                    or phase == "ROAR_BEFORE_ATTACK"
                    or phase == "FLY_TO_PORTAL"
                    or phase == "SEARCH_FOR_BREATH_ATTACK_TARGET"
                end

                local function get_box_players(context, x_range, y_range, z_range)
                  local players_in_box = {}
                  local boss_location = context.boss:get_location()
                  local players = context.players:all_players_in_world()
                  for index = 1, #players do
                    local player = players[index]
                    local player_location = player:get_location()
                    if player_location.world == boss_location.world
                      and math.abs(player_location.x - boss_location.x) <= x_range
                      and math.abs(player_location.y - boss_location.y) <= y_range
                      and math.abs(player_location.z - boss_location.z) <= z_range then
                      players_in_box[#players_in_box + 1] = player
                    end
                  end
                  return players_in_box
                end

                local function cancel_state_task(context, key)
                  local task_id = context.state[key]
                  if task_id ~= nil then
                    context.scheduler:cancel_task(task_id)
                    context.state[key] = nil
                  end
                end

                local function deactivate(context)
                  context.state.firing = false
                  context.state.is_active = false
                  context.state.firing_timer = 0
                  cancel_state_task(context, "activation_task")
                  cancel_state_task(context, "firing_task")
                end

                local function stop_condition(context)
                  if not context.boss.exists or not context.boss:is_alive() or not context.boss.is_in_combat then
                    deactivate(context)
                    return true
                  end

                  local phase = context.boss:get_ender_dragon_phase()
                  if phase ~= nil and not is_flying_phase(phase) then
                    deactivate(context)
                    return true
                  end

                  return false
                end

                local function task_behavior(context)
                __TASK_BEHAVIOR__
                end

                local function fire(context)
                  context.cooldowns:set_local(__POWER_COOLDOWN__)
                  context.cooldowns:set_global(__GLOBAL_COOLDOWN__)
                  context.state.is_active = true
                  context.state.firing_timer = 0
                  cancel_state_task(context, "firing_task")
                  context.state.firing_task = context.scheduler:run_every(1, function(context)
                    context.state.firing_timer = (context.state.firing_timer or 0) + 1
                    if stop_condition(context) or context.state.firing_timer > (20 * 5) then
                      cancel_state_task(context, "firing_task")
                      context.state.firing = false
                      return
                    end

                    task_behavior(context)
                  end)
                end

                return {
                  api_version = 1,
                  on_enter_combat = function(context)
                    if context.state.is_active then
                      return
                    end

                    context.state.is_active = true
                    cancel_state_task(context, "activation_task")
                    context.state.activation_task = context.scheduler:run_every(5, function(context)
                      if stop_condition(context) then
                        return
                      end

                      if context.state.firing or not context.cooldowns:local_ready() or not context.cooldowns:global_ready() then
                        return
                      end

                      if math.random() > 0.1 then
                        return
                      end

                      if is_blocked_phase(context.boss:get_ender_dragon_phase()) then
                        return
                      end

                      if #get_box_players(context, 10, 100, 10) > 0 then
                        context.state.firing = true
                        fire(context)
                      end
                    end)
                  end,
                  on_exit_combat = function(context)
                    deactivate(context)
                  end
                }
                """
                .replace("__POWER_COOLDOWN__", Integer.toString(powerCooldownTicks))
                .replace("__GLOBAL_COOLDOWN__", Integer.toString(globalCooldownTicks))
                .replace("__TASK_BEHAVIOR__", indent(taskBehavior, 2));
    }

    private static String indent(String content, int levels) {
        String prefix = "  ".repeat(Math.max(0, levels));
        return prefix + content.replace("\n", "\n" + prefix);
    }
}
