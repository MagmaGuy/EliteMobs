package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.ReloadCommand;
import com.magmaguy.elitemobs.config.CommandMessagesConfig;
import com.magmaguy.elitemobs.dungeons.EMPackage;
import com.magmaguy.magmacore.command.AdvancedCommand;
import com.magmaguy.magmacore.command.CommandData;
import com.magmaguy.magmacore.command.SenderType;
import com.magmaguy.magmacore.nightbreak.NightbreakAccount;
import com.magmaguy.magmacore.nightbreak.NightbreakContentManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class UpdateContentCommand extends AdvancedCommand {

    public UpdateContentCommand() {
        super(List.of("updatecontent", "updateall"));
        setPermission("elitemobs.updatecontent");
        setSenderType(SenderType.ANY);
        setDescription("Updates all outdated content packages via Nightbreak");
        setUsage("/em updatecontent");
    }

    @Override
    public void execute(CommandData commandData) {
        CommandSender sender = commandData.getCommandSender();

        if (!NightbreakAccount.hasToken()) {
            sender.sendMessage(
                CommandMessagesConfig.getUpdateNoTokenMessage());
            return;
        }

        // Find all outdated packages with Nightbreak slugs
        // Deduplicate by slug so each unique slug is only downloaded once
        List<EMPackage> outdatedWithSlugs = new ArrayList<>();
        Set<String> seenSlugs = new HashSet<>();
        for (EMPackage pkg : EMPackage.getEmPackages().values()) {
            if (pkg.isOutOfDate()) {
                String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
                if (slug != null && !slug.isEmpty() && seenSlugs.add(slug)) {
                    outdatedWithSlugs.add(pkg);
                }
            }
        }

        if (outdatedWithSlugs.isEmpty()) {
            sender.sendMessage(
                CommandMessagesConfig.getUpdateAllUpToDateMessage());
            return;
        }

        sender.sendMessage(
            CommandMessagesConfig.getUpdateFoundOutdatedMessage().replace("$count", String.valueOf(outdatedWithSlugs.size())));

        Player player = sender instanceof Player ? (Player) sender : null;
        File importsFolder = new File(MetadataHandler.PLUGIN.getDataFolder(), "imports");
        if (!importsFolder.exists()) {
            importsFolder.mkdirs();
        }

        // Download packages sequentially
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger failed = new AtomicInteger(0);

        downloadNextPackage(outdatedWithSlugs, 0, importsFolder, player, sender, completed, failed);
    }

    private void downloadNextPackage(List<EMPackage> packages, int index, File importsFolder,
                                      Player player, CommandSender sender,
                                      AtomicInteger completed, AtomicInteger failed) {
        // Check if player disconnected
        if (player != null && !player.isOnline()) {
            // Continue downloads silently, just don't send messages
            if (index >= packages.size()) {
                if (completed.get() > 0) {
                    Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
                        ReloadCommand.reload(Bukkit.getConsoleSender());
                    }, 20L);
                }
                return;
            }
            EMPackage pkg = packages.get(index);
            String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
            NightbreakContentManager.downloadAsync(slug, importsFolder, null, success -> {
                if (success) completed.incrementAndGet();
                else failed.incrementAndGet();
                downloadNextPackage(packages, index + 1, importsFolder, player, sender, completed, failed);
            });
            return;
        }

        if (index >= packages.size()) {
            // All done
            sender.sendMessage(
                CommandMessagesConfig.getUpdateCompleteMessage()
                        .replace("$completed", String.valueOf(completed.get()))
                        .replace("$failed", String.valueOf(failed.get())));
            if (completed.get() > 0) {
                sender.sendMessage(
                    CommandMessagesConfig.getUpdateReloadingMessage());
                Bukkit.getScheduler().runTaskLater(MetadataHandler.PLUGIN, () -> {
                    ReloadCommand.reload(sender);
                }, 20L);
            }
            return;
        }

        EMPackage pkg = packages.get(index);
        String slug = pkg.getContentPackagesConfigFields().getNightbreakSlug();
        String name = pkg.getContentPackagesConfigFields().getName();

        sender.sendMessage(
            CommandMessagesConfig.getUpdateProgressMessage()
                    .replace("$current", String.valueOf(index + 1))
                    .replace("$total", String.valueOf(packages.size()))
                    .replace("$name", name));

        // Pass null for player to suppress library's own "use em reload" messages
        NightbreakContentManager.downloadAsync(slug, importsFolder, null, success -> {
            if (success) {
                completed.incrementAndGet();
                if (player == null || player.isOnline()) {
                    int remaining = packages.size() - (index + 1);
                    if (remaining > 0) {
                        sender.sendMessage(
                            CommandMessagesConfig.getUpdateDownloadedMoreMessage()
                                    .replace("$name", name)
                                    .replace("$remaining", String.valueOf(remaining)));
                    } else {
                        sender.sendMessage(
                            CommandMessagesConfig.getUpdateDownloadedMessage().replace("$name", name));
                    }
                }
            } else {
                failed.incrementAndGet();
                if (player == null || player.isOnline()) {
                    sender.sendMessage(
                        CommandMessagesConfig.getUpdateFailedMessage().replace("$name", name));
                }
            }
            // Download next package
            downloadNextPackage(packages, index + 1, importsFolder, player, sender, completed, failed);
        });
    }
}
