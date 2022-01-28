package com.magmaguy.elitemobs.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.bukkit.BukkitCommandManager;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.text;

/**
 * Created by MagmaGuy on 21/01/2017.
 */

public class CommandHandler {

    private BukkitCommandManager<CommandSender> manager;
    //private CommandConfirmationManager<CommandSender> paymentConfirmationManager;
    private MinecraftHelp<CommandSender> minecraftHelp;
    private BukkitAudiences bukkitAudiences;

    /*
    Commands powered by Cloud
     */

    public CommandHandler() {
        Function<CommandTree, CommandExecutionCoordinator> commandExecutionCoordinator = null;
        try {
            Class<?> c = Class.forName("cloud.commandframework.execution.CommandExecutionCoordinator");
            Method method = c.getDeclaredMethod("simpleCoordinator");
            commandExecutionCoordinator = (Function<CommandTree, CommandExecutionCoordinator>) method.invoke(Function.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            manager = new BukkitCommandManager(
                    /* Owning plugin */ MetadataHandler.PLUGIN,
                    /* Coordinator function */ commandExecutionCoordinator,
                    /* Command Sender -> C */ Function.identity(),
                    /* C -> Command Sender */ Function.identity()
            );
        } catch (final Exception e) {
            new WarningMessage("Failed to initialize the command manager");
            /* Disable the plugin */
            MetadataHandler.PLUGIN.getServer().getPluginManager().disablePlugin(MetadataHandler.PLUGIN);
            return;
        }

        //try {
        //    manager.registerBrigadier();
        //} catch (final Exception e) {
        //    new WarningMessage("Failed to initialize Brigadier support: " + e.getMessage());
        //}

        // Create a BukkitAudiences instance (adventure) in order to use the minecraft-extras help system
        bukkitAudiences = BukkitAudiences.create(MetadataHandler.PLUGIN);

        minecraftHelp = new MinecraftHelp<CommandSender>(
                "/elitemobs help",
                bukkitAudiences::sender,
                manager
        );

        // Override the default exception handlers
        new MinecraftExceptionHandler<CommandSender>()
                .withInvalidSyntaxHandler()
                .withInvalidSenderHandler()
                .withNoPermissionHandler()
                .withArgumentParsingHandler()
                .withCommandExecutionHandler()
                .withDecorator(
                        component -> text()
                                .append(text("[", NamedTextColor.DARK_GRAY))
                                .append(text("Example", NamedTextColor.GOLD))
                                .append(text("] ", NamedTextColor.DARK_GRAY))
                                .append(component).build()
                ).apply(manager, bukkitAudiences::sender);

        constructCommands();
    }

    public void constructCommands() {

        // /ag
        final Command.Builder<CommandSender> agBuilder = manager.commandBuilder("adventurersguild", "ag");
        manager.command(agBuilder.meta(CommandMeta.DESCRIPTION, "Teleports players to the Adventurers' Guild Hub")
                .senderType(Player.class)
                //permission is dealt inside of the command
                .handler(commandContext -> {
                    if (DefaultConfig.isEmLeadsToStatusMenu())
                        AdventurersGuildCommand.adventurersGuildCommand((Player) commandContext.getSender());
                }));

        // /shareitem
        final Command.Builder<CommandSender> shareItemBuilder = manager.commandBuilder("shareitem");
        manager.command(shareItemBuilder.meta(CommandMeta.DESCRIPTION, "Shares a held Elite item on chat.")
                .senderType(Player.class)
                //permission is dealt inside of the command
                .permission("elitemobs.shareitem")
                .handler(commandContext -> ShareItem.showOnChat((Player) commandContext.getSender())));

        // Base command builder
        final Command.Builder<CommandSender> builder = manager.commandBuilder("elitemobs", "em");

        manager.command(builder.literal("help")
                .argument(StringArgument.optional("query", StringArgument.StringMode.GREEDY))
                .handler(context -> {
                    minecraftHelp.queryCommands(context.getOrDefault("query", ""), context.getSender());
                }));

        //// Create a world argument
        //final CommandArgument<CommandSender, World> worldArgument = WorldArgument.of("world");

        // /em
        manager.command(builder
                .meta(CommandMeta.DESCRIPTION, "Opens the main player interface")
                .senderType(Player.class)
                .handler(commandContext -> {
                    if (DefaultConfig.isEmLeadsToStatusMenu())
                        new PlayerStatusScreen((Player) commandContext.getSender());
                }));

        new AdminCommands(manager, builder);
        new UserCommands(manager, builder);

    }

}
