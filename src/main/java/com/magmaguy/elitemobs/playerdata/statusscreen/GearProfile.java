package com.magmaguy.elitemobs.playerdata.statusscreen;

import com.magmaguy.elitemobs.combatsystem.ArmorDefenseCalculator;
import com.magmaguy.elitemobs.combatsystem.WeaponOffenseCalculator;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.config.menus.premade.PlayerStatusMenuConfig;
import com.magmaguy.elitemobs.config.menus.premade.SkillBonusMenuConfig;
import com.magmaguy.elitemobs.playerdata.ElitePlayerInventory;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.CombatLevelCalculator;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.SkillXPCalculator;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonusRegistry;
import com.magmaguy.magmacore.util.AttributeManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;

/** Produces the stable, curve-aware values shown by every Gear menu surface. */
final class GearProfile {
    private GearProfile() {
    }

    static String resolve(String template, Player player) {
        return resolve(template, capture(player));
    }

    static String resolve(String template, Snapshot snapshot) {
        if (template == null) return null;
        return template
                .replace("$combatLevel", Integer.toString(snapshot.combatLevel))
                .replace("$referenceLevel", Integer.toString(snapshot.referenceLevel))
                .replace("$weaponLevel", format(snapshot.weaponLevel))
                .replace("$weaponSkillLevel", Integer.toString(snapshot.weaponSkillLevel))
                .replace("$weaponSkill", snapshot.weaponSkill)
                .replace("$armorSkillLevel", Integer.toString(snapshot.armorSkillLevel))
                .replace("$armorLevel", format(snapshot.armorLevel))
                .replace("$weaponFactor", formatMultiplier(snapshot.weaponFactor))
                .replace("$critChance", format(snapshot.critChancePercent))
                .replace("$enchantmentBonus", format(snapshot.enchantmentBonusPercent))
                .replace("$threatMultiplier", formatMultiplier(snapshot.threatMultiplier))
                .replace("$health", format(snapshot.health))
                .replace("$maxHealth", format(snapshot.maxHealth))
                .replace("$weaponPerks", snapshot.weaponPerks)
                .replace("$armorPerks", snapshot.armorPerks)
                .replace("$offenseMatch", Integer.toString((int) Math.round(snapshot.weaponFactor * 100D)))
                .replace("$defenseMatch", Integer.toString(snapshot.defenseMatchPercent))
                // Preserve placeholders exported by older PlayerStatusScreen.yml files.
                .replace("$damage", format(snapshot.legacyDamage))
                .replace("$defense", format(snapshot.legacyDefense))
                .replace("$armor", format(snapshot.legacyDefense))
                .replace("$threat", Integer.toString(snapshot.combatLevel));
    }

    static Snapshot capture(Player player) {
        ItemStack weapon = player.getInventory().getItemInMainHand();
        int combatLevel = CombatLevelCalculator.calculateCombatLevel(player.getUniqueId());
        int referenceLevel = Math.max(1, combatLevel);
        double weaponLevel = WeaponOffenseCalculator.getEffectiveWeaponLevel(weapon);
        SkillType weaponSkillType = SkillType.fromMaterial(weapon.getType());
        boolean skillsExcluded = SkillsConfig.isWorldExcludedFromSkills(player);
        int weaponSkillLevel = skillsExcluded || weaponSkillType == null ? 1 : Math.max(1,
                SkillXPCalculator.levelFromTotalXP(PlayerData.getSkillXP(player.getUniqueId(), weaponSkillType)));
        String weaponSkill = weaponSkillType == null
                ? PlayerStatusMenuConfig.getGearUnarmedLabel()
                : SkillBonusMenuConfig.getSkillTypeDisplayName(weaponSkillType);
        int armorSkillLevel = skillsExcluded ? 1 : Math.max(1, SkillXPCalculator.levelFromTotalXP(
                PlayerData.getSkillXP(player.getUniqueId(), SkillType.ARMOR)));
        double armorLevel = ArmorDefenseCalculator.getGearScore(player, ArmorDefenseCalculator.DamageType.MELEE);
        double weaponFactor = WeaponOffenseCalculator.getWeaponAdjustment(weaponLevel, referenceLevel);
        int defenseMatchPercent = (int) Math.round(
                ArmorDefenseCalculator.getGearReduction(armorLevel, referenceLevel) * 100D);
        ElitePlayerInventory inventory = ElitePlayerInventory.getPlayer(player);
        double critChancePercent = inventory == null ? 0D : inventory.getCritChance(true) * 100D;
        double enchantmentBonusPercent = inventory == null ? 0D : inventory.getEliteEnchantmentDamage(true) * 100D;
        double threatMultiplier = 1D + (inventory == null ? 0D : inventory.getLoudStrikesBonusMultiplier(true));
        double maxHealth = Math.max(0D, AttributeManager.getAttributeValue(player, "generic_max_health"));
        double health = Math.max(0D, Math.min(player.getHealth(), maxHealth));
        String weaponPerks = formattedPerks(player, weaponSkillType);
        String armorPerks = formattedPerks(player, SkillType.ARMOR);
        double legacyDamage = inventory == null ? weaponLevel : inventory.baseDamage();
        double legacyDefense = inventory == null ? 0D : inventory.getEliteDefense(false);
        return new Snapshot(combatLevel, referenceLevel, weaponLevel, weaponSkill, weaponSkillLevel, armorSkillLevel,
                armorLevel, weaponFactor, defenseMatchPercent, critChancePercent, enchantmentBonusPercent,
                threatMultiplier, health, maxHealth, weaponPerks, armorPerks, legacyDamage, legacyDefense);
    }

    private static String formattedPerks(Player player, SkillType skillType) {
        if (skillType == null) return PlayerStatusMenuConfig.getGearNoSelectedPerksLabel();
        List<String> bonuses = SkillBonusRegistry.getFormattedBonuses(player, skillType);
        return bonuses.isEmpty() ? PlayerStatusMenuConfig.getGearNoSelectedPerksLabel() : String.join(", ", bonuses);
    }

    private static String format(double value) {
        if (!Double.isFinite(value)) return "0";
        if (Math.abs(value - Math.rint(value)) < 0.05D) return Long.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.1f", value);
    }

    private static String formatMultiplier(double value) {
        if (!Double.isFinite(value)) return "1.00";
        return String.format(Locale.ROOT, "%.2f", value);
    }

    record Snapshot(int combatLevel, int referenceLevel, double weaponLevel, String weaponSkill,
                    int weaponSkillLevel, int armorSkillLevel, double armorLevel, double weaponFactor,
                    int defenseMatchPercent, double critChancePercent, double enchantmentBonusPercent,
                    double threatMultiplier, double health, double maxHealth, String weaponPerks,
                    String armorPerks, double legacyDamage, double legacyDefense) {
    }
}
