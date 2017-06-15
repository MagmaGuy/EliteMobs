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

package com.magmaguy.elitemobs.majorpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.mobcustomizer.NameHandler;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import com.magmaguy.elitemobs.powerstances.MajorPowerPowerStance;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

import static com.magmaguy.elitemobs.ChatColorConverter.chatColorConverter;

/**
 * Created by MagmaGuy on 18/05/2017.
 */
public class ZombieNecronomicon extends MajorPowers implements Listener {

    Plugin plugin = Bukkit.getPluginManager().getPlugin(MetadataHandler.ELITE_MOBS);
    String powerMetadata = MetadataHandler.ZOMBIE_NECRONOMICON_MD;
    private int chantIndex = 0;
    private boolean summoningEffectOn = false;

    Configuration configuration = ConfigValues.translationConfig;
    private static Random random = new Random();

    @Override
    public void applyPowers(Entity entity) {

        entity.setMetadata(powerMetadata, new FixedMetadataValue(plugin, true));
        MajorPowerPowerStance majorPowerStanceMath = new MajorPowerPowerStance();
        majorPowerStanceMath.itemEffect(entity);

    }

    @Override
    public boolean existingPowers(Entity entity) {

        return entity.hasMetadata(powerMetadata);

    }

    @EventHandler
    public void onPlayerDetect(EntityTargetEvent event) {

        Entity targetter = event.getEntity();
        Entity targetted = event.getTarget();

        if (targetter.hasMetadata(MetadataHandler.ZOMBIE_CHANTING)) {

            return;

        }

        targetter.setMetadata(MetadataHandler.ZOMBIE_CHANTING, new FixedMetadataValue(plugin, true));

        if (targetted instanceof Player && targetter.hasMetadata(powerMetadata)) {

            ((LivingEntity) targetter).setAI(false);

            necronomiconVisualEffect((Zombie)targetter);

            new BukkitRunnable(){

                ArrayList<Entity> entityList = new ArrayList<>();

                @Override
                public void run() {

                    if (!targetted.isValid() || !targetter.isValid() || targetted.getWorld() != targetter.getWorld()
                            || targetted.getLocation().distance(targetter.getLocation()) > 30 ) {


                        for (Entity entity : entityList) {

                            if (entity.isValid()) {

                                entity.remove();

                            }

                        }

                        ((LivingEntity) targetter).setAI(true);
                        targetter.removeMetadata(MetadataHandler.ZOMBIE_CHANTING, plugin);
                        cancel();
                        return;

                    }

                    int randomizedNumber = random.nextInt(5) + 1;

                    for(Iterator<Entity> iterator = entityList.iterator(); iterator.hasNext();) {

                        Entity currentEntity = iterator.next();

                        if (!currentEntity.isValid()) {

                            iterator.remove();

                        }

                    }

                    if (entityList.size() < 11) {

                        ((Zombie) targetter).setAI(false);

                        if (!summoningEffectOn) {

                            necronomiconVisualEffect((Zombie) targetter);

                        }

                        if (randomizedNumber < 5){

                            Zombie zombie = (Zombie) targetter.getWorld().spawnEntity(targetter.getLocation(), EntityType.ZOMBIE);
                            zombie.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(plugin, 1));
                            zombie.setMetadata(MetadataHandler.FORBIDDEN_MD, new FixedMetadataValue(plugin, true));
                            zombie.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(plugin, true));
                            zombie.setMetadata(MetadataHandler.CUSTOM_NAME, new FixedMetadataValue(plugin, true));
                            zombie.setMetadata(MetadataHandler.CUSTOM_HEALTH, new FixedMetadataValue(plugin, true));
                            zombie.setMaxHealth(zombie.getMaxHealth() / 2);
                            zombie.setCustomName(chatColorConverter(configuration.getString("ZombieNecronomicon.Summoned zombie")));
                            zombie.setCustomNameVisible(true);

                            zombie.setVelocity(new Vector((random.nextDouble()-0.5)/30, 0.5, (random.nextDouble()-0.5)/30));

                            entityList.add(zombie);

                        } else {

                            Skeleton skeleton = (Skeleton) targetter.getWorld().spawnEntity(targetter.getLocation(), EntityType.SKELETON);
                            skeleton.setMetadata(MetadataHandler.ELITE_MOB_MD, new FixedMetadataValue(plugin, 1));
                            skeleton.setMetadata(MetadataHandler.FORBIDDEN_MD, new FixedMetadataValue(plugin, true));
                            skeleton.setMetadata(MetadataHandler.CUSTOM_POWERS_MD, new FixedMetadataValue(plugin, true));
                            skeleton.setMetadata(MetadataHandler.CUSTOM_NAME, new FixedMetadataValue(plugin, true));
                            skeleton.setMetadata(MetadataHandler.CUSTOM_HEALTH, new FixedMetadataValue(plugin, true));
                            skeleton.setMaxHealth(skeleton.getMaxHealth() / 2);
                            skeleton.setCustomName(chatColorConverter(configuration.getString("ZombieNecronomicon.Summoned skeleton")));
                            skeleton.setCustomNameVisible(true);

                            skeleton.setVelocity(new Vector((random.nextDouble()-0.5)/30, 0.5, (random.nextDouble()-0.5)/30));

                            entityList.add(skeleton);

                        }

                    } else {

                        ((Zombie) targetter).setAI(true);

                    }


                }

            }.runTaskTimer(plugin,20 * 3, 20 * 5);

        }

    }

    private void necronomiconVisualEffect (Zombie zombie) {

        summoningEffectOn = true;
        nameScroller(zombie);

        if (!ConfigValues.defaultConfig.getBoolean("Turn on visual effects that indicate an attack is about to happen")) {

            return;

        }

        new BukkitRunnable(){

            int counter = 0;
            HashMap<Integer, List<Item>> fourTrack = new HashMap();

            @Override
            public void run() {

                if (!zombie.isValid() || zombie.hasAI()) {

                    for (List<Item> itemList : fourTrack.values()) {

                        for (Item item : itemList) {

                            item.removeMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, plugin);
                            item.removeMetadata(MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, plugin);
                            item.remove();

                        }

                    }

                    if (zombie.isValid()) {

                        zombie.setCustomName(NameHandler.customAggressiveName(zombie));

                    }

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
                            item.setMetadata(MetadataHandler.MAJOR_VISUAL_EFFECT_MD, new FixedMetadataValue(plugin, true));
                            item.setMetadata(MetadataHandler.BETTERDROPS_COMPATIBILITY_MD, new FixedMetadataValue(plugin, true));

                            itemList.add(item);

                        }

                        fourTrack.put(i, itemList);

                    }

                } else {

                    itemMover(fourTrack, zombie, counter);

                }

                counter++;

            }

        }.runTaskTimer(plugin, 5, 5);

    }

    private void itemMover (HashMap<Integer, List<Item>> xTrack, Entity entity, int counter){

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

        new BukkitRunnable(){

            String fullChant = chatColorConverter(configuration.getString("ZombieNecronomicon.Summoning chant"));

            @Override
            public void run() {

                if(!zombie.isValid() || zombie.hasAI()) {

                    cancel();
                    return;

                }

                if (chantIndex+31 > fullChant.length()) {

                    chantIndex = 0;

                }

                String subString = fullChant.substring(chantIndex, chantIndex+31);

                zombie.setCustomName(subString);

                chantIndex++;


            }

        }.runTaskTimer(plugin, 0, 1);

    }

    @EventHandler
    public void onSummonedCreatureDeath (EntityDeathEvent event) {

        if (event.getEntity() instanceof Zombie && event.getEntity().hasMetadata(MetadataHandler.ELITE_MOB_MD) && event.getEntity().getCustomName() != null &&
                (event.getEntity().getCustomName().equals(configuration.getString("ZombieNecronomicon.Summoned zombie")) ||
                        event.getEntity().getCustomName().equals(configuration.getString("ZombieNecronomicon.Summoned skeleton")))) {

            event.getDrops().clear();

        }

    }

}
