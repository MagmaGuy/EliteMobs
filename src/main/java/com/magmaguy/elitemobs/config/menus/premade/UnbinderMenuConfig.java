package com.magmaguy.elitemobs.config.menus.premade;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.ConfigurationEngine;
import com.magmaguy.elitemobs.config.menus.MenusConfigFields;
import com.magmaguy.elitemobs.utils.ItemStackGenerator;
import com.magmaguy.elitemobs.utils.ItemStackSerializer;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;

public class UnbinderMenuConfig extends MenusConfigFields {

    @Getter
    private static String shopName;
    //    @Getter
//    private static ItemStack infoButton;
    @Getter
    private static ItemStack eliteItemInputInfoButton;
    @Getter
    private static ItemStack eliteUnbindInputInfoButton;
    @Getter
    private static ItemStack outputInfoButton;
    //    @Getter
//    private static int infoSlot;
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
    private static List<Integer> cancelSlots;
    @Getter
    private static ItemStack confirmButton;
    @Getter
    private static List<Integer> confirmSlots;

    public UnbinderMenuConfig() {
        super("unbind_menu", true);
    }

    @Override
    public void processAdditionalFields() {
        shopName = ConfigurationEngine.setString(file, fileConfiguration, "menuTitle", ":offset_-8::elitemob_unbind:", true);
        eliteItemInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputInformationSlot", 2);
        eliteItemInputSlot = ConfigurationEngine.setInt(fileConfiguration, "eliteItemInputSlot", 11);
        ItemStackSerializer.serialize(
                "eliteItemInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&2Elite Item Input slots",
                        List.of("&aThis slot is for your elite item!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteItemInputInfoButton = ItemStackSerializer.deserialize("eliteItemInputInformationButton", fileConfiguration);
        eliteScrapInputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "unbindScrollInputInformationSlot", 4);
        eliteUnbindInputSlot = ConfigurationEngine.setInt(fileConfiguration, "unbindScrollInputSlot", 13);
        ItemStackSerializer.serialize(
                "unbindScrollInputInformationButton",
                ItemStackGenerator.generateItemStack(Material.GREEN_BANNER,
                        "&5Unbind Scroll &aInput slots",
                        List.of("&aThis slot is for your &9Unbind Scroll&a!"), MetadataHandler.signatureID),
                fileConfiguration);
        eliteUnbindInputInfoButton = ItemStackSerializer.deserialize("unbindScrollInputInformationButton", fileConfiguration);
        outputInformationSlot = ConfigurationEngine.setInt(fileConfiguration, "informationOutputButtonSlot", 6);
        outputSlot = ConfigurationEngine.setInt(fileConfiguration, "outputSlot", 15);
        ItemStackSerializer.serialize("cancelButton", ItemStackGenerator.generateItemStack(Material.BARRIER,
                "&4Cancel", List.of("&cCancel unbind!"), MetadataHandler.signatureID), fileConfiguration);
        ItemStackSerializer.serialize(
                "outputInformationButton",
                ItemStackGenerator.generateItemStack(Material.RED_BANNER,
                        "&2Unbound Item Output slot",
                        List.of("&aThis slot previews the result of your unbind!"), MetadataHandler.signatureID),
                fileConfiguration);
        outputInfoButton = ItemStackSerializer.deserialize("outputInformationButton", fileConfiguration);
        cancelButton = ItemStackSerializer.deserialize("cancelButton", fileConfiguration);
        cancelSlots = ConfigurationEngine.setInt(fileConfiguration, "cancelButtonSlots", Arrays.asList(29, 30, 38, 39));
        ItemStackSerializer.serialize("confirmButton", ItemStackGenerator.generateItemStack(Material.EMERALD,
                "&2Confirm!", List.of("&aUnbind item!"), 31175), fileConfiguration);
        confirmButton = ItemStackSerializer.deserialize("confirmButton", fileConfiguration);
        confirmSlots = ConfigurationEngine.setInt(fileConfiguration, "confirmUnbindSlots", Arrays.asList(32, 33, 41, 42));
    }
}
