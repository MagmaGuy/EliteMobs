package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.items.upgradesystem.EnchantmentAcquisition;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.dungeons.WorldDungeonPackage;
import com.magmaguy.elitemobs.instanced.WorldOperationQueue;
import com.magmaguy.elitemobs.menus.ItemEnchantmentMenu;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantmentDungeonInstance extends DungeonInstance {
    @Getter
    @Setter
    Player player;
    private EnchantmentAcquisition acquisition;
    private BukkitTask acquisitionMonitor;
    private boolean challengeResolved = false;

    public EnchantmentDungeonInstance(ContentPackagesConfigFields contentPackagesConfigFields,
                                      Location lobbyLocation,
                                      Location startLocation,
                                      World world,
                                      Player player,
                                      String difficultyName) {
        super(contentPackagesConfigFields,
                lobbyLocation,
                startLocation,
                world,
                player,
                difficultyName);
        this.player = player;
    }

    public static boolean setupRandomEnchantedChallengeDungeon(Player player, EnchantmentAcquisition acquisition) {
        List<ContentPackagesConfigFields> contentPackagesConfigFieldsList = new ArrayList<>();
        WorldDungeonPackage.getEmPackages().values().stream().forEach(emPackage -> {if (emPackage.isInstalled() && emPackage.getContentPackagesConfigFields().isEnchantmentChallenge()) contentPackagesConfigFieldsList.add(emPackage.getContentPackagesConfigFields());});
        if (contentPackagesConfigFieldsList.isEmpty()) {
            player.sendMessage(DungeonsConfig.getEnchantNoChallengeMessage());
            return false;
        }
        ContentPackagesConfigFields contentPackagesConfigFields = contentPackagesConfigFieldsList.get(ThreadLocalRandom.current().nextInt(0, contentPackagesConfigFieldsList.size()));
        String instancedWordName = WorldInstantiator.getNewWorldName(contentPackagesConfigFields.getWorldName());

        if (!launchEvent(contentPackagesConfigFields, instancedWordName, player)) return false;

        boolean[] accepted = {false};
        WorldOperationQueue.queueOperation(
                player,
                () -> cloneWorldFiles(contentPackagesConfigFields, instancedWordName) != null,
                () -> {
                    if (!player.isOnline() || MetadataHandler.shutdownRequested || !acquisition.isOwned()) return;
                    acquisition.validateProviders();
                    DungeonInstance instance = initializeInstancedWorld(contentPackagesConfigFields, instancedWordName,
                            player, (String) contentPackagesConfigFields.getDifficulties().get(0).get("name"));
                    if (instance instanceof EnchantmentDungeonInstance challenge && !challenge.isDefunct()) {
                        challenge.accept(acquisition);
                        accepted[0] = true;
                    } else if (instance != null) instance.removeInstance();
                },
                contentPackagesConfigFields.getName(),
                () -> { if (!accepted[0]) acquisition.abort("challenge startup failed or was cancelled"); });
        return true;
    }

    private void accept(EnchantmentAcquisition acquisition) {
        this.acquisition = acquisition;
        acquisitionMonitor = Bukkit.getScheduler().runTaskTimer(MetadataHandler.PLUGIN, () -> {
            try { acquisition.validateProviders(); }
            catch (RuntimeException invalid) { removeInstance(); }
        }, 20L, 20L);
    }

    @Override
    protected void cancelScheduledTasks() {
        if (acquisitionMonitor != null) { acquisitionMonitor.cancel(); acquisitionMonitor = null; }
        super.cancelScheduledTasks();
    }

    @Override
    public void removeInstance() {
        if (acquisition != null && !challengeResolved) {
            challengeResolved = true;
            acquisition.abort("challenge infrastructure or provider lifetime ended before resolution");
        }
        super.removeInstance();
    }

    /**
     * Enchantment challenges escrow one player's item and therefore remain private after their
     * owner is admitted by the constructor. They must never become a party carry or accept a
     * browser join while the challenge is waiting to start.
     */
    @Override
    public boolean addNewPlayer(Player player) {
        return false;
    }

    @Override
    public boolean requestPartyEntry(Player player) {
        return false;
    }

    @Override
    public void endMatch() {
        if (players.isEmpty()) {
            removeInstance();
            return;
        }
        //Enchantment challenges tear down quickly; route through the cancellable destroyMatchTask slot instead
        //of a fire-and-forget BukkitRunnable so teardown can cancel it (the old runnable pinned the instance
        //in CraftScheduler.pending for its whole delay -- the exact leak shape from the heap dump).
        scheduleDelayedDestroy(20 * 10L);
    }

    @Override
    protected void victory() {
        if (!markChallengeResolved()) return;
        acquisition.success();
        super.victory();
        player.sendMessage(DungeonsConfig.getEnchantChallengeCompleteMessage());
        player.sendMessage(DungeonsConfig.getEnchantChallengeSuccessMessage());
        ItemEnchantmentMenu.broadcastEnchantmentMessage(acquisition.upgraded(), player, SpecialItemSystemsConfig.getSuccessAnnouncement());
    }

    @Override
    protected void defeat() {
        if (!markChallengeResolved()) return;
        boolean destroyed = ThreadLocalRandom.current().nextDouble() < SpecialItemSystemsConfig.getCriticalFailureChanceDuringChallengeChance();
        if (destroyed) acquisition.criticalFailure(); else acquisition.failure();
        super.defeat();
        if (destroyed) { // Preserve the existing intentional challenge-defeat loss probability.
            player.sendMessage(DungeonsConfig.getEnchantCriticalFailureMessage().replace("$item", acquisition.original().getItemMeta().getDisplayName()));
            ItemEnchantmentMenu.broadcastEnchantmentMessage(acquisition.upgraded(), player, SpecialItemSystemsConfig.getCriticalFailureAnnouncement());
        } else {
            player.sendMessage(DungeonsConfig.getEnchantChallengeFailedMessage().replace("$item", acquisition.original().getItemMeta().getDisplayName()));
        }
    }

    private boolean markChallengeResolved() {
        if (challengeResolved || acquisition == null) return false;
        try { acquisition.validateProviders(); }
        catch (RuntimeException invalid) { removeInstance(); return false; }
        challengeResolved = true;
        if (acquisitionMonitor != null) { acquisitionMonitor.cancel(); acquisitionMonitor = null; }
        return true;
    }

}
