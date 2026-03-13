package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.powerstances.GenericRotationMatrixMath;
import com.magmaguy.elitemobs.powerstances.VisualItemInitializer;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.magmacore.util.ChatColorConverter.convert;

public class ZombieNecronomiconSupport {

    private static final Map<UUID, State> states = new ConcurrentHashMap<>();

    private ZombieNecronomiconSupport() {
    }

    public static void handleBossDamagedByPlayer(EliteEntity eliteEntity, LivingEntity targetted, String summoningChant) {
        if (eliteEntity == null || targetted == null) {
            return;
        }
        State state = states.computeIfAbsent(eliteEntity.getEliteUUID(), key -> new State());
        if (state.firing) return;
        state.entityList.removeIf(entity -> !entity.exists());
        if (state.entityList.size() > 9) return;
        state.firing = true;
        necronomiconVisualEffect(eliteEntity, state, summoningChant);
        spawnReinforcements(eliteEntity, targetted, state, summoningChant);
    }

    private static void necronomiconVisualEffect(EliteEntity eliteEntity, State state, String summoningChant) {
        LivingEntity livingEntity = eliteEntity.getLivingEntity();
        livingEntity.setAI(false);
        state.firing = true;
        nameScroller(livingEntity, state, summoningChant);

        if (!MobCombatSettingsConfig.isEnableWarningVisualEffects()) {
            return;
        }

        new BukkitRunnable() {
            final HashMap<Integer, List<Item>> fourTrack = new HashMap<>();
            int counter = 0;

            @Override
            public void run() {
                if (!livingEntity.isValid() || livingEntity.hasAI()) {
                    for (List<Item> itemList : fourTrack.values()) {
                        for (Item item : itemList) {
                            item.remove();
                        }
                    }
                    if (livingEntity.isValid()) {
                        livingEntity.setCustomName(eliteEntity.getName());
                    }
                    state.firing = false;
                    cancel();
                    return;
                }

                if (counter == 0) {
                    for (int i = 0; i < 8; i++) {
                        List<Item> itemList = new ArrayList<>();
                        for (int j = 0; j < 4; j++) {
                            ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
                            Item item = VisualItemInitializer.initializeItem(itemStack, livingEntity.getLocation());
                            itemList.add(item);
                        }
                        fourTrack.put(i, itemList);
                    }
                } else {
                    itemMover(fourTrack, livingEntity, counter);
                }

                counter++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 5, 5);
    }

    private static void itemMover(HashMap<Integer, List<Item>> xTrack, Entity entity, int counter) {
        double a = 0;
        double b = 1;
        double c = 0;
        double numberOfPointsPerFullRotation = 64;
        double x = 0;
        double y = 0;

        for (int trackNumber : xTrack.keySet()) {
            List<Item> itemList = xTrack.get(trackNumber);

            for (Item item : itemList) {
                double z = itemList.indexOf(item) + 1;
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

    private static void nameScroller(LivingEntity livingEntity, State state, String summoningChant) {
        new BukkitRunnable() {
            final String fullChant = convert(summoningChant);

            @Override
            public void run() {
                if (!livingEntity.isValid() || livingEntity.hasAI()) {
                    cancel();
                    return;
                }

                if (state.chantIndex + 31 > fullChant.length()) {
                    state.chantIndex = 0;
                }

                String subString = fullChant.substring(state.chantIndex, state.chantIndex + 31);
                livingEntity.setCustomName(subString);
                state.chantIndex++;
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 0, 1);
    }

    private static void spawnReinforcements(EliteEntity eliteEntity, LivingEntity targetted, State state, String summoningChant) {
        LivingEntity targetter = eliteEntity.getLivingEntity();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!eliteEntity.isValid() || !targetted.isValid() || !targetter.isValid() || targetted.getWorld() != targetter.getWorld()
                        || targetted.getLocation().distance(targetter.getLocation()) > 30) {

                    for (CustomBossEntity entity : state.entityList) {
                        if (entity.isValid()) {
                            entity.remove(RemovalReason.REINFORCEMENT_CULL);
                        }
                    }

                    if (eliteEntity.isValid()) {
                        targetter.setAI(true);
                    }
                    cancel();
                    return;
                }

                int randomizedNumber = ThreadLocalRandom.current().nextInt(5) + 1;
                state.entityList.removeIf(currentEntity -> !currentEntity.exists());

                if (state.entityList.size() < 11) {
                    targetter.setAI(false);

                    if (!state.firing) {
                        necronomiconVisualEffect(eliteEntity, state, summoningChant);
                    }

                    if (randomizedNumber < 5) {
                        CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity("necronomicon_zombie.yml");
                        if (customBossEntity == null) {
                            Logger.warn("necronomicon_zombie.yml is not valid!");
                            return;
                        }
                        customBossEntity.spawn(targetter.getLocation(), eliteEntity.getLevel(), false);

                        if (!customBossEntity.exists() || !customBossEntity.getLivingEntity().isValid()) {
                            targetter.setAI(true);
                            cancel();
                            return;
                        }

                        customBossEntity.getLivingEntity().setVelocity(new Vector(
                                (ThreadLocalRandom.current().nextDouble() - 0.5) / 30,
                                0.5,
                                (ThreadLocalRandom.current().nextDouble() - 0.5) / 30));

                        eliteEntity.addReinforcement(customBossEntity);
                        state.entityList.add(customBossEntity);
                    } else {
                        CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity("necronomicon_skeleton.yml");
                        if (customBossEntity == null) {
                            Logger.warn("necronomicon_skeleton.yml is not valid!");
                            targetter.setAI(true);
                            cancel();
                            return;
                        }
                        customBossEntity.spawn(targetter.getLocation(), eliteEntity.getLevel(), false);

                        if (!customBossEntity.getLivingEntity().isValid()) {
                            targetter.setAI(true);
                            cancel();
                            return;
                        }

                        customBossEntity.getLivingEntity().setVelocity(new Vector(
                                (ThreadLocalRandom.current().nextDouble() - 0.5) / 30,
                                0.5,
                                (ThreadLocalRandom.current().nextDouble() - 0.5) / 30));

                        eliteEntity.addReinforcement(customBossEntity);
                        state.entityList.add(customBossEntity);
                    }
                } else {
                    targetter.setAI(true);
                }
            }
        }.runTaskTimer(MetadataHandler.PLUGIN, 20 * 3L, 20 * 3L);
    }

    private static final class State {
        private int chantIndex = 0;
        private final ArrayList<CustomBossEntity> entityList = new ArrayList<>();
        private boolean firing = false;
    }
}
