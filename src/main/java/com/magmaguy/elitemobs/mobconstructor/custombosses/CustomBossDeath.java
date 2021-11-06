package com.magmaguy.elitemobs.mobconstructor.custombosses;

import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.api.EliteMobDeathEvent;
import com.magmaguy.elitemobs.config.MobCombatSettingsConfig;
import com.magmaguy.elitemobs.config.VanillaItemDrop;
import com.magmaguy.elitemobs.config.custombosses.CustomBossesConfigFields;
import com.magmaguy.elitemobs.items.customitems.CustomItem;
import com.magmaguy.elitemobs.items.itemconstructor.SpecialLoot;
import com.magmaguy.elitemobs.ondeathcommands.OnDeathCommands;
import com.magmaguy.elitemobs.thirdparty.discordsrv.DiscordSRVAnnouncement;
import com.magmaguy.elitemobs.utils.Round;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CustomBossDeath implements Listener {

    private static void doLoot(CustomBossEntity customBossEntity) {
        for (Player player : customBossEntity.getDamagers().keySet())
            if (!customBossEntity.isTriggeredAntiExploit())
                dropLoot(player, customBossEntity);
    }

    public static void dropLoot(Player player, CustomBossEntity customBossEntity) {
        if (customBossEntity.customBossesConfigFields.getUniqueLootList().isEmpty()) return;
        for (CustomBossesConfigFields.UniqueLoot uniqueLoot : customBossEntity.customBossesConfigFields.getParsedUniqueLootList())
            if (ThreadLocalRandom.current().nextDouble() < uniqueLoot.chance)
                CustomItem.dropPlayerLoot(player, customBossEntity.getLevel(), uniqueLoot.customItem.getFileName(), customBossEntity.getLocation());
        for (SpecialLoot specialLoot : customBossEntity.customBossesConfigFields.getSpecialLoot().values())
            if (ThreadLocalRandom.current().nextDouble() < specialLoot.getChance())
                customBossEntity.getLocation().getWorld().dropItem(customBossEntity.getLocation(),
                        specialLoot.generateItemStack(player, Math.max(customBossEntity.getLevel() - 3, 1), customBossEntity.getLevel() + 3));
            for (VanillaItemDrop vanillaItemDrop : customBossEntity.customBossesConfigFields.getParsedVanillaLootList())
                if (ThreadLocalRandom.current().nextDouble() < vanillaItemDrop.getChance())
                    customBossEntity.getLocation().getWorld().dropItem(customBossEntity.getLocation(), vanillaItemDrop.getItemStack());
    }

    private static void doDeathMessage(CustomBossEntity customBossEntity) {
        //Do death message
        String playersList = "";
        for (Player player : customBossEntity.getDamagers().keySet()) {
            if (playersList.isEmpty())
                playersList += player.getDisplayName();
            else
                playersList += ", &f" + player.getDisplayName();
        }

        playersList = ChatColorConverter.convert(playersList);

        if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() > 0 && customBossEntity.hasDamagers())
            if (customBossEntity.customBossesConfigFields.getDeathMessages() != null &&
                    customBossEntity.customBossesConfigFields.getDeathMessages().size() > 0) {
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

                for (String string : customBossEntity.customBossesConfigFields.getDeathMessages()) {
                    if (string.contains("$damager1name"))
                        if (topDamager != null)
                            string = string.replace("$damager1name", topDamager.getDisplayName());
                        else
                            string = "";
                    if (string.contains("$damager1damage"))
                        if (topDamager != null)
                            string = string.replace("$damager1damage",
                                    Round.twoDecimalPlaces(customBossEntity.getDamagers().get(topDamager)) + "");
                        else
                            string = "";
                    if (string.contains("$damager2name"))
                        if (secondDamager != null)
                            string = string.replace("$damager2name", secondDamager.getDisplayName());
                        else
                            string = "";
                    if (string.contains("$damager2damage"))
                        if (secondDamager != null)
                            string = string.replace("$damager2damage",
                                    Round.twoDecimalPlaces(customBossEntity.getDamagers().get(secondDamager)) + "");
                        else
                            string = "";
                    if (string.contains("$damager3name"))
                        if (thirdDamager != null)
                            string = string.replace("$damager3name", thirdDamager.getDisplayName());
                        else
                            string = "";
                    if (string.contains("$damager3damage"))
                        if (thirdDamager != null)
                            string = string.replace("$damager3damage",
                                    Round.twoDecimalPlaces(customBossEntity.getDamagers().get(thirdDamager)) + "");
                        else
                            string = "";
                    if (string.contains("$players"))
                        string = string.replace("$players", playersList);
                    Bukkit.broadcastMessage(ChatColorConverter.convert(string));
                    if (string.length() > 0)
                        if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() > 2)
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
                if (customBossEntity.customBossesConfigFields.getDeathMessage() != null) {
                    if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() > 0)
                        Bukkit.broadcastMessage(ChatColorConverter.convert(
                                customBossEntity.customBossesConfigFields.getDeathMessage().replace("$players", playersList)));
                    if (customBossEntity.customBossesConfigFields.getAnnouncementPriority() > 2)
                        new DiscordSRVAnnouncement(ChatColorConverter.convert(
                                customBossEntity.customBossesConfigFields.getDeathMessage().replace("$players", playersList)));
                }
            }
    }

    //sorts damagers
    private static HashMap<Player, Double> sortByComparator(HashMap<Player, Double> unsortMap, final boolean order) {

        List<Map.Entry<Player, Double>> list = new LinkedList<Map.Entry<Player, Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Map.Entry<Player, Double>>() {
            public int compare(Map.Entry<Player, Double> o1,
                               Map.Entry<Player, Double> o2) {
                if (order)
                    return o1.getValue().compareTo(o2.getValue());
                else
                    return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Maintaining insertion order with the help of LinkedList
        HashMap<Player, Double> sortedMap = new LinkedHashMap<Player, Double>();
        for (Map.Entry<Player, Double> entry : list)
            sortedMap.put(entry.getKey(), entry.getValue());

        return sortedMap;
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onEliteMobDeath(EliteMobDeathEvent event) {
        if (!(event.getEliteEntity() instanceof CustomBossEntity)) return;
        CustomBossEntity customBossEntity = (CustomBossEntity) event.getEliteEntity();

        doLoot(customBossEntity);
        doDeathMessage(customBossEntity);

        //untrack from trackable custom bosses
        CustomBossEntity.trackableCustomBosses.remove(customBossEntity);

        //todo this should be moved to the death event
        if (customBossEntity.customBossesConfigFields.getOnDeathCommands() != null &&
                !customBossEntity.customBossesConfigFields.getOnDeathCommands().isEmpty())
            OnDeathCommands.parseConsoleCommand(customBossEntity.customBossesConfigFields.getOnDeathCommands(), event);

        //todo this should be set by the elite entity not the custom boss
        if (!customBossEntity.customBossesConfigFields.isDropsVanillaLoot()) {
            event.getEntityDeathEvent().setDroppedExp(0);
            for (ItemStack itemStack : event.getEntityDeathEvent().getDrops())
                itemStack.setAmount(0);
        }

    }

}
