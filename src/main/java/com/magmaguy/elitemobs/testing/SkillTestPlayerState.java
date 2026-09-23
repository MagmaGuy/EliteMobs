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

    private Player player;
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
    private double health, absorption;
    private long absorptionExpiresAt = Long.MAX_VALUE;
    private java.util.LinkedHashMap<String, Runnable> restoration;


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
        health = player.getHealth();
        absorption = player.getAbsorptionAmount();
        var effect = player.getPotionEffect(org.bukkit.potion.PotionEffectType.ABSORPTION);
        if (effect != null && !effect.isInfinite()) absorptionExpiresAt =
                com.magmaguy.elitemobs.utils.GameClock.getCurrentTick() + effect.getDuration();
        captured = true;
        simulator.savePlayerArmor();

        setBaseValue(Attribute.ATTACK_SPEED, 100.0);
        setBaseValue(Attribute.KNOCKBACK_RESISTANCE, 1.0);
        setBaseValue(Attribute.MAX_ABSORPTION, 2000.0);
    }

    void restore() {
        if (!captured) return;
        Player current = org.bukkit.Bukkit.getPlayer(playerId);
        if (current != null) player = current;
        if (restoration == null) prepareRestoration();
        RuntimeException failure = null;
        for (var entry : new ArrayList<>(restoration.entrySet())) {
            try {
                entry.getValue().run();
                restoration.remove(entry.getKey());
            } catch (RuntimeException error) {
                if (failure == null) failure = new IllegalStateException("Unfinished diagnostic player restoration");
                failure.addSuppressed(new IllegalStateException(entry.getKey(), error));
            }
        }
        if (failure != null) throw failure;
        captured = false;
    }

    private void prepareRestoration() {
        restoration = new java.util.LinkedHashMap<>();
        restoration.put("remove test bonuses", () -> SkillBonusRegistry.removeAllBonuses(player));
        for (SkillType type : SkillType.values()) {
            restoration.put("skill " + type, () -> {
                Long savedXp = skillXp.get(type);
                if (savedXp != null) PlayerData.setSkillXP(playerId, type, savedXp);
                for (String id : new ArrayList<>(PlayerSkillSelection.getActiveSkills(playerId, type)))
                    PlayerSkillSelection.removeActiveSkill(playerId, type, id);
                for (String id : skillSelections.getOrDefault(type, List.of()))
                    PlayerSkillSelection.addActiveSkill(playerId, type, id);
            });
        }
        restoration.put("attack speed", () -> setBaseValue(Attribute.ATTACK_SPEED, attackSpeed));
        restoration.put("knockback resistance", () -> setBaseValue(Attribute.KNOCKBACK_RESISTANCE, knockbackResistance));
        restoration.put("maximum health", () -> setBaseValue(Attribute.MAX_HEALTH, maxHealth));
        restoration.put("maximum absorption", () -> setBaseValue(Attribute.MAX_ABSORPTION, maxAbsorption));
        restoration.put("walk speed", () -> player.setWalkSpeed(walkSpeed));
        restoration.put("passive modifiers", this::removePassiveModifiers);
        restoration.put("equipment", simulator::restorePlayerArmor);
        restoration.put("original bonuses", () -> {
            if (restoration.keySet().stream().anyMatch(key -> !key.equals("original bonuses") && !key.equals("vitals")))
                throw new IllegalStateException("Original bonuses await restored skills, attributes and equipment");
            ArmorSkillHealthBonus.applyHealthBonus(player);
            SkillBonusRegistry.applyAllBonuses(player);
        });
        restoration.put("vitals", () -> {
            if (restoration.size() != 1) throw new IllegalStateException("Vitals await restored maximum attributes");
            if (player.isDead()) throw new IllegalStateException("Cannot restore vitals of a dead diagnostic player");
            player.setHealth(Math.min(health, player.getMaxHealth()));
            AttributeInstance maximum = player.getAttribute(Attribute.MAX_ABSORPTION);
            double allowed = maximum == null ? 0 : maximum.getValue();
            double remaining = com.magmaguy.elitemobs.utils.GameClock.getCurrentTick() < absorptionExpiresAt ? absorption : 0;
            player.setAbsorptionAmount(Math.min(remaining, allowed));
        });
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
