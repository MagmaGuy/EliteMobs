package com.magmaguy.elitemobs.config.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.CustomConfigFieldsInterface;
import com.magmaguy.elitemobs.config.VanillaItemDrop;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.SpecialLoot;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CustomBossesConfigFields extends CustomConfigFields implements CustomConfigFieldsInterface {


    private static final HashSet<CustomBossesConfigFields> naturallySpawnedElites = new HashSet<>();
    public static HashMap<String, CustomBossesConfigFields> regionalElites = new HashMap<>();
    @Getter
    private final List<UniqueLoot> parsedUniqueLootList = new ArrayList<>();
    @Getter
    private final List<VanillaItemDrop> parsedVanillaLootList = new ArrayList<>();
    @Getter
    private final HashMap<String, SpecialLoot> specialLoot = new HashMap();
    @Getter
    @Setter
    private EntityType entityType = EntityType.ZOMBIE;
    @Getter
    @Setter
    private String name = "Default Name";
    @Setter
    private String level = "dynamic";
    @Getter
    @Setter
    private boolean isPersistent = false;
    @Getter
    @Setter
    private double healthMultiplier = 1;
    @Getter
    @Setter
    private double damageMultiplier = 1;
    @Getter
    @Setter
    private boolean frozen = false;
    @Getter
    @Setter
    private boolean reinforcement = false;
    @Getter
    @Setter
    private List<String> onDeathCommands = new ArrayList<>();
    @Getter
    @Setter
    private List<String> onSpawnCommands = new ArrayList<>();
    @Getter
    @Setter
    private List<String> onCombatEnterCommands = new ArrayList<>();
    @Getter
    @Setter
    private List<String> onCombatLeaveCommands = new ArrayList<>();
    @Getter
    @Setter
    private List<String> uniqueLootList = new ArrayList<>();
    @Getter
    @Setter
    private List<String> powers = new ArrayList<>();
    @Getter
    @Setter
    private List<String> onDamageMessages = new ArrayList<>();
    @Getter
    @Setter
    private List<String> onDamagedMessages = new ArrayList<>();
    @Getter
    @Setter
    private List<String> trails = new ArrayList<>();
    @Getter
    @Setter
    private List<String> phases = new ArrayList<>();
    @Getter
    @Setter
    private String locationMessage = null;
    @Getter
    @Setter
    private String mountedEntity = null;
    @Getter
    @Setter
    private String spawnMessage = null;
    @Getter
    @Setter
    private String deathMessage = null;
    @Getter
    @Setter
    private List<String> deathMessages = new ArrayList<>();
    @Getter
    @Setter
    private String escapeMessage = null;
    @Getter
    @Setter
    private boolean baby = false;
    @Getter
    @Setter
    private boolean dropsEliteMobsLoot = true;
    @Getter
    @Setter
    private boolean dropsVanillaLoot = true;
    /**
     * Integration with LibsDisguises. Only used if that plugin is loaded.
     */
    @Getter
    @Setter
    private String disguise = null;
    @Getter
    @Setter
    private String customDisguiseData = null;
    /**
     * Announcement priority:
     * 0 - no messages
     * 1 - spawn/kill/escape messages
     * 2 - spawn/kill/escape messages + player tracking
     * 3 - spawn/kill/escape messages + player tracking + DiscordSRV discord notifications
     * <p>
     * Default is 1 since the spawn messages have to be added to config files intentionally and it's weird to have to
     * enabled them elsewhere on purpose
     */
    @Getter
    @Setter
    private int announcementPriority = 0;
    @Getter
    @Setter
    private Integer followDistance = 0;
    @Getter
    @Setter
    private int spawnCooldown = 0;
    @Getter
    @Setter
    private int timeout = 0;
    @Getter
    @Setter
    private double leashRadius = 0;
    @Getter
    @Setter
    private ItemStack helmet = null;
    @Getter
    @Setter
    private ItemStack chestplate = null;
    @Getter
    @Setter
    private ItemStack leggings = null;
    @Getter
    @Setter
    private ItemStack boots = null;
    @Getter
    @Setter
    private ItemStack mainHand = null;
    @Getter
    @Setter
    private ItemStack offHand = null;
    @Getter
    private boolean regionalBoss = false;
    @Getter
    @Setter
    private boolean cullReinforcements = true;
    @Getter
    @Setter
    private HashMap<Material, Double> damageModifiers = new HashMap();

    //this saves files for regional boss respawn cooldowns
    @Getter
    @Setter
    private boolean filesOutOfSync = false;
    @Getter
    private List<String> onSpawnBlockStates = new ArrayList<>(), onRemoveBlockStates = new ArrayList<>();

    /**
     * Creates a new default pre-made Custom Boss. The boss is further customized through a builder pattern.
     *
     * @param fileName
     * @param entityType
     * @param isEnabled
     * @param name
     * @param level
     */
    public CustomBossesConfigFields(String fileName,
                                    EntityType entityType,
                                    boolean isEnabled,
                                    String name,
                                    String level) {
        super(fileName, isEnabled);
        this.entityType = entityType;
        this.name = name;
        this.level = level;
    }

    public CustomBossesConfigFields(String fileName,
                                    boolean isEnabled) {
        super(fileName, isEnabled);
    }

    public static HashSet<CustomBossesConfigFields> getNaturallySpawnedElites() {
        return naturallySpawnedElites;
    }

    //This method unifies all level placeholders down to $level and applies a custom level for quest display purposes
    public String getCleanName(int level) {
        return ChatColorConverter.convert(getName().replace("$level", level + "")
                .replace("$normalLevel", ChatColorConverter.convert("&2[&a" + level + "&2]&f"))
                .replace("$minibossLevel", ChatColorConverter.convert("&6〖&e" + level + "&6〗&f"))
                .replace("$bossLevel", ChatColorConverter.convert("&4『&c" + level + "&4』&f"))
                .replace("$reinforcementLevel", ChatColorConverter.convert("&8〔&7") + level + "&8〕&f")
                .replace("$eventBossLevel", ChatColorConverter.convert("&4「&c" + level + "&4」&f")));
    }

    public void runtimeSetLeashRadius(double leashRadius) {
        this.leashRadius = leashRadius;
        this.fileConfiguration.set("leashRadius", leashRadius);
        ConfigurationEngine.fileSaverCustomValues(fileConfiguration, file);
    }

    public double getDamageModifier(Material material) {
        return damageModifiers.get(material) == null ? 1 : damageModifiers.get(material);
    }

    public int getLevel() {
        if (level.equalsIgnoreCase("dynamic")) {
            return -1;
        } else {
            try {
                return Integer.valueOf(level);
            } catch (Exception ex) {
                new WarningMessage("Regional Elite Mob level for " + getFilename() + " is neither numeric nor dynamic. Fix the configuration for it.");
                return 1;
            }
        }
    }

    public void setOnSpawnBlockStates(List<String> onSpawnBlockStates) {
        this.onSpawnBlockStates = onSpawnBlockStates;
        fileConfiguration.set("onSpawnBlockStates", onSpawnBlockStates);
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            new WarningMessage("Failed to save on spawn block states!", true);
        }
    }

    public void setOnRemoveBlockStates(List<String> onRemoveBlockStates) {
        this.onRemoveBlockStates = onRemoveBlockStates;
        fileConfiguration.set("onRemoveBlockStates", onRemoveBlockStates);
        try {
            fileConfiguration.save(file);
        } catch (Exception ex) {
            new WarningMessage("Failed to save on remove block states!", true);
        }
    }

    @Override
    public void processConfigFields() {
        this.isEnabled = processBoolean("isEnabled", isEnabled, true, true);
        this.entityType = processEnum("entityType", entityType, EntityType.ZOMBIE, true);
        this.name = processString("name", name, "Default Name", true);
        //Levels are strings because "dynamic" is a valid value
        this.level = processString("level", level, "dynamic", true);
        this.isPersistent = processBoolean("isPersistent", isPersistent, false, false);
        this.healthMultiplier = processDouble("healthMultiplier", healthMultiplier, 1D, false);
        this.damageMultiplier = processDouble("damageMultiplier", damageMultiplier, 1D, false);
        this.baby = processBoolean("isBaby", baby, false, false);
        this.dropsEliteMobsLoot = processBoolean("dropsEliteMobsLoot", dropsEliteMobsLoot, true, false);
        this.dropsVanillaLoot = processBoolean("dropsVanillaLoot", dropsVanillaLoot, true, false);
        this.frozen = processBoolean("frozen", frozen, false, false);
        this.reinforcement = processBoolean("reinforcement", reinforcement, false, false);
        this.onDeathCommands = processStringList("onDeathCommands", onDeathCommands, new ArrayList<>(), false);
        this.onSpawnCommands = processStringList("onSpawnCommands", onSpawnCommands, new ArrayList<>(), false);
        this.onCombatEnterCommands = processStringList("onCombatEnterCommands", onCombatEnterCommands, new ArrayList<>(), false);
        this.onCombatLeaveCommands = processStringList("onCombatLeaveCommands", onCombatLeaveCommands, new ArrayList<>(), false);
        this.deathMessages = processStringList("deathMessages", deathMessages, new ArrayList<>(), false);
        this.uniqueLootList = processStringList("uniqueLootList", uniqueLootList, new ArrayList<>(), false);
        for (String entry : this.uniqueLootList) {
            if (SpecialLoot.isSpecialLootEntry(entry)) continue;
            if (entry.contains("minecraft:")) continue;
            try {
                CustomItem customItem = CustomItem.getCustomItem(entry.split(":")[0]);
                if (customItem == null)
                    throw new Exception();
                this.parsedUniqueLootList.add(new UniqueLoot(Double.parseDouble(entry.split(":")[1]), customItem));
            } catch (Exception ex) {
                new WarningMessage("Boss " + this.getName() + " has an invalid loot entry - " + entry + " - Skipping it!");
            }
        }
        for (String string : uniqueLootList)
            if (SpecialLoot.isSpecialLootEntry(string))
                specialLoot.put(string, new SpecialLoot(string));
        // minecraft:type=ITEM_TYPE:amount=AMOUNT
        for (String string : uniqueLootList)
            if (string.contains("minecraft:")) {
                try {
                    String[] processedStrings = string.split(":");
                    Material type = null;
                    int amount = 1;
                    double chance = 1d;
                    for (String processedString : processedStrings) {
                        if (processedString.equals("minecraft"))
                            continue;
                        else if (processedString.contains("type="))
                            try {
                                type = Material.valueOf(processedString.split("=")[1]);
                            } catch (Exception ex) {
                                new WarningMessage("Custom Item entry " + string + " for boss " + getFilename() + " is not a valid entry!");
                                new WarningMessage("Material type " + processedString.split("=")[1] + " is not valid! Make sure you are using valid Spigot API material values! These may not be the same as Minecraft item names.");
                            }
                        else if (processedString.contains("amount="))
                            try {
                                amount = Integer.parseInt(processedString.split("=")[1]);
                            } catch (Exception ex) {
                                new WarningMessage("Custom Item entry " + string + " for boss " + getFilename() + " is not a valid entry!");
                                new WarningMessage("Amount " + processedString.split("=")[1] + " is not valid! Make sure you have a valid natural number.");
                            }
                        else if (processedString.contains("chance="))
                            try {
                                chance = Double.parseDouble(processedString.split("=")[1]);
                            } catch (Exception ex) {
                                new WarningMessage("Custom Item entry " + string + " for boss " + getFilename() + " is not a valid entry!");
                                new WarningMessage("Chance " + processedString.split("=")[1] + " is not valid! Make sure you have a valid number between 0 and 1.");
                            }
                    }
                    if (type == null) continue;
                    ItemStack processedItemStack = new ItemStack(type, amount);
                    parsedVanillaLootList.add(new VanillaItemDrop(processedItemStack, chance));
                } catch (Exception ex) {
                    new WarningMessage("Custom Item entry " + string + " for boss " + getFilename() + " is not a valid entry! Are you using the correct spigot API material names?");
                    new WarningMessage("Correct format: " + "minecraft:type=MATERIAL_TYPE:amount=AMOUNT");
                }
            }

        //this can't be converted directly to an enum list because there are some special string features in here
        this.powers = processStringList("powers", powers, new ArrayList<>(), false);
        this.onDamageMessages = processStringList("onDamageMessages", onDamageMessages, new ArrayList<>(), false);
        this.onDamagedMessages = processStringList("onDamagedMessages", onDamagedMessages, new ArrayList<>(), false);
        this.trails = processStringList("trails", trails, new ArrayList<>(), false);
        this.phases = processStringList("phases", phases, new ArrayList<>(), false);
        this.locationMessage = processString("locationMessage", locationMessage, null, false);
        this.mountedEntity = processString("mountedEntity", mountedEntity, null, false);
        this.spawnMessage = processString("spawnMessage", spawnMessage, null, false);
        this.deathMessage = processString("deathMessage", deathMessage, null, false);
        this.escapeMessage = processString("escapeMessage", escapeMessage, null, false);
        this.disguise = processString("disguise", disguise, null, false);
        this.customDisguiseData = processString("customDisguiseData", customDisguiseData, null, false);
        this.announcementPriority = processInt("announcementPriority", announcementPriority, 0, false);
        this.followDistance = processInt("followDistance", followDistance, 0, false);
        this.spawnCooldown = processInt("spawnCooldown", spawnCooldown, 0, false);
        this.timeout = processInt("timeout", timeout, 0, false);
        this.leashRadius = processDouble("leashRadius", leashRadius, 0, false);
        this.helmet = processItemStack("helmet", helmet, null, false);
        this.chestplate = processItemStack("chestplate", chestplate, null, false);
        this.leggings = processItemStack("leggings", leggings, null, false);
        this.boots = processItemStack("boots", boots, null, false);
        this.mainHand = processItemStack("mainHand", mainHand, null, false);
        this.offHand = processItemStack("offHand", offHand, null, false);
        this.regionalBoss = processBoolean("isRegionalBoss", isRegionalBoss(), false, false);

        this.onSpawnBlockStates = processStringList("onSpawnBlockStates", onSpawnBlockStates, new ArrayList<>(), false);
        this.onRemoveBlockStates = processStringList("onRemoveBlockStates", onRemoveBlockStates, new ArrayList<>(), false);

        cullReinforcements = processBoolean("cullReinforcements", cullReinforcements, true, false);
        damageModifiers = processDamageModifiers("damageModifiers", damageModifiers);
    }

    private HashMap<Material, Double> processDamageModifiers(String path, HashMap<Material, Double> pluginDefaults) {
        HashMap<Material, Double> hashMap = new HashMap<>();
        if (!fileConfiguration.contains(path))
            return pluginDefaults;

        for (String rawDamageModifier : fileConfiguration.getStringList(path)) {
            if (rawDamageModifier.contains(",")) {
                String[] parsedStrings = rawDamageModifier.split(",");
                Material material = null;
                Double multiplier = null;
                for (String parsedDamageModifier : parsedStrings) {
                    if (parsedDamageModifier.contains("material:"))
                        try {
                            material = Material.getMaterial(parsedDamageModifier.replace("material:", ""));
                        } catch (Exception ex) {
                            new WarningMessage("Boss " + getFilename() + " has invalid entry " + parsedDamageModifier + " !");
                        }
                    else if (parsedDamageModifier.contains("multiplier:"))
                        try {
                            multiplier = Double.parseDouble(parsedDamageModifier.replace("multiplier:", ""));
                        } catch (Exception ex) {
                            new WarningMessage("Boss " + getFilename() + " has invalid entry " + parsedDamageModifier + " !");
                        }
                    else
                        new WarningMessage("Entry " + parsedDamageModifier + " is invalid for boss file " + getFilename() + " !");
                }

                if (material != null && multiplier != null)
                    hashMap.put(material, multiplier);

            } else {
                String[] parsedStrings = rawDamageModifier.split(":");
                Material material = null;
                Double multiplier = null;
                for (String parsedDamageModifier : parsedStrings) {
                    if (parsedDamageModifier.contains("material="))
                        try {
                            material = Material.getMaterial(parsedDamageModifier.replace("material=", ""));
                        } catch (Exception ex) {
                            new WarningMessage("Boss " + getFilename() + " has invalid entry " + parsedDamageModifier + " !");
                        }
                    else if (parsedDamageModifier.contains("multiplier="))
                        try {
                            multiplier = Double.parseDouble(parsedDamageModifier.replace("multiplier=", ""));
                        } catch (Exception ex) {
                            new WarningMessage("Boss " + getFilename() + " has invalid entry " + parsedDamageModifier + " !");
                        }
                    else
                        new WarningMessage("Entry " + parsedDamageModifier + " is invalid for boss file " + getFilename() + " !");
                }

                if (material != null && multiplier != null)
                    hashMap.put(material, multiplier);
            }
        }
        if (!hashMap.isEmpty())
            fileConfiguration.addDefault(path, deserializeDamageModifiers(hashMap));
        return hashMap;
    }

    private List<String> deserializeDamageModifiers(HashMap<Material, Double> damageModifiers) {
        List<String> deserializedDamageModifiers = new ArrayList<>();
        damageModifiers.entrySet().forEach((entry) -> {
            deserializedDamageModifiers.add("material=" + entry.getKey().toString() + ":" + "multiplier=" + entry.getValue());
        });
        return deserializedDamageModifiers;
    }

    protected List<String> majorBossDeathString(String slainLine) {
        return Arrays.asList(
                "&e&l---------------------------------------------",
                "&4" + slainLine,
                "&c&l    1st Damager: $damager1name &cwith $damager1damage damage!",
                "&6&l    2nd Damager: $damager2name &6with $damager2damage damage!",
                "&e&l    3rd Damager: $damager3name &ewith $damager3damage damage!",
                "&aSlayers: $players",
                "&e&l---------------------------------------------");
    }

    public class UniqueLoot {
        public double chance;
        public CustomItem customItem;

        public UniqueLoot(double chance, CustomItem customItem) {
            this.chance = chance;
            this.customItem = customItem;
        }
    }

}
