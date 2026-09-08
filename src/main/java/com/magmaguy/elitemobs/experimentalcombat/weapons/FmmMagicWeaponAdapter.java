package com.magmaguy.elitemobs.experimentalcombat.weapons;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.experimentalcombat.ExperimentalCombatEnemyAuthorization;
import com.magmaguy.elitemobs.experimentalcombat.damage.ExperimentalDamageScaling;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import com.magmaguy.elitemobs.skills.SkillType;
import com.magmaguy.freeminecraftmodels.api.magic.MagicAttackRequest;
import com.magmaguy.freeminecraftmodels.api.magic.MagicAttackResolver;
import com.magmaguy.freeminecraftmodels.api.magic.MagicDamageApplication;
import com.magmaguy.freeminecraftmodels.api.magic.MagicTargetRequest;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponAPI;
import com.magmaguy.freeminecraftmodels.api.magic.MagicWeaponKind;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

/** FMM-linked implementation loaded only after the optional plugin is present. */
final class FmmMagicWeaponAdapter
        implements ExperimentalMagicWeaponIntegration.Connection, MagicAttackResolver {
    private static final int REQUIRED_CAPABILITY_VERSION = 2;

    private final Plugin owner;
    private volatile boolean registered;
    private boolean calculationWarningSent;

    private FmmMagicWeaponAdapter(Plugin owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    static ExperimentalMagicWeaponIntegration.ConnectAttempt connect(Plugin owner) {
        if (MagicWeaponAPI.capabilityVersion() < REQUIRED_CAPABILITY_VERSION)
            return ExperimentalMagicWeaponIntegration.ConnectAttempt.unavailable(
                    ExperimentalMagicWeaponIntegration.ConnectStatus.INCOMPATIBLE);
        if (!MagicWeaponAPI.isServiceAvailable())
            return ExperimentalMagicWeaponIntegration.ConnectAttempt.unavailable(
                    ExperimentalMagicWeaponIntegration.ConnectStatus.WAITING_FOR_SERVICE);
        if (!MagicWeaponAPI.isOperational())
            return ExperimentalMagicWeaponIntegration.ConnectAttempt.unavailable(
                    ExperimentalMagicWeaponIntegration.ConnectStatus.CONTENT_UNAVAILABLE);

        FmmMagicWeaponAdapter adapter = new FmmMagicWeaponAdapter(owner);
        if (!MagicWeaponAPI.registerResolver(owner, adapter))
            return ExperimentalMagicWeaponIntegration.ConnectAttempt.unavailable(
                    ExperimentalMagicWeaponIntegration.ConnectStatus.REGISTRATION_REJECTED);
        adapter.registered = true;
        return ExperimentalMagicWeaponIntegration.ConnectAttempt.connected(adapter);
    }

    @Override
    public boolean isOperational() {
        return registered && owner.isEnabled() && MagicWeaponAPI.isOperational();
    }

    @Override
    public void resolve(MagicAttackRequest request, MagicDamageApplication application) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(application, "application");

        SkillType progressionSkill = progressionSkill(request.attackKind().weaponKind());
        EliteEntity elite;
        try {
            elite = EntityTracker.getEliteMobEntity(request.target());
            if (elite != null && !elite.isValid()) elite = null;
        } catch (RuntimeException failure) {
            warnCalculationFailure(failure);
            applyWithEliteMobsBypass(request, progressionSkill,
                    request.balance().standaloneDamage(), application);
            return;
        }
        if (elite == null) {
            application.apply(request.balance().standaloneDamage());
            return;
        }

        double damage;
        try {
            damage = scaledDamage(request, elite, progressionSkill);
        } catch (RuntimeException failure) {
            warnCalculationFailure(failure);
            damage = request.balance().standaloneDamage();
        }
        applyWithEliteMobsBypass(request, progressionSkill, damage, application);
    }

    @Override
    public boolean isTargetEligible(MagicTargetRequest request) {
        Objects.requireNonNull(request, "request");
        // Weapon impacts are independent of the player's class controls and world baseline.
        return ExperimentalCombatEnemyAuthorization.canTargetWithMagicWeapon(
                request.attacker(), request.target());
    }

    @Override
    public int targetPriority(MagicTargetRequest request) {
        Objects.requireNonNull(request, "request");
        try {
            EliteEntity elite = EntityTracker.getEliteMobEntity(request.target());
            if (elite != null && elite.isValid()) return PRIORITY_PREMIUM;
        } catch (RuntimeException ignored) {
            // Fall through to the plugin-neutral classification.
        }
        return MagicAttackResolver.defaultTargetPriority(request.target());
    }

    private double scaledDamage(
            MagicAttackRequest request,
            EliteEntity elite,
            SkillType progressionSkill) {
        int skillLevel = 1;
        if (!SkillsConfig.isWorldExcludedFromSkills(request.attacker())
                && PlayerData.isDataLoaded(request.attacker().getUniqueId())) {
            skillLevel = Math.max(1, PlayerData.getSkillLevel(
                    request.attacker().getUniqueId(), progressionSkill));
        }

        ItemStack weapon = request.weapon();
        double itemLevel = EliteItemManager.getItemLevel(weapon);
        if (!Double.isFinite(itemLevel) || itemLevel < 0D) itemLevel = 0D;
        Material carrierMaterial = weapon.getType();
        double basePower = request.balance().basePower() * request.balance().impactScale();
        return ExperimentalDamageScaling.magicWeapon(
                elite,
                skillLevel,
                itemLevel,
                basePower,
                carrierMaterial);
    }

    private static void applyWithEliteMobsBypass(
            MagicAttackRequest request,
            SkillType progressionSkill,
            double damage,
            MagicDamageApplication application) {
        CombatDamageContext.runPlayerToEliteBypass(
                new CombatDamageContext.PlayerDamageSource(request.attackId(), progressionSkill),
                () -> application.apply(damage));
    }

    private void warnCalculationFailure(RuntimeException failure) {
        if (calculationWarningSent) return;
        calculationWarningSent = true;
        Logger.warn("EliteMobs could not scale an FMM magic impact. Conservative FMM "
                + "damage was used: " + failure.getMessage());
    }

    static SkillType progressionSkill(MagicWeaponKind weaponKind) {
        return switch (Objects.requireNonNull(weaponKind, "weaponKind")) {
            case STAFF -> SkillType.STAVES;
            case WAND -> SkillType.WANDS;
        };
    }

    @Override
    public void close() {
        if (!registered) return;
        registered = false;
        MagicWeaponAPI.unregisterResolver(owner);
    }
}
