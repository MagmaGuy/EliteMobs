package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.NPCConfig;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

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
    private NPCInteractions.NPCInteractionType npcInteractionType;

    /**
     * Spawns NPC based off of the values in the NPCConfig config file. Runs at startup and on reload.
     *
     * @param key Name of the config key for this NPC
     */
    public NPCEntity(String key) {

        Bukkit.getLogger().warning(key);

        key += ".";

        Configuration configuration = ConfigValues.npcConfig;

        setSpawnLocation(configuration.getString(key + NPCConfig.LOCATION));

        this.villager = (Villager) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);

        setName(configuration.getString(key + NPCConfig.NAME));
//        setCareer(configuration.getString(key + NPCConfig.TYPE));
        setGreetings(configuration.getStringList(key + NPCConfig.GREETINGS));
        setDialog(configuration.getStringList(key + NPCConfig.DIALOG));
        setFarewell(configuration.getStringList(key + NPCConfig.FAREWELL));
        setCanMove(configuration.getBoolean(key + NPCConfig.CAN_MOVE));
        setCanTalk(configuration.getBoolean(key + NPCConfig.CAN_TALK));
        setActivationRadius(configuration.getDouble(key + NPCConfig.ACTIVATION_RADIUS));
        setDisappearsAtNight(configuration.getBoolean(key + NPCConfig.DISAPPEARS_AT_NIGHT));
        setNpcInteractionType(configuration.getString(key + NPCConfig.INTERACTION_TYPE));

        EntityTracker.registerNPCEntity(this);

        Bukkit.getLogger().warning("Spawned at " + villager.getLocation().toString());

    }

    public void respawnNPC() {
        this.villager.remove();

        this.villager = (Villager) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);
        villager.setCustomName(this.name);
        villager.setCustomNameVisible(true);
        villager.setCareer(this.career);
        villager.setAI(!this.canMove);

    }

    public Villager getVillager() {
        return this.villager;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        name = ChatColorConverter.convert(name);
        this.name = name;
        this.villager.setCustomName(name);
        this.villager.setCustomNameVisible(true);
    }

    public void setCareer(String career) {
        this.career = Villager.Career.valueOf(career);
        this.villager.setCareer(this.career);
    }

    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    public void setSpawnLocation(String spawnLocation) {
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

    public List<String> getGreetings() {
        return this.greetings;
    }

    public void setGreetings(List<String> greetings) {
        this.greetings = greetings;
    }

    public List<String> getDialog() {
        return this.dialog;
    }

    public void setDialog(List<String> dialog) {
        this.dialog = dialog;
    }

    public List getFarewell(List<String> farewell) {
        return this.farewell;
    }

    public void setFarewell(List<String> farewell) {
        this.farewell = farewell;
    }

    public boolean getCanMove() {
        return this.canMove;
    }

    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
        this.villager.setAI(canMove);
        Bukkit.getLogger().warning("AI: " + canMove);
        if (!canMove)
            villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 3));
    }

    public boolean getCanTalk() {
        return this.canTalk;
    }

    public void setCanTalk(boolean canTalk) {
        this.canTalk = canTalk;
    }

    public double getActivationRadius() {
        return this.activationRadius;
    }

    public void setActivationRadius(double activationRadius) {
        this.activationRadius = activationRadius;
    }

    public boolean getDisappearsAtNight() {
        return this.disappearsAtNight;
    }

    public void setDisappearsAtNight(boolean disappearsAtNight) {
        this.disappearsAtNight = disappearsAtNight;
    }

    public NPCInteractions.NPCInteractionType getInteractionType() {
        return this.npcInteractionType;
    }

    public void setNpcInteractionType(String npcInteractionType) {
        this.npcInteractionType = NPCInteractions.NPCInteractionType.valueOf(npcInteractionType);
    }

}
