package com.magmaguy.elitemobs.instanced.dungeons;

import lombok.Getter;

import java.util.Locale;

public class DungeonObjective {

    @Getter
    protected boolean completed = false;
    @Getter
    protected DungeonInstance dungeonInstance;

    /*
    Valid configuration string formats:
    Kill target: filename=boss.yml:amount=X
     */
    public DungeonObjective(DungeonInstance dungeonInstance, String objectiveString) {
        this.dungeonInstance = dungeonInstance;
    }

    public static DungeonObjective registerObjective(DungeonInstance dungeonInstance, String objectiveString) {
        return new DungeonKillTargetObjective(dungeonInstance, parse(objectiveString));
    }

    public static TargetDefinition parse(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Empty dungeon objective");
        String input = value.trim();
        if (!input.contains("=") && input.toLowerCase(Locale.ROOT).endsWith(".yml")) input = "filename=" + input;
        String filename = null;
        Integer amount = null;
        for (String field : input.split(":", -1)) {
            String[] pair = field.split("=", -1);
            if (pair.length != 2) throw new IllegalArgumentException("Invalid dungeon objective: " + value);
            String key = pair[0].trim().toLowerCase(Locale.ROOT);
            String argument = pair[1].trim();
            switch (key) {
                case "filename" -> {
                    if (filename != null || !argument.toLowerCase(Locale.ROOT).endsWith(".yml"))
                        throw new IllegalArgumentException("Invalid or duplicate objective filename: " + value);
                    filename = argument;
                }
                case "amount" -> {
                    if (amount != null) throw new IllegalArgumentException("Duplicate objective amount: " + value);
                    try { amount = Integer.valueOf(argument); }
                    catch (NumberFormatException failure) { throw new IllegalArgumentException("Invalid objective amount: " + value, failure); }
                    if (amount < 1) throw new IllegalArgumentException("Objective amount must be positive: " + value);
                }
                case "clearpercentage", "clearpercent" -> throw new IllegalArgumentException(
                        "Percentage dungeon objectives are unsupported; use filename=<boss.yml>:amount=<count>: " + value);
                default -> throw new IllegalArgumentException("Unknown dungeon objective field: " + value);
            }
        }
        if (filename == null) throw new IllegalArgumentException("Dungeon objective requires a filename: " + value);
        return new TargetDefinition(filename, amount == null ? 1 : amount);
    }

    public record TargetDefinition(String filename, int amount) {}

    protected void initializeObjective(DungeonInstance dungeonInstance) {
        this.dungeonInstance = dungeonInstance;
    }

    public void unregister() {
    }

}
