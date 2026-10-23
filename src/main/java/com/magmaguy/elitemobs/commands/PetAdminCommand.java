package com.magmaguy.elitemobs.commands;

import com.magmaguy.elitemobs.config.PetsConfig;
import com.magmaguy.elitemobs.pets.PetCollection;
import com.magmaguy.elitemobs.pets.PetControl;
import com.magmaguy.elitemobs.pets.PetManager;
import com.magmaguy.elitemobs.pets.PetRecord;
import com.magmaguy.elitemobs.pets.PetSpecies;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.command.arguments.DynamicListStringCommandArgument;
import com.magmaguy.magmacore.command.arguments.ListStringCommandArgument;
import com.magmaguy.magmacore.util.Logger;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

/**
 * Admin and testing switches for pets: {@code mode creatures|cosmetic} picks whether species live wild in the
 * world, {@code spawn <species>} puts a wild one where you look, {@code give <species>} adds an owned one to your
 * collection, {@code list} shows the loaded species.
 */
public class PetAdminCommand extends AdvancedCommand {
    public PetAdminCommand() {
        super(List.of("petadmin"));
        addArgument("action", new ListStringCommandArgument(List.of("mode", "spawn", "give", "list"), "<action>"));
        addOptionalArgument("value", new DynamicListStringCommandArgument(() -> {
            PetManager manager = PetManager.get();
            List<String> values = new java.util.ArrayList<>(List.of("creatures", "cosmetic"));
            if (manager != null) manager.species().forEach(species -> values.add(species.id()));
            return values;
        }, "[value]"));
        setUsage("/em petadmin <mode|spawn|give|list> [value]");
        setPermission("elitemobs.pets.admin");
        setSenderType(SenderType.PLAYER);
        setDescription("Pet mode switch and testing spawns.");
    }

    @Override
    public void execute(CommandData commandData) {
        Player player = commandData.getPlayerSender();
        PetManager manager = PetManager.get();
        if (manager == null) {
            Logger.sendMessage(player, "&cNo pet species are loaded; the Mind runtime may be unavailable on this server version.");
            return;
        }
        String action = commandData.getStringArgument("action").toLowerCase(Locale.ROOT);
        String value = commandData.getStringArgument("value");
        switch (action) {
            case "mode" -> {
                if (value == null) {
                    Logger.sendMessage(player, "&7Pet mode is &f" + PetsConfig.getMode().name().toLowerCase(Locale.ROOT));
                    return;
                }
                try {
                    PetsConfig.setModeAndSave(PetsConfig.Mode.valueOf(value.toUpperCase(Locale.ROOT)));
                    Logger.sendMessage(player, "&aPet mode is now &f" + PetsConfig.getMode().name().toLowerCase(Locale.ROOT));
                } catch (IllegalArgumentException ignored) {
                    Logger.sendMessage(player, "&cUse creatures or cosmetic.");
                }
            }
            case "list" -> {
                for (PetSpecies species : manager.species())
                    Logger.sendMessage(player, "&f" + species.id() + " &7(" + species.boss().getFilename() + ", model " + species.boss().getCustomModel()
                            + ", tame with " + species.tamingFood() + ", breed with " + species.breedingFood() + ")");
                if (manager.species().isEmpty()) Logger.sendMessage(player, "&7No pet species are loaded.");
            }
            case "spawn" -> {
                PetSpecies species = manager.species(value);
                if (species == null) {
                    Logger.sendMessage(player, "&cUnknown pet species. /em petadmin list shows them.");
                    return;
                }
                if (PetsConfig.getMode() != PetsConfig.Mode.CREATURES) {
                    Logger.sendMessage(player, "&cWild pets only exist in creatures mode. &7/em petadmin mode creatures &cfirst.");
                    return;
                }
                PetControl spawned = manager.spawnWild(species, player.getTargetBlock(null, 12).getLocation().add(0.5D, 1D, 0.5D));
                Logger.sendMessage(player, spawned == null ? "&cThe pet could not be spawned; see the console." : "&aSpawned a wild " + species.id() + ".");
            }
            case "give" -> {
                PetSpecies species = manager.species(value);
                if (species == null) {
                    Logger.sendMessage(player, "&cUnknown pet species. /em petadmin list shows them.");
                    return;
                }
                PetRecord record = new PetRecord(species.id(), species.pickVariant(java.util.concurrent.ThreadLocalRandom.current()), false, System.currentTimeMillis());
                PetCollection collection = manager.collection(player.getUniqueId());
                collection.add(record);
                manager.equip(player, record);
            }
            default -> Logger.sendMessage(player, "&cUse mode, spawn, give or list.");
        }
    }
}
