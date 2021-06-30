package com.magmaguy.elitemobs.events;

import com.magmaguy.elitemobs.EliteMobs;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.announcements.AnnouncementPriority;
import com.magmaguy.elitemobs.config.customevents.CustomEventsConfigFields;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardCompatibility;
import com.magmaguy.elitemobs.thirdparty.worldguard.WorldGuardFlagChecker;
import com.magmaguy.elitemobs.utils.CommandRunner;
import com.magmaguy.elitemobs.utils.WarningMessage;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public abstract class CustomEvent {

    public static boolean isLocationValid(Location location) {
        return !(EliteMobs.worldGuardIsEnabled && !WorldGuardFlagChecker.checkFlag(location, WorldGuardCompatibility.getEliteMobsEventsFlag()));
    }

    public enum EventType {
        BREAK_BLOCK,
        FISH,
        TILL_SOIL
    }

    //Common fields
    public EventType eventType;
    public ArrayList<EliteMobEntity> eventEliteMobs = new ArrayList<>();
    public ArrayList<CustomBossEntity> primaryEliteMobs = new ArrayList<>();
    public BukkitTask eventWatchdog;
    public int announcementPriority;
    public CustomEventsConfigFields customEventsConfigFields;
    public String startMessage, endMessage;
    public List<String> startEventCommands, endEventCommands;

    /**
     * Instantiates a Custom Event, does not necessarily start one but starts scanning for whether one should happen
     */
    public CustomEvent(CustomEventsConfigFields customEventsConfigFields) {
        this.customEventsConfigFields = customEventsConfigFields;
        this.eventType = customEventsConfigFields.getEventType();
        this.startMessage = customEventsConfigFields.getStartMessage();
        this.endMessage = customEventsConfigFields.getEndMessage();
        this.startEventCommands = customEventsConfigFields.getEventStartCommands();
        this.endEventCommands = customEventsConfigFields.getEventEndCommands();
        this.announcementPriority = customEventsConfigFields.getAnnouncementPriority();
        this.primaryCustomBossFilenames = customEventsConfigFields.getBossFilenames();
    }

    public List<String> getPrimaryCustomBossFilenames() {
        return primaryCustomBossFilenames;
    }

    public void setPrimaryCustomBossFilenames(List<String> primaryCustomBossFilenames) {
        this.primaryCustomBossFilenames = primaryCustomBossFilenames;
    }

    public List<String> primaryCustomBossFilenames;


    public Location getEventStartLocation() {
        return eventStartLocation;
    }

    public void setEventStartLocation(Location eventStartLocation) {
        this.eventStartLocation = eventStartLocation;
    }

    public Location eventStartLocation;

    //START CHECKS - currently in each class

    //ACTIVE

    /**
     * Starts a Custom Event watchdog, which will scan whether the event is still ongoing or if it has ended every second
     */
    public void start() {
        startModifiers();
        boolean elitesNotNull = true;
        for (String filename : primaryCustomBossFilenames) {
            CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss(filename, getEventStartLocation());
            if (customBossEntity == null)
                elitesNotNull = false;
            primaryEliteMobs.add(customBossEntity);
            eventEliteMobs.add(customBossEntity);
        }
        if (!elitesNotNull) {
            new WarningMessage("Event (event name) has failed to start because the bosses failed to spawn correctly!" +
                    "This could be due to an issue with the configuration of the bosses in the event, with the configuration" +
                    " of the event or due to a protection in the target location!");
            new WarningMessage("Target location: " + getEventStartLocation().toString());
        }
        if (this.startMessage != null)
            AnnouncementPriority.announce(this.startMessage, eventStartLocation.getWorld(), this.announcementPriority);
        if (this.startEventCommands != null)
            CommandRunner.runCommandFromList(this.startEventCommands, new ArrayList<>());
        eventWatchdog = new BukkitRunnable() {
            @Override
            public void run() {
                eventWatchdog();
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20, 20);
    }

    public abstract void startModifiers();

    /**
     * Checks whether the conditions for the event to end have been met
     */
    public abstract void eventWatchdog();


    //END

    /**
     * Starts the end of the event, deletes all EliteMobEntities spawned by the event and queues further event completion requirements
     */
    public void end() {
        eventEliteMobs.stream().forEach(eliteMobEntity -> {
            if (eliteMobEntity != null)
                eliteMobEntity.remove(true);
        });
        if (this.endMessage != null)
            AnnouncementPriority.announce(this.endMessage, eventStartLocation.getWorld(), this.announcementPriority);
        if (this.endEventCommands != null)
            CommandRunner.runCommandFromList(this.endEventCommands, new ArrayList<>());
        endModifiers();
    }

    /**
     * Gets called by the end() method, completes any extension-specific behavior for the end of the event, such as
     * implementing cooldowns or doing announcements.
     */
    public abstract void endModifiers();

    //End broadcasts / rewards
}
