package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.NPCConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Villager;

import java.util.List;

public class NPCEntity {

    private Villager villager;

    private String name;
    private Villager.Career career;
    private Location location;
    private List<String> greetings;
    private List<String> dialog;
    private List<String> farewell;
    private boolean canMove;
    private boolean canTalk;
    private double activationRadius;
    private boolean disappearsAtNight;

    /**
     * Spawns NPC based off of the values in the NPCConfig config file. Runs at startup and on reload.
     *
     * @param key Name of the config key for this NPC
     */
    public NPCEntity(String key) {

        Configuration configuration = ConfigValues.npcConfig;

        setName(configuration.getString(key + NPCConfig.NAME));
        setCareer(configuration.getString(key + NPCConfig.TYPE));
        setLocation(configuration.getString(key + NPCConfig.LOCATION));
        setGreetings(configuration.getStringList(key + NPCConfig.GREETINGS));
        setDialog(configuration.getStringList(key + NPCConfig.DIALOG));
        setFarewell(configuration.getStringList(key + NPCConfig.FAREWELL));
        setCanMove(configuration.getBoolean(key + NPCConfig.CAN_MOVE));
        setCanTalk(configuration.getBoolean(key + NPCConfig.CAN_TALK));
        setActivationRadius(configuration.getDouble(key + NPCConfig.ACTIVATION_RADIUS));
        setDisappearsAtNight(configuration.getBoolean(key + NPCConfig.DISAPPEARS_AT_NIGHT));

    }

    /**
     * Sets the display name of the npc
     *
     * @param name Name the NPC will display
     */
    public void setName(String name) {
        this.name = name;
        this.villager.setCustomName(name);
        this.villager.setCustomNameVisible(true);
    }

    /**
     * Sets the career that the NPC will have. This determines its skin.
     *
     * @param career Valid parameters: ARMORER, BUTCHER, CARTOGRAPHER, CLERIC, FARMER, FISHERMAN, FLETCHER, LEATHERWORKER,
     *               LIBRARIAN, NITWIT, SHEPHERD, TOOL_SMITH, WEAPON_SMITH
     */
    public void setCareer(String career) {
        this.villager.setCareer(Villager.Career.valueOf(career));
    }

    /**
     * Sets the spawn location. Actual location may vary if movement is allowed.
     *
     * @param location The spawning location
     */
    public void setLocation(String location) {
        int counter = 0;
        World world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        for (String substring : location.split(",")) {
            switch (counter) {
                case 0:
                    /*
                    World is contained here
                     */
                    world = Bukkit.getWorld(substring);
                    break;
                case 1:
                    /*
                    X value is contained here
                     */
                    x = Double.valueOf(substring);
                    break;
                case 2:
                    /*
                    Y value is contained here
                     */
                    y = Double.valueOf(substring);
                    break;
                case 3:
                    /*
                    Z value is contained here
                     */
                    z = Double.valueOf(substring);
                    break;
                case 4:
                    yaw = Float.valueOf(substring);
                    break;
                case 5:
                    pitch = Float.valueOf(substring);
                    break;
            }

            counter++;
        }

        if (world == null) return;

        this.location = new Location(world, x, y, z, yaw, pitch);
    }

    /**
     * Sets the list of greetings that the NPC will display when players enter the activation radius
     *
     * @param greetings List of greetings
     */
    public void setGreetings(List<String> greetings) {
        this.greetings = greetings;
    }

    /**
     * Sets the dialog the NPC will display when players are within range but not interacting
     *
     * @param dialog List of dialog
     */
    public void setDialog(List<String> dialog) {
        this.dialog = dialog;
    }

    /**
     * Sets the list of farewells
     *
     * @param farewell
     */
    public void setFarewell(List<String> farewell) {
        this.farewell = farewell;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    public void setCanTalk(boolean canTalk) {
        this.canTalk = canTalk;
    }

    public void setActivationRadius(double activationRadius) {
        this.activationRadius = activationRadius;
    }

    public void setDisappearsAtNight(boolean disappearsAtNight) {
        this.disappearsAtNight = disappearsAtNight;
    }

}
