package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;

import java.util.List;

public class CustomModelsConfig extends ConfigurationFile {
    public static String anvilHammer;
    public static String bagOfCoins;
    public static String boxInput;
    public static String boxOutput;
    public static String goldenQuestionMark;
    public static String handWithCoins;
    public static String redCross;
    public static String whiteAnvil;
    public static boolean useModels;
    public static boolean useAttributeScaling;

    public CustomModelsConfig() {
        super("CustomModels.yml");
    }

    @Override
    public void initializeValues() {
        useModels = ConfigurationEngine.setBoolean(List.of("Sets whether the models in this configuration file will be used.",
                "Will not apply to custom models of things that aren't here, such as bosses."),
                fileConfiguration, "useModels", true);
        useAttributeScaling = ConfigurationEngine.setBoolean(List.of(
                "Sets whether elite items receive attribute bonuses based on their level.",
                "When enabled, higher level items will have increased damage, armor, toughness, etc.",
                "At level 100, items will be roughly 100% better than vanilla netherite.",
                "This applies to procedurally generated items that receive custom skins."),
                fileConfiguration, "useAttributeScaling", true);
        anvilHammer = ConfigurationEngine.setString(file, fileConfiguration, "anvilHammer", "elitemobs:ui/anvilhammer", false);
        redCross = ConfigurationEngine.setString(file, fileConfiguration, "cancel", "elitemobs:ui/redcross", false);
        goldenQuestionMark = ConfigurationEngine.setString(file, fileConfiguration, "goldenQuestionMark", "elitemobs:ui/goldenquestionmark", false);
        bagOfCoins = ConfigurationEngine.setString(file, fileConfiguration, "bagOfCoins", "elitemobs:ui/bagofcoins", false);
        boxInput = ConfigurationEngine.setString(file, fileConfiguration, "boxInput", "elitemobs:ui/boxinput", false);
        boxOutput = ConfigurationEngine.setString(file, fileConfiguration, "boxOutput", "elitemobs:ui/boxoutput", false);
        handWithCoins = ConfigurationEngine.setString(file, fileConfiguration, "handWithCoins", "elitemobs:ui/handwithcoins", false);
        whiteAnvil = ConfigurationEngine.setString(file, fileConfiguration, "whiteAnvil", "elitemobs:ui/whiteanvil", false);
    }
}
