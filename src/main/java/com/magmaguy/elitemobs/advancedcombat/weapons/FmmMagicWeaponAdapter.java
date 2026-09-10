package com.magmaguy.elitemobs.advancedcombat.weapons;

import com.magmaguy.elitemobs.api.utils.EliteItemManager;
import com.magmaguy.elitemobs.combatsystem.CombatDamageContext;
import com.magmaguy.elitemobs.config.SkillsConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.advancedcombat.AdvancedCombatEnemyAuthorization;
import com.magmaguy.elitemobs.advancedcombat.damage.AdvancedDamageScaling;
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
        implements AdvancedMagicWeaponIntegration.Connection, MagicAttackResolver {
    private static final int REQUIRED_CAPABILITY_VERSION = 7;

    private final Plugin owner;
    private volatile boolean registered;
    private boolean calculationWarningSent;

    private FmmMagicWeaponAdapter(Plugin owner) {
        this.owner = Objects.requireNonNull(owner, "owner");
    }

    static AdvancedMagicWeaponIntegration.ConnectAttempt connect(Plugin owner) {
        if (MagicWeaponAPI.capabilityVersion() < REQUIRED_CAPABILITY_VERSION)
            return AdvancedMagicWeaponIntegration.ConnectAttempt.unavailable(
                    AdvancedMagicWeaponIntegration.ConnectStatus.INCOMPATIBLE);
        if (!MagicWeaponAPI.isServiceAvailable())
            return AdvancedMagicWeaponIntegration.ConnectAttempt.unavailable(
                    AdvancedMagicWeaponIntegration.ConnectStatus.WAITING_FOR_SERVICE);
        if (!MagicWeaponAPI.isOperational())
            return AdvancedMagicWeaponIntegration.ConnectAttempt.unavailable(
                    AdvancedMagicWeaponIntegration.ConnectStatus.CONTENT_UNAVAILABLE);

        FmmMagicWeaponAdapter adapter = new FmmMagicWeaponAdapter(owner);
        if (!MagicWeaponAPI.registerResolver(owner, adapter))
            return AdvancedMagicWeaponIntegration.ConnectAttempt.unavailable(
                    AdvancedMagicWeaponIntegration.ConnectStatus.REGISTRATION_REJECTED);
        adapter.registered = true;
        return AdvancedMagicWeaponIntegration.ConnectAttempt.connected(adapter);
    }

    @Override
    public boolean isOperational() {
        return registered && owner.isEnabled() && MagicWeaponAPI.isOperational();
    }

    @Override
    public java.util.Map<String, Double> capture(org.bukkit.entity.Player player, ItemStack weapon,
            com.magmaguy.freeminecraftmodels.api.magic.MagicAttackKind attackKind) {
        var inventory = com.magmaguy.elitemobs.playerdata.ElitePlayerInventory.getPlayer(player);
        int skillLevel = 1;
        if (!SkillsConfig.isWorldExcludedFromSkills(player) && PlayerData.isDataLoaded(player.getUniqueId()))
            skillLevel = Math.max(1, PlayerData.getSkillLevel(player.getUniqueId(), progressionSkill(attackKind.weaponKind())));
        return java.util.Map.of("critical", com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEventFilter.captureCriticalHit(player) ? 1D : 0D,
                "loud_strikes", inventory == null ? 0D : inventory.getLoudStrikesBonusMultiplier(true),
                "skill_level", (double) skillLevel);
    }

    @Override
    public void resolve(MagicAttackRequest request, MagicDamageApplication application) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(application, "application");

        if (EliteItemManager.isOnLastDamage(request.weapon())) return;
        SkillType progressionSkill = progressionSkill(request.attackKind().weaponKind());
        EliteEntity elite;
        try {
            elite = EntityTracker.getEliteMobEntity(request.target());
            if (elite != null && !elite.isValid()) elite = null;
        } catch (RuntimeException failure) {
            warnCalculationFailure(failure);
            application.apply(0);
            return;
        }
        if (elite == null) {
            application.apply(request.balance().standaloneDamage()
                    * EliteItemManager.getEliteDamageEnchantmentMultiplier(request.weapon()));
            return;
        }

        double damage;
        try {
            damage = scaledDamage(request, elite, progressionSkill);
        } catch (RuntimeException failure) {
            warnCalculationFailure(failure);
            application.apply(0);
            return;
        }
        applyWithEliteMobsBypass(request, progressionSkill, damage, application);
    }

    @Override
    public boolean canAttack(org.bukkit.entity.Player player, ItemStack weapon,
                             com.magmaguy.freeminecraftmodels.api.magic.MagicAttackKind attackKind) {
        com.magmaguy.elitemobs.items.ItemDurability.prepareMagicWeapon(weapon);
        return !EliteItemManager.isOnLastDamage(weapon);
    }

    @Override
    public boolean isTargetEligible(MagicTargetRequest request) {
        Objects.requireNonNull(request, "request");
        // Weapon impacts are independent of the player's class controls and world baseline.
        return AdvancedCombatEnemyAuthorization.canTargetWithMagicWeapon(
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
        int skillLevel = (int) fact(request, "skill_level");

        ItemStack weapon = request.weapon();
        double itemLevel = EliteItemManager.getItemLevel(weapon);
        if (!Double.isFinite(itemLevel) || itemLevel < 0D) itemLevel = 0D;
        Material carrierMaterial = weapon.getType();
        double basePower = request.balance().basePower() * request.balance().impactScale();
        return AdvancedDamageScaling.magicWeapon(
                elite,
                skillLevel,
                itemLevel,
                basePower,
                carrierMaterial) * EliteItemManager.getEliteDamageEnchantmentMultiplier(weapon);
    }

    private static void applyWithEliteMobsBypass(
            MagicAttackRequest request,
            SkillType progressionSkill,
            double damage,
            MagicDamageApplication application) {
        CombatDamageContext.runPlayerToEliteBypass(
                new CombatDamageContext.PlayerDamageSource(request.attackId(), progressionSkill,
                        fact(request, "critical") == 1D, fact(request, "loud_strikes")),
                () -> application.apply(damage));
    }

    private static double fact(MagicAttackRequest request, String key) {
        Double value = request.resolverFacts().get(key);
        if (value == null || !Double.isFinite(value) || value < 0)
            throw new IllegalArgumentException("Missing or invalid launch fact: " + key);
        return value;
    }

    private void warnCalculationFailure(RuntimeException failure) {
        if (calculationWarningSent) return;
        calculationWarningSent = true;
        Logger.warn("EliteMobs could not scale an FMM magic impact. The impact was rejected: " + failure.getMessage());
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
