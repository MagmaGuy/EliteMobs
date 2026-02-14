package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import lombok.Getter;

public class LootMenuConfig extends MenusConfigFields {

    @Getter
    private static String greedListTitle;
    @Getter
    private static String greedListLore1;
    @Getter
    private static String greedListLore2;
    @Getter
    private static String greedListLore3;
    @Getter
    private static String needListTitle;
    @Getter
    private static String needListLore1;
    @Getter
    private static String needListLore2;
    @Getter
    private static String needListLore3;
    @Getter
    private static String noGroupLootMessage;

    public LootMenuConfig() {
        super("loot_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        greedListTitle = ConfigurationEngine.setString(file, fileConfiguration, "greedListTitle",
                "&2Greed Item List", true);
        greedListLore1 = ConfigurationEngine.setString(file, fileConfiguration, "greedListLore1",
                "Click to move to the Need item list!", true);
        greedListLore2 = ConfigurationEngine.setString(file, fileConfiguration, "greedListLore2",
                "Items in the greed list will only", true);
        greedListLore3 = ConfigurationEngine.setString(file, fileConfiguration, "greedListLore3",
                "be rolled for if no one needs them!", true);
        needListTitle = ConfigurationEngine.setString(file, fileConfiguration, "needListTitle",
                "&2Need Item List", true);
        needListLore1 = ConfigurationEngine.setString(file, fileConfiguration, "needListLore1",
                "Click to move to the Greed item list!", true);
        needListLore2 = ConfigurationEngine.setString(file, fileConfiguration, "needListLore2",
                "Items in the need list will only", true);
        needListLore3 = ConfigurationEngine.setString(file, fileConfiguration, "needListLore3",
                "be rolled for people who needed them!", true);
        noGroupLootMessage = ConfigurationEngine.setString(file, fileConfiguration, "noGroupLootMessage",
                "&4[EliteMobs] &6You don't currently have any group loot to vote on!", true);
    }
}
