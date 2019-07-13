package com.magmaguy.elitemobs.powers.majorpowers.zombie;

import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobDamagedByPlayerEvent;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.powers.PowersConfig;
import com.magmaguy.elitemobs.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.powers.MajorPower;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.elitemobs.ChatColorConverter.convert;

/**
 * Created by MagmaGuy on 18/05/2017.
 */
public class ZombieNecronomicon extends MajorPower implements Listener {

    private int chantIndex = 0;

    private HashSet<EliteMobEntity> chantingMobs = new HashSet<>();
    //todo: Shouldn't this be static?

    public ZombieNecronomicon() {
        super(PowersConfig.getPower("zombie_necronomicon.yml"));
    }

    @EventHandler
    public void onPlayerDetect(EliteMobDamagedByPlayerEvent event) {
        ZombieNecronomicon zombieNecronomicon = (ZombieNecronomicon) event.getEliteMobEntity().getPower(this);
        if (zombieNecronomicon == null) return;
        if (zombieNecronomicon.getIsFiring()) return;

        zombieNecronomicon.setIsFiring(true);
        necronomiconVisualEffect(event.getEliteMobEntity(), zombieNecronomicon);
        spawnReinforcements(event.getEliteMobEntity(), event.getPlayer(), zombieNecronomicon);
    }

    private void necronomiconVisualEffect(EliteMobEntity eliteMobEntity, ZombieNecronomicon zombieNecronomicon) {

        Zombie zombie = (Zombie) eliteMobEntity.getLivingEntity();
        zombie.setAI(false);
        zombieNecronomicon.setIsFiring(true);
        nameScroller(zombie, zombieNecronomicon);

        if (!ConfigValues.mobCombatSettingsConfig.getBoolean(MobCombatSettingsConfig.ENABLE_WARNING_VISUAL_EFFECTS))
            return;

        new BukkitRunnable() {
            int counter = 0;
            HashMap<Integer, List<Item>> fourTrack = new HashMap();

            @Override
            public void run() {
                if (!zombie.isValid() || zombie.hasAI()) {
                    for (List<Item> itemList : fourTrack.values())
                        for (Item item : itemList)
                            item.remove();
                    if (zombie.isValid())
                        zombie.setCustomName(EntityTracker.getEliteMobEntity(zombie).getName());
                    zombieNecronomicon.setIsFiring(false);
                    cancel();
                    return;
                }

                if (counter == 0) {

                    //establish 4tracks
                    for (int i = 0; i < 8; i++) {
                        List<Item> itemList = new ArrayList<>();
                        for (int j = 0; j < 4; j++) {
                            ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
                            Item item = VisualItemInitializer.initializeItem(itemStack, zombie.getLocation());
                            itemList.add(item);
                        }
                        fourTrack.put(i, itemList);
                    }
                } else
                    itemMover(fourTrack, zombie, counter);

                counter++;
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 5, 5);

    }

    private void itemMover(HashMap<Integer, List<Item>> xTrack, Entity entity, int counter) {

        double a = 0;
        double b = 1;
        double c = 0;

        double numberOfPointsPerFullRotation = 64;

        double x = 0;
        double y = 0;
        double z;

        for (int trackNumber : xTrack.keySet()) {

            List<Item> itemList = xTrack.get(trackNumber);

            for (Item item : itemList) {

                //each track is offset by 45 degrees
                //items are then offset on the Z value depending on their position

                z = (itemList.indexOf(item) + 1);

                //to predict the counter offset value you have to divide 8 (for 8 tracks total)

                int newCounter = (int) (counter + trackNumber * (numberOfPointsPerFullRotation / 8));

                Location currentLocation = item.getLocation();

                Location centerLocationFixed = entity.getLocation().add(0, 3, 0);

                Vector vector = GenericRotationMatrixMath.applyRotation(a, b, c, numberOfPointsPerFullRotation, x, y, z, newCounter);
                Location newLocation = new Location(entity.getWorld(), vector.getX(), vector.getY(), vector.getZ()).add(centerLocationFixed);

                Vector velocity = (newLocation.subtract(currentLocation)).toVector().multiply(0.3);
                item.setVelocity(velocity);

            }

        }

    }

    private void nameScroller(Zombie zombie, ZombieNecronomicon zombieNecronomicon) {

        new BukkitRunnable() {
            String fullChant = convert(ConfigValues.translationConfig.getString("ZombieNecronomicon.Summoning chant"));

            @Override
            public void run() {

                if (!zombie.isValid() || zombie.hasAI()) {
                    cancel();
                    return;
                }

                if (zombieNecronomicon.chantIndex + 31 > fullChant.length())
                    zombieNecronomicon.chantIndex = 0;

                String subString = fullChant.substring(zombieNecronomicon.chantIndex, zombieNecronomicon.chantIndex + 31);
                zombie.setCustomName(subString);
                zombieNecronomicon.chantIndex++;
            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void spawnReinforcements(EliteMobEntity eliteMobEntity, LivingEntity targetted, ZombieNecronomicon zombieNecronomicon) {

        LivingEntity targetter = eliteMobEntity.getLivingEntity();

        new BukkitRunnable() {

            ArrayList<Entity> entityList = new ArrayList<>();

            @Override
            public void run() {

                if (!targetted.isValid() || !targetter.isValid() || targetted.getWorld() != targetter.getWorld()
                        || targetted.getLocation().distance(targetter.getLocation()) > 30) {

                    for (Entity entity : entityList)
                        if (entity.isValid())
                            entity.remove();

                    targetter.setAI(true);
                    chantingMobs.remove(targetter);
                    cancel();
                    return;

                }

                int randomizedNumber = ThreadLocalRandom.current().nextInt(5) + 1;

                entityList.removeIf(currentEntity -> !currentEntity.isValid());

                if (entityList.size() < 11) {

                    targetter.setAI(false);

                    if (!zombieNecronomicon.getIsFiring())
                        necronomiconVisualEffect(eliteMobEntity, zombieNecronomicon);

                    if (randomizedNumber < 5) {

                        CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss("necronomicon_zombie.yml", targetter.getLocation(), eliteMobEntity.getLevel());

                        customBossEntity.getLivingEntity().setVelocity(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) / 30, 0.5,
                                (ThreadLocalRandom.current().nextDouble() - 0.5) / 30));

                        entityList.add(customBossEntity.getLivingEntity());

                    } else {

                        CustomBossEntity customBossEntity = CustomBossEntity.constructCustomBoss("necronomicon_skeleton.yml", targetter.getLocation(), eliteMobEntity.getLevel());

                        customBossEntity.getLivingEntity().setVelocity(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) / 30, 0.5,
                                (ThreadLocalRandom.current().nextDouble() - 0.5) / 30));

                        entityList.add(customBossEntity.getLivingEntity());

                    }

                } else
                    targetter.setAI(true);


            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 3, 20 * 3);

    }

    @EventHandler
    public void onSummonedCreatureDeath(EntityDeathEvent event) {
        if (event.getEntity() instanceof Zombie && EntityTracker.isEliteMob(event.getEntity()) && event.getEntity().getCustomName() != null &&
                (event.getEntity().getCustomName().equals(ConfigValues.translationConfig.getString("ZombieNecronomicon.Summoned zombie")) ||
                        event.getEntity().getCustomName().equals(ConfigValues.translationConfig.getString("ZombieNecronomicon.Summoned skeleton"))))
            event.getDrops().clear();
    }

}
