package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.Arrays;
import java.util.List;

public class ZombieFriendsConfig extends PowersConfigFields {
    public static String friend1Name, friend2Name;
    public static List<String> friendDeathMessage, originalEntityDialog, reinforcementDialog;

    public ZombieFriendsConfig() {
        super("zombie_friends",
                true,
                "Friends",
                Material.ZOMBIE_HEAD.toString());
    }

    @Override
    public void processAdditionalFields() {

        friend1Name = ConfigurationEngine.setString(fileConfiguration, "friend1Name", "BFF #1");
        friend2Name = ConfigurationEngine.setString(fileConfiguration, "friend2Name", "BFF #2");

        friendDeathMessage = ConfigurationEngine.setList(fileConfiguration, "friendDeathMessage", Arrays.asList(
                "Noooo!",
                "Mediocre!",
                "Zacharias!",
                "He's deader than before!",
                "Vengeance!",
                "Revenge!",
                "I can't believe you've done this."));

        originalEntityDialog = ConfigurationEngine.setList(fileConfiguration, "originalEntityDialog", Arrays.asList(
                "Let's play ZombieCraft later!",
                "Feel the power of friendship!",
                "El pueblo, unido!",
                "I called my friends over!",
                "BFF power!",
                "One for all!",
                "Get him!",
                "Screw you guys, I'm going home!"));

        reinforcementDialog = ConfigurationEngine.setList(fileConfiguration, "reinforcementDialog", Arrays.asList(
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
                "All for one!"));
    }
}
