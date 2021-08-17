package com.magmaguy.elitemobs.powers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.bosspowers.*;
import com.magmaguy.elitemobs.powers.defensivepowers.*;
import com.magmaguy.elitemobs.powers.majorpowers.blaze.TrackingFireball;
import com.magmaguy.elitemobs.powers.majorpowers.enderdragon.EnderDragonDiscoFireballs;
import com.magmaguy.elitemobs.powers.majorpowers.enderdragon.EnderDragonEmpoweredLightning;
import com.magmaguy.elitemobs.powers.majorpowers.enderdragon.EnderDragonShockwave;
import com.magmaguy.elitemobs.powers.majorpowers.enderdragon.EnderDragonTornado;
import com.magmaguy.elitemobs.powers.majorpowers.enderdragon.bombardments.*;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonPillar;
import com.magmaguy.elitemobs.powers.majorpowers.skeleton.SkeletonTrackingArrow;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieBloat;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieFriends;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieNecronomicon;
import com.magmaguy.elitemobs.powers.majorpowers.zombie.ZombieParents;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.*;
import com.magmaguy.elitemobs.powers.offensivepowers.*;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;

public class ElitePower {

    public static final HashSet<ElitePower> elitePowers = new HashSet(Arrays.asList(
            //Boss powers
            new FlamePyre(),
            new Flamethrower(),
            new GoldExplosion(),
            new GoldShotgun(),
            new HyperLoot(),
            new SpiritWalk(),
            new SummonTheReturned(),
            new MeteorShower(),
            new BulletHell(),
            new SummonEmbers(),
            new DeathSlice(),
            new LightningBolts(),
            new FrostCone(),
            new Thunderstorm(),
            new Firestorm(),
            new BonusCoins(),

            //major powers
            new SkeletonPillar(),
            new SkeletonTrackingArrow(),
            new ZombieBloat(),
            new ZombieFriends(),
            new ZombieNecronomicon(),
            new ZombieParents(),
            new TrackingFireball(),
            new EnderDragonEnderFireballBombardment(),
            new EnderDragonPotionBombardment(),
            new EnderDragonArrowBombardment(),
            new EnderDragonEndermiteBombardment(),
            new EnderDragonAimedFireball(),
            new EnderDragonEmpoweredLightning(),
            new EnderDragonShockwave(),
            new EnderDragonDiscoFireballs(),
            new EnderDragonFireballBombardment(),
            new EnderDragonTornado(),
            new FireworksBarrage(),

            //Defensive powers
            new Invisibility(),
            new InvulnerabilityArrow(),
            new InvulnerabilityFallDamage(),
            new InvulnerabilityFire(),
            new InvulnerabilityKnockback(),

            //Miscellaneous powers
            new BonusLoot(),
            new Corpse(),
            new Implosion(),
            new MoonWalk(),
            new MovementSpeed(),
            new Taunt(),

            //Offensive powers
            new AttackArrow(),
            new AttackBlinding(),
            new AttackConfusing(),
            new AttackFire(),
            new AttackFireball(),
            new AttackFreeze(),
            new AttackGravity(),
            new AttackLightning(),
            new AttackPoison(),
            new AttackPush(),
            new AttackVacuum(),
            new AttackWeakness(),
            new AttackWeb(),
            new AttackWither(),
            new ArrowFireworks(),
            new ArrowRain(),
            new GroundPound()
    ));
    private static final HashSet<MinorPower> defensivePowers = new HashSet<>(Arrays.asList(
            new Invisibility(),
            new InvulnerabilityArrow(),
            new InvulnerabilityFallDamage(),
            new InvulnerabilityFire(),
            new InvulnerabilityKnockback()));
    private static final HashSet<MinorPower> miscellaneousPowers = new HashSet<>(Arrays.asList(
            new BonusLoot(),
            new Corpse(),
            new Implosion(),
            new MoonWalk(),
            new MovementSpeed(),
            new Taunt(),
            new GroundPound()));
    private static final HashSet<MinorPower> offensivePowers = new HashSet<>(Arrays.asList(
            new AttackArrow(),
            new AttackBlinding(),
            new AttackConfusing(),
            new AttackFire(),
            new AttackFireball(),
            new AttackFreeze(),
            new AttackGravity(),
            new AttackLightning(),
            new AttackPoison(),
            new AttackPush(),
            new AttackVacuum(),
            new AttackWeakness(),
            new AttackWeb(),
            new AttackWither(),
            new ArrowFireworks(),
            new ArrowRain()));
    private final String fileName;
    private final String trail;
    private final String name;
    private final int powerCooldownTime;
    private final int globalCooldownTime;
    private final FileConfiguration configuration;
    private boolean cooldown = false;
    private boolean powerCooldownActive = false;
    private boolean isFiring = false;
    public ElitePower(PowersConfigFields powersConfigFields) {
        this.fileName = powersConfigFields.getFilename();
        this.name = powersConfigFields.getName();
        this.trail = powersConfigFields.getEffect();
        this.configuration = powersConfigFields.getFileConfiguration();
        this.powerCooldownTime = powersConfigFields.getPowerCooldown();
        this.globalCooldownTime = powersConfigFields.getGlobalCooldown();
    }

    private static HashSet<ElitePower> getElitePowers() {
        return elitePowers;
    }

    public static ElitePower getElitePower(String elitePowerName) {
        for (ElitePower elitePower : getElitePowers())
            if (elitePower.getFileName().equalsIgnoreCase(elitePowerName)) {
                try {
                    return elitePower.getClass().newInstance();
                } catch (Exception ex) {
                    new WarningMessage("Failed to instance power");
                }
            }
        return null;
    }

    public static HashSet<MinorPower> getDefensivePowers() {
        return defensivePowers;
    }

    public static HashSet<MinorPower> getMiscellaneousPowers() {
        return miscellaneousPowers;
    }

    public static HashSet<MinorPower> getOffensivePowers() {
        return offensivePowers;
    }

    protected static boolean eventIsValid(EliteMobDamagedByPlayerEvent event, ElitePower elitePower) {
        if (event.isCancelled()) return false;
        if (!event.getEliteMobEntity().getLivingEntity().hasAI()) return false;
        if (elitePower.getGlobalCooldownActive()) return false;
        return !event.getEliteMobEntity().isCooldown();
    }

    /**
     * This is overwritten by certain classes to apply powers to a living entity upon activation
     *
     * @param livingEntity
     */
    public void applyPowers(LivingEntity livingEntity) {
    }

    public String getFileName() {
        return fileName;
    }

    public String getTrail() {
        return this.trail;
    }

    public String getName() {
        return name;
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public boolean getGlobalCooldownActive() {
        return this.cooldown;
    }

    public boolean isInCooldown(EliteEntity eliteEntity) {
        return this.powerCooldownActive || eliteEntity.isCooldown();
    }

    public void doCooldown(EliteEntity eliteEntity) {
        this.powerCooldownActive = true;
        eliteEntity.doGlobalPowerCooldown(globalCooldownTime * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                powerCooldownActive = false;
            }
        }.runTaskLater(MetadataHandler.PLUGIN, powerCooldownTime * 20L);

    }

    private void setGlobalCooldownTime(boolean cooldown) {
        this.cooldown = cooldown;
    }

    protected void doGlobalCooldown(int ticks, EliteEntity eliteEntity) {
        setGlobalCooldownTime(true);
        eliteEntity.doCooldown();
        new BukkitRunnable() {
            @Override
            public void run() {
                setGlobalCooldownTime(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

    protected void doGlobalCooldown(int ticks) {
        setGlobalCooldownTime(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                setGlobalCooldownTime(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

    public boolean getIsFiring() {
        return this.isFiring;
    }

    public void setIsFiring(boolean isFiring) {
        this.isFiring = isFiring;
    }

    public int getPowerCooldownTime() {
        return powerCooldownTime;
    }

    public int getGlobalCooldown() {
        return globalCooldownTime;
    }

}
