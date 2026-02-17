package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class GetLootMenuConfig extends MenusConfigFields {
    public static String menuName;
    public static ItemStack infoItem;
    public static ItemStack leftArrowItem, rightArrowItem, previousLootItem, nextLootItem;
    public static String tierTranslation, itemFilterTranslation;

    public GetLootMenuConfig() {
        super("get_loot_menu", true);
    }

    @Override
    public void processAdditionalFields() {

        menuName = ConfigurationEngine.setString(file, fileConfiguration, "menuName", "[EM] Getloot menu", true);

        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        List.of("&8Support the plugins you enjoy!")),
                fileConfiguration);
        infoItem = ItemStackSerializer.deserialize("infoButton", fileConfiguration);

        ItemStackSerializer.serialize(
                "leftButton",
                ItemStackGenerator.generateSkullItemStack("MHF_ArrowLeft",
                        "Previous Item Ranks",
                        List.of("")),
                fileConfiguration);
        leftArrowItem = ItemStackSerializer.deserialize("leftButton", fileConfiguration);

        ItemStackSerializer.serialize(
                "rightButton",
                ItemStackGenerator.generateSkullItemStack("MHF_ArrowRight",
                        "Next Item Ranks",
                        List.of("")),
                fileConfiguration);
        rightArrowItem = ItemStackSerializer.deserialize("rightButton", fileConfiguration);

        tierTranslation = ConfigurationEngine.setString(file, fileConfiguration, "tierTranslation", "Level", true);
        itemFilterTranslation = ConfigurationEngine.setString(file, fileConfiguration, "itemFilterTranslation", "Filter by items of this level.", true);

        ItemStackSerializer.serialize(
                "previousLoot",
                ItemStackGenerator.generateSkullItemStack("MHF_ArrowLeft",
                        "Previous Loot Page",
                        List.of("")),
                fileConfiguration);
        previousLootItem = ItemStackSerializer.deserialize("previousLoot", fileConfiguration);

        ItemStackSerializer.serialize(
                "nextLoot",
                ItemStackGenerator.generateSkullItemStack("MHF_ArrowRight",
                        "Next Loot Page",
                        List.of("")),
                fileConfiguration);
        nextLootItem = ItemStackSerializer.deserialize("nextLoot", fileConfiguration);

    }

}
