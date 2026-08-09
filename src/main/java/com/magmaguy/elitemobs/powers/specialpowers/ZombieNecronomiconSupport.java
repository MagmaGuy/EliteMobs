package com.magmaguy.elitemobs.powers.specialpowers;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.EliteMobRemoveEvent;
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
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static com.magmaguy.magmacore.util.ChatColorConverter.convert;

public class ZombieNecronomiconSupport implements Listener {

    private static final Map<UUID, State> states = new ConcurrentHashMap<>();

    public ZombieNecronomiconSupport() {
    }

    public static void handleBossDamagedByPlayer(EliteEntity eliteEntity, LivingEntity targetted, String summoningChant) {
        if (eliteEntity == null || targetted == null) return;

        State state = states.computeIfAbsent(eliteEntity.getEliteUUID(),
                ignored -> new State(eliteEntity));
        state.pruneReinforcements();
        // Preserve the original activation threshold. A running controller may grow the list to 11,
        // but a fresh activation is only accepted while there are at most nine reinforcements.
        if (state.reinforcements.size() > 9 || state.hasReinforcementController()) return;
        state.start(targetted, summoningChant);
    }

    @EventHandler
    public void onEliteMobRemove(EliteMobRemoveEvent event) {
        State state = states.remove(event.getEliteMobEntity().getEliteUUID());
        if (state != null) state.cleanup(true, false);
    }

    public static void shutdown() {
        for (State state : new ArrayList<>(states.values()))
            state.cleanup(true, false);
        states.clear();
    }

    private static void itemMover(Map<Integer, List<Item>> xTrack, Entity entity, int counter) {
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
                Vector vector = GenericRotationMatrixMath.applyRotation(
                        a, b, c, numberOfPointsPerFullRotation, x, y, z, newCounter);
                Location newLocation = new Location(
                        entity.getWorld(), vector.getX(), vector.getY(), vector.getZ()).add(centerLocationFixed);
                Vector velocity = newLocation.subtract(currentLocation).toVector().multiply(0.3);
                item.setVelocity(velocity);
            }
        }
    }

    private static final class State {
        private final UUID eliteUuid;
        private final EliteEntity eliteEntity;
        private final List<CustomBossEntity> reinforcements = new ArrayList<>();
        private final Map<Integer, List<Item>> visualItems = new HashMap<>();
        private LivingEntity target;
        private String summoningChant;
        private int chantIndex;
        private BukkitTask reinforcementController;
        private BukkitTask nameController;
        private BukkitTask visualController;

        private State(EliteEntity eliteEntity) {
            this.eliteUuid = eliteEntity.getEliteUUID();
            this.eliteEntity = eliteEntity;
        }

        private void start(LivingEntity target, String summoningChant) {
            this.target = target;
            this.summoningChant = summoningChant;
            LivingEntity owner = eliteEntity.getLivingEntity();
            if (owner == null || !owner.isValid()) {
                cleanup(true, true);
                return;
            }

            owner.setAI(false);
            ensurePresentation();
            reinforcementController = new BukkitRunnable() {
                @Override
                public void run() {
                    runReinforcementTick();
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 60L, 60L);
        }

        private void runReinforcementTick() {
            if (!hasValidContext()) {
                cleanup(true, true);
                return;
            }

            pruneReinforcements();
            LivingEntity owner = eliteEntity.getLivingEntity();
            // Preserve the original running cap: the controller can spawn exactly one mob each
            // 60-tick pass while the live reinforcement count is below 11.
            if (reinforcements.size() >= 11) {
                owner.setAI(true);
                stopPresentation();
                return;
            }

            owner.setAI(false);
            ensurePresentation();
            spawnOneReinforcement(owner);
        }

        private boolean hasValidContext() {
            if (!eliteEntity.isValid() || target == null || !target.isValid()) return false;
            LivingEntity owner = eliteEntity.getLivingEntity();
            if (owner == null || !owner.isValid()) return false;
            if (target.getWorld() != owner.getWorld()) return false;
            return target.getLocation().distanceSquared(owner.getLocation()) <= 30D * 30D;
        }

        private void spawnOneReinforcement(LivingEntity owner) {
            int randomizedNumber = ThreadLocalRandom.current().nextInt(5) + 1;
            boolean spawnZombie = randomizedNumber < 5;
            String filename = spawnZombie ? "necronomicon_zombie.yml" : "necronomicon_skeleton.yml";
            CustomBossEntity customBossEntity = CustomBossEntity.createCustomBossEntity(filename);
            if (customBossEntity == null) {
                Logger.warn(filename + " is not valid!");
                // Preserve the old error-path distinction: a missing zombie definition retries on
                // the next controller pass, while a missing skeleton definition ends this casting run.
                if (!spawnZombie) stopAfterSpawnFailure();
                return;
            }

            customBossEntity.spawn(owner.getLocation(), eliteEntity.getLevel(), false);
            if (!customBossEntity.exists() || customBossEntity.getLivingEntity() == null ||
                    !customBossEntity.getLivingEntity().isValid()) {
                stopAfterSpawnFailure();
                return;
            }

            customBossEntity.getLivingEntity().setVelocity(new Vector(
                    (ThreadLocalRandom.current().nextDouble() - 0.5) / 30,
                    0.5,
                    (ThreadLocalRandom.current().nextDouble() - 0.5) / 30));
            eliteEntity.addReinforcement(customBossEntity);
            reinforcements.add(customBossEntity);
        }

        private void ensurePresentation() {
            LivingEntity owner = eliteEntity.getLivingEntity();
            if (owner == null || !owner.isValid() || owner.hasAI()) return;
            startNameController(owner);
            if (MobCombatSettingsConfig.isEnableWarningVisualEffects()) startVisualController(owner);
        }

        private void startNameController(LivingEntity owner) {
            if (isActive(nameController)) return;
            String convertedChant = convert(summoningChant == null ? "" : summoningChant);
            if (convertedChant == null || convertedChant.isBlank()) convertedChant = eliteEntity.getName();
            if (convertedChant == null) convertedChant = "";
            // The scroller renders a 31-character window. Short or empty custom chants used to
            // throw from substring(), cancelling only this task and leaving the caster frozen with
            // AI disabled. Pad them so malformed presentation text cannot break the power lifecycle.
            String fullChant = convertedChant.length() < 31
                    ? (convertedChant + " ".repeat(31)).substring(0, 31)
                    : convertedChant;
            nameController = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!owner.isValid() || owner.hasAI()) {
                        stopPresentation();
                        return;
                    }

                    if (chantIndex + 31 > fullChant.length()) chantIndex = 0;
                    owner.setCustomName(fullChant.substring(chantIndex, chantIndex + 31));
                    chantIndex++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 0L, 1L);
        }

        private void startVisualController(LivingEntity owner) {
            if (isActive(visualController)) return;
            visualController = new BukkitRunnable() {
                private int counter;

                @Override
                public void run() {
                    if (!owner.isValid() || owner.hasAI()) {
                        stopPresentation();
                        return;
                    }

                    if (counter == 0) {
                        for (int i = 0; i < 8; i++) {
                            List<Item> itemList = new ArrayList<>();
                            for (int j = 0; j < 4; j++) {
                                ItemStack itemStack = new ItemStack(Material.WRITTEN_BOOK, 1);
                                itemList.add(VisualItemInitializer.initializeItem(itemStack, owner.getLocation()));
                            }
                            visualItems.put(i, itemList);
                        }
                    } else {
                        itemMover(visualItems, owner, counter);
                    }
                    counter++;
                }
            }.runTaskTimer(MetadataHandler.PLUGIN, 5L, 5L);
        }

        private boolean hasReinforcementController() {
            return isActive(reinforcementController);
        }

        private static boolean isActive(BukkitTask task) {
            return task != null && !task.isCancelled();
        }

        private void stopAfterSpawnFailure() {
            cancelTask(reinforcementController);
            reinforcementController = null;
            LivingEntity owner = eliteEntity.getLivingEntity();
            if (owner != null && owner.isValid()) owner.setAI(true);
            stopPresentation();
        }

        private void stopPresentation() {
            cancelTask(nameController);
            cancelTask(visualController);
            nameController = null;
            visualController = null;
            removeVisualItems();

            LivingEntity owner = eliteEntity.getLivingEntity();
            if (owner != null && owner.isValid()) owner.setCustomName(eliteEntity.getName());
        }

        private void cleanup(boolean cullReinforcements, boolean removeState) {
            cancelTask(reinforcementController);
            reinforcementController = null;
            stopPresentation();

            LivingEntity owner = eliteEntity.getLivingEntity();
            if (owner != null && owner.isValid()) owner.setAI(true);

            if (cullReinforcements) {
                for (CustomBossEntity reinforcement : new ArrayList<>(reinforcements))
                    if (reinforcement != null && reinforcement.isValid())
                        reinforcement.remove(RemovalReason.REINFORCEMENT_CULL);
                reinforcements.clear();
            }
            if (removeState) states.remove(eliteUuid, this);
        }

        private void pruneReinforcements() {
            reinforcements.removeIf(entity -> entity == null || !entity.exists());
        }

        private void removeVisualItems() {
            for (List<Item> itemList : visualItems.values())
                for (Item item : itemList)
                    if (item != null && item.isValid()) item.remove();
            visualItems.clear();
        }

        private static void cancelTask(BukkitTask task) {
            if (task != null && !task.isCancelled()) task.cancel();
        }
    }
}
