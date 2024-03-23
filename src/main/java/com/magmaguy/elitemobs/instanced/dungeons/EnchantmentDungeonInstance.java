package com.magmaguy.elitemobs.instanced.dungeons;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.SpecialItemSystemsConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfig;
import com.magmaguy.elitemobs.config.dungeonpackager.DungeonPackagerConfigFields;
import com.magmaguy.elitemobs.menus.ItemEnchantmentMenu;
import com.magmaguy.elitemobs.utils.WorldInstantiator;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

@Setter
@Getter
public class EnchantmentDungeonInstance extends DungeonInstance {
    Player player;
    private ItemStack upgradedItem;
    private ItemStack currentItem;

    public EnchantmentDungeonInstance(DungeonPackagerConfigFields dungeonPackagerConfigFields,
                                      Location lobbyLocation,
                                      Location startLocation,
                                      World world,
                                      File instancedWorldFile,
                                      Player player,
                                      String difficultyName) {
        super(dungeonPackagerConfigFields,
                lobbyLocation,
                startLocation,
                world,
                instancedWorldFile,
                player,
                difficultyName);
        this.player = player;
    }

    public static boolean setupRandomEnchantedChallengeDungeon(Player player, ItemStack upgradedItem, ItemStack itemFromInventory) {
        List<DungeonPackagerConfigFields> dungeonPackagerConfigFieldsList = new ArrayList<>(DungeonPackagerConfig.getEnchantedChallengeDungeonPackages().values().stream().toList());
        dungeonPackagerConfigFieldsList.removeIf(dungeonPackagerConfigFields -> !dungeonPackagerConfigFields.isEnabled());
        if (dungeonPackagerConfigFieldsList.isEmpty()) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cYou rolled challenge but your server has not installed any challenge dungeons! &2This will count as an automatic enchantment success."));
            return false;
        }
        DungeonPackagerConfigFields dungeonPackagerConfigFields = dungeonPackagerConfigFieldsList.get(ThreadLocalRandom.current().nextInt(0, dungeonPackagerConfigFieldsList.size()));
        String instancedWordName = WorldInstantiator.getNewWorldName(dungeonPackagerConfigFields.getWorldName());

        if (!launchEvent(dungeonPackagerConfigFields, instancedWordName, player)) return false;

        CompletableFuture<File> future = CompletableFuture.supplyAsync(() ->
                cloneWorldFiles(dungeonPackagerConfigFields, instancedWordName, player));
        future.thenAccept(file -> {
            if (file == null) return;
            new BukkitRunnable() {
                @Override
                public void run() {
                    DungeonInstance dungeonInstance = initializeInstancedWorld(dungeonPackagerConfigFields, instancedWordName, player, file, (String) dungeonPackagerConfigFields.getDifficulties().get(0).get("name"));
                    if (dungeonInstance instanceof EnchantmentDungeonInstance enchantmentDungeonInstance) {
                        enchantmentDungeonInstance.setUpgradedItem(upgradedItem.clone());
                        enchantmentDungeonInstance.setCurrentItem(itemFromInventory.clone());
                    }
                }
            }.runTask(MetadataHandler.PLUGIN);
        });

        return true;
    }

    @Override
    public void endMatch() {
        new BukkitRunnable() {
            @Override
            public void run() {
                removeInstance();
            }
        }.runTaskLater(MetadataHandler.PLUGIN, 20 * 10);
    }

    @Override
    protected void victory() {
        super.victory();
        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2Challenge complete! Instance will close in 10 seconds. &6You can leave earlier by doing &9/em quit&6!"));
        player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &2You succeeded your item enchantment challenge! Your item has been successfully enchanted."));
        ItemEnchantmentMenu.broadcastEnchantmentMessage(upgradedItem, player, SpecialItemSystemsConfig.getSuccessAnnouncement());
        //scanCurrentItemForRemoval();
        HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(upgradedItem);
        if (!leftOvers.isEmpty()) player.getWorld().dropItem(player.getLocation(), upgradedItem);
    }

    @Override
    protected void defeat() {
        super.defeat();
        if (ThreadLocalRandom.current().nextDouble() < SpecialItemSystemsConfig.getCriticalFailureChanceDuringChallengeChance()) {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &4Critical enchantment failure! You have lost " + currentItem.getItemMeta().getDisplayName() + " &c!"));
            ItemEnchantmentMenu.broadcastEnchantmentMessage(upgradedItem, player, SpecialItemSystemsConfig.getCriticalFailureAnnouncement());
            //scanCurrentItemForRemoval();
        } else {
            player.sendMessage(ChatColorConverter.convert("&8[EliteMobs] &cYou have failed the enchantment challenge! Your item " + currentItem.getItemMeta().getDisplayName() + " &cwas not enchanted."));
            HashMap<Integer, ItemStack> leftOvers = player.getInventory().addItem(currentItem);
            if (!leftOvers.isEmpty()) player.getWorld().dropItem(player.getLocation(), currentItem);
        }
    }

    /*
    private void scanCurrentItemForRemoval() {
        if (player.getInventory().contains(currentItem))
            player.getInventory().remove(currentItem);
        else if (player.getInventory().getHelmet() != null && player.getInventory().getHelmet().equals(currentItem))
            player.getInventory().setHelmet(null);
        else if (player.getInventory().getChestplate() != null && player.getInventory().getChestplate().equals(currentItem))
            player.getInventory().setChestplate(null);
        else if (player.getInventory().getLeggings() != null && player.getInventory().getLeggings().equals(currentItem))
            player.getInventory().setLeggings(null);
        else if (player.getInventory().getBoots() != null && player.getInventory().getBoots().equals(currentItem))
            player.getInventory().setBoots(null);
        else if (player.getInventory().getItemInOffHand().equals(currentItem))
            player.getInventory().setItemInOffHand(null);
    }
     */

}
