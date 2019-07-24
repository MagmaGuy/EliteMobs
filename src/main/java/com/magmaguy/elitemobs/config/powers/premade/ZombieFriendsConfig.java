package com.magmaguy.elitemobs.config.powers.premade;

import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import org.bukkit.Material;

import java.util.Arrays;

public class ZombieFriendsConfig extends PowersConfigFields {
    public ZombieFriendsConfig() {
        super("zombie_friends",
                true,
                "Friends",
                Material.ZOMBIE_HEAD.toString());

        super.getAdditionalConfigOptions().put("friend1Name", "BFF #1");
        super.getAdditionalConfigOptions().put("friend2Name", "BFF #2");

        super.getAdditionalConfigOptions().put("friendDeathMessage", Arrays.asList(
                "Noooo!",
                "Mediocre!",
                "Zacharias!",
                "He's deader than before!",
                "Vengeance!",
                "Revenge!",
                "I can't believe you've done this."));

        super.getAdditionalConfigOptions().put("originalEntityDialog", Arrays.asList(
                "Let's play ZombieCraft later!",
                "Feel the power of friendship!",
                "El pueblo, unido!",
                "I called my friends over!",
                "BFF power!",
                "One for all!",
                "Get him!",
                "Screw you guys, I'm going home!"));

        super.getAdditionalConfigOptions().put("reinforcementDialog", Arrays.asList(
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
