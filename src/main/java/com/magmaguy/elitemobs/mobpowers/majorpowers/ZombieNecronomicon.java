/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.magmaguy.elitemobs.mobpowers.majorpowers;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.EntityTracker;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteMobEntity;
import com.magmaguy.elitemobs.mobconstructor.ReinforcementMobEntity;
import com.magmaguy.elitemobs.mobpowers.minorpowers.EventValidator;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
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
    private boolean summoningEffectOn = false;
    private HashSet<EliteMobEntity> chantingMobs = new HashSet<>();

    @Override
    public void applyPowers(Entity entity) {
    }

    @EventHandler
    public void onPlayerDetect(EntityTargetLivingEntityEvent event) {

        EliteMobEntity eliteMobEntity = EventValidator.getEventEliteMob(this, event);
        if (eliteMobEntity == null) return;

        if (chantingMobs.contains(eliteMobEntity)) return;
        chantingMobs.add(eliteMobEntity);

        necronomiconVisualEffect(eliteMobEntity);
        spawnReinforcements(eliteMobEntity, event.getTarget());

    }

    private void necronomiconVisualEffect(EliteMobEntity eliteMobEntity) {

        Zombie zombie = (Zombie) eliteMobEntity.getLivingEntity();

        zombie.setAI(false);
        summoningEffectOn = true;
        nameScroller(zombie);

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

                    summoningEffectOn = false;
                    cancel();
                    return;

                }

                if (counter == 0) {

                    //establish 4tracks
                    for (int i = 0; i < 8; i++) {

                        List<Item> itemList = new ArrayList<>();

                        for (int j = 0; j < 4; j++) {

                            ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);

                            Item item = zombie.getWorld().dropItem(zombie.getLocation(), itemStack);
                            item.setGravity(false);
                            item.setPickupDelay(Integer.MAX_VALUE);
                            EntityTracker.registerItemVisualEffects(item);
//                            MetadataHandler.registerMetadata(item, MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, true);

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

    private void nameScroller(Zombie zombie) {

        new BukkitRunnable() {

            String fullChant = convert(ConfigValues.translationConfig.getString("ZombieNecronomicon.Summoning chant"));

            @Override
            public void run() {

                if (!zombie.isValid() || zombie.hasAI()) {

                    cancel();
                    return;

                }

                if (chantIndex + 31 > fullChant.length()) {

                    chantIndex = 0;

                }

                String subString = fullChant.substring(chantIndex, chantIndex + 31);

                zombie.setCustomName(subString);

                chantIndex++;


            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);

    }

    private void spawnReinforcements(EliteMobEntity eliteMobEntity, LivingEntity targetted) {

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

                    if (!summoningEffectOn)
                        necronomiconVisualEffect(eliteMobEntity);

                    if (randomizedNumber < 5) {

                        ReinforcementMobEntity trashMobEntity = new ReinforcementMobEntity(EntityType.ZOMBIE, targetter.getLocation(), eliteMobEntity.getLevel(),
                                ChatColorConverter.convert(ConfigValues.translationConfig.getString("ZombieNecronomicon.Summoned zombie")));

                        trashMobEntity.getLivingEntity().setVelocity(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) / 30, 0.5,
                                (ThreadLocalRandom.current().nextDouble() - 0.5) / 30));

                        entityList.add(trashMobEntity.getLivingEntity());

                    } else {

                        ReinforcementMobEntity trashMobEntity = new ReinforcementMobEntity(EntityType.SKELETON, targetter.getLocation(), eliteMobEntity.getLevel(),
                                ChatColorConverter.convert(ConfigValues.translationConfig.getString("ZombieNecronomicon.Summoned skeleton")));

                        trashMobEntity.getLivingEntity().setVelocity(new Vector((ThreadLocalRandom.current().nextDouble() - 0.5) / 30, 0.5,
                                (ThreadLocalRandom.current().nextDouble() - 0.5) / 30));

                        entityList.add(trashMobEntity.getLivingEntity());

                    }

                } else
                    targetter.setAI(true);


            }

        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 3, 20 * 5);

    }

    @EventHandler
    public void onSummonedCreatureDeath(EntityDeathEvent event) {

        if (event.getEntity() instanceof Zombie && EntityTracker.isEliteMob(event.getEntity()) && event.getEntity().getCustomName() != null &&
                (event.getEntity().getCustomName().equals(ConfigValues.translationConfig.getString("ZombieNecronomicon.Summoned zombie")) ||
                        event.getEntity().getCustomName().equals(ConfigValues.translationConfig.getString("ZombieNecronomicon.Summoned skeleton"))))
            event.getDrops().clear();

    }

}
