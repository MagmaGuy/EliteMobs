package com.magmaguy.elitemobs.config.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.powers.lua.LuaPowerManager;
import com.magmaguy.magmacore.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PremadeLuaPowers {

    private static final List<PremadeLuaPower> PREMADE_LUA_POWERS = List.of(
            new PremadeLuaPower("attack_blinding.lua", "attack_blinding.yml", "WITCH", PowersConfigFields.PowerType.MISCELLANEOUS,
                    playerDamagedPotion("blindness", 60, 0, 0, 0)),
            new PremadeLuaPower("attack_confusing.lua", "attack_confusing.yml", "WITCH", PowersConfigFields.PowerType.MISCELLANEOUS,
                    playerDamagedPotion("nausea", 60, 0, 0, 0)),
            new PremadeLuaPower("attack_fire.lua", "attack_fire.yml", "BLAZE_POWDER", PowersConfigFields.PowerType.OFFENSIVE,
                    """
                            return {
                              api_version = 1,
                              on_player_damaged_by_boss = function(context)
                                if context.player == nil then
                                  return
                                end
                                context.player:set_fire_ticks(60)
                              end
                            }
                            """),
            new PremadeLuaPower("attack_freeze.lua", "attack_freeze.yml", "PACKED_ICE", PowersConfigFields.PowerType.OFFENSIVE,
                    """
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
                                  player:add_visual_freeze_ticks(1)
                                  if iterations >= 60 then
                                    context.scheduler.cancel_task(task_id)
                                  end
                                end)

                                context.cooldowns.set_local(300)
                                context.cooldowns.set_global(60)
                              end
                            }
                            """),
            new PremadeLuaPower("attack_gravity.lua", "attack_gravity.yml", "ELYTRA", PowersConfigFields.PowerType.OFFENSIVE,
                    playerDamagedPotion("levitation", 60, 0, 0, 0)),
            new PremadeLuaPower("attack_poison.lua", "attack_poison.yml", "EMERALD", PowersConfigFields.PowerType.OFFENSIVE,
                    playerDamagedPotion("poison", 60, 0, 0, 0)),
            new PremadeLuaPower("attack_weakness.lua", "attack_weakness.yml", "TOTEM_OF_UNDYING", PowersConfigFields.PowerType.MISCELLANEOUS,
                    playerDamagedPotion("weakness", 60, 0, 0, 0)),
            new PremadeLuaPower("attack_web.lua", "attack_web.yml", "COBWEB", PowersConfigFields.PowerType.MISCELLANEOUS,
                    """
                            return {
                              api_version = 1,
                              on_player_damaged_by_boss = function(context)
                                if context.player == nil then
                                  return
                                end
                                if not context.cooldowns.local_ready() or not context.cooldowns.global_ready() then
                                  return
                                end

                                context.player:place_temporary_block("COBWEB", 120, true)
                                context.cooldowns.set_local(60)
                                context.cooldowns.set_global(20)
                              end
                            }
                            """),
            new PremadeLuaPower("attack_wither.lua", "attack_wither.yml", "WITHER_SKELETON_SKULL", PowersConfigFields.PowerType.OFFENSIVE,
                    playerDamagedPotion("wither", 60, 0, 0, 0)),
            new PremadeLuaPower("corpse.lua", "corpse.yml", "BONE_BLOCK", PowersConfigFields.PowerType.MISCELLANEOUS,
                    """
                            return {
                              api_version = 1,
                              on_death = function(context)
                                context.boss:place_temporary_block("BONE_BLOCK", 2400, true)
                              end
                            }
                            """),
            new PremadeLuaPower("invisibility.lua", "invisibility.yml", "GLASS_PANE", PowersConfigFields.PowerType.MISCELLANEOUS,
                    spawnSelfPotion("invisibility", Integer.MAX_VALUE, 0)),
            new PremadeLuaPower("invulnerability_fire.lua", "invulnerability_fire.yml", "FLAME", PowersConfigFields.PowerType.DEFENSIVE,
                    spawnSelfPotion("fire_resistance", Integer.MAX_VALUE, 1)),
            new PremadeLuaPower("moonwalk.lua", "moonwalk.yml", "SLIME_BLOCK", PowersConfigFields.PowerType.MISCELLANEOUS,
                    spawnSelfPotion("jump_boost", Integer.MAX_VALUE, 3))
    );

    private PremadeLuaPowers() {
    }

    public static Map<String, PowersConfigFields> load() {
        LinkedHashMap<String, PowersConfigFields> loadedPowers = new LinkedHashMap<>();
        File powersDirectory = new File(MetadataHandler.PLUGIN.getDataFolder(), "powers");
        if (!powersDirectory.exists() && !powersDirectory.mkdirs()) {
            Logger.warn("Failed to create powers directory for premade Lua powers.");
            return loadedPowers;
        }

        for (PremadeLuaPower premadeLuaPower : PREMADE_LUA_POWERS) {
            try {
                File file = new File(powersDirectory, premadeLuaPower.luaFileName());
                if (!file.exists() && !file.createNewFile()) {
                    Logger.warn("Failed to create premade Lua power file " + premadeLuaPower.luaFileName() + ".");
                    continue;
                }
                Files.writeString(file.toPath(), premadeLuaPower.source(), StandardCharsets.UTF_8);

                PowersConfigFields configFields = LuaPowerManager.loadLuaPower(
                        premadeLuaPower.luaFileName(),
                        file,
                        premadeLuaPower.effect(),
                        premadeLuaPower.powerType());

                loadedPowers.put(premadeLuaPower.luaFileName(), configFields);
                loadedPowers.put(premadeLuaPower.legacyYamlFileName(), configFields);
            } catch (IOException exception) {
                Logger.warn("Failed to seed premade Lua power " + premadeLuaPower.luaFileName() + ".");
                exception.printStackTrace();
            } catch (Exception exception) {
                Logger.warn("Failed to load premade Lua power " + premadeLuaPower.luaFileName() + ".");
                exception.printStackTrace();
            }
        }

        return loadedPowers;
    }

    private static String playerDamagedPotion(String potionEffectType,
                                             int duration,
                                             int amplifier,
                                             int localCooldown,
                                             int globalCooldown) {
        String cooldownGuard = "";
        String cooldownSet = "";
        if (localCooldown > 0 || globalCooldown > 0) {
            cooldownGuard = """
                                    if not context.cooldowns.local_ready() or not context.cooldowns.global_ready() then
                                      return
                                    end

                    """;
            StringBuilder builder = new StringBuilder();
            if (localCooldown > 0) {
                builder.append("    context.cooldowns.set_local(").append(localCooldown).append(")\n");
            }
            if (globalCooldown > 0) {
                builder.append("    context.cooldowns.set_global(").append(globalCooldown).append(")\n");
            }
            cooldownSet = builder.toString();
        }

        return """
                return {
                  api_version = 1,
                  on_player_damaged_by_boss = function(context)
                    if context.player == nil then
                      return
                    end
                %s    context.player:apply_potion_effect("%s", %d, %d)
                %s  end
                }
                """.formatted(cooldownGuard, potionEffectType, duration, amplifier, cooldownSet);
    }

    private static String spawnSelfPotion(String potionEffectType, int duration, int amplifier) {
        return """
                return {
                  api_version = 1,
                  on_spawn = function(context)
                    context.boss:apply_potion_effect("%s", %d, %d)
                  end
                }
                """.formatted(potionEffectType, duration, amplifier);
    }

    private record PremadeLuaPower(String luaFileName,
                                   String legacyYamlFileName,
                                   String effect,
                                   PowersConfigFields.PowerType powerType,
                                   String source) {
    }
}
