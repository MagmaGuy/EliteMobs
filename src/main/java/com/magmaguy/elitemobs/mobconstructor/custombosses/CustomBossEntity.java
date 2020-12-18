package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.*;
import com.magmaguy.elitemobs.combatsystem.antiexploit.PreventMountExploit;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossConfigFields;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.ondeathcommands.OnDeathCommands;
import com.magmaguy.elitemobs.powers.ElitePower;
import com.magmaguy.elitemobs.powers.miscellaneouspowers.Taunt;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.thirdparty.libsdisguises.DisguiseEntity;
import com.magmaguy.elitemobs.utils.CommandRunner;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.Round;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarFlag;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

public class CustomBossEntity extends EliteMobEntity implements Listener {

    public static CustomBossEntity constructCustomBoss(String fileName, Location location, int mobLevel) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossesConfig.getCustomBoss(fileName);
        if (!customBossMobsConfigAttributes.isEnabled()) return null;

        return new CustomBossEntity(
                customBossMobsConfigAttributes,
                EntityType.valueOf(customBossMobsConfigAttributes.getEntityType()),
                location,
                mobLevel,
                ElitePowerParser.parsePowers(customBossMobsConfigAttributes.getPowers()));
    }

    public static CustomBossEntity constructCustomBoss(String fileName, Location location, int mobLevel, RegionalBossEntity regionalBossEntity, boolean isPhaseBossRespawn) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossesConfig.getCustomBoss(fileName);
        if (!customBossMobsConfigAttributes.isEnabled()) return null;

        return new CustomBossEntity(
                customBossMobsConfigAttributes,
                EntityType.valueOf(customBossMobsConfigAttributes.getEntityType()),
                location,
                mobLevel,
                ElitePowerParser.parsePowers(customBossMobsConfigAttributes.getPowers()),
                regionalBossEntity,
                isPhaseBossRespawn);
    }

    public static CustomBossEntity constructCustomBoss(String fileName, Location location, int mobLevel, double health, UUID phaseBossUUID) {
        CustomBossConfigFields customBossMobsConfigAttributes = CustomBossesConfig.getCustomBoss(fileName);
        if (!customBossMobsConfigAttributes.isEnabled()) return null;

        return new CustomBossEntity(
                customBossMobsConfigAttributes,
                EntityType.valueOf(customBossMobsConfigAttributes.getEntityType()),
                location,
                mobLevel,
                ElitePowerParser.parsePowers(customBossMobsConfigAttributes.getPowers()),
                health,
                phaseBossUUID);
    }

    private static final HashMap<UUID, CustomBossEntity> customBosses = new HashMap<>();

    private static void addCustomBoss(CustomBossEntity customBossEntity) {
        customBosses.put(customBossEntity.uuid, customBossEntity);
    }

    public static HashSet<CustomBossEntity> trackableCustomBosses = new HashSet<>();

    private static void removeCustomBoss(UUID uuid) {
        customBosses.remove(uuid);
    }

    private static boolean isCustomBoss(UUID uuid) {
        return customBosses.containsKey(uuid);
    }

    public static CustomBossEntity getCustomBoss(UUID uuid) {
        return customBosses.get(uuid);
    }

    public static HashMap<UUID, CustomBossEntity> getCustomBosses() {
        return customBosses;
    }

    public CustomBossConfigFields customBossConfigFields;
    private final HashMap<CustomItem, Double> uniqueLootList = new HashMap<>();
    public UUID uuid;
    private boolean trailIsActive = false;
    private boolean trackable = false;

    public LivingEntity advancedGetEntity() {
        if (getLivingEntity() != null)
            return getLivingEntity();
        else {
            if (this.uuid == null)
                return null;
            setLivingEntity((LivingEntity) Bukkit.getEntity(this.uuid));
            return (LivingEntity) Bukkit.getEntity(this.uuid);
        }
    }

    /**
     * For normal Custom Bosses (not regional and not phase bosses)
     *
     * @param customBossConfigFields
     * @param entityType
     * @param location
     * @param mobLevel
     * @param elitePowers
     */
    public CustomBossEntity(CustomBossConfigFields customBossConfigFields,
                            EntityType entityType,
                            Location location,
                            int mobLevel,
                            HashSet<ElitePower> elitePowers) {
        super(entityType, location, mobLevel, customBossConfigFields.getName(), elitePowers, CreatureSpawnEvent.SpawnReason.CUSTOM);
        if (super.getLivingEntity() == null) {
            new WarningMessage("Failed to spawn boss " + customBossConfigFields.getFileName() + " . Cause for failure:" +
                    " Tried to spawn in a region that prevented it from spawning. This is probably not an EliteMobs issue," +
                    " but a region management issue. Check if mobs are allowed to spawn where you are trying to spawn it. Location: "
                    + location.toString());
            return;
        }
        initializeCustomBoss(customBossConfigFields, location);
        spawnMessage();
        if (customBossConfigFields.getPhases().size() > 0)
            new PhaseBossEntity(this);
    }

    /**
     * For regional bosses
     *
     * @param customBossConfigFields
     * @param entityType
     * @param location
     * @param mobLevel
     * @param elitePowers
     */
    public CustomBossEntity(CustomBossConfigFields customBossConfigFields,
                            EntityType entityType,
                            Location location,
                            int mobLevel,
                            HashSet<ElitePower> elitePowers,
                            RegionalBossEntity regionalBossEntity,
                            boolean isPhaseBossRespawn) {
        super(entityType, location, mobLevel, customBossConfigFields.getName(), elitePowers, CreatureSpawnEvent.SpawnReason.CUSTOM);
        if (super.getLivingEntity() == null) {
            new WarningMessage("Failed to spawn boss " + customBossConfigFields.getFileName() + " . Cause for failure:" +
                    " Tried to spawn in a region that prevented it from spawning. This is probably not an EliteMobs issue," +
                    " but a region management issue. Check if mobs are allowed to spawn where you are trying to spawn it. Location: "
                    + location.toString());
            return;
        }
        initializeCustomBoss(customBossConfigFields, location);
        if (!isPhaseBossRespawn)
            spawnMessage();
        if (customBossConfigFields.getPhases().size() > 0) {
            new PhaseBossEntity(this, regionalBossEntity);
        }
    }

    /**
     * For Custom bosses spawned by the phase boss system
     *
     * @param customBossConfigFields
     * @param entityType
     * @param location
     * @param mobLevel
     * @param elitePowers
     * @param health
     * @param phaseBossUUID
     */
    public CustomBossEntity(CustomBossConfigFields customBossConfigFields,
                            EntityType entityType,
                            Location location,
                            int mobLevel,
                            HashSet<ElitePower> elitePowers,
                            double health,
                            UUID phaseBossUUID) {
        super(entityType, location, mobLevel, customBossConfigFields.getName(), elitePowers, CreatureSpawnEvent.SpawnReason.CUSTOM, health, phaseBossUUID);
        initializeCustomBoss(customBossConfigFields, location);
        this.setHealth(health * getMaxHealth());
    }

    private void initializeCustomBoss(CustomBossConfigFields customBossConfigFields,
                                      Location location) {
        if (super.getLivingEntity() == null) {
            this.uuid = null;
            new WarningMessage("Failed to spawn boss " + customBossConfigFields.getFileName() + " . Cause for failure:" +
                    " Tried to spawn in a region that prevented it from spawning. This is probably not an EliteMobs issue," +
                    " but a region management issue. Check if mobs are allowed to spawn where you are trying to spawn it. Location: "
                    + location.toString());
            return;
        }
        this.uuid = super.getLivingEntity().getUniqueId();
        super.setDamageMultiplier(customBossConfigFields.getDamageMultiplier());
        super.setHealthMultiplier(customBossConfigFields.getHealthMultiplier());
        super.setHasSpecialLoot(customBossConfigFields.getDropsEliteMobsLoot());
        this.customBossConfigFields = customBossConfigFields;
        setEquipment();
        if (super.getLivingEntity() instanceof Ageable)
            if (customBossConfigFields.isBaby())
                ((Ageable) super.getLivingEntity()).setBaby();
            else
                ((Ageable) super.getLivingEntity()).setAdult();
        super.setPersistent(customBossConfigFields.getIsPersistent());
        if (customBossConfigFields.getTrails() != null) startBossTrails();
        if (customBossConfigFields.getAnnouncementPriority() > 1 && MobCombatSettingsConfig.showCustomBossLocation && customBossConfigFields.getLocationMessage() != null) {
            this.trackable = true;
            trackableCustomBosses.add(this);
            sendLocation();
        }
        if (customBossConfigFields.getDropsVanillaLoot())
            super.setHasVanillaLoot(customBossConfigFields.getDropsVanillaLoot());
        parseUniqueLootList();
        addCustomBoss(this);
        if (customBossConfigFields.getFollowRange() != null && customBossConfigFields.getFollowRange() > 0 && getLivingEntity() instanceof Mob)
            getLivingEntity().getAttribute(Attribute.GENERIC_FOLLOW_RANGE).setBaseValue(customBossConfigFields.getFollowRange());
        mountEntity();
        if (customBossConfigFields.getDisguise() != null)
            if (Bukkit.getPluginManager().isPluginEnabled("LibsDisguises")) {
                DisguiseEntity.disguise(customBossConfigFields.getDisguise(), getLivingEntity(), customBossConfigFields);
                super.setName(customBossConfigFields.getName());
            }
        if (customBossConfigFields.getFrozen()) {
            getLivingEntity().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 10));
            getLivingEntity().setCollidable(false);
            getLivingEntity().setGravity(false);
        }
        CommandRunner.runCommandFromList(customBossConfigFields.getOnSpawnCommands(), new ArrayList<>());
        startEscapeMechanismDelay(customBossConfigFields.getTimeout());
    }

    private void spawnMessage() {
        if (customBossConfigFields.getSpawnMessage() == null) return;
        if (customBossConfigFields.getAnnouncementPriority() < 1) return;
        if (customBossConfigFields.getAnnouncementPriority() == 1)
            Bukkit.broadcastMessage(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
        else
            for (Player player : getLivingEntity().getWorld().getPlayers())
                player.sendMessage(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
        if (customBossConfigFields.getAnnouncementPriority() < 3) return;
        new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossConfigFields.getSpawnMessage()));
    }

    public void startBossTrails() {
        //todo: this is not good
        if (trailIsActive) return;
        trailIsActive = true;
        for (String string : this.customBossConfigFields.getTrails()) {
            try {
                Particle particle = Particle.valueOf(string);
                doParticleTrail(particle);
            } catch (Exception ex) {
            }
            try {
                Material material = Material.valueOf(string);
                doItemTrail(material);
            } catch (Exception ex) {
            }
        }
    }

    private void doParticleTrail(Particle particle) {
        new BukkitRunnable() {
            @Override
            public void run() {
                //In case of boss death or chunk unload, stop the effect
                if (!getLivingEntity().isValid()) {
                    cancel();
                    trailIsActive = false;
                    return;
                }
                //All conditions cleared, do the boss flair effect
                Location entityCenter = getLivingEntity().getLocation().clone().add(0, getLivingEntity().getHeight() / 2, 0);
                getLivingEntity().getWorld().spawnParticle(particle, entityCenter, 1, 0.1, 0.1, 0.1, 0.05);
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private void doItemTrail(Material material) {
        new BukkitRunnable() {

            @Override
            public void run() {
                //In case of boss death, stop the effect
                if (!getLivingEntity().isValid()) {
                    cancel();
                    trailIsActive = false;
                    return;
                }
                //All conditions cleared, do the boss flair effect
                Location entityCenter = getLivingEntity().getLocation().clone().add(0, getLivingEntity().getHeight() / 2, 0);
                Item item = VisualItemInitializer.initializeItem(ItemStackGenerator.generateItemStack
                        (material, "visualItem", Arrays.asList(ThreadLocalRandom.current().nextDouble() + "")), entityCenter);
                item.setVelocity(new Vector(
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10,
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10,
                        ThreadLocalRandom.current().nextDouble() / 5 - 0.10));
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        item.remove();
                        EntityTracker.wipeEntity(item);
                    }
                }.runTaskLater(MetadataHandler.PLUGIN, 20);

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
    }

    private void parseUniqueLootList() {
        for (String entry : this.customBossConfigFields.getUniqueLootList()) {
            try {
                CustomItem customItem = CustomItem.getCustomItem(entry.split(":")[0]);
                if (customItem == null)
                    throw new Exception();
                this.uniqueLootList.put(customItem, Double.parseDouble(entry.split(":")[1]));
            } catch (Exception ex) {
                new WarningMessage("Boss " + this.getName() + " has an invalid loot entry - " + entry + " - Skipping it!");
            }
        }
    }

    private HashMap<CustomItem, Double> getUniqueLootList() {
        return this.uniqueLootList;
    }

    private void setEquipment() {
        try {
            getLivingEntity().getEquipment().setHelmet(customBossConfigFields.getHelmet());
            getLivingEntity().getEquipment().setChestplate(customBossConfigFields.getChestplate());
            getLivingEntity().getEquipment().setLeggings(customBossConfigFields.getLeggings());
            getLivingEntity().getEquipment().setBoots(customBossConfigFields.getBoots());
            getLivingEntity().getEquipment().setItemInMainHand(customBossConfigFields.getMainHand());
            getLivingEntity().getEquipment().setItemInOffHand(customBossConfigFields.getOffHand());
        } catch (Exception ex) {
            new WarningMessage("Tried to assign a material slot to an invalid entity! Boss is from file" + customBossConfigFields.getFileName());
        }
    }

    private final HashSet<Player> trackingPlayer = new HashSet<>();

    private void sendLocation() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.getWorld().equals(getLivingEntity().getWorld())) continue;
            TextComponent interactiveMessage = new TextComponent(MobCombatSettingsConfig.bossLocationMessage);
            interactiveMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/elitemobs trackcustomboss " + player.getName() + " " + this.uuid));
            interactiveMessage.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Track the " + getName()).create()));
            player.spigot().sendMessage(interactiveMessage);
        }
    }

    public void realTimeTracking(Player player) {
        if (trackingPlayer.contains(player)) {
            trackingPlayer.remove(player);
            return;
        }
        if (!getLivingEntity().getWorld().equals(player.getWorld())) {
            player.sendMessage("You're not in the right world to track this boss!");
        }
        trackingPlayer.add(player);
        startBossBarTask(player);

    }

    HashMap<Player, BossBar> playerBossbars = new HashMap<>();

    public void startBossBarTask(Player player) {

        if (playerBossbars.containsKey(player))
            return;

        String locationString = (int) getLivingEntity().getLocation().getX() +
                ", " + (int) getLivingEntity().getLocation().getY() +
                ", " + (int) getLivingEntity().getLocation().getZ();
        BossBar bossBar = Bukkit.createBossBar(bossBarMessage(player, locationString), BarColor.GREEN, BarStyle.SOLID, BarFlag.PLAY_BOSS_MUSIC);
        bossBar.setProgress(getHealth() / getMaxHealth());
        bossBar.addPlayer(player);

        playerBossbars.put(player, bossBar);


        new BukkitRunnable() {
            @Override
            public void run() {
                if (getLivingEntity().isDead() ||
                        !player.isOnline() ||
                        !player.getWorld().equals(getLivingEntity().getWorld()) ||
                        !trackingPlayer.contains(player) && player.getLocation().distance(getLivingEntity().getLocation()) > 20) {
                    bossBar.removeAll();
                    cancel();
                    playerBossbars.remove(player);
                    return;
                }

                String locationString = (int) getLivingEntity().getLocation().getX() +
                        ", " + (int) getLivingEntity().getLocation().getY() +
                        ", " + (int) getLivingEntity().getLocation().getZ();
                bossBar.setTitle(bossBarMessage(player, locationString));
                bossBar.setProgress(getHealth() / getMaxHealth());

            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 20);
    }

    public String bossBarMessage(Player player, String locationString) {
        if (customBossConfigFields.getLocationMessage().contains("$distance") || customBossConfigFields.getLocationMessage().contains("$location")) {
            if (!player.getLocation().getWorld().equals(advancedGetEntity().getWorld()))
                return ChatColorConverter.convert(MobCombatSettingsConfig.defaultOtherWorldBossLocationMessage
                        .replace("$name", getName()));

            return ChatColorConverter.convert(customBossConfigFields.getLocationMessage()
                    .replace("$distance", "" + (int) getLivingEntity().getLocation().distance(player.getLocation())))
                    .replace("$location", locationString);
        }

        return ChatColorConverter.convert(customBossConfigFields.getLocationMessage());

    }

    private void dropLoot(Player player) {

        if (getUniqueLootList().isEmpty()) return;

        for (CustomItem customItem : getUniqueLootList().keySet())
            if (ThreadLocalRandom.current().nextDouble() < getUniqueLootList().get(customItem))
                CustomItem.dropPlayerLoot(player, (int) getTier(), customItem.getFileName(), getLivingEntity().getLocation());
    }

    private void mountEntity() {
        if (customBossConfigFields.getMountedEntity() == null) return;
        try {
            EntityType entityType = EntityType.valueOf(customBossConfigFields.getMountedEntity());
            LivingEntity livingEntity = (LivingEntity) getLivingEntity().getWorld().spawnEntity(getLivingEntity().getLocation(), entityType);
            PreventMountExploit.bypass = true;
            livingEntity.addPassenger(getLivingEntity());

        } catch (Exception ex) {
            //This runs when it's not an API entity
            for (CustomBossConfigFields iteratedField : CustomBossConfigFields.customBossConfigFields) {
                if (iteratedField.getFileName().equalsIgnoreCase(customBossConfigFields.getMountedEntity())) {
                    CustomBossEntity customBossEntity = constructCustomBoss(customBossConfigFields.getMountedEntity(), getLivingEntity().getLocation(), getLevel());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (customBossEntity == null || customBossEntity.getLivingEntity() == null)
                                return;
                            PreventMountExploit.bypass = true;
                            customBossEntity.getLivingEntity().addPassenger(getLivingEntity());
                        }
                    }.runTaskLater(MetadataHandler.PLUGIN, 2);
                    return;
                }
            }
            new WarningMessage("Attempted to make Custom Boss " + customBossConfigFields.getFileName() + " mount invalid" +
                    " entity or boss " + customBossConfigFields.getMountedEntity() + " . Fix this in the configuration file.");
        }
    }

    /**
     * Starts the escape mechanic for bosses that have this feature. After a set time, in minutes, the boss will escape,
     * potentially broadcasting an escape message.
     */
    private void startEscapeMechanismDelay(int timeout) {

        if (timeout < 1) return;

        new BukkitRunnable() {

            @Override
            public void run() {
                LivingEntity livingEntity = advancedGetEntity();
                if (livingEntity == null || livingEntity.isDead()) {
                    remove();
                    return;
                }
                remove();
                if (customBossConfigFields.getAnnouncementPriority() < 1) return;
                if (customBossConfigFields.getEscapeMessage() != null)
                    for (Player player : Bukkit.getOnlinePlayers())
                        if (player.getWorld().equals(livingEntity.getWorld()))
                            player.sendMessage(ChatColorConverter.convert(customBossConfigFields.getEscapeMessage()));
                if (customBossConfigFields.getAnnouncementPriority() < 3) return;
                new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossConfigFields.getEscapeMessage()));

            }

        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 60 * timeout);

    }


    @Override
    public void remove() {
        super.remove();
        trackableCustomBosses.remove(this);
    }

    public static class CustomBossEntityEvents implements Listener {

        public static HashMap<Chunk, Spawnable> spawnableEntities = new HashMap<>();

        @EventHandler
        public void onSpawn(ChunkLoadEvent event) {
            if (spawnableEntities.isEmpty()) return;
            if (!spawnableEntities.containsKey(event.getChunk())) return;
            Spawnable spawnable = spawnableEntities.get(event.getChunk());
            spawnableEntities.remove(event.getChunk());
            spawnable.eliteMobEntity.continueCustomBossCreation((LivingEntity) spawnable.location.getWorld().spawnEntity(spawnable.location, spawnable.entityType));
        }

        @EventHandler
        public void onEliteMobDeath(EliteMobDeathEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;
            CustomBossEntity customBossEntity = (CustomBossEntity) event.getEliteMobEntity();

            if (customBossEntity.trackable)
                trackableCustomBosses.remove(customBossEntity);

            //Do death message
            String playersList = "";
            for (Player player : event.getEliteMobEntity().getDamagers().keySet()) {
                if (playersList.isEmpty())
                    playersList += player.getDisplayName();
                else
                    playersList += ", &f" + player.getDisplayName();

                //Do loot
                if (!customBossEntity.getTriggeredAntiExploit())
                    customBossEntity.dropLoot(player);
            }

            playersList = ChatColorConverter.convert(playersList);

            if (customBossEntity.customBossConfigFields.getAnnouncementPriority() > 0 && customBossEntity.hasDamagers())
                if (customBossEntity.customBossConfigFields.getDeathMessages() != null && customBossEntity.customBossConfigFields.getDeathMessages().size() > 0) {
                    Player topDamager = null, secondDamager = null, thirdDamager = null;

                    HashMap<Player, Double> sortedMap = sortByComparator(customBossEntity.getDamagers(), false);

                    Iterator<Player> sortedMapIterator = sortedMap.keySet().iterator();
                    for (int i = 1; i < 4; i++) {
                        if (i > sortedMap.size())
                            break;
                        Player nextPlayer = sortedMapIterator.next();
                        switch (i) {
                            case 1:
                                topDamager = nextPlayer;
                                break;
                            case 2:
                                secondDamager = nextPlayer;
                                break;
                            case 3:
                                thirdDamager = nextPlayer;
                                break;
                        }
                    }

                    for (String string : customBossEntity.customBossConfigFields.getDeathMessages()) {
                        if (string.contains("$damager1name"))
                            if (topDamager != null)
                                string = string.replace("$damager1name", topDamager.getDisplayName());
                            else
                                string = "";
                        if (string.contains("$damager1damage"))
                            if (topDamager != null)
                                string = string.replace("$damager1damage", Round.twoDecimalPlaces(customBossEntity.getDamagers().get(topDamager)) + "");
                            else
                                string = "";
                        if (string.contains("$damager2name"))
                            if (secondDamager != null)
                                string = string.replace("$damager2name", secondDamager.getDisplayName());
                            else
                                string = "";
                        if (string.contains("$damager2damage"))
                            if (secondDamager != null)
                                string = string.replace("$damager2damage", Round.twoDecimalPlaces(customBossEntity.getDamagers().get(secondDamager)) + "");
                            else
                                string = "";
                        if (string.contains("$damager3name"))
                            if (thirdDamager != null)
                                string = string.replace("$damager3name", thirdDamager.getDisplayName());
                            else
                                string = "";
                        if (string.contains("$damager3damage"))
                            if (thirdDamager != null)
                                string = string.replace("$damager3damage", Round.twoDecimalPlaces(customBossEntity.getDamagers().get(thirdDamager)) + "");
                            else
                                string = "";
                        if (string.contains("$players"))
                            string = string.replace("$players", playersList);
                        Bukkit.broadcastMessage(ChatColorConverter.convert(string));
                        if (string.length() > 0)
                            if (customBossEntity.customBossConfigFields.getAnnouncementPriority() > 2)
                                new DiscordSRVAnnouncement(ChatColorConverter.convert(string));
                    }

                    for (Player player : Bukkit.getOnlinePlayers())
                        if (customBossEntity.getDamagers().containsKey(player))
                            player.sendMessage(
                                    ChatColorConverter.convert(
                                            MobCombatSettingsConfig.bossKillParticipationMessage.replace(
                                                    "$playerDamage",
                                                    Round.twoDecimalPlaces(customBossEntity.getDamagers().get(player)) + "")));

                } else {
                    if (customBossEntity.customBossConfigFields.getAnnouncementPriority() > 0 && customBossEntity.customBossConfigFields.getDeathMessage() != null)
                        Bukkit.broadcastMessage(ChatColorConverter.convert(customBossEntity.customBossConfigFields.getDeathMessage().replace("$players", playersList)));
                    if (customBossEntity.customBossConfigFields.getAnnouncementPriority() > 2)
                        new DiscordSRVAnnouncement(ChatColorConverter.convert(customBossEntity.customBossConfigFields.getDeathMessage().replace("$players", playersList)));
                }

            removeCustomBoss(customBossEntity.uuid);

            if (customBossEntity.customBossConfigFields.getOnDeathCommands() != null && !customBossEntity.customBossConfigFields.getOnDeathCommands().isEmpty())
                OnDeathCommands.parseConsoleCommand(customBossEntity.customBossConfigFields.getOnDeathCommands(), event);

            if (!customBossEntity.customBossConfigFields.getDropsVanillaLoot()) {
                event.getEntityDeathEvent().setDroppedExp(0);
                for (ItemStack itemStack : event.getEntityDeathEvent().getDrops())
                    itemStack.setAmount(0);
            }

        }

        private static HashMap<Player, Double> sortByComparator(HashMap<Player, Double> unsortMap, final boolean order) {

            List<Entry<Player, Double>> list = new LinkedList<Entry<Player, Double>>(unsortMap.entrySet());

            // Sorting the list based on values
            Collections.sort(list, new Comparator<Entry<Player, Double>>() {
                public int compare(Entry<Player, Double> o1,
                                   Entry<Player, Double> o2) {
                    if (order) {
                        return o1.getValue().compareTo(o2.getValue());
                    } else {
                        return o2.getValue().compareTo(o1.getValue());
                    }
                }
            });

            // Maintaining insertion order with the help of LinkedList
            HashMap<Player, Double> sortedMap = new LinkedHashMap<Player, Double>();
            for (Entry<Player, Double> entry : list) {
                sortedMap.put(entry.getKey(), entry.getValue());
            }

            return sortedMap;
        }

        @EventHandler
        public void onDamagedMessages(EliteMobDamagedEvent eliteMobDamagedEvent) {
            if (!(eliteMobDamagedEvent.getEliteMobEntity() instanceof CustomBossEntity)) return;
            CustomBossEntity customBossEntity = (CustomBossEntity) eliteMobDamagedEvent.getEliteMobEntity();
            if (customBossEntity.customBossConfigFields.getOnDamagedMessages().isEmpty()) return;
            Taunt.nametagProcessor(customBossEntity.getLivingEntity(), customBossEntity.customBossConfigFields.getOnDamagedMessages());
        }

        @EventHandler
        public void onDamageMessages(PlayerDamagedByEliteMobEvent playerDamagedByEliteMobEvent) {
            if (!(playerDamagedByEliteMobEvent.getEliteMobEntity() instanceof CustomBossEntity)) return;
            CustomBossEntity customBossEntity = (CustomBossEntity) playerDamagedByEliteMobEvent.getEliteMobEntity();
            if (customBossEntity.customBossConfigFields.getOnDamageMessages().isEmpty()) return;
            Taunt.nametagProcessor(customBossEntity.getLivingEntity(), customBossEntity.customBossConfigFields.getOnDamageMessages());
        }

        @EventHandler
        public void onChunkLoadEvent(ChunkLoadEvent event) {
            for (Entity entity : event.getChunk().getEntities()) {
                //if (entity instanceof LivingEntity)
                //if (CrashFix.isPersistentEntity(entity))
                //if (isCustomBoss(entity.getUniqueId())) {
                CustomBossEntity customBossEntity = getCustomBoss(entity.getUniqueId());
                if (customBossEntity == null) continue;
                customBossEntity.setLivingEntity((LivingEntity) entity);
                customBossEntity.startBossTrails();
            }
            //}

        }

        @EventHandler
        public void removeSlowEvent(EliteMobEnterCombatEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;
            if (event.getEliteMobEntity().getLivingEntity().getPotionEffect(PotionEffectType.SLOW) == null) return;
            event.getEliteMobEntity().getLivingEntity().removePotionEffect(PotionEffectType.SLOW);
        }

        @EventHandler
        public void onCombatExitEvent(EliteMobExitCombatEvent event) {
            if (!MobCombatSettingsConfig.regenerateCustomBossHealthOnCombatEnd) return;
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;

        }

        @EventHandler
        public void displayBossBar(EliteMobEnterCombatEvent event) {
            if (!(event.getEliteMobEntity() instanceof CustomBossEntity)) return;
            CustomBossEntity customBossEntity = (CustomBossEntity) event.getEliteMobEntity();
            if (customBossEntity.customBossConfigFields.getLocationMessage() == null || customBossEntity.customBossConfigFields.getLocationMessage().length() == 0)
                return;

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!event.getEliteMobEntity().getLivingEntity().isValid() || !customBossEntity.isInCombat()) {
                        cancel();
                        return;
                    }

                    for (Entity entity : event.getEliteMobEntity().getLivingEntity().getNearbyEntities(20, 20, 20))
                        if (entity.getType().equals(EntityType.PLAYER))
                            customBossEntity.startBossBarTask((Player) entity);

                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0, 5);
        }

    }

}
