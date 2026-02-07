package com.magmaguy.elitemobs.testing;

import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.DamageBreakdown;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.EliteItemLore;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.RegionalBossEntity;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.elitemobs.skills.bonuses.SkillBonus;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

/**
 * Combat Simulator - Manages test dummies and simulates attacks for skill testing.
 * <p>
 * Uses a singleton dummy pattern - one dummy at a time, killed and replaced between weapon types.
 * <p>
 * ATTACK SIMULATION:
 * <ul>
 *   <li>simulateMeleeAttack() - Uses player.attack(target), resets iframes before each hit</li>
 *   <li>simulateRangedAttack() - Spawns arrow toward target, needs tick delay for travel</li>
 *   <li>simulateTridentAttack() - Spawns trident toward target, needs tick delay for travel</li>
 *   <li>simulateIncomingDamage() - For armor skills, calls player.damage(amount, attacker)</li>
 * </ul>
 * <p>
 * CONFIG REQUIREMENTS:
 * <ul>
 *   <li>training_dummy.yml - For offensive skill tests (damageMultiplier can be 0)</li>
 *   <li>training_dummy_combat.yml - For armor skill tests (needs damageMultiplier > 0)</li>
 * </ul>
 *
 * @see SkillSystemTest - The main test orchestrator
 */
public class CombatSimulator {

    @Getter
    private final Player player;

    // All active dummies by skill ID
    private final Map<String, CustomBossEntity> dummies = new HashMap<>();

    // Track dummy positions for visual progress
    private final Map<String, Location> dummyLocations = new HashMap<>();

    // Track the current singleton dummy entity directly for guaranteed cleanup
    private LivingEntity currentDummyEntity = null;

    /**
     * Test dummy config file name.
     * Uses the level 50 training dummy as a balanced default for testing.
     */
    private static final String DUMMY_CONFIG = "training_dummy_lv50.yml";

    /**
     * Alternative config for armor skill testing (with damageMultiplier > 0).
     * Uses level 50 dummy - note: training dummies have damageMultiplier: 0.0
     * so armor skill testing may need a different approach.
     */
    private static final String COMBAT_DUMMY_CONFIG = "training_dummy_lv50.yml";

    // Distance in front of player to spawn dummy
    private static final double SPAWN_DISTANCE = 5.0;

    public CombatSimulator(Player player) {
        this.player = player;
    }

    /**
     * Spawns a single dummy for the current skill test.
     * Removes any existing dummy first (singleton pattern).
     */
    public boolean spawnSingleDummy(SkillBonus skill) {
        // Kill the current singleton entity directly first (guaranteed cleanup)
        killCurrentDummy();

        // Also clear all tracked dummies (belt and suspenders)
        removeAllDummies();
        resetProjectileTracking();

        // Spawn 5 blocks in front of the player, at ground level
        org.bukkit.util.Vector direction = player.getLocation().getDirection();
        direction.setY(0).normalize(); // Flatten to horizontal
        Location spawnLoc = player.getLocation().add(direction.multiply(5));

        // Find the highest block at that XZ position to avoid spawning in ground
        int highestY = spawnLoc.getWorld().getHighestBlockYAt(spawnLoc);
        spawnLoc.setY(highestY + 1);

        spawnLoc.setDirection(player.getLocation().toVector().subtract(spawnLoc.toVector()).normalize());

        return spawnDummyForSkill(skill, spawnLoc);
    }

    /**
     * Kills the current singleton dummy entity directly.
     */
    private void killCurrentDummy() {
        if (currentDummyEntity != null) {
            if (currentDummyEntity.isValid() && !currentDummyEntity.isDead()) {
                currentDummyEntity.setHealth(0);
                currentDummyEntity.remove();
            }
            currentDummyEntity = null;
        }
    }

    /**
     * Spawns a dummy for a specific skill at a location.
     * Uses COMBAT_DUMMY_CONFIG for armor skills (needs damageMultiplier > 0),
     * and DUMMY_CONFIG for offensive skills.
     */
    private boolean spawnDummyForSkill(SkillBonus skill, Location location) {
        // Armor skills need a dummy that can deal damage (damageMultiplier > 0)
        // Offensive skills can use the passive dummy
        String configToUse = (skill.getSkillType() == SkillType.ARMOR) ? COMBAT_DUMMY_CONFIG : DUMMY_CONFIG;

        CustomBossesConfigFields config = CustomBossesConfig.getCustomBoss(configToUse);
        if (config == null) {
            // Fallback to regular dummy if combat dummy doesn't exist
            config = CustomBossesConfig.getCustomBoss(DUMMY_CONFIG);
            configToUse = DUMMY_CONFIG;
        }
        if (config == null) return false;

        CustomBossEntity dummy = RegionalBossEntity.createTemporaryRegionalBossEntity(configToUse, location);
        if (dummy == null) return false;

        dummy.spawn(true);

        if (dummy.getLivingEntity() != null) {
            LivingEntity entity = dummy.getLivingEntity();

            // Disable invulnerability frames so dummy can take damage every tick
            entity.setMaximumNoDamageTicks(0);
            entity.setNoDamageTicks(0);

            // Set custom name showing the skill being tested
            String skillName = skill.getBonusName();
            String typeName = skill.getSkillType().getDisplayName();
            String displayName = "§e" + typeName + " §7- §f" + skillName + " §8[Pending]";

            entity.setCustomName(displayName);
            entity.setCustomNameVisible(true);

            // Make it glow so it's visible from afar
            entity.setGlowing(true);

            // Track the singleton entity directly for guaranteed cleanup
            currentDummyEntity = entity;

            dummies.put(skill.getSkillId(), dummy);
            dummyLocations.put(skill.getSkillId(), location);
            return true;
        }
        return false;
    }

    /**
     * Updates a dummy's name to show it's currently being tested.
     * <p>
     * Note: Not used in batch testing mode, but available for single-skill testing.
     */
    public void markDummyAsTesting(String skillId, String skillName, SkillType skillType) {
        CustomBossEntity dummy = dummies.get(skillId);
        if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
            String displayName = "§6⚔ " + skillType.getDisplayName() + " §7- §e" + skillName + " §6[TESTING]";
            dummy.getLivingEntity().setCustomName(displayName);
        }
    }

    /**
     * Updates a dummy's name to show the test passed.
     * <p>
     * Note: Not used in batch testing mode, but available for single-skill testing.
     */
    public void markDummyAsPassed(String skillId, String skillName, SkillType skillType, double procRate) {
        CustomBossEntity dummy = dummies.get(skillId);
        if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
            String displayName = String.format("§a✓ %s §7- §f%s §a[%.0f%%]",
                    skillType.getDisplayName(), skillName, procRate * 100);
            dummy.getLivingEntity().setCustomName(displayName);
            dummy.getLivingEntity().setGlowing(false);
        }
    }

    /**
     * Updates a dummy's name to show the test failed.
     * <p>
     * Note: Not used in batch testing mode, but available for single-skill testing.
     */
    public void markDummyAsFailed(String skillId, String skillName, SkillType skillType, String reason) {
        CustomBossEntity dummy = dummies.get(skillId);
        if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
            String displayName = "§c✗ " + skillType.getDisplayName() + " §7- §f" + skillName + " §c[FAILED]";
            dummy.getLivingEntity().setCustomName(displayName);
            // Keep glowing red for failed tests
        }
    }

    /**
     * Gets a dummy by skill ID.
     */
    public CustomBossEntity getDummy(String skillId) {
        return dummies.get(skillId);
    }

    /**
     * Gets the living entity for a dummy.
     */
    public LivingEntity getDummyEntity(String skillId) {
        CustomBossEntity dummy = dummies.get(skillId);
        if (dummy != null && dummy.isValid()) {
            return dummy.getLivingEntity();
        }
        return null;
    }

    /**
     * Checks if a dummy exists and is alive.
     */
    public boolean hasDummy(String skillId) {
        LivingEntity entity = getDummyEntity(skillId);
        return entity != null && entity.isValid() && !entity.isDead();
    }

    /**
     * Heals a specific dummy.
     */
    public void healDummy(String skillId) {
        CustomBossEntity dummy = dummies.get(skillId);
        if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
            dummy.fullHeal();
        }
    }

    /**
     * Respawns a dummy if it died.
     */
    public boolean respawnIfDead(String skillId, SkillBonus skill) {
        CustomBossEntity dummy = dummies.get(skillId);
        if (dummy == null || !dummy.isValid() || dummy.getLivingEntity() == null || dummy.getLivingEntity().isDead()) {
            Location spawnLoc = dummyLocations.get(skillId);
            if (spawnLoc == null) {
                spawnLoc = player.getLocation().add(SPAWN_DISTANCE, 0, 0);
            }

            // Remove old dummy reference
            dummies.remove(skillId);

            // Clear projectile tracking state to avoid health comparison issues with new dummy
            if (skillId.equals(lastProjectileTarget)) {
                lastProjectileTarget = null;
                lastTargetHealthBefore = 0;
            }

            // Spawn new dummy
            return spawnDummyForSkill(skill, spawnLoc);
        }
        return true;
    }

    /**
     * Removes a specific dummy by skill ID.
     */
    public void removeDummy(String skillId) {
        CustomBossEntity dummy = dummies.remove(skillId);
        if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
            LivingEntity entity = dummy.getLivingEntity();
            entity.setHealth(0);
            entity.remove();
            if (entity == currentDummyEntity) {
                currentDummyEntity = null;
            }
        }
        dummyLocations.remove(skillId);
    }

    /**
     * Removes all dummies.
     */
    public void removeAllDummies() {
        for (CustomBossEntity dummy : dummies.values()) {
            if (dummy != null && dummy.isValid() && dummy.getLivingEntity() != null) {
                LivingEntity entity = dummy.getLivingEntity();
                entity.setHealth(0);
                entity.remove();
            }
        }
        dummies.clear();
        dummyLocations.clear();
        // Also kill singleton reference
        killCurrentDummy();
    }

    /**
     * Equips the player with a weapon appropriate for the skill type.
     */
    public void equipWeapon(SkillType skillType) {
        ItemStack weapon = switch (skillType) {
            case SWORDS -> new ItemStack(Material.NETHERITE_SWORD);
            case AXES -> new ItemStack(Material.NETHERITE_AXE);
            case BOWS -> new ItemStack(Material.BOW);
            case CROSSBOWS -> new ItemStack(Material.CROSSBOW);
            case TRIDENTS -> new ItemStack(Material.TRIDENT);
            case HOES -> new ItemStack(Material.NETHERITE_HOE);
            case MACES -> new ItemStack(Material.MACE);
            case SPEARS -> {
                try {
                    yield new ItemStack(Material.IRON_SPEAR);
                } catch (NoSuchFieldError e) {
                    yield new ItemStack(Material.TRIDENT); // Fallback for pre-1.21.11
                }
            }
            case ARMOR -> null;
        };

        if (weapon != null) {
            player.getInventory().setItemInMainHand(weapon);
        }
    }

    /**
     * Equips the player with a full armor set at the specified elite level.
     * Used for armor skill testing to ensure player has appropriate gear.
     *
     * @param level The elite level for the armor
     */
    public void equipArmorSet(int level) {
        ItemStack helmet = createEliteArmor(Material.IRON_HELMET, level);
        ItemStack chestplate = createEliteArmor(Material.IRON_CHESTPLATE, level);
        ItemStack leggings = createEliteArmor(Material.IRON_LEGGINGS, level);
        ItemStack boots = createEliteArmor(Material.IRON_BOOTS, level);

        player.getInventory().setHelmet(helmet);
        player.getInventory().setChestplate(chestplate);
        player.getInventory().setLeggings(leggings);
        player.getInventory().setBoots(boots);
    }

    /**
     * Creates an elite armor piece at the specified level.
     */
    private ItemStack createEliteArmor(Material material, int level) {
        ItemStack armor = new ItemStack(material);

        // Add durability enchantment
        ItemMeta meta = armor.getItemMeta();
        meta.addEnchant(Enchantment.UNBREAKING, 5, true);
        armor.setItemMeta(meta);

        // Set elite level
        EliteItemManager.setEliteLevel(armor, level);

        // Generate lore
        new EliteItemLore(armor, false);

        return armor;
    }

    // Saved armor for restoration
    private ItemStack savedHelmet;
    private ItemStack savedChestplate;
    private ItemStack savedLeggings;
    private ItemStack savedBoots;

    /**
     * Saves the player's current armor for later restoration.
     */
    public void savePlayerArmor() {
        savedHelmet = player.getInventory().getHelmet();
        savedChestplate = player.getInventory().getChestplate();
        savedLeggings = player.getInventory().getLeggings();
        savedBoots = player.getInventory().getBoots();
    }

    /**
     * Restores the player's saved armor.
     */
    public void restorePlayerArmor() {
        player.getInventory().setHelmet(savedHelmet);
        player.getInventory().setChestplate(savedChestplate);
        player.getInventory().setLeggings(savedLeggings);
        player.getInventory().setBoots(savedBoots);
    }

    /**
     * Performs a melee attack against a specific dummy.
     * Uses the actual player attack method for realistic combat.
     * Attack cooldown is bypassed by setting high attack speed in setupPlayerForTesting().
     */
    public double simulateMeleeAttack(String skillId) {
        LivingEntity target = getDummyEntity(skillId);
        if (target == null || !target.isValid()) return -1;

        // Reset iframes before attack
        target.setNoDamageTicks(0);

        double healthBefore = target.getHealth();
        // Use actual player attack - attack speed is set very high to bypass cooldown
        player.attack(target);
        double healthAfter = target.getHealth();
        return Math.max(0, healthBefore - healthAfter);
    }

    // Track pending projectile damage for async measurement
    private double lastProjectileDamage = 0;
    private String lastProjectileTarget = null;
    private double lastTargetHealthBefore = 0;

    /**
     * Clears projectile tracking state. Call this when starting a new skill test
     * to avoid stale data from previous tests.
     */
    public void resetProjectileTracking() {
        lastProjectileDamage = 0;
        lastProjectileTarget = null;
        lastTargetHealthBefore = 0;
    }

    /**
     * Performs a ranged attack (bow/crossbow) against a specific dummy.
     * For testing, we manually create and process the EliteMobDamagedByPlayerEvent since
     * target.damage(arrow) doesn't trigger the correct damage cause for projectiles.
     *
     * @return The damage dealt, or -1 if target invalid
     */
    public double simulateRangedAttack(String skillId) {
        LivingEntity target = getDummyEntity(skillId);
        if (target == null || !target.isValid()) return -1;

        // Reset iframes before attack
        target.setNoDamageTicks(0);

        double healthBefore = target.getHealth();

        // Get the elite entity for the target
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
        if (eliteEntity == null || !eliteEntity.isValid()) return -1;

        // Create and process the damage event manually for skill testing
        // Uses the test constructor that allows explicit ranged attack flag
        double baseDamage = 8.0;
        EliteMobDamagedByPlayerEvent event = new EliteMobDamagedByPlayerEvent(
                eliteEntity, player, baseDamage, true);
        event.applySkillBonuses();

        // Apply the damage to the target
        // Use bypass to prevent recursive skill processing
        double finalDamage = event.getDamage();
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
        try {
            target.damage(finalDamage, player);
        } finally {
            EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
        }

        double healthAfter = target.getHealth();
        return Math.max(0, healthBefore - healthAfter);
    }

    /**
     * Performs a trident throw attack against a specific dummy.
     * For testing, we manually create and process the EliteMobDamagedByPlayerEvent since
     * target.damage(trident) doesn't trigger the correct damage cause for projectiles.
     *
     * @return The damage dealt, or -1 if target invalid
     */
    public double simulateTridentAttack(String skillId) {
        LivingEntity target = getDummyEntity(skillId);
        if (target == null || !target.isValid()) return -1;

        // Reset iframes before attack
        target.setNoDamageTicks(0);

        double healthBefore = target.getHealth();

        // Get the elite entity for the target
        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
        if (eliteEntity == null || !eliteEntity.isValid()) return -1;

        // Create and process the damage event manually for skill testing
        // Uses the test constructor that allows explicit ranged attack flag
        double baseDamage = 8.0;
        EliteMobDamagedByPlayerEvent event = new EliteMobDamagedByPlayerEvent(
                eliteEntity, player, baseDamage, true);
        event.applySkillBonuses();

        // Apply the damage to the target
        // Use bypass to prevent recursive skill processing
        double finalDamage = event.getDamage();
        EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = true;
        try {
            target.damage(finalDamage, player);
        } finally {
            EliteMobDamagedByPlayerEvent.EliteMobDamagedByPlayerEventFilter.bypass = false;
        }

        double healthAfter = target.getHealth();
        return Math.max(0, healthBefore - healthAfter);
    }

    /**
     * Simulates the player taking damage from a specific dummy (for armor skill testing).
     * Uses absorption hearts as a buffer to prevent death from amplified damage.
     */
    public double simulateIncomingDamage(String skillId, double amount) {
        LivingEntity attacker = getDummyEntity(skillId);
        if (attacker == null) return 0;

        // Reset player iframes so we can take damage every tick
        player.setNoDamageTicks(0);

        // Set full health and add large absorption buffer to prevent death
        player.setHealth(player.getMaxHealth());
        player.setAbsorptionAmount(1000.0); // Large buffer

        double healthBefore = player.getHealth();
        double absorptionBefore = player.getAbsorptionAmount();
        double totalBefore = healthBefore + absorptionBefore;

        player.damage(amount, attacker);

        double healthAfter = player.getHealth();
        double absorptionAfter = player.getAbsorptionAmount();
        double totalAfter = healthAfter + absorptionAfter;

        // Calculate actual damage dealt (including absorption consumed)
        double actualDamage = totalBefore - totalAfter;

        // Restore for next hit
        player.setHealth(player.getMaxHealth());
        player.setAbsorptionAmount(0); // Clear absorption

        return Math.max(0, actualDamage);
    }

    /**
     * Gets the number of active dummies.
     */
    public int getDummyCount() {
        return dummies.size();
    }

    // ===== DAMAGE BREAKDOWN TESTING =====

    /**
     * Performs a melee attack with full damage breakdown tracking.
     * Returns the breakdown object for analysis.
     *
     * @param skillId The skill ID for the dummy
     * @return DamageBreakdown with all damage components, or null if attack failed
     */
    public DamageBreakdown simulateMeleeAttackWithBreakdown(String skillId) {
        LivingEntity target = getDummyEntity(skillId);
        if (target == null || !target.isValid()) return null;

        // Start tracking breakdown
        DamageBreakdown breakdown = DamageBreakdown.startTracking(player);

        // Reset iframes before attack
        target.setNoDamageTicks(0);

        double healthBefore = target.getHealth();
        // Use actual player attack - attack speed is set very high to bypass cooldown
        player.attack(target);
        double healthAfter = target.getHealth();
        double actualDamage = Math.max(0, healthBefore - healthAfter);

        // Stop tracking and get results
        DamageBreakdown result = DamageBreakdown.stopTracking(player);
        if (result != null) {
            // The breakdown should have been populated during the attack event
            result.compute();
        }

        return result;
    }

    /**
     * Performs multiple melee attacks and returns an averaged breakdown summary.
     *
     * @param skillId   The skill ID for the dummy
     * @param hitCount  Number of hits to simulate
     * @return Formatted string with breakdown summary
     */
    public String simulateMultipleAttacksWithBreakdown(String skillId, int hitCount) {
        LivingEntity target = getDummyEntity(skillId);
        if (target == null || !target.isValid()) return "§cNo valid target found!";

        EliteEntity eliteEntity = EntityTracker.getEliteMobEntity(target);
        if (eliteEntity == null) return "§cTarget is not an elite entity!";

        StringBuilder report = new StringBuilder();
        report.append("§6=== COMBAT SIMULATION REPORT ===\n");
        report.append(String.format("§7Target: §f%s §7(Lv %d, HP: %.0f)\n",
                target.getName(), eliteEntity.getLevel(), target.getMaxHealth()));
        report.append(String.format("§7Hits simulated: §f%d\n\n", hitCount));

        double totalDamage = 0;
        double minDamage = Double.MAX_VALUE;
        double maxDamage = 0;
        int crits = 0;
        DamageBreakdown sampleBreakdown = null;

        for (int i = 0; i < hitCount; i++) {
            // Respawn if dead
            if (!target.isValid() || target.isDead() || target.getHealth() <= 0) {
                healDummy(skillId);
                target = getDummyEntity(skillId);
                if (target == null) break;
            }

            DamageBreakdown breakdown = simulateMeleeAttackWithBreakdown(skillId);
            if (breakdown != null) {
                double dmg = breakdown.getFinalDamage();
                totalDamage += dmg;
                minDamage = Math.min(minDamage, dmg);
                maxDamage = Math.max(maxDamage, dmg);
                if (breakdown.isCriticalHit()) crits++;

                // Keep first non-crit sample for detailed breakdown
                if (sampleBreakdown == null && !breakdown.isCriticalHit()) {
                    sampleBreakdown = breakdown;
                }
            }

            // Heal for next hit
            healDummy(skillId);
        }

        double avgDamage = hitCount > 0 ? totalDamage / hitCount : 0;
        double critRate = hitCount > 0 ? (double) crits / hitCount * 100 : 0;
        double hitsToKill = avgDamage > 0 ? target.getMaxHealth() / avgDamage : 0;

        report.append("§6--- DAMAGE STATISTICS ---\n");
        report.append(String.format("§7Average Damage: §f%.1f\n", avgDamage));
        report.append(String.format("§7Min/Max: §f%.1f §7/ §f%.1f\n", minDamage, maxDamage));
        report.append(String.format("§7Crit Rate: §f%.1f%% §7(%d/%d)\n", critRate, crits, hitCount));
        report.append(String.format("§7Estimated Hits to Kill: §f%.1f\n", hitsToKill));
        report.append(String.format("§7Total DPS (1 hit/s): §f%.1f\n\n", avgDamage));

        // Include detailed breakdown from sample hit
        if (sampleBreakdown != null) {
            report.append("§6--- SAMPLE HIT BREAKDOWN ---\n");
            report.append(sampleBreakdown.toFormattedString());
        }

        return report.toString();
    }

    /**
     * Outputs a detailed damage breakdown to the player.
     *
     * @param breakdown The breakdown to display
     */
    public void sendBreakdownToPlayer(DamageBreakdown breakdown) {
        if (breakdown == null) {
            Logger.sendMessage(player, "§cNo damage breakdown available.");
            return;
        }

        // Split the breakdown into lines and send each one
        String[] lines = breakdown.toFormattedString().split("\n");
        for (String line : lines) {
            Logger.sendMessage(player, line);
        }
    }

    /**
     * Runs a quick damage test and outputs the results.
     * Spawns a temporary dummy, performs an attack, and shows the breakdown.
     *
     * @param dummyLevel The level of dummy to spawn (uses training_dummy_lv{level}.yml)
     */
    public void runQuickDamageTest(int dummyLevel) {
        String dummyConfig = "training_dummy_lv" + dummyLevel + ".yml";
        CustomBossesConfigFields config = CustomBossesConfig.getCustomBoss(dummyConfig);

        if (config == null) {
            Logger.sendMessage(player, "§cDummy config not found: " + dummyConfig);
            return;
        }

        // Spawn dummy in front of player
        org.bukkit.util.Vector direction = player.getLocation().getDirection();
        direction.setY(0).normalize();
        Location spawnLoc = player.getLocation().add(direction.multiply(5));
        int highestY = spawnLoc.getWorld().getHighestBlockYAt(spawnLoc);
        spawnLoc.setY(highestY + 1);
        spawnLoc.setDirection(player.getLocation().toVector().subtract(spawnLoc.toVector()).normalize());

        CustomBossEntity dummy = RegionalBossEntity.createTemporaryRegionalBossEntity(dummyConfig, spawnLoc);
        if (dummy == null) {
            Logger.sendMessage(player, "§cFailed to spawn dummy!");
            return;
        }

        dummy.spawn(true);

        if (dummy.getLivingEntity() == null) {
            Logger.sendMessage(player, "§cDummy entity is null!");
            return;
        }

        LivingEntity entity = dummy.getLivingEntity();
        entity.setMaximumNoDamageTicks(0);
        entity.setNoDamageTicks(0);
        entity.setCustomName("§eDamage Test Dummy §7(Lv " + dummyLevel + ")");
        entity.setCustomNameVisible(true);
        entity.setGlowing(true);

        Logger.sendMessage(player, "§aSpawned Lv" + dummyLevel + " test dummy. Attack it to see damage breakdown!");
        Logger.sendMessage(player, "§7Dummy HP: §f" + String.format("%.0f", entity.getMaxHealth()));

        // Start tracking - the breakdown will be populated when the player attacks
        DamageBreakdown.startTracking(player);
    }

    /**
     * Checks if the player has an active damage breakdown tracking session.
     */
    public boolean hasActiveBreakdownTracking() {
        return DamageBreakdown.isTracking(player);
    }

    /**
     * Stops breakdown tracking and returns the result.
     */
    public DamageBreakdown stopBreakdownTracking() {
        return DamageBreakdown.stopTracking(player);
    }
}
