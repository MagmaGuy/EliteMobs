package com.magmaguy.elitemobs.powers.meta;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.CustomConfigFields;
import com.magmaguy.elitemobs.config.powers.LuaPowerConfigFields;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.powers.lua.LuaElitePower;
import com.magmaguy.elitemobs.powers.scripts.EliteScript;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import com.magmaguy.shaded.reflections.Reflections;

import java.util.HashMap;
import java.util.HashSet;

public class ElitePower {

    @Getter
    private static final HashMap<String, CustomConfigFields> elitePowers = new HashMap<>();
    @Getter
    private static final HashSet<CustomConfigFields> bossPowers = new HashSet<>();
    @Getter
    private static final HashSet<CustomConfigFields> majorPowers = new HashSet<>();
    @Getter
    private static final HashSet<CustomConfigFields> defensivePowers = new HashSet<>();
    @Getter
    private static final HashSet<CustomConfigFields> miscellaneousPowers = new HashSet<>();
    @Getter
    private static final HashSet<CustomConfigFields> offensivePowers = new HashSet<>();
    @Getter
    private static final HashSet<CustomConfigFields> specialPowers = new HashSet<>();

    @Getter
    private final String fileName;
    @Getter
    private final CustomConfigFields powersConfigFields;
    @Getter
    private String trail = null;
    @Getter
    @Setter
    private int powerCooldownTime = 0;
    @Getter
    @Setter
    private int globalCooldownTime = 0;
    @Getter
    @Setter
    private boolean inGlobalCooldown = false;
    @Getter
    private boolean powerCooldownActive = false;
    @Getter
    @Setter
    private boolean isFiring = false;
    @Getter
    @Setter
    private EliteEntity ownerEntity = null;


    //Constructor for scripts
    public ElitePower(CustomConfigFields customConfigFields) {
        this.fileName = customConfigFields.getFilename();
        this.powersConfigFields = customConfigFields;
    }

    //Costructor for classic powers
    public ElitePower(PowersConfigFields powersConfigFields) {
        this.powersConfigFields = powersConfigFields;
        this.fileName = powersConfigFields.getFilename();
        this.trail = powersConfigFields.getEffect();
        this.powerCooldownTime = powersConfigFields.getPowerCooldown();
        this.globalCooldownTime = powersConfigFields.getGlobalCooldown();
    }

    public static void addPower(EliteEntity eliteEntity, PowersConfigFields configFields) {
        if (configFields instanceof LuaPowerConfigFields luaPowerConfigFields) {
            LuaElitePower luaElitePower = new LuaElitePower(luaPowerConfigFields);
            luaElitePower.setOwnerEntity(eliteEntity);
            eliteEntity.getElitePowers().add(luaElitePower);
            luaElitePower.applyPowers(eliteEntity.getLivingEntity());
            return;
        }

        if (configFields.getEliteScriptBlueprints().isEmpty())
            try {
                ElitePower elitePower = configFields.getElitePowerClass().newInstance();
                elitePower.setOwnerEntity(eliteEntity);
                eliteEntity.getElitePowers().add(elitePower);
                elitePower.applyPowers(eliteEntity.getLivingEntity());
            } catch (Exception ex) {
                Logger.warn("Failed to assign power for config field " + configFields.getFilename());
            }
        else {
            eliteEntity.getElitePowers().addAll(EliteScript.generateBossScripts(configFields.getEliteScriptBlueprints(), eliteEntity));
            eliteEntity.getElitePowers().forEach(power -> {
                if (power.getOwnerEntity() == null) {
                    power.setOwnerEntity(eliteEntity);
                }
            });
        }
    }

    public static void shutdown() {
        elitePowers.clear();
        bossPowers.clear();
        majorPowers.clear();
        defensivePowers.clear();
        miscellaneousPowers.clear();
        offensivePowers.clear();
        specialPowers.clear();
    }

    public static void initializePowers() {
        Reflections reflections = new Reflections("com.magmaguy.elitemobs.powers");
        reflections.getSubTypesOf(ElitePower.class).forEach(power -> {
            try {
                ElitePower thisPower = power.newInstance();
                switch (((PowersConfigFields) thisPower.getPowersConfigFields()).getPowerType()) {
                    case DEFENSIVE -> defensivePowers.add(thisPower.getPowersConfigFields());
                    case OFFENSIVE -> offensivePowers.add(thisPower.getPowersConfigFields());
                    case MAJOR_BLAZE, MAJOR_ENDERMAN, MAJOR_SKELETON, MAJOR_GHAST, MAJOR_ZOMBIE ->
                            majorPowers.add(thisPower.getPowersConfigFields());
                    case MISCELLANEOUS -> miscellaneousPowers.add(thisPower.getPowersConfigFields());
                }
                elitePowers.put(thisPower.getFileName(), thisPower.getPowersConfigFields());
            } catch (Exception ex) {
                //Not sure why stuff in the meta package is getting scanned, seems like the package scan isn't working as intended
                //todo: figure out why package scanning is getting more than what is in the packages here
                //Logger.warn("Failed to initialize power " + power.getName());
            }
        });
    }

    /**
     * Validates a player hit for a power that may opt out of the cooldown system entirely.
     * <p>
     * {@code ignoreGlobalCooldown} has exactly one caller,
     * {@link com.magmaguy.elitemobs.powers.meta.CustomSummonPower.CustomSummonPowerEvent#onHit}, and it exists so
     * that ON_HIT reinforcements roll their own configured chance on every qualifying hit instead of queueing behind
     * the boss's power rotation. The flag used to skip only {@link #isInGlobalCooldown()}, which is a per-power flag
     * that {@link CustomSummonPower} never sets - so the flag was a no-op while the boss-wide cooldown below still
     * rejected the hit.
     * <p>
     * That boss-wide flag is {@link EliteEntity#isInCooldown()}, set by {@link EliteEntity#doGlobalPowerCooldown(int)}
     * whenever <i>any</i> other power fires: every {@link EliteScript} with a {@code Cooldowns.global} entry, and every
     * Lua power calling {@code cooldowns.set_global(...)}. On a script-heavy boss those windows overlap almost
     * continuously, so reinforcements were silently suppressed for most (on a busy boss, effectively all) of the fight,
     * with no console output of any kind because the rejection happens before the summon is ever attempted.
     *
     * @param ignoreGlobalCooldown When true, no cooldown of any kind gates the hit
     */
    protected static boolean eventIsValid(EliteMobDamagedByPlayerEvent event, ElitePower elitePower, boolean ignoreGlobalCooldown) {
        if (event.isCancelled()) return false;
        if (event.getEliteMobEntity().getLivingEntity() == null) return false;
        if (!event.getEliteMobEntity().getLivingEntity().hasAI()) return false;
        if (ignoreGlobalCooldown) return true;
        if (elitePower.isInGlobalCooldown()) return false;
        return !event.getEliteMobEntity().isInCooldown();
    }

    protected static boolean eventIsValid(EliteMobDamagedByPlayerEvent event, ElitePower elitePower) {
        if (event.isCancelled()) return false;
        if (event.getEliteMobEntity().getLivingEntity() == null) return false;
        if (!event.getEliteMobEntity().getLivingEntity().hasAI()) return false;
        if (elitePower.isInGlobalCooldown()) return false;
        if (elitePower.isInCooldown(event.getEliteMobEntity())) return false;
        return !event.getEliteMobEntity().isInCooldown();
    }

    protected static boolean eventIsValid(PlayerDamagedByEliteMobEvent event, ElitePower elitePower) {
        if (event.isCancelled()) return false;
        if (event.getEliteMobEntity().getLivingEntity() == null) return false;
        if (!event.getEliteMobEntity().getLivingEntity().hasAI()) return false;
        if (elitePower.isInGlobalCooldown()) return false;
        return !event.getEliteMobEntity().isInCooldown();
    }

    /**
     * This is overwritten by certain classes to apply powers to a living entity upon activation
     *
     * @param livingEntity
     */
    public void applyPowers(LivingEntity livingEntity) {
        //This is overwritten by certain classes to apply powers to a living entity upon activation
    }

    public int getExecutionPriority() {
        return 0;
    }

    /**
     * Since the Lua migration every power is either a {@link LuaElitePower} or an {@link EliteScript}, so the Java class
     * of a power says nothing about whether it is a minor or a major power. The configured power type is the only
     * reliable classification left, which is what the power stance rings filter on.
     *
     * @return The configured {@link PowersConfigFields.PowerType}, or null when the backing config isn't a power config
     */
    public PowersConfigFields.PowerType getPowerType() {
        if (powersConfigFields instanceof PowersConfigFields fields)
            return fields.getPowerType();
        return null;
    }

    public static void registerConfiguredPower(PowersConfigFields configFields) {
        elitePowers.put(configFields.getFilename(), configFields);
        if (configFields.getPowerType() == null) {
            return;
        }
        switch (configFields.getPowerType()) {
            case DEFENSIVE -> defensivePowers.add(configFields);
            case OFFENSIVE -> offensivePowers.add(configFields);
            case MAJOR_BLAZE, MAJOR_ENDERMAN, MAJOR_SKELETON, MAJOR_GHAST, MAJOR_ZOMBIE -> majorPowers.add(configFields);
            case MISCELLANEOUS -> miscellaneousPowers.add(configFields);
            case UNIQUE -> specialPowers.add(configFields);
            default -> {
            }
        }
    }

    public boolean isInCooldown(EliteEntity eliteEntity) {
        return this.powerCooldownActive || eliteEntity.isInCooldown();
    }

    public void setInCooldown(EliteEntity eliteEntity, boolean inCooldown) {
        eliteEntity.setInCooldown(inCooldown);
        setInGlobalCooldown(inCooldown);
    }

    public void doCooldown(EliteEntity eliteEntity) {
        this.powerCooldownActive = true;
        if (globalCooldownTime < 1) return;

        eliteEntity.doGlobalPowerCooldown(globalCooldownTime * 20);

        new BukkitRunnable() {
            @Override
            public void run() {
                powerCooldownActive = false;
            }
        }.runTaskLater(MetadataHandler.PLUGIN, powerCooldownTime * 20L);

    }

    public void doCooldownTicks(EliteEntity eliteEntity) {
        this.powerCooldownActive = true;
        if (globalCooldownTime > 0)
            eliteEntity.doGlobalPowerCooldown(globalCooldownTime);
        if (powerCooldownTime > 0)
            new BukkitRunnable() {
                @Override
                public void run() {
                    powerCooldownActive = false;
                }
            }.runTaskLater(MetadataHandler.PLUGIN, powerCooldownTime);

    }

    protected void doGlobalCooldown(int ticks, EliteEntity eliteEntity) {
        setInGlobalCooldown(true);
        eliteEntity.doCooldown();
        new BukkitRunnable() {
            @Override
            public void run() {
                setInGlobalCooldown(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

    protected void doGlobalCooldown(int ticks) {
        setInGlobalCooldown(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                setInGlobalCooldown(false);
            }
        }.runTaskLater(MetadataHandler.PLUGIN, ticks);
    }

}
