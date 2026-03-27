package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PeaceBannerConfig extends ConfigurationFile {

    @Getter
    private static boolean enabled;
    @Getter
    private static boolean craftable;
    @Getter
    private static int chunkRadius;
    @Getter
    private static boolean suppressEvents;
    @Getter
    private static List<String> recipeShape;
    @Getter
    private static Map<String, String> recipeIngredients;
    @Getter
    private static String itemName;
    @Getter
    private static List<String> itemLore;
    @Getter
    private static String placedMessage;
    @Getter
    private static String removedMessage;

    public PeaceBannerConfig() {
        super("PeaceBanner.yml");
    }

    @Override
    public void initializeValues() {
        enabled = ConfigurationEngine.setBoolean(
                List.of("Whether the Peace Banner feature is enabled."),
                fileConfiguration, "enabled", true);
        craftable = ConfigurationEngine.setBoolean(
                List.of("Whether the Peace Banner can be crafted by players."),
                fileConfiguration, "craftable", true);
        chunkRadius = ConfigurationEngine.setInt(
                List.of("The radius (in chunks) around a placed Peace Banner where elite mobs are suppressed."),
                fileConfiguration, "chunkRadius", 4);
        suppressEvents = ConfigurationEngine.setBoolean(
                List.of("Whether placed Peace Banners also suppress EliteMobs events (action events, timed events)."),
                fileConfiguration, "suppressEvents", true);

        // Recipe shape
        recipeShape = ConfigurationEngine.setList(
                List.of("The crafting recipe shape. Each string is one row of the crafting grid."),
                file, fileConfiguration, "recipeShape", List.of("BBB", "BWB", "BBB"), false);

        // Recipe ingredients — stored as a YAML section
        Map<String, Object> defaultIngredients = Map.of("B", "BONE", "W", "ANY_BANNER");
        fileConfiguration.addDefault("recipeIngredients", defaultIngredients);
        fileConfiguration.setComments("recipeIngredients", List.of(
                "Maps recipe characters to materials. Use ANY_BANNER to accept all 16 banner colors."));
        recipeIngredients = new HashMap<>();
        var section = fileConfiguration.getConfigurationSection("recipeIngredients");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                recipeIngredients.put(key, section.getString(key));
            }
        } else {
            // Fallback if section doesn't exist yet (first run, defaults not yet saved)
            recipeIngredients.putAll(Map.of("B", "BONE", "W", "ANY_BANNER"));
        }

        itemName = ConfigurationEngine.setString(
                List.of("The display name of the Peace Banner item."),
                file, fileConfiguration, "itemName", "&aPeace Banner", true);

        itemLore = ConfigurationEngine.setList(
                List.of("The lore lines shown on the Peace Banner item."),
                file, fileConfiguration, "itemLore",
                List.of(
                        "&7Place to create a peaceful zone",
                        "&7Prevents elite mob spawning",
                        "&7within a &f4 chunk &7radius"),
                true);

        placedMessage = ConfigurationEngine.setString(
                List.of("Message sent to the player when they place a Peace Banner."),
                file, fileConfiguration, "placedMessage",
                "&aPeace Banner placed! Elite mobs will not spawn within a &f$radius chunk &aradius.", true);

        removedMessage = ConfigurationEngine.setString(
                List.of("Message sent to the player when they break a Peace Banner."),
                file, fileConfiguration, "removedMessage",
                "&cPeace Banner removed. Elite mobs can now spawn in this area again.", true);
    }
}
