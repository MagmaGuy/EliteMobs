package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.config.DungeonsConfig;
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
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class EnchantmentDungeonInstance extends DungeonInstance {
    @Getter
    @Setter
    Player player;
    @Getter
    @Setter
    private ItemStack upgradedItem;
    @Getter
    @Setter
    private ItemStack currentItem;
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

    public static boolean setupRandomEnchantedChallengeDungeon(Player player, ItemStack upgradedItem, ItemStack itemFromInventory) {
        List<ContentPackagesConfigFields> contentPackagesConfigFieldsList = new ArrayList<>();
        WorldDungeonPackage.getEmPackages().values().stream().forEach(emPackage -> {if (emPackage.isInstalled() && emPackage.getContentPackagesConfigFields().isEnchantmentChallenge()) contentPackagesConfigFieldsList.add(emPackage.getContentPackagesConfigFields());});
        if (contentPackagesConfigFieldsList.isEmpty()) {
            player.sendMessage(DungeonsConfig.getEnchantNoChallengeMessage());
            return false;
        }
        ContentPackagesConfigFields contentPackagesConfigFields = contentPackagesConfigFieldsList.get(ThreadLocalRandom.current().nextInt(0, contentPackagesConfigFieldsList.size()));
        String instancedWordName = WorldInstantiator.getNewWorldName(contentPackagesConfigFields.getWorldName());

        if (!launchEvent(contentPackagesConfigFields, instancedWordName, player)) return false;

        // Clone single-item snapshots before passing to lambda to avoid later stack mutation.
        ItemStack upgradedItemClone = cloneSingleItem(upgradedItem);
        ItemStack currentItemClone = cloneSingleItem(itemFromInventory);

        WorldOperationQueue.queueOperation(
                player,
                () -> cloneWorldFiles(contentPackagesConfigFields, instancedWordName, player) != null,
                () -> {
                    DungeonInstance dungeonInstance = initializeInstancedWorld(contentPackagesConfigFields, instancedWordName, player, (String) contentPackagesConfigFields.getDifficulties().get(0).get("name"));
                    if (dungeonInstance instanceof EnchantmentDungeonInstance enchantmentDungeonInstance) {
                        enchantmentDungeonInstance.setUpgradedItem(upgradedItemClone);
                        enchantmentDungeonInstance.setCurrentItem(currentItemClone);
                    }
                },
                contentPackagesConfigFields.getName()
        );

        return true;
    }

    private static ItemStack cloneSingleItem(ItemStack itemStack) {
        ItemStack clone = itemStack.clone();
        clone.setAmount(1);
        return clone;
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
        super.victory();
        player.sendMessage(DungeonsConfig.getEnchantChallengeCompleteMessage());
        player.sendMessage(DungeonsConfig.getEnchantChallengeSuccessMessage());
        ItemEnchantmentMenu.broadcastEnchantmentMessage(upgradedItem, player, SpecialItemSystemsConfig.getSuccessAnnouncement());
        HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(upgradedItem);
        if (!leftOvers.isEmpty())
            leftOvers.values().forEach(leftover -> player.getWorld().dropItem(player.getLocation(), leftover));
    }

    @Override
    protected void defeat() {
        if (!markChallengeResolved()) return;
        super.defeat();
        if (ThreadLocalRandom.current().nextDouble() < SpecialItemSystemsConfig.getCriticalFailureChanceDuringChallengeChance()) {
            player.sendMessage(DungeonsConfig.getEnchantCriticalFailureMessage().replace("$item", currentItem.getItemMeta().getDisplayName()));
            ItemEnchantmentMenu.broadcastEnchantmentMessage(upgradedItem, player, SpecialItemSystemsConfig.getCriticalFailureAnnouncement());
        } else {
            player.sendMessage(DungeonsConfig.getEnchantChallengeFailedMessage().replace("$item", currentItem.getItemMeta().getDisplayName()));
            HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(currentItem);
            if (!leftOvers.isEmpty())
                leftOvers.values().forEach(leftover -> player.getWorld().dropItem(player.getLocation(), leftover));
        }
    }

    private boolean markChallengeResolved() {
        if (challengeResolved) return false;
        challengeResolved = true;
        return true;
    }

}
