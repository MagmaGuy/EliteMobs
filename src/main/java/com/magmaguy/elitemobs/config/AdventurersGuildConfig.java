package com.magmaguy.elitemobs.config;

import com.magmaguy.magmacore.config.ConfigurationFile;
import lombok.Getter;

import java.util.List;

public class AdventurersGuildConfig extends ConfigurationFile {
    @Getter
    private static boolean agTeleport;
    @Getter
    private static String adventurersGuildMenuName;
    @Getter
    private static boolean skillBasedGearRestriction;
    @Getter
    private static String gearRestrictionMessage;

    public AdventurersGuildConfig() {
        super("AdventurersGuild.yml");
    }

    @Override
    public void initializeValues() {
        agTeleport = ConfigurationEngine.setBoolean(
                List.of("Sets if user commands get rerouted to the adventurer's guild hub. This is highly recommended for gameplay immersion and tutorial purposes."),
                fileConfiguration, "userCommandsTeleportToAdventurersGuild", true);
        adventurersGuildMenuName = ConfigurationEngine.setString(
                List.of("Sets the in-game display name of the adventurer's guild"),
                file, fileConfiguration, "adventurersGuildMenuName", "&6&lAdventurer's Hub", true);

        skillBasedGearRestriction = ConfigurationEngine.setBoolean(
                List.of("Sets if players are restricted from equipping gear above their skill level.",
                        "Gear type determines which skill is checked (e.g., axes check AXES skill, armor checks ARMOR skill)."),
                fileConfiguration, "skillBasedGearRestriction", true);
        gearRestrictionMessage = ConfigurationEngine.setString(
                List.of("Sets the message sent to players when they try to equip gear above their skill level.",
                        "$itemLevel is the item level, $skillLevel is their current skill level, $skillType is the skill name."),
                file, fileConfiguration, "gearRestrictionMessage", "&c[EM] You need $skillType level $itemLevel to equip this! (Current: $skillLevel)", true);
    }
}
