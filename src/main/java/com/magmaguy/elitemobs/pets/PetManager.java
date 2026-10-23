package com.magmaguy.elitemobs.pets;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.api.internal.RemovalReason;
import com.magmaguy.elitemobs.api.mind.EliteMindBodyProfile;
import com.magmaguy.elitemobs.config.PetsConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfig;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.entitytracker.EntityTracker;
import com.magmaguy.elitemobs.mobconstructor.CustomSpawn;
import com.magmaguy.elitemobs.mobconstructor.EliteEntity;
import com.magmaguy.elitemobs.mobconstructor.EliteMindServiceModule;
import com.magmaguy.elitemobs.mobconstructor.InternalMindActorSpawnRequest;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Owns every pet body in the world and every online player's collection: wild spawns, taming, feeding, breeding,
 * growing up, equipping, and the watchdog that keeps an equipped pet beside its owner. Everything runs on the
 * server thread.
 */
public final class PetManager {
    private static final UUID NO_OWNER = new UUID(0L, 0L);
    private static final NamespacedKey PET_KEY = new NamespacedKey(MetadataHandler.PLUGIN, "pet");
    private static final double BREED_DISTANCE = 8D;
    private static PetManager instance;

    private final Map<String, PetSpecies> species = new LinkedHashMap<>();
    private final Map<UUID, PetControl> byBody = new HashMap<>();
    private final Map<UUID, PetControl> byElite = new HashMap<>();
    private final Map<UUID, PetCollection> collections = new HashMap<>();
    private final Map<UUID, Map<String, PetControl>> ownedBodies = new HashMap<>();
    private BukkitTask watchdog;
    private BukkitTask wildSpawner;
    private long wildSpawnerInterval;

    private PetManager() {
    }

    public static PetManager get() {
        return instance;
    }

    public static void initialize() {
        shutdown();
        PetManager manager = new PetManager();
        instance = manager;
        manager.loadSpecies();
        if (manager.species.isEmpty()) return;
        manager.watchdog = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, manager::watch, 20L, 20L);
        manager.scheduleWildSpawner();
        for (Player player : Bukkit.getOnlinePlayers()) manager.playerJoined(player);
    }

    public static void shutdown() {
        PetManager manager = instance;
        if (manager == null) return;
        instance = null;
        if (manager.watchdog != null) manager.watchdog.cancel();
        if (manager.wildSpawner != null) manager.wildSpawner.cancel();
        for (PetControl control : new ArrayList<>(manager.byElite.values())) manager.despawn(control, RemovalReason.SHUTDOWN);
        manager.collections.clear();
        manager.ownedBodies.clear();
    }

    private void loadSpecies() {
        species.clear();
        for (CustomBossesConfigFields boss : CustomBossesConfig.getCustomBosses().values()) {
            PetSpecies pet = boss.getPet();
            if (pet == null || !boss.isEnabled()) continue;
            PetSpecies previous = species.putIfAbsent(pet.id(), pet);
            if (previous != null)
                Logger.warn("Pet species " + pet.id() + " is defined by both " + previous.boss().getFilename() + " and " + boss.getFilename() + "; keeping the first");
        }
    }

    public Collection<PetSpecies> species() {
        return Collections.unmodifiableCollection(species.values());
    }

    public PetSpecies species(String id) {
        return id == null ? null : species.get(id.toLowerCase(Locale.ROOT));
    }

    /** The pet behind an entity, or null when the entity is not a pet body. */
    public PetControl pet(Entity entity) {
        if (entity == null) return null;
        PetControl control = byBody.get(entity.getUniqueId());
        if (control != null) return control;
        EliteEntity elite = EntityTracker.getEliteMobEntity(entity);
        return elite == null ? null : byElite.get(elite.getEliteUUID());
    }

    public PetControl pet(EliteEntity elite) {
        return elite == null ? null : byElite.get(elite.getEliteUUID());
    }

    public Collection<PetControl> pets() {
        return Collections.unmodifiableCollection(byElite.values());
    }

    public PetCollection collection(UUID player) {
        return collections.computeIfAbsent(player, PetCollection::load);
    }

    // ---- spawning --------------------------------------------------------------------------------------------

    public PetControl spawnWild(PetSpecies pet, Location location) {
        Location at = PetMindProgramFactory.hoverPoint(new PetControl(pet, null, null, null, false, location), location, 0D);
        return spawn(pet, null, null, pet.pickVariant(ThreadLocalRandom.current()), false, at, location);
    }

    /** Puts an equipped pet from the collection into the world beside its owner. */
    public PetControl spawnOwned(Player owner, PetRecord record) {
        PetSpecies pet = species(record.species);
        if (pet == null) return null;
        unspawnOwned(owner.getUniqueId(), record);
        Location at = PetMindProgramFactory.hoverPoint(new PetControl(pet, owner.getUniqueId(), record, record.model, record.baby, null),
                owner.getLocation(), 2D);
        return spawn(pet, owner.getUniqueId(), record, record.model, record.baby, at, null);
    }

    private PetControl spawn(PetSpecies pet, UUID owner, PetRecord record, String model, boolean baby, Location at, Location home) {
        CustomBossesConfigFields boss = pet.boss();
        PetControl control = new PetControl(pet, owner, record, model, baby, home);
        String override = baby && pet.babyModel() != null ? pet.babyModel() : model;
        if (override != null && override.equals(boss.getCustomModel())) override = null;
        Optional<EliteEntity> spawned;
        try {
            spawned = EliteMindServiceModule.spawnInternal(new InternalMindActorSpawnRequest(
                    MetadataHandler.PLUGIN,
                    owner == null ? NO_OWNER : owner,
                    at,
                    1,
                    EliteMindBodyProfile.forCarrier(boss.getEntityType().getKey(), pet.locomotion()),
                    PetMindProgramFactory.create(MetadataHandler.PLUGIN, control),
                    elite -> prepare(elite, control),
                    boss,
                    false,
                    override));
        } catch (RuntimeException failure) {
            Logger.warn("Could not spawn pet " + pet.id() + ": " + failure.getMessage());
            return null;
        }
        if (spawned.isEmpty() || !(spawned.get() instanceof CustomBossEntity actor)) {
            Logger.warn("Could not spawn pet " + pet.id() + ": the Mind runtime is unavailable");
            return null;
        }
        control.setActor(actor);
        actor.addCustomData(PET_KEY, control);
        byElite.put(actor.getEliteUUID(), control);
        LivingEntity body = actor.getLivingEntity();
        if (body != null) byBody.put(body.getUniqueId(), control);
        if (owner != null && record != null) ownedBodies.computeIfAbsent(owner, ignored -> new HashMap<>()).put(record.id, control);
        return control;
    }

    private void prepare(EliteEntity elite, PetControl control) {
        LivingEntity body = elite.getLivingEntity();
        if (body == null) return;
        body.setRemoveWhenFarAway(false);
        // Babies are the same model at a fraction of the size; the boss file's scale still applies.
        AttributeInstance scale = body.getAttribute(Attribute.SCALE);
        if (scale != null)
            scale.setBaseValue(control.species().boss().getScale() * (control.isBaby() ? control.species().babyScale() : 1D));
        if (control.record() != null && control.record().name != null && !control.record().name.isBlank() && elite instanceof CustomBossEntity actor)
            actor.setName(ChatColorConverter.convert(control.record().name), true);
    }

    public void despawn(PetControl control, RemovalReason reason) {
        forget(control);
        CustomBossEntity actor = control.actor();
        if (actor != null && actor.exists()) actor.remove(reason);
    }

    /** Drops a pet from every map; the body may already be gone. */
    void forget(PetControl control) {
        CustomBossEntity actor = control.actor();
        if (actor != null) {
            byElite.remove(actor.getEliteUUID());
            LivingEntity body = actor.getLivingEntity();
            if (body != null) byBody.remove(body.getUniqueId());
        }
        if (control.ownerId() != null && control.record() != null) {
            Map<String, PetControl> owned = ownedBodies.get(control.ownerId());
            if (owned != null) owned.remove(control.record().id, control);
        }
    }

    private void unspawnOwned(UUID owner, PetRecord record) {
        Map<String, PetControl> owned = ownedBodies.get(owner);
        PetControl live = owned == null ? null : owned.get(record.id);
        if (live != null) despawn(live, RemovalReason.OTHER);
    }

    public PetControl liveBody(UUID owner, PetRecord record) {
        Map<String, PetControl> owned = ownedBodies.get(owner);
        return owned == null ? null : owned.get(record.id);
    }

    // ---- players ---------------------------------------------------------------------------------------------

    public void playerJoined(Player player) {
        UUID id = player.getUniqueId();
        Bukkit.getScheduler().runTaskAsynchronously(MetadataHandler.PLUGIN, () -> {
            PetCollection collection = PetCollection.load(id);
            Bukkit.getScheduler().runTask(MetadataHandler.PLUGIN, () -> {
                if (instance != this || !player.isOnline()) return;
                collections.put(id, collection);
                for (PetRecord record : collection.equipped()) spawnOwned(player, record);
            });
        });
    }

    public void playerLeft(Player player) {
        UUID id = player.getUniqueId();
        Map<String, PetControl> owned = ownedBodies.remove(id);
        if (owned != null) for (PetControl control : new ArrayList<>(owned.values())) despawn(control, RemovalReason.OTHER);
        collections.remove(id);
    }

    public boolean equip(Player player, PetRecord record) {
        PetCollection collection = collection(player.getUniqueId());
        int limit = PetsConfig.getMaxEquippedPets();
        if (limit > 0 && collection.equipped().size() >= limit && !record.equipped) {
            player.sendMessage(ChatColorConverter.convert(PetsConfig.getMaxEquippedMessage().replace("$amount", String.valueOf(limit))));
            return false;
        }
        record.equipped = true;
        collection.save();
        spawnOwned(player, record);
        player.sendMessage(ChatColorConverter.convert(PetsConfig.getEquippedMessage().replace("$pet", displayName(record))));
        return true;
    }

    public void unequip(Player player, PetRecord record) {
        record.equipped = false;
        collection(player.getUniqueId()).save();
        unspawnOwned(player.getUniqueId(), record);
        player.sendMessage(ChatColorConverter.convert(PetsConfig.getUnequippedMessage().replace("$pet", displayName(record))));
    }

    public String displayName(PetRecord record) {
        if (record.name != null && !record.name.isBlank()) return record.name;
        PetSpecies pet = species(record.species);
        String name = pet == null ? record.species : ChatColorConverter.convert(pet.boss().getName());
        return record.baby ? "Baby " + name : name;
    }

    // ---- interactions ----------------------------------------------------------------------------------------

    /**
     * A player right-clicked a pet. Wild adults are tamed with their species' food; owned pets are fed their breeding
     * food, or told to stay and to follow again by any other click, as a dog is. Returns whether the click was
     * consumed; a lead passes through to the vanilla leash.
     */
    public boolean interact(Player player, PetControl control) {
        long tick = com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock.currentTick();
        // A click reaches us from the model and from the body; one interaction per few ticks.
        if (control.lastInteractionTick() + 5 > tick) return true;
        control.setLastInteractionTick(tick);
        PetSpecies pet = control.species();
        ItemStack held = player.getInventory().getItemInMainHand();
        if (control.isWild()) {
            if (PetsConfig.getMode() != PetsConfig.Mode.CREATURES || control.isBaby() || !pet.isTamingFood(held)) return false;
            consume(player, held);
            playInteract(control);
            if (ThreadLocalRandom.current().nextDouble() < pet.tameChance()) {
                tame(player, control);
            } else {
                particles(control, Particle.SMOKE, 8);
                player.sendMessage(ChatColorConverter.convert(PetsConfig.getTameFailedMessage().replace("$pet", ChatColorConverter.convert(pet.boss().getName()))));
            }
            return true;
        }
        if (!control.isOwnedBy(player)) return true;
        if (!control.isBaby() && pet.isBreedingFood(held)) {
            if (!control.canBreed(tick)) {
                player.sendMessage(ChatColorConverter.convert(PetsConfig.getBreedingCooldownMessage().replace("$pet", displayName(control.record()))));
                return true;
            }
            consume(player, held);
            control.setLoveUntil(tick + pet.loveTicks());
            particles(control, Particle.HEART, 7);
            playInteract(control);
            tryBreed(control, tick);
            return true;
        }
        if (held.getType() == Material.LEAD) return false;
        // Anything else is the dog gesture: a click tells it to stay, the next one to follow again.
        if (pet.canStay()) {
            boolean stay = !control.isStaying();
            LivingEntity body = control.body();
            control.setStay(stay, body == null ? player.getLocation() : body.getLocation());
            player.sendMessage(ChatColorConverter.convert((stay ? PetsConfig.getStayMessage() : PetsConfig.getFollowMessage())
                    .replace("$pet", displayName(control.record()))));
        }
        playInteract(control);
        particles(control, Particle.HEART, 2);
        return true;
    }

    private void tame(Player player, PetControl wild) {
        PetSpecies pet = wild.species();
        LivingEntity body = wild.body();
        Location at = body == null ? player.getLocation() : body.getLocation();
        PetRecord record = new PetRecord(pet.id(), wild.model(), false, System.currentTimeMillis());
        PetCollection collection = collection(player.getUniqueId());
        int limit = PetsConfig.getMaxEquippedPets();
        if (limit > 0 && collection.equipped().size() >= limit) record.equipped = false;
        collection.add(record);
        particles(wild, Particle.HEART, 12);
        despawn(wild, RemovalReason.OTHER);
        if (record.equipped) {
            PetControl owned = spawn(pet, player.getUniqueId(), record, record.model, false, at, null);
            if (owned != null) playInteract(owned);
        }
        player.sendMessage(ChatColorConverter.convert(PetsConfig.getTamedMessage().replace("$pet", displayName(record))));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.6F, 1.4F);
    }

    private void tryBreed(PetControl first, long tick) {
        if (!first.isInLove(tick)) return;
        Map<String, PetControl> owned = ownedBodies.get(first.ownerId());
        if (owned == null) return;
        LivingEntity body = first.body();
        if (body == null) return;
        for (PetControl second : owned.values()) {
            if (second == first || second.species() != first.species() || !second.isInLove(tick) || second.isBaby()) continue;
            LivingEntity other = second.body();
            if (other == null || other.getWorld() != body.getWorld() || other.getLocation().distance(body.getLocation()) > BREED_DISTANCE) continue;
            breed(first, second, tick);
            return;
        }
    }

    private void breed(PetControl mother, PetControl father, long tick) {
        PetSpecies pet = mother.species();
        Player owner = mother.owner().orElse(null);
        if (owner == null) return;
        mother.endLove();
        father.endLove();
        mother.setBreedCooldownUntil(tick + pet.breedingCooldownTicks());
        father.setBreedCooldownUntil(tick + pet.breedingCooldownTicks());
        long cooldownMs = System.currentTimeMillis() + pet.breedingCooldownTicks() * 50L;
        if (mother.record() != null) mother.record().breedCooldownUntil = cooldownMs;
        if (father.record() != null) father.record().breedCooldownUntil = cooldownMs;
        // The baby takes one parent's colouring, as Minecraft's variant-bearing animals do.
        String model = ThreadLocalRandom.current().nextBoolean() ? mother.model() : father.model();
        PetRecord baby = new PetRecord(pet.id(), model, true, System.currentTimeMillis());
        PetCollection collection = collection(owner.getUniqueId());
        int limit = PetsConfig.getMaxEquippedPets();
        if (limit > 0 && collection.equipped().size() >= limit) baby.equipped = false;
        collection.add(baby);
        particles(mother, Particle.HEART, 10);
        particles(father, Particle.HEART, 10);
        if (baby.equipped) {
            LivingEntity body = mother.body();
            Location at = body == null ? owner.getLocation() : body.getLocation();
            spawn(pet, owner.getUniqueId(), baby, model, true, at, null);
        }
        owner.sendMessage(ChatColorConverter.convert(PetsConfig.getEquippedMessage().replace("$pet", displayName(baby))));
    }

    private void consume(Player player, ItemStack held) {
        player.playSound(player.getLocation(), Sound.ENTITY_GENERIC_EAT, 1F, 1F);
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        held.setAmount(held.getAmount() - 1);
    }

    public void playInteract(PetControl control) {
        CustomBossEntity actor = control.actor();
        if (actor != null && actor.getCustomModel() != null) actor.getCustomModel().playAnimationByName("interact");
    }

    private void particles(PetControl control, Particle particle, int count) {
        LivingEntity body = control.body();
        if (body == null) return;
        Location at = body.getLocation().add(0D, body.getHeight() * 0.5D, 0D);
        body.getWorld().spawnParticle(particle, at, count, 0.5D, 0.5D, 0.5D, 0D);
    }

    /** A pet body died or was removed by something other than this manager. */
    public void bodyGone(PetControl control, boolean died) {
        forget(control);
        if (!died || control.record() == null || control.ownerId() == null) return;
        PetCollection collection = collection(control.ownerId());
        if (PetsConfig.isPetsDiePermanently()) collection.remove(control.record());
        else {
            control.record().equipped = false;
            collection.save();
        }
    }

    // ---- watchdog ---------------------------------------------------------------------------------------------

    private void watch() {
        long now = System.currentTimeMillis();
        long tick = com.magmaguy.elitemobs.advancedcombat.MonotonicTickClock.currentTick();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PetCollection collection = collections.get(player.getUniqueId());
            if (collection == null) continue;
            for (PetRecord record : collection.equipped()) {
                PetSpecies pet = species(record.species);
                if (pet == null) continue;
                if (record.baby && now >= record.bornAt + pet.babyGrowthTicks() * 50L) {
                    record.baby = false;
                    collection.save();
                    spawnOwned(player, record);
                    continue;
                }
                PetControl live = liveBody(player.getUniqueId(), record);
                LivingEntity body = live == null ? null : live.body();
                // Lost to a chunk unload, a world change or a death: bring the pet back beside its owner.
                if (body == null || !body.isValid() || body.getWorld() != player.getWorld()) {
                    if (live != null) forget(live);
                    spawnOwned(player, record);
                }
            }
        }
        double despawnDistance = PetsConfig.getWildDespawnDistance();
        for (PetControl control : new ArrayList<>(byElite.values())) {
            if (!control.isWild()) continue;
            LivingEntity body = control.body();
            if (body == null || !body.isValid()) {
                forget(control);
                continue;
            }
            if (PetsConfig.getMode() != PetsConfig.Mode.CREATURES || nearestPlayerDistance(body.getLocation()) > despawnDistance)
                despawn(control, RemovalReason.OTHER);
        }
        if (wildSpawnerInterval != PetsConfig.getWildSpawnIntervalTicks()) scheduleWildSpawner();
    }

    private static double nearestPlayerDistance(Location location) {
        double nearest = Double.POSITIVE_INFINITY;
        for (Player player : location.getWorld().getPlayers())
            nearest = Math.min(nearest, player.getLocation().distance(location));
        return nearest;
    }

    // ---- wild spawning ----------------------------------------------------------------------------------------

    private void scheduleWildSpawner() {
        if (wildSpawner != null) wildSpawner.cancel();
        wildSpawnerInterval = Math.max(20, PetsConfig.getWildSpawnIntervalTicks());
        wildSpawner = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, this::spawnWildAroundPlayers, wildSpawnerInterval, wildSpawnerInterval);
    }

    private void spawnWildAroundPlayers() {
        if (PetsConfig.getMode() != PetsConfig.Mode.CREATURES) return;
        List<PetSpecies> candidates = new ArrayList<>();
        for (PetSpecies pet : species.values()) if (pet.wildSpawn().enabled()) candidates.add(pet);
        if (candidates.isEmpty()) return;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ThreadLocalRandom.current().nextDouble() >= PetsConfig.getWildSpawnChancePerPlayer()) continue;
            if (wildNear(player.getLocation(), 128D) >= PetsConfig.getWildPetCapPerPlayer()) continue;
            PetSpecies pet = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
            if (ThreadLocalRandom.current().nextDouble() >= pet.wildSpawn().chance()) continue;
            if (wildNear(player.getLocation(), 128D, pet) >= pet.wildSpawn().cap()) continue;
            Location at = wildSpawnLocation(pet, player);
            if (at != null) spawnWild(pet, at);
        }
    }

    private int wildNear(Location location, double radius, PetSpecies only) {
        int count = 0;
        for (PetControl control : byElite.values()) {
            if (!control.isWild() || (only != null && control.species() != only)) continue;
            LivingEntity body = control.body();
            if (body != null && body.getWorld() == location.getWorld() && body.getLocation().distance(location) <= radius) count++;
        }
        return count;
    }

    private int wildNear(Location location, double radius) {
        return wildNear(location, radius, null);
    }

    /** A spot from the species' custom spawn rules, or a surface point 24 to 48 blocks from the player. */
    private Location wildSpawnLocation(PetSpecies pet, Player player) {
        String customSpawn = pet.wildSpawn().customSpawn();
        if (customSpawn != null && !customSpawn.isBlank()) {
            CustomSpawn spawn = new CustomSpawn(customSpawn, new CustomBossEntity(pet.boss()));
            Location at = spawn.generateRandomSpawnLocation();
            spawn.setKeepTrying(false);
            if (at != null) return at;
            return null;
        }
        World world = player.getWorld();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        double angle = random.nextDouble() * Math.PI * 2D;
        double distance = 24D + random.nextDouble() * 24D;
        Location at = player.getLocation().clone().add(Math.cos(angle) * distance, 0D, Math.sin(angle) * distance);
        if (!world.isChunkLoaded(at.getBlockX() >> 4, at.getBlockZ() >> 4)) return null;
        at.setY(world.getHighestBlockYAt(at) + 1D);
        return at;
    }
}
