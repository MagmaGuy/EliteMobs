package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.api.DungeonInstallEvent;
import com.magmaguy.elitemobs.api.DungeonUninstallEvent;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.elitemobs.utils.EventCaller;
import com.magmaguy.elitemobs.utils.WarningMessage;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EMPackage {

    protected static final HashMap<String, EMPackage> content = new HashMap<>();
    @Getter
    private static final Map<String, EMPackage> emPackages = new HashMap<>();
    @Getter
    protected final DungeonPackagerConfigFields dungeonPackagerConfigFields;
    @Getter
    @Setter
    protected boolean isDownloaded;
    @Getter
    @Setter
    protected boolean isInstalled;
    @Setter
    protected boolean outOfDate = false;
    @Getter
    protected List<CustomBossEntity> customBossEntityList = new ArrayList<>();
    protected List<TreasureChest> treasureChestList = new ArrayList<>();
    protected List<NPCEntity> npcEntities = new ArrayList<>();

    public EMPackage(DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        this.dungeonPackagerConfigFields = dungeonPackagerConfigFields;
        emPackages.put(dungeonPackagerConfigFields.getFilename(), this);
        baseInitialization();
    }

    /**
     * Data gets stored under two formats: either the filename, i.e. the filename to a custom boss, or the name of a world
     * for world-based content
     *
     * @param data name
     * @return EM package associated to this data
     */
    public static EMPackage getContent(String data) {
        return content.get(data);
    }

    public static void shutdown() {
        content.clear();
        emPackages.clear();
    }

    public static void initialize(DungeonPackagerConfigFields dungeonPackagerConfigFields) {
        if (dungeonPackagerConfigFields.getContentType() != null) {
            switch (dungeonPackagerConfigFields.getContentType()) {
                case INSTANCED_DUNGEON:
                    new WorldInstancedDungeonPackage(dungeonPackagerConfigFields);
                    break;
                case OPEN_DUNGEON:
                    new WorldDungeonPackage(dungeonPackagerConfigFields);
                    break;
                case HUB:
                    new WorldPackage(dungeonPackagerConfigFields);
                    break;
                case SCHEMATIC_DUNGEON:
                    new SchematicDungeonPackage(dungeonPackagerConfigFields);
                    break;
            }
        } else {
            //This is maintained for legacy reasons. Moving forward this should no longer be used
            switch (dungeonPackagerConfigFields.getDungeonLocationType()) {
                case WORLD:
                    if (dungeonPackagerConfigFields.getWorldName() == null) {
                        new WarningMessage("Dungeon world name for dungeon package " + dungeonPackagerConfigFields.getFilename() + " is not set correctly! This dungeon will not work.");
                        return;
                    }
                    if (dungeonPackagerConfigFields.getWorldName().equals("em_adventurers_guild"))
                        new WorldDungeonPackage(dungeonPackagerConfigFields);
                    new WorldDungeonPackage(dungeonPackagerConfigFields);
                    break;
                case SCHEMATIC:
                    new SchematicDungeonPackage(dungeonPackagerConfigFields);
                    break;
            }
        }
    }

    /**
     * Very first initialization - checks if content is downloaded / installed, loads worlds
     */
    public void baseInitialization() {
        //Gets @Override by its children
    }

    /**
     * Initializes content - this means it associates bosses, treasure chests and NPCs to the dungeon
     */
    public void initializeContent() {
        //Gets @Override by its children
    }

    /**
     * Installation method used by world dungeons
     *
     * @return Whether the content was installed correctly
     */
    public boolean install(Player player) {
        DungeonInstallEvent event = new DungeonInstallEvent(dungeonPackagerConfigFields);
        new EventCaller(event);
        if (!event.isCancelled()) isInstalled = true;
        return !event.isCancelled();
    }

    /**
     * Installation method used by schematic packages
     *
     * @param player Player installing the content
     * @return Whether the content was installed correctly
     */
    public boolean install(Player player, boolean paste) {
        DungeonInstallEvent event = new DungeonInstallEvent(dungeonPackagerConfigFields);
        new EventCaller(event);
        if (!event.isCancelled()) isInstalled = true;
        return !event.isCancelled();
    }

    /**
     * Uninstalls content, unloading entities, worlds and more
     *
     * @return Whether the uninstallation process was successful
     */
    public boolean uninstall(Player player) {
        DungeonUninstallEvent event = new DungeonUninstallEvent(dungeonPackagerConfigFields);
        new EventCaller(event);
        if (!event.isCancelled()) isInstalled = false;
        return !event.isCancelled();
    }

}
