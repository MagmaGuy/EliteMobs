# Scaled Combat System Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a "scaled combat" mode where bosses are simulated at the attacking/defending player's level, making gear matter but level irrelevant — primarily for overworld elites and event bosses.

**Architecture:** The existing damage formulas (`playerToEliteDamageFormula` and `eliteToPlayerDamageFormula`) are reused with `mobLevel` overridden to the player's skill level. Offensive damage is then rescaled from the simulated mob HP to the actual boss HP. A config toggle enables this globally for natural elites, and per-boss via `scaledCombat: true`.

**Tech Stack:** Java 21, Spigot API 1.21+, Lombok, EliteMobs config system (MagmaCore `ConfigurationEngine`)

---

### Task 1: Add `scaledCombat` field to `CustomBossesConfigFields`

**Files:**
- Modify: `src/main/java/com/magmaguy/elitemobs/config/custombosses/CustomBossesConfigFields.java:183-184` (after `normalizedCombat`)
- Modify: `src/main/java/com/magmaguy/elitemobs/config/custombosses/CustomBossesConfigFields.java:389-392` (after `normalizedCombat` processing in `initializeValues`)

**Step 1: Add the field declaration**

After line 183 (`private boolean normalizedCombat = false;`), add:

```java
@Getter
@Setter
private boolean scaledCombat = false;
```

**Step 2: Add config processing**

After the `normalizedCombat` processing block (line 392), add:

```java
this.scaledCombat = processBoolean("scaledCombat", scaledCombat, false, false);
```

**Step 3: Compile**

Run: `powershell.exe -Command ".\gradlew.bat compileJava"`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/java/com/magmaguy/elitemobs/config/custombosses/CustomBossesConfigFields.java
git commit -m "feat(scaled): add scaledCombat config field to CustomBossesConfigFields"
```

---

### Task 2: Add global scaled combat config to `MobCombatSettingsConfig`

**Files:**
- Modify: `src/main/java/com/magmaguy/elitemobs/config/MobCombatSettingsConfig.java`

**Step 1: Add field declarations**

After `normalizeRegionalBosses` (line 77), add:

```java
@Getter
private static boolean useScaledCombatForNaturalElites;
@Getter
private static double scaledDamageToEliteMultiplier;
@Getter
private static double scaledDamageToPlayerMultiplier;
```

**Step 2: Add config initialization**

After the `normalizeRegionalBosses` config line (line 224), add:

```java
useScaledCombatForNaturalElites = ConfigurationEngine.setBoolean(
        List.of("Sets if naturally spawned elite mobs use scaled combat.",
                "Scaled combat simulates the boss at the player's level, so gear still matters but level doesn't.",
                "This makes overworld elites feel equally fair for all players regardless of progression.",
                "Dungeon bosses use normalized combat and are unaffected by this setting."),
        fileConfiguration, "useScaledCombatForNaturalElites", true);
scaledDamageToEliteMultiplier = ConfigurationEngine.setDouble(
        List.of("Sets the multiplier for player damage dealt to bosses using scaled combat.",
                "2.0 = 200%, 0.5 = 50%"),
        fileConfiguration, "scaledDamageToEliteMultiplier", 1);
scaledDamageToPlayerMultiplier = ConfigurationEngine.setDouble(
        List.of("Sets the multiplier for damage dealt to players by bosses using scaled combat.",
                "2.0 = 200%, 0.5 = 50%"),
        fileConfiguration, "scaledDamageToPlayerMultiplier", 1);
```

**Step 3: Compile**

Run: `powershell.exe -Command ".\gradlew.bat compileJava"`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/java/com/magmaguy/elitemobs/config/MobCombatSettingsConfig.java
git commit -m "feat(scaled): add global scaled combat config options"
```

---

### Task 3: Add `scaledCombat` state to `CustomBossEntity` and `isScaledCombat()` to `EliteEntity`

**Files:**
- Modify: `src/main/java/com/magmaguy/elitemobs/mobconstructor/EliteEntity.java`
- Modify: `src/main/java/com/magmaguy/elitemobs/mobconstructor/custombosses/CustomBossEntity.java`

**Step 1: Add `isScaledCombat()` to `EliteEntity`**

In `EliteEntity.java`, near the `isNaturalEntity` field (line 83), add:

```java
/**
 * Returns whether this entity uses scaled combat.
 * Natural elites use scaled combat when the global config is enabled.
 * Custom bosses override this with their per-boss config.
 */
public boolean isScaledCombat() {
    return isNaturalEntity && MobCombatSettingsConfig.isUseScaledCombatForNaturalElites();
}
```

**Step 2: Add `scaledCombat` field and override to `CustomBossEntity`**

In `CustomBossEntity.java`, after the `normalizedCombat` field (line 92), add:

```java
@Getter
private boolean scaledCombat;
```

In `setCustomBossesConfigFields` method (after line 198 where `normalizedCombat = true;`), add:

```java
scaledCombat = customBossesConfigFields.isScaledCombat();
```

Override `isScaledCombat()` in `CustomBossEntity`:

```java
@Override
public boolean isScaledCombat() {
    // Explicit per-boss config takes priority
    if (scaledCombat) return true;
    // Normalized combat bosses (dungeon/regional) never use scaled combat
    if (normalizedCombat) return false;
    // Fall back to parent (natural entity check)
    return super.isScaledCombat();
}
```

**Step 3: Compile**

Run: `powershell.exe -Command ".\gradlew.bat compileJava"`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/java/com/magmaguy/elitemobs/mobconstructor/EliteEntity.java
git add src/main/java/com/magmaguy/elitemobs/mobconstructor/custombosses/CustomBossEntity.java
git commit -m "feat(scaled): add isScaledCombat() to EliteEntity and CustomBossEntity"
```

---

### Task 4: Implement scaled combat in `EliteMobDamagedByPlayerEvent` (player → boss)

**Files:**
- Modify: `src/main/java/com/magmaguy/elitemobs/api/EliteMobDamagedByPlayerEvent.java`

**Context:** The `onEliteMobAttacked` method (line 908) computes damage via `playerToEliteDamageFormula()` (line 957), then applies `damageModifier` and `combatMultiplier` (lines 964-980). For scaled combat, we need to:
1. Re-run the formula with `mobLevel = weaponSkillLevel`
2. Rescale the result from simulated mob HP to actual boss HP
3. Use the scaled combat multiplier

**Step 1: Add a scaled combat version of the formula**

Add this new method in the `EliteMobDamagedByPlayerEventFilter` inner class, after `playerToEliteDamageFormula`:

```java
/**
 * Wraps playerToEliteDamageFormula for scaled combat.
 * Simulates the boss at the player's weapon skill level, then rescales
 * to the boss's actual HP pool. This makes level irrelevant while
 * keeping gear meaningful.
 */
private static double scaledPlayerToEliteDamage(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
    // 1. Get the player's weapon skill level (the "simulated" mob level)
    int simulatedMobLevel = getPlayerWeaponSkillLevel(player);
    if (simulatedMobLevel <= 0) simulatedMobLevel = 1;

    // For ranged attacks, read skill level from projectile PDC
    if (event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE && event.getDamager() instanceof Projectile proj) {
        int storedSkillLevel = ItemTagger.getArrowSkillLevel(proj);
        if (storedSkillLevel > 0) simulatedMobLevel = storedSkillLevel;
    }

    // 2. Save the real mob level, temporarily override it
    int realMobLevel = eliteEntity.getLevel();
    eliteEntity.setLevel(simulatedMobLevel);

    // 3. Run the standard formula (now sees mobLevel = player's level)
    double formulaDamage = playerToEliteDamageFormula(player, eliteEntity, event);

    // 4. Restore real level
    eliteEntity.setLevel(realMobLevel);

    // 5. Rescale: convert from "damage to simulated mob" to "equivalent % of actual boss HP"
    double simulatedMobHP = LevelScaling.calculateMobHealth(simulatedMobLevel, 0) * eliteEntity.getHealthMultiplier();
    double actualBossMaxHP = eliteEntity.getMaxHealth();
    double damagePercentage = formulaDamage / simulatedMobHP;
    double rescaledDamage = damagePercentage * actualBossMaxHP;

    return rescaledDamage;
}
```

**Step 2: Hook into `onEliteMobAttacked`**

In the damage computation block (around line 953-961), replace the formula call for the melee/ranged/sweep branch. Change:

```java
} else if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
        || event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
    // Main combat formula: melee, sweep, or ranged
    damage = playerToEliteDamageFormula(player, eliteEntity, event);
```

To:

```java
} else if (event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        || event.getCause().equals(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK)
        || event.getCause().equals(EntityDamageEvent.DamageCause.PROJECTILE)) {
    // Main combat formula: melee, sweep, or ranged
    if (eliteEntity.isScaledCombat())
        damage = scaledPlayerToEliteDamage(player, eliteEntity, event);
    else
        damage = playerToEliteDamageFormula(player, eliteEntity, event);
```

**Step 3: Update the combat multiplier selection**

In the combat multiplier section (around line 972-977), add the scaled branch. Change:

```java
double combatMultiplier;
if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
    combatMultiplier = MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier();
else
    combatMultiplier = MobCombatSettingsConfig.getDamageToEliteMultiplier();
```

To:

```java
double combatMultiplier;
if (eliteEntity.isScaledCombat())
    combatMultiplier = MobCombatSettingsConfig.getScaledDamageToEliteMultiplier();
else if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
    combatMultiplier = MobCombatSettingsConfig.getNormalizedDamageToEliteMultiplier();
else
    combatMultiplier = MobCombatSettingsConfig.getDamageToEliteMultiplier();
```

**Step 4: Handle thorns for scaled combat**

In the thorns branch (around line 945-952), add scaled handling:

```java
} else if (event.getCause().equals(EntityDamageEvent.DamageCause.THORNS)) {
    if (eliteEntity.isScaledCombat()) {
        int simulatedLevel = getPlayerWeaponSkillLevel(player);
        if (simulatedLevel <= 0) simulatedLevel = 1;
        double baseDamage = LevelScaling.calculateBaseDamageToElite(simulatedLevel);
        double thornsDamage = baseDamage * getEliteThornsLevel(player) * WeaponOffenseCalculator.THORNS_PERCENT_PER_LEVEL;
        double simulatedMobHP = LevelScaling.calculateMobHealth(simulatedLevel, 0) * eliteEntity.getHealthMultiplier();
        double actualBossMaxHP = eliteEntity.getMaxHealth();
        damage = thornsDamage * (actualBossMaxHP / simulatedMobHP);
    } else {
        damage = calculateThornsDamage(player, eliteEntity);
    }
```

**Step 5: Ensure `setLevel` exists and is public on EliteEntity**

Check that `EliteEntity` has a public `setLevel(int)` method and a `getMaxHealth()` method. If `setLevel` isn't public or doesn't exist, we need to either make it accessible or use a different approach (pass the override level as a parameter instead of mutating state).

Note: If `setLevel` is not available or is risky to call (side effects), an alternative approach is to extract the mob level as a parameter to `playerToEliteDamageFormula` instead of reading it from `eliteEntity.getLevel()`. This is cleaner but requires modifying the formula method signature.

**Step 6: Compile**

Run: `powershell.exe -Command ".\gradlew.bat compileJava"`
Expected: BUILD SUCCESSFUL

**Step 7: Commit**

```bash
git add src/main/java/com/magmaguy/elitemobs/api/EliteMobDamagedByPlayerEvent.java
git commit -m "feat(scaled): implement scaled player→boss damage formula"
```

---

### Task 5: Implement scaled combat in `PlayerDamagedByEliteMobEvent` (boss → player)

**Files:**
- Modify: `src/main/java/com/magmaguy/elitemobs/api/PlayerDamagedByEliteMobEvent.java`

**Context:** The `eliteToPlayerDamageFormula` method (line 366) computes `mobLevel = eliteEntity.getLevel()` (line 373). For scaled combat, we override this to `armorSkillLevel`.

**Step 1: Add scaled combat branch in `eliteToPlayerDamageFormula`**

After the `mobLevel` assignment (line 373), add:

```java
// Scaled combat: simulate the boss at the player's armor skill level
if (eliteEntity.isScaledCombat()) {
    mobLevel = armorSkillLevel;
}
```

This is all that's needed for defense — the rest of the formula naturally produces matched-combat damage when `mobLevel == armorSkillLevel`.

**Step 2: Update the config multiplier selection**

In the config multiplier section (around line 417-420), add the scaled branch. Change:

```java
double configMultiplier;
if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
    configMultiplier = MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
else
    configMultiplier = MobCombatSettingsConfig.getDamageToPlayerMultiplier();
```

To:

```java
double configMultiplier;
if (eliteEntity.isScaledCombat())
    configMultiplier = MobCombatSettingsConfig.getScaledDamageToPlayerMultiplier();
else if (eliteEntity instanceof CustomBossEntity customBossEntity && customBossEntity.isNormalizedCombat())
    configMultiplier = MobCombatSettingsConfig.getNormalizedDamageToPlayerMultiplier();
else
    configMultiplier = MobCombatSettingsConfig.getDamageToPlayerMultiplier();
```

**Step 3: Compile**

Run: `powershell.exe -Command ".\gradlew.bat compileJava"`
Expected: BUILD SUCCESSFUL

**Step 4: Commit**

```bash
git add src/main/java/com/magmaguy/elitemobs/api/PlayerDamagedByEliteMobEvent.java
git commit -m "feat(scaled): implement scaled boss→player damage formula"
```

---

### Task 6: Verify `setLevel` safety and adjust approach if needed

**Files:**
- Modify: `src/main/java/com/magmaguy/elitemobs/mobconstructor/EliteEntity.java` (if needed)
- Modify: `src/main/java/com/magmaguy/elitemobs/api/EliteMobDamagedByPlayerEvent.java` (if needed)

**Context:** Task 4 temporarily mutates `eliteEntity.setLevel()` to run the formula. If `setLevel` has side effects (triggers health recalculation, name update, etc.), this approach is unsafe.

**Step 1: Check if `setLevel` has side effects**

Read the `setLevel` method in `EliteEntity.java`. If it does more than set the field (e.g., calls `setMaxHealth()`, updates name, fires events), we need the alternative approach.

**Step 2: If side effects exist, use parameter override instead**

Refactor `playerToEliteDamageFormula` to accept an optional `mobLevelOverride` parameter:

```java
private static double playerToEliteDamageFormula(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event) {
    return playerToEliteDamageFormula(player, eliteEntity, event, eliteEntity.getLevel());
}

private static double playerToEliteDamageFormula(Player player, EliteEntity eliteEntity, EntityDamageByEntityEvent event, int mobLevel) {
    // Replace: int mobLevel = eliteEntity.getLevel();
    // Use the parameter instead
    // ... rest of formula unchanged
}
```

Then `scaledPlayerToEliteDamage` calls the overload:

```java
double formulaDamage = playerToEliteDamageFormula(player, eliteEntity, event, simulatedMobLevel);
```

This avoids mutating the entity entirely.

**Step 3: Compile and verify**

Run: `powershell.exe -Command ".\gradlew.bat compileJava"`
Expected: BUILD SUCCESSFUL

**Step 4: Commit (if changes were needed)**

```bash
git add src/main/java/com/magmaguy/elitemobs/api/EliteMobDamagedByPlayerEvent.java
git commit -m "refactor(scaled): use parameter override instead of mutating mob level"
```

---

### Task 7: Final compile and integration test

**Step 1: Full build**

Run: `powershell.exe -Command ".\gradlew.bat build"`
Expected: BUILD SUCCESSFUL (ignore pre-existing `SkillTestTypeCommand` error if present)

**Step 2: Verify no regressions**

Check that:
- Standard combat (non-scaled, non-normalized) is unchanged — the formula branches are additive, not modifying existing paths
- Normalized combat is unchanged — `isScaledCombat()` returns false for normalized bosses
- The new `scaledCombat` config field defaults to `false` — no existing boss configs are affected
- Natural elites default to scaled combat (`useScaledCombatForNaturalElites: true`)

**Step 3: Commit any final fixes**

```bash
git add -A
git commit -m "feat(scaled): complete scaled combat system implementation"
```
