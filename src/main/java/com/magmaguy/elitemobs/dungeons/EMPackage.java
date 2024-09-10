package com.magmaguy.elitemobs.dungeons;

import com.magmaguy.elitemobs.config.contentpackages.ContentPackagesConfigFields;
import com.magmaguy.elitemobs.mobconstructor.custombosses.CustomBossEntity;
import com.magmaguy.elitemobs.npcs.NPCEntity;
import com.magmaguy.elitemobs.treasurechest.TreasureChest;
import com.magmaguy.magmacore.menus.ContentPackage;
import com.magmaguy.magmacore.util.ChatColorConverter;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import com.magmaguy.magmacore.util.Logger;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EMPackage extends ContentPackage {

    protected static final HashMap<String, EMPackage> content = new HashMap<>();
    @Getter
    private static final Map<String, EMPackage> emPackages = new HashMap<>();
    @Getter
    protected final ContentPackagesConfigFields contentPackagesConfigFields;
    @Getter
    @Setter
    protected boolean isDownloaded;
    @Getter
    @Setter
    protected boolean isInstalled;
    @Setter
    protected boolean outOfDate = false;
    @Getter
    protected List<CustomBossEntity> customBossEntityList = new ArrayList<>();
    protected List<TreasureChest> treasureChestList = new ArrayList<>();
    protected List<NPCEntity> npcEntities = new ArrayList<>();

    public EMPackage(ContentPackagesConfigFields contentPackagesConfigFields) {
        this.contentPackagesConfigFields = contentPackagesConfigFields;
        emPackages.put(contentPackagesConfigFields.getFilename(), this);
        baseInitialization();
    }

    /**
     * Data gets stored under two formats: either the filename, i.e. the filename to a custom boss, or the name of a world
     * for world-based content
     *
     * @param data name
     * @return EM package associated to this data
     */
    public static EMPackage getContent(String data) {
        return content.get(data);
    }

    public static void shutdown() {
        content.clear();
        emPackages.clear();
    }

    public static void initialize(ContentPackagesConfigFields contentPackagesConfigFields) {
        if (contentPackagesConfigFields.getContentType() != null) {
            switch (contentPackagesConfigFields.getContentType()) {
                case INSTANCED_DUNGEON:
                    new WorldInstancedDungeonPackage(contentPackagesConfigFields);
                    break;
                case OPEN_DUNGEON:
                    new WorldDungeonPackage(contentPackagesConfigFields);
                    break;
                case HUB:
                    new WorldPackage(contentPackagesConfigFields);
                    break;
                case SCHEMATIC_DUNGEON:
                    Logger.warn("Tried to load schematic dungeon " + contentPackagesConfigFields.getFilename() + "! This will not work because schematic dungeons have been removed as of EliteMobs 9.0 and replaced with world dungeons. If you want the schematic dungeon experience, I recommend you use BetterStructures with the elite shrines packages, which work better than schematics ever could. Fix this by deleting it from the dungeonpackager file.");
                    break;
                case META_PACKAGE:
                    new MetaPackage(contentPackagesConfigFields);
                    break;
                case ITEMS_PACKAGE:
                    new ItemsPackage(contentPackagesConfigFields);
                    break;
                case EVENTS_PACKAGE:
                    new EventsPackage(contentPackagesConfigFields);
                    break;
                case MODELS_PACKAGE:
                    new ModelsPackage(contentPackagesConfigFields);
                    break;
            }
        }
    }

    public void setupMenuToggle(Player player) {
        if (isInstalled) {
            doUninstall(player);
            return;
        }
        if (isDownloaded) {
            doInstall(player);
            return;
        }
        doDownload(player);
    }

    protected ItemStack getInstalledItemStack() {
        return generateItemStack(List.of("Content is installed!", "Click to uninstall!"), Material.GREEN_STAINED_GLASS_PANE);
    }

    protected ItemStack getNotInstalledItemStack() {
        return generateItemStack(List.of("Content is not installed!", "Click to install!"), Material.YELLOW_STAINED_GLASS_PANE);
    }

    protected ItemStack getPartiallyInstalledItemStack() {
        return generateItemStack(List.of("Content partially installed!",
                "This is either because you haven't downloaded all of it,",
                "or because some elements have been manually disabled.",
                "Click to download!"), Material.ORANGE_STAINED_GLASS_PANE);
    }

    protected ItemStack getNotDownloadedItemStack() {
        return generateItemStack(List.of("Content is not downloaded!", "Click for download link!"), Material.RED_STAINED_GLASS_PANE);
    }

    private ItemStack generateItemStack(List<String> specificTooltip, Material material) {
        List<String> tooltip = new ArrayList<>(specificTooltip);
        tooltip.addAll(contentPackagesConfigFields.getSetupMenuDescription());
        ItemStack itemStack = ItemStackGenerator.generateItemStack(material, contentPackagesConfigFields.getName(), tooltip);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public abstract void doInstall(Player player);

    public abstract void doUninstall(Player player);

    public void doDownload(Player player) {
        player.sendMessage("----------------------------------------------------");
        player.sendMessage(ChatColorConverter.convert("&4Download this at &9" + getContentPackagesConfigFields().getDownloadLink() + " &4!"));
        player.sendMessage("----------------------------------------------------");
    }

    protected ContentState getContentState() {
        if (isInstalled) return ContentState.INSTALLED;
        if (isDownloaded) return ContentState.NOT_INSTALLED;
        return ContentState.NOT_DOWNLOADED;
    }

    public boolean isOutOfDate() {
        if (!isInstalled) return false;
        return outOfDate;
    }

    /**
     * Very first initialization - checks if content is downloaded / installed, loads worlds
     */
    public abstract void baseInitialization();

    /**
     * Initializes content - this means it associates bosses, treasure chests and NPCs to the dungeon (if not instanced)
     */
    public abstract void initializeContent();

}
