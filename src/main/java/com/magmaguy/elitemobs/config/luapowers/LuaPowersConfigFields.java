package com.magmaguy.elitemobs.config.luapowers;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import lombok.Getter;

import java.util.List;

public abstract class LuaPowersConfigFields {

    @Getter
    private final String filename;
    @Getter
    private final String legacyFilename;
    @Getter
    private final String effect;
    @Getter
    private final PowersConfigFields.PowerType powerType;

    protected LuaPowersConfigFields(String baseFileName,
                                    String effect,
                                    PowersConfigFields.PowerType powerType) {
        this.filename = baseFileName + ".lua";
        this.legacyFilename = baseFileName + ".yml";
        this.effect = effect;
        this.powerType = powerType;
    }

    public abstract String getSource();

    protected static String playerDamagedPotion(String potionEffectType,
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

    protected static String spawnSelfPotion(String potionEffectType, int duration, int amplifier) {
        return """
                return {
                  api_version = 1,
                  on_spawn = function(context)
                    context.boss:apply_potion_effect("%s", %d, %d)
                  end
                }
                """.formatted(potionEffectType, duration, amplifier);
    }

    protected static String luaQuote(String value) {
        if (value == null) {
            return "\"\"";
        }
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    protected static String luaList(List<String> values) {
        StringBuilder builder = new StringBuilder("{\n");
        for (String value : values) {
            builder.append("    ").append(luaQuote(value)).append(",\n");
        }
        builder.append("  }");
        return builder.toString();
    }
}
