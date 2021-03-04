package com.magmaguy.elitemobs.commands;

import cloud.commandframework.Command;
import cloud.commandframework.CommandTree;
import cloud.commandframework.arguments.standard.StringArgument;
import cloud.commandframework.execution.CommandExecutionCoordinator;
import cloud.commandframework.extra.confirmation.CommandConfirmationManager;
import cloud.commandframework.meta.CommandMeta;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.minecraft.extras.MinecraftHelp;
import cloud.commandframework.paper.PaperCommandManager;
import com.magmaguy.elitemobs.ChatColorConverter;
import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.commands.guild.AdventurersGuildCommand;
import com.magmaguy.elitemobs.items.ShareItem;
import com.magmaguy.elitemobs.playerdata.statusscreen.PlayerStatusScreen;
import com.magmaguy.elitemobs.config.ConfigValues;
import com.magmaguy.elitemobs.config.DefaultConfig;
import com.magmaguy.elitemobs.config.EconomySettingsConfig;
import com.magmaguy.elitemobs.config.TranslationConfig;
import com.magmaguy.elitemobs.utils.WarningMessage;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static net.kyori.adventure.text.Component.text;

/**
 * Created by MagmaGuy on 21/01/2017.
 */

public class CommandHandler {

    private PaperCommandManager<CommandSender> manager;
    private CommandConfirmationManager<CommandSender> paymentConfirmationManager;
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
            manager = new PaperCommandManager(
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

        // Create the confirmation manager. This allows us to require certain commands to be
        // confirmed before they can be executed
        paymentConfirmationManager = new CommandConfirmationManager<>(
                /* Timeout */ 30L,
                /* Timeout unit */ TimeUnit.SECONDS,
                /* Action when confirmation is required */ context -> context.getCommandContext().getSender().sendMessage(
                ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.ECONOMY_TAX_MESSAGE)
                        .replace("$command", "/em confirm")
                        .replace("$percentage", (EconomySettingsConfig.playerToPlayerTaxes * 100) + ""))),
                /* Action when no confirmation is pending */ sender -> sender.sendMessage(
                ChatColorConverter.convert(ConfigValues.translationConfig.getString(TranslationConfig.NO_PENDING_COMMANDS)))
        );

        // Register the confirmation processor. This will enable confirmations for commands that require it
        paymentConfirmationManager.registerConfirmationProcessor(manager);

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
                    if (DefaultConfig.emLeadsToStatusMenu)
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

        // Add a confirmation command
        manager.command(builder.literal("confirm")
                .meta(CommandMeta.DESCRIPTION, "Confirm a pending command")
                .handler(paymentConfirmationManager.createConfirmationExecutionHandler()));

        //// Create a world argument
        //final CommandArgument<CommandSender, World> worldArgument = WorldArgument.of("world");

        // /em
        manager.command(builder
                .meta(CommandMeta.DESCRIPTION, "Opens the main player interface")
                .senderType(Player.class)
                .handler(commandContext -> {
                    if (DefaultConfig.emLeadsToStatusMenu)
                        new PlayerStatusScreen((Player) commandContext.getSender());
                }));

        new AdminCommands(manager, builder);
        new UserCommands(manager, builder, paymentConfirmationManager);

    }

}
