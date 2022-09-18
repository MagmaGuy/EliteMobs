package com.magmaguy.elitemobs.powers.meta;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.api.PlayerDamagedByEliteMobEvent;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.reflections.Reflections;

import java.util.HashSet;

public class ElitePower {

    @Getter
    private static final HashSet<ElitePower> elitePowers = new HashSet<>();
    @Getter
    private static final HashSet<ElitePower> bossPowers = new HashSet<>();
    @Getter
    private static final HashSet<ElitePower> majorPowers = new HashSet<>();
    @Getter
    private static final HashSet<ElitePower> defensivePowers = new HashSet<>();
    @Getter
    private static final HashSet<ElitePower> miscellaneousPowers = new HashSet<>();
    @Getter
    private static final HashSet<ElitePower> offensivePowers = new HashSet<>();
    @Getter
    private static final HashSet<ElitePower> specialPowers = new HashSet<>();

    @Getter
    private final String fileName;
    @Getter
    private final String trail;
    @Getter
    private final String name;
    @Getter
    @Setter
    private int powerCooldownTime;
    @Getter
    @Setter
    private int globalCooldownTime;
    @Getter
    private final PowersConfigFields powersConfigFields;
    @Getter
    @Setter
    private boolean inGlobalCooldown = false;
    @Getter
    private boolean powerCooldownActive = false;
    @Getter
    @Setter
    private boolean isFiring = false;

    //Constructor for scripts
    public ElitePower(ConfigurationSection configurationSection, String scriptName) {
        this.fileName = scriptName;
        this.trail = null;
        this.name = scriptName;
        this.powerCooldownTime = 0;//todo: update
        this.globalCooldownTime = 0;//todo: update
        this.powersConfigFields = PowersConfig.getPower("elite_script.yml"); //todo: this needs to get removed in the rewrite
    }

    //Costructor for classic powers
    public ElitePower(PowersConfigFields powersConfigFields) {
        this.powersConfigFields = powersConfigFields;
        this.fileName = powersConfigFields.getFilename();
        this.name = powersConfigFields.getName();
        this.trail = powersConfigFields.getEffect();
        this.powerCooldownTime = powersConfigFields.getPowerCooldown();
        this.globalCooldownTime = powersConfigFields.getGlobalCooldown();
    }

    public static void initializePowers() {
        //boss powers
        initializePackage("bosspowers", bossPowers);
        //defensive powers
        initializePackage("defensivepowers", defensivePowers);
        //major powers
        initializePackage("majorpowers", majorPowers);
        //miscellaneous powers
        initializePackage("miscellaneouspowers", miscellaneousPowers);
        //offensive powers
        initializePackage("offensivepowers", offensivePowers);
        //special powers
        initializePackage("specialpowers", specialPowers);
        //all powers
        elitePowers.addAll(bossPowers);
        elitePowers.addAll(defensivePowers);
        elitePowers.addAll(majorPowers);
        elitePowers.addAll(miscellaneousPowers);
        elitePowers.addAll(offensivePowers);
        elitePowers.addAll(specialPowers);
    }

    private static void initializePackage(String specificPackage, HashSet<ElitePower> elitePowers) {
        Reflections reflections = new Reflections("com.magmaguy.elitemobs.powers." + specificPackage);
        reflections.getSubTypesOf(ElitePower.class).forEach(power -> {
            try {
                elitePowers.add(power.newInstance());
            } catch (Exception ex) {
                //Not sure why stuff in the meta package is getting scanned, seems like the package scan isn't working as intended
                //todo: figure out why package scanning is getting more than what is in the packages here
                //new WarningMessage("Failed to initialize power " + specificPackage + " " + power.getName());
            }
        });
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


    protected static boolean eventIsValid(EliteMobDamagedByPlayerEvent event, ElitePower elitePower) {
        if (event.isCancelled()) return false;
        if (event.getEliteMobEntity().getLivingEntity() == null) return false;
        if (!event.getEliteMobEntity().getLivingEntity().hasAI()) return false;
        if (elitePower.isInGlobalCooldown()) return false;
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

    public boolean isInCooldown(EliteEntity eliteEntity) {
        return this.powerCooldownActive || eliteEntity.isInCooldown();
    }

    public void setInCooldown(EliteEntity eliteEntity, boolean inCooldown) {
        eliteEntity.setInCooldown(inCooldown);
        setInGlobalCooldown(inCooldown);
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
