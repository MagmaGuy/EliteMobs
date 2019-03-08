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
    private Location spawnLocation;
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
        setSpawnLocation(configuration.getString(key + NPCConfig.LOCATION));
        setGreetings(configuration.getStringList(key + NPCConfig.GREETINGS));
        setDialog(configuration.getStringList(key + NPCConfig.DIALOG));
        setFarewell(configuration.getStringList(key + NPCConfig.FAREWELL));
        setCanMove(configuration.getBoolean(key + NPCConfig.CAN_MOVE));
        setCanTalk(configuration.getBoolean(key + NPCConfig.CAN_TALK));
        setActivationRadius(configuration.getDouble(key + NPCConfig.ACTIVATION_RADIUS));
        setDisappearsAtNight(configuration.getBoolean(key + NPCConfig.DISAPPEARS_AT_NIGHT));

    }

    private String getName() {
        return this.name;
    }

    private void setName(String name) {
        this.name = name;
        this.villager.setCustomName(name);
        this.villager.setCustomNameVisible(true);
    }

    private Villager.Career getCareer() {
        return this.career;
    }

    private void setCareer(String career) {
        this.villager.setCareer(Villager.Career.valueOf(career));
    }

    private Location getSpawnLocation() {
        return this.spawnLocation;
    }

    private void setSpawnLocation(String spawnLocation) {
        int counter = 0;
        World world = null;
        double x = 0;
        double y = 0;
        double z = 0;
        float yaw = 0;
        float pitch = 0;
        for (String substring : spawnLocation.split(",")) {
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

        this.spawnLocation = new Location(world, x, y, z, yaw, pitch);
    }

    private List<String> getGreetings() {
        return this.greetings;
    }

    private void setGreetings(List<String> greetings) {
        this.greetings = greetings;
    }

    private List<String> getDialog() {
        return this.dialog;
    }

    private void setDialog(List<String> dialog) {
        this.dialog = dialog;
    }

    private List getFarewell(List<String> farewell) {
        return this.farewell;
    }

    private void setFarewell(List<String> farewell) {
        this.farewell = farewell;
    }

    private boolean getCanMove() {
        return this.canMove;
    }

    private void setCanMove(boolean canMove) {
        this.canMove = canMove;
    }

    private boolean getCanTalk() {
        return this.canTalk;
    }

    private void setCanTalk(boolean canTalk) {
        this.canTalk = canTalk;
    }

    private double getActivationRadius() {
        return this.activationRadius;
    }

    private void setActivationRadius(double activationRadius) {
        this.activationRadius = activationRadius;
    }

    private boolean getDisappearsAtNight() {
        return this.disappearsAtNight;
    }

    private void setDisappearsAtNight(boolean disappearsAtNight) {
        this.disappearsAtNight = disappearsAtNight;
    }

}
