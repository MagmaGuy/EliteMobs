package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.CustomModelsConfig;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.CustomModelAdder;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import com.magmaguy.magmacore.util.ItemStackGenerator;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class UnbinderMenuConfig extends MenusConfigFields {

    @Getter
    private static String shopName;
    @Getter
    private static ItemStack infoButton;
    @Getter
    private static ItemStack eliteItemInputInfoButton;
    @Getter
    private static ItemStack eliteUnbindInputInfoButton;
    @Getter
    private static ItemStack outputInfoButton;
    @Getter
    private static int infoSlot;
    @Getter
    private static int eliteItemInputInformationSlot;
    @Getter
    private static int eliteScrapInputInformationSlot;
    @Getter
    private static int outputInformationSlot;
    @Getter
    private static int eliteItemInputSlot;
    @Getter
    private static int eliteUnbindInputSlot;
    @Getter
    private static int outputSlot;
    @Getter
    private static ItemStack cancelButton;
    @Getter
    private static int cancelSlot;
    @Getter
    private static ItemStack confirmButton;
    @Getter
    private static int confirmSlot;

    public UnbinderMenuConfig() {
        super("unbind_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        shopName = ConfigurationEngine.setString(file, fileConfiguration, "shopName", "[EM] Unbind menu!", true);
        ItemStackSerializer.serialize(
                "infoButton",
                ItemStackGenerator.generateSkullItemStack("magmaguy",
                        "&4&lEliteMobs &r&cby &4&lMagmaGuy",
                        new ArrayList<>(List.of("&8Support the plugins you enjoy!",
                                "&aUse an &5Unbind Scroll &ato remove Soulbind from an item!"))),
                fileConfiguration);
        infoButton = ItemStackSerializer.deserialize("infoButton", fileConfiguration);
        infoSlot = ConfigurationEngine.setInt(fileConfiguration, "infoButtonSlot", 4);
        eliteItemInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputInformationSlot", 20);
        eliteItemInputSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputSlot", 29);
        ItemStackSerializer.serialize(
                "eliteItemInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Elite Item Input slots",
                        List.of("&aThis slot is for your elite item!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteItemInputInfoButton = ItemStackSerializer.deserialize("eliteItemInputInformationButton", fileConfiguration);
        CustomModelAdder.addCustomModel(eliteItemInputInfoButton, CustomModelsConfig.boxInput);
        eliteScrapInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "unbindScrollInputInformationSlot", 22);
        eliteUnbindInputSlot = ConfigurationEngine.setInt(fileConfiguration, "unbindScrollInputSlot", 31);
        ItemStackSerializer.serialize(
                "unbindScrollInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&5Unbind Scroll &aInput slots",
                        List.of("&aThis slot is for your &9Unbind Scroll&a!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteUnbindInputInfoButton = ItemStackSerializer.deserialize("unbindScrollInputInformationButton", fileConfiguration);
        outputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "informationOutputButtonSlot", 24);
        outputSlot = ConfigurationEngine.setInt(fileConfiguration, "outputSlot", 33);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", List.of("&cCancel unbind!"), MetadataHandler.signatureID), fileConfiguration);
        ItemStackSerializer.serialize(
                "outputInformationButton",
                ItemStackGenerator.generateItemStack(Material.RED_BANNER,
                        "&2Unbound Item Output slot",
                        List.of("&aThis slot previews the result of your unbind!"), MetadataHandler.signatureID),
                fileConfiguration);
        outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", fileConfiguration);
        CustomModelAdder.addCustomModel(eliteItemInputInfoButton, CustomModelsConfig.boxOutput);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        cancelSlot = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlot", 27);
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm!", List.of("&aUnbind item!"), 31175), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        CustomModelAdder.addCustomModel(confirmButton, CustomModelsConfig.whiteAnvil);
        confirmSlot = ConfigurationEngine.setInt(fileConfiguration, "confirmUnbindSlot", 35);
    }
}
