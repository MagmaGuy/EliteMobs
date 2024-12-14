package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;

public class CustomModelsConfig extends ConfigurationFile {
    public static String anvilHammer;
    public static String bagOfCoins;
    public static String boxInput;
    public static String boxOutput;
    public static String darkGrayLock;
    public static String goldenQuestionMark;
    public static String grayLock;
    public static String greenLock;
    public static String handWithCoins;
    public static String redCross;
    public static String redCrown;
    public static String redLock;
    public static String whiteAnvil;
    public static String yellowCrown;
    public static String yellowLock;
    public CustomModelsConfig() {
        super("CustomModels");
    }

    @Override
    public void initializeValues() {
        anvilHammer = ConfigurationEngine.setString(file, fileConfiguration, "anvilHammer", "elitemobs:ui/anvilhammer", false);
        redCross = ConfigurationEngine.setString(file, fileConfiguration, "cancel", "elitemobs:ui/redcross", false);
        goldenQuestionMark = ConfigurationEngine.setString(file, fileConfiguration, "goldenQuestionMark", "elitemobs:ui/goldenquestionmark", false);
        bagOfCoins = ConfigurationEngine.setString(file, fileConfiguration, "bagOfCoins", "elitemobs:ui/bagofcoins", false);
        boxInput = ConfigurationEngine.setString(file, fileConfiguration, "boxInput", "elitemobs:ui/boxinput", false);
        boxOutput = ConfigurationEngine.setString(file, fileConfiguration, "boxOutput", "elitemobs:ui/boxoutput", false);
        darkGrayLock = ConfigurationEngine.setString(file, fileConfiguration, "darkGrayLock", "elitemobs:ui/darkgraylock", false);
        grayLock = ConfigurationEngine.setString(file, fileConfiguration, "grayLock", "elitemobs:ui/graylock", false);
        greenLock = ConfigurationEngine.setString(file, fileConfiguration, "greenLock", "elitemobs:ui/greenlock", false);
        handWithCoins = ConfigurationEngine.setString(file, fileConfiguration, "handWithCoins", "elitemobs:ui/handwithcoins", false);
        redCrown = ConfigurationEngine.setString(file, fileConfiguration, "redCrown", "elitemobs:ui/redcrown", false);
        redLock = ConfigurationEngine.setString(file, fileConfiguration, "redLock", "elitemobs:ui/redlock", false);
        whiteAnvil = ConfigurationEngine.setString(file, fileConfiguration, "whiteAnvil", "elitemobs:ui/whiteanvil", false);
        yellowCrown = ConfigurationEngine.setString(file, fileConfiguration, "yellowCrown", "elitemobs:ui/yellowcrown", false);
        yellowLock = ConfigurationEngine.setString(file, fileConfiguration, "yellowLock", "elitemobs:ui/yellowlock", false);
    }
}
