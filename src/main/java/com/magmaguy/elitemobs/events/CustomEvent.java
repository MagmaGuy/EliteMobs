package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.announcements.AnnouncementPriority;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.ValidWorldsConfig;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.CommandRunner;
import com.magmaguy.elitemobs.utils.InfoMessage;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class CustomEvent {

    //Start Conditions
    public StartConditions startConditions;
    //Common fields
    public EventType eventType;
    public ArrayList<CustomBossEntity> primaryEliteMobs = new ArrayList<>();
    public BukkitTask eventWatchdog;
    public int announcementPriority;
    public CustomEventsConfigFields customEventsConfigFields;
    public String startMessage, endMessage;
    public List<String> startEventCommands, endEventCommands;
    public List<String> primaryCustomBossFilenames;
    public Location eventStartLocation;
    public float eventStartTime;
    public int currentDay;

    /**
     * Instantiates a Custom Event, does not necessarily start one but starts scanning for whether one should happen
     */
    public CustomEvent(CustomEventsConfigFields customEventsConfigFields) {
        this.startConditions = new StartConditions(customEventsConfigFields);

        this.customEventsConfigFields = customEventsConfigFields;
        this.eventType = customEventsConfigFields.getEventType();
        this.startMessage = customEventsConfigFields.getStartMessage();
        this.endMessage = customEventsConfigFields.getEndMessage();
        this.startEventCommands = customEventsConfigFields.getEventStartCommands();
        this.endEventCommands = customEventsConfigFields.getEventEndCommands();
        this.announcementPriority = customEventsConfigFields.getAnnouncementPriority();
        this.primaryCustomBossFilenames = customEventsConfigFields.getBossFilenames();
    }

    public static boolean isLocationValid(Location location) {
        if (!ValidWorldsConfig.getValidWorlds().contains(Objects.requireNonNull(location.getWorld()).getName()))
            return false;
        return !(EliteMobs.worldGuardIsEnabled &&
                !WorldGuardFlagChecker.checkFlag(location, WorldGuardCompatibility.getEliteMobsEventsFlag()));
    }

    public CustomEventsConfigFields getCustomEventsConfigFields() {
        return customEventsConfigFields;
    }

    public List<String> getPrimaryCustomBossFilenames() {
        return primaryCustomBossFilenames;
    }

    public void setPrimaryCustomBossFilenames(List<String> primaryCustomBossFilenames) {
        this.primaryCustomBossFilenames = primaryCustomBossFilenames;
    }

    public Location getEventStartLocation() {
        return eventStartLocation;
    }

    public void setEventStartLocation(Location eventStartLocation) {
        this.eventStartLocation = eventStartLocation;
    }

    /**
     * Starts a Custom Event watchdog, which will scan whether the event is still ongoing or if it has ended every second
     */
    public void start() {
        startModifiers();
        if (primaryEliteMobs.isEmpty()) {
            new WarningMessage("Event " + customEventsConfigFields.getFilename() + " has failed to start because the bosses failed to spawn correctly!" +
                    "This could be due to an issue with the configuration of the bosses in the event, with the configuration" +
                    " of the event or due to a protection in the target location!");
            new WarningMessage("Target location: " + getEventStartLocation().toString());
        }
        if (this.startMessage != null)
            AnnouncementPriority.announce(this.startMessage, eventStartLocation.getWorld(), this.announcementPriority);
        if (this.startEventCommands != null)
            CommandRunner.runCommandFromList(this.startEventCommands, new ArrayList<>());
        eventStartTime = System.currentTimeMillis();
        currentDay = dayCalculator();
        eventWatchdog = new BukkitRunnable() {
            @Override
            public void run() {
                commonWatchdogBehavior();
                eventWatchdog();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);
    }

    private int dayCalculator() {
        return (int) Math.floor(eventStartLocation.getWorld().getFullTime() / 24000f);
    }

    //START CHECKS - currently in each class

    //ACTIVE

    public abstract void startModifiers();

    /**
     * Runs checks that are common to all event types
     */
    public void commonWatchdogBehavior() {
        if (customEventsConfigFields.getEventEndTime() != -1)
            if (currentDay != dayCalculator() ||
                    eventStartLocation.getWorld().getTime() > customEventsConfigFields.getEventEndTime()) {
                end();
                return;
            }
        if (customEventsConfigFields.getEventDuration() > 0)
            if (System.currentTimeMillis() - eventStartTime > customEventsConfigFields.getEventDuration() * 60 * 1000) {
                end();
                return;
            }
        if (customEventsConfigFields.isEndEventWithBossDeath()) {
            AtomicBoolean allBossesAreDead = new AtomicBoolean(true);
            primaryEliteMobs.forEach((primaryEliteMob) -> {
                if (primaryEliteMob.exists())
                    allBossesAreDead.set(false);
            });
            if (allBossesAreDead.get()) {
                end();
                return;
            }
        }
    }

    /**
     * Checks whether the conditions for the event to end have been met
     */
    public abstract void eventWatchdog();

    /**
     * Starts the end of the event, deletes all EliteMobEntities spawned by the event and queues further event completion requirements
     */
    public void end() {
        new InfoMessage("Event " + customEventsConfigFields.getFilename() + " ended!");
        if (eventWatchdog != null)
            eventWatchdog.cancel();
        primaryEliteMobs.forEach(eliteMobEntity -> {
            if (eliteMobEntity.exists()) eliteMobEntity.remove(RemovalReason.BOSS_TIMEOUT);
        });
        if (this.endMessage != null)
            AnnouncementPriority.announce(this.endMessage, eventStartLocation.getWorld(), this.announcementPriority);
        if (this.endEventCommands != null)
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> CommandRunner.runCommandFromList(this.endEventCommands, new ArrayList<>()));
        endModifiers();
    }

    //END

    /**
     * Gets called by the end() method, completes any extension-specific behavior for the end of the event, such as
     * implementing cooldowns or doing announcements.
     */
    public abstract void endModifiers();

    public enum EventType {
        //Action Events
        DEFAULT,
        BREAK_BLOCK,
        FISH,
        TILL_SOIL,
        TIMED
    }
}
