package com.magmaguy.elitemobs.parties;

import com.magmaguy.elitemobs.advancedcombat.CombatHealthFormatter;
import com.magmaguy.elitemobs.config.PartyConfig;
import com.magmaguy.elitemobs.instanced.MatchInstance;
import com.magmaguy.elitemobs.instanced.dungeons.DungeonInstance;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

/**
 * One party member as a viewer sees them: the party sidebar line and the raw values behind it. Reads Bukkit
 * state, so it runs on the main thread; the party sidebar and the PlaceholderAPI snapshot both use it.
 */
public final class PartyMemberDisplay {

    private PartyMemberDisplay() {
    }

    /** Raw member values. Health is null for an offline member, lives is null outside a dungeon with lives. */
    public record MemberState(String name, boolean online, boolean downed, Double health, Double maxHealth,
                              Integer lives) {
    }

    /** The dungeon whose lives and downed state the viewer's sidebar shows, or null. */
    public static DungeonInstance getDungeonInstance(Player viewer) {
        MatchInstance matchInstance = PlayerData.getMatchInstance(viewer);
        return matchInstance instanceof DungeonInstance dungeonInstance ? dungeonInstance : null;
    }

    public static MemberState state(Player member, DungeonInstance dungeonInstance) {
        boolean online = member != null && member.isOnline();
        String name = member == null ? PartyConfig.getUnknownPlayerName() : member.getName();
        boolean downed = member != null
                && dungeonInstance != null
                && dungeonInstance.isSpectator(member)
                && dungeonInstance.getRemainingLives(member) != null;
        Integer lives = member == null || dungeonInstance == null ? null : dungeonInstance.getRemainingLives(member);
        if (lives != null) lives = Math.max(0, lives);
        if (!online) return new MemberState(name, false, downed, null, null, lives);
        AttributeInstance attribute = member.getAttribute(Attribute.MAX_HEALTH);
        double maximum = Math.max(1D, attribute == null ? member.getHealth() : attribute.getValue());
        double current = downed || member.isDead() ? 0D : Math.max(0D, Math.min(maximum, member.getHealth()));
        return new MemberState(name, true, downed, current, maximum, lives);
    }

    /** The leader or member line template for this member. */
    public static String template(Party party, java.util.UUID memberId) {
        return memberId.equals(party.getLeader())
                ? PartyConfig.getSidebarLeaderLine()
                : PartyConfig.getSidebarMemberLine();
    }

    /** Renders a party sidebar line, color codes converted. */
    public static String renderLine(String template, MemberState state) {
        String health = renderHealth(state);
        if (state.downed()) health += PartyConfig.getSidebarDownedDisplay();
        String lives = state.lives() == null
                ? ""
                : PartyConfig.getSidebarLivesDisplay().replace("$lives", String.valueOf(state.lives()));
        boolean hasHealthPlaceholder = template.contains("$health");
        boolean hasLivesPlaceholder = template.contains("$lives");
        String rendered = template
                .replace("$player", state.name())
                .replace("$health", health)
                .replace("$lives", lives);
        // Preserve the new information for existing Party.yml files whose customized line
        // templates predate the placeholders.
        if (!hasHealthPlaceholder) rendered += health;
        if (!hasLivesPlaceholder) rendered += lives;
        return ChatColorConverter.convert(rendered);
    }

    private static String renderHealth(MemberState state) {
        if (state.health() == null) return " &8-/-";
        double maximum = state.maxHealth();
        double current = state.health();
        double fraction = current / maximum;
        // Green at full health, red at half health, almost black when downed.
        int from = fraction >= .5D ? 0xFF0000 : 0x080000;
        int to = fraction >= .5D ? 0x55FF55 : 0xFF0000;
        double blend = fraction >= .5D ? (fraction - .5D) * 2D : fraction * 2D;
        int red = interpolateChannel(from >> 16, to >> 16, blend);
        int green = interpolateChannel(from >> 8, to >> 8, blend);
        int blue = interpolateChannel(from, to, blend);
        String tint = net.md_5.bungee.api.ChatColor.of(new java.awt.Color(red, green, blue)).toString();
        return " " + tint + CombatHealthFormatter.format(current) + "&7/&f" + CombatHealthFormatter.format(maximum);
    }

    private static int interpolateChannel(int from, int to, double blend) {
        return (int) Math.round((from & 255) + ((to & 255) - (from & 255)) * blend);
    }
}
