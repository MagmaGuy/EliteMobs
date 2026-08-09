package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.ArmorSkillHealthBonus;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.PlayerSkillSelection;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.elitemobs.skills.bonuses.skills.hoes.GrimReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.LongReachSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.spears.PolearmMasterySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.FlurrySkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.PoiseSkill;
import com.magmaguy.elitemobs.skills.bonuses.skills.swords.SwiftStrikesSkill;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Captures, prepares, and restores every player property changed by a combat diagnostic. */
final class SkillTestPlayerState {

    private final Player player;
    private final UUID playerId;
    private final CombatSimulator simulator;
    private final Map<SkillType, Long> skillXp = new EnumMap<>(SkillType.class);
    private final Map<SkillType, List<String>> skillSelections = new EnumMap<>(SkillType.class);
    private double attackSpeed;
    private double knockbackResistance;
    private double maxHealth;
    private double maxAbsorption;
    private float walkSpeed;
    private boolean captured;

    SkillTestPlayerState(Player player, CombatSimulator simulator) {
        this.player = player;
        this.playerId = player.getUniqueId();
        this.simulator = simulator;
    }

    void captureAndPrepare() {
        if (captured) return;
        for (SkillType type : SkillType.values()) {
            skillXp.put(type, PlayerData.getSkillXP(playerId, type));
            skillSelections.put(type, new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerId, type)));
        }
        attackSpeed = baseValue(Attribute.ATTACK_SPEED);
        knockbackResistance = baseValue(Attribute.KNOCKBACK_RESISTANCE);
        maxHealth = baseValue(Attribute.MAX_HEALTH);
        maxAbsorption = baseValue(Attribute.MAX_ABSORPTION);
        walkSpeed = player.getWalkSpeed();
        simulator.savePlayerArmor();
        captured = true;

        setBaseValue(Attribute.ATTACK_SPEED, 100.0);
        setBaseValue(Attribute.KNOCKBACK_RESISTANCE, 1.0);
        setBaseValue(Attribute.MAX_ABSORPTION, 2000.0);
    }

    void restore() {
        if (!captured) return;
        SkillBonusRegistry.removeAllBonuses(player);
        for (SkillType type : SkillType.values()) {
            Long savedXp = skillXp.get(type);
            if (savedXp != null) PlayerData.setSkillXP(playerId, type, savedXp);
            for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerId, type))) {
                PlayerSkillSelection.removeActiveSkill(playerId, type, id);
            }
            for (String id : skillSelections.getOrDefault(type, List.of())) {
                PlayerSkillSelection.addActiveSkill(playerId, type, id);
            }
        }
        setBaseValue(Attribute.ATTACK_SPEED, attackSpeed);
        setBaseValue(Attribute.KNOCKBACK_RESISTANCE, knockbackResistance);
        setBaseValue(Attribute.MAX_HEALTH, maxHealth);
        setBaseValue(Attribute.MAX_ABSORPTION, maxAbsorption);
        player.setWalkSpeed(walkSpeed);
        removePassiveModifiers();
        simulator.restorePlayerArmor();
        ArmorSkillHealthBonus.applyHealthBonus(player);
        SkillBonusRegistry.applyAllBonuses(player);
        player.setHealth(Math.min(player.getHealth(), player.getMaxHealth()));
        captured = false;
    }

    private double baseValue(Attribute attribute) {
        AttributeInstance instance = player.getAttribute(attribute);
        return instance == null ? 0 : instance.getBaseValue();
    }

    private void setBaseValue(Attribute attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(value);
    }

    private void removePassiveModifiers() {
        SwiftStrikesSkill.removeSpeedBonus(player);
        PoiseSkill.removeKnockbackResistance(player);
        FlurrySkill.removeAttackSpeedModifier(player);
        GrimReachSkill.removeReachBonus(player);
        LongReachSkill.removeReachBonus(player);
        PolearmMasterySkill.removeAttackSpeedBonus(player);
    }
}
