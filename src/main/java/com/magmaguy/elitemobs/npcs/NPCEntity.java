package com.magmaguy.elitemobs.npcs;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.NPCConfig;
import com.magmaguy.elitemobs.npcs.chatter.NPCChatBubble;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class NPCEntity {

    private Villager villager;

    private String name;
    private String role;
    private ArmorStand roleDisplay;
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

        key += ".";

        Configuration configuration = ConfigValues.npcConfig;

        if (!setSpawnLocation(configuration.getString(key + NPCConfig.LOCATION))) return;

        this.villager = (Villager) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);

        setName(configuration.getString(key + NPCConfig.NAME));
        initializeRole(configuration.getString(key + NPCConfig.ROLE));
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

    }

    /**
     * Respawns the villager associated with the NPCEntity. Used for when chunks that contain NPCEntities load back up
     * as it makes sure the former Villager is slain.
     */
    public void respawnNPC() {
        this.villager.remove();

        this.villager = (Villager) spawnLocation.getWorld().spawnEntity(spawnLocation, EntityType.VILLAGER);
        villager.setCustomName(this.name);
        villager.setCustomNameVisible(true);
        villager.setCareer(this.career);
        villager.setAI(!this.canMove);

    }

    /**
     * Returns the Villager associated to this NPCEntity
     *
     * @return Villager associated to this NPCEntity
     */
    public Villager getVillager() {
        return this.villager;
    }

    /**
     * Returns the name of this NCPEntity
     *
     * @return Name of this NPCEntity
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the NPCEntity
     *
     * @param name Name to be set
     */
    public void setName(String name) {
        name = ChatColorConverter.convert(name);
        this.name = name;
        this.villager.setCustomName(name);
        this.villager.setCustomNameVisible(true);
    }

    /**
     * Gets the role of the NPCEntity. The role is the text that shows up below the name of the NPCEntity.
     *
     * @return Role name
     */
    public String getRole() {
        return this.role;
    }

    //Can't be used after the NPCEntity is done initialising
    private void initializeRole(String role) {
        this.role = role;
        this.roleDisplay = (ArmorStand) this.villager.getWorld().spawnEntity(villager.getLocation().add(new Vector(0, 1.72, 0)), EntityType.ARMOR_STAND);
        EntityTracker.registerArmorStands(this.roleDisplay);
        this.roleDisplay.setCustomName(role);
        this.roleDisplay.setCustomNameVisible(true);
        this.roleDisplay.setMarker(true);
        this.roleDisplay.setVisible(false);
        this.roleDisplay.setGravity(false);
    }

    /**
     * Sets the role of the NPCEntity. The role is the text that shows up below the name of the NPCEntity.
     *
     * @param role Role to be set
     */
    public void setRole(String role) {
        this.role = role;
        this.roleDisplay.setCustomName(role);
    }

    //TODO: Figure out why changing careers errors

    /**
     * Sets the career of the NPC, changing the skin the villager uses.
     *
     * @param career Career to be set
     */
    public void setCareer(String career) {
        this.career = Villager.Career.valueOf(career);
        this.villager.setCareer(this.career);
    }

    /**
     * Gets the spawn location of the NPCEntity
     *
     * @return Entity's spawn location
     */
    public Location getSpawnLocation() {
        return this.spawnLocation;
    }

    private boolean setSpawnLocation(String spawnLocation) {
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

        if (world == null) return false;

        this.spawnLocation = new Location(world, x, y, z, yaw, pitch);

        return true;
    }

    /**
     * Returns the list of greetings that this NPCEntity uses
     *
     * @return List of greetings used by this NPCEntity
     */
    public List<String> getGreetings() {
        return this.greetings;
    }

    /**
     * Sets the list of greetings to be used by the NPCEntity
     *
     * @param greetings List of greetings to be set
     */
    public void setGreetings(List<String> greetings) {
        this.greetings = greetings;
    }

    /**
     * Returns the list of dialogs that this NPCEntity uses. Dialog is triggered if the player remains near the NPCEntity
     * after being greeted by it.
     *
     * @return List of dialog used by this NPCEntity
     */
    public List<String> getDialog() {
        return this.dialog;
    }

    /**
     * Sets the list of dialogs to be used by this NPCEntity. Dialog is triggered if the player remains near the NPCEntity
     * after being greeted by it.
     *
     * @param dialog List of dialogs to be set
     */
    public void setDialog(List<String> dialog) {
        this.dialog = dialog;
    }

    /**
     * Returns the list of farewells that this NPCEntity uses. Farewells are triggered when a player exits an NPC's
     * GUI.
     *
     * @return List of farewells used by this NPCEntity
     */
    public List getFarewell() {
        return this.farewell;
    }

    /**
     * Sets the list of farewells to be used by this NPCEntity. Farewells are triggered when a player exits an NPC's
     * GUI.
     *
     * @param farewell List of farewells to be set
     */
    public void setFarewell(List<String> farewell) {
        this.farewell = farewell;
    }

    /**
     * Returns whether or not the NPCEntity can move.
     *
     * @return whether the NPCEntity can move
     */
    public boolean getCanMove() {
        return this.canMove;
    }

    /**
     * Sets the NPCEntity's ability to move
     *
     * @param canMove Sets if the NPCEntity can move
     */
    public void setCanMove(boolean canMove) {
        this.canMove = canMove;
        this.villager.setAI(canMove);
        if (!canMove)
            villager.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 3));
    }

    /**
     * Returns whether the NPCEntity can use NPCChatBubble
     *
     * @return Whether the NPCEntity can use NPCChatBubble
     */
    public boolean getCanTalk() {
        return this.canTalk;
    }

    /**
     * @param canTalk
     */
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

    public void say(List<String> messages, Player player) {
        new NPCChatBubble(selectString(messages), this.villager, player);
    }

    public void say(String message, Player player) {
        new NPCChatBubble(message, this.villager, player);
    }

    public void sayGreeting(Player player) {
        new NPCChatBubble(selectString(this.greetings), this.villager, player);
    }

    public void sayDialog(Player player) {
        new NPCChatBubble(selectString(this.dialog), this.villager, player);
    }

    public void sayFarewell(Player player) {
        new NPCChatBubble(selectString(this.farewell), this.villager, player);
    }

    private String selectString(List<String> strings) {
        return strings.get(ThreadLocalRandom.current().nextInt(strings.size()));
    }

}
