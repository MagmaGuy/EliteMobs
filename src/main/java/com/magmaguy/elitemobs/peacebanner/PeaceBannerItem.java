package com.magmaguy.elitemobs.peacebanner;

import com.magmaguy.elitemobs.MetadataHandler;
import com.magmaguy.elitemobs.config.PeaceBannerConfig;
import com.magmaguy.magmacore.util.ChatColorConverter;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;

public class PeaceBannerItem {

    public static final NamespacedKey PEACE_BANNER_KEY =
            new NamespacedKey(MetadataHandler.PLUGIN, "PeaceBanner");
    private static final NamespacedKey RECIPE_KEY =
            new NamespacedKey(MetadataHandler.PLUGIN, "peace_banner_recipe");

    /**
     * All 16 banner materials, used so the crafting recipe accepts any banner color as input.
     */
    private static final List<Material> ALL_BANNERS = List.of(
            Material.WHITE_BANNER, Material.ORANGE_BANNER, Material.MAGENTA_BANNER,
            Material.LIGHT_BLUE_BANNER, Material.YELLOW_BANNER, Material.LIME_BANNER,
            Material.PINK_BANNER, Material.GRAY_BANNER, Material.LIGHT_GRAY_BANNER,
            Material.CYAN_BANNER, Material.PURPLE_BANNER, Material.BLUE_BANNER,
            Material.BROWN_BANNER, Material.GREEN_BANNER, Material.RED_BANNER,
            Material.BLACK_BANNER
    );

    /**
     * Creates a new Peace Banner ItemStack with the designer pattern, display name, lore, and PDC tag.
     */
    public static ItemStack createPeaceBanner() {
        ItemStack banner = new ItemStack(Material.BLUE_BANNER);
        BannerMeta meta = (BannerMeta) banner.getItemMeta();

        // 11-layer designer pattern
        meta.addPattern(new Pattern(DyeColor.WHITE, PatternType.GRADIENT_UP));
        meta.addPattern(new Pattern(DyeColor.LIGHT_BLUE, PatternType.BORDER));
        meta.addPattern(new Pattern(DyeColor.WHITE, PatternType.CROSS));
        meta.addPattern(new Pattern(DyeColor.WHITE, PatternType.RHOMBUS));
        meta.addPattern(new Pattern(DyeColor.BLUE, PatternType.STRIPE_DOWNRIGHT));
        meta.addPattern(new Pattern(DyeColor.BLUE, PatternType.DIAGONAL_UP_LEFT));
        meta.addPattern(new Pattern(DyeColor.BLUE, PatternType.CIRCLE));
        meta.addPattern(new Pattern(DyeColor.BLUE, PatternType.BORDER));
        meta.addPattern(new Pattern(DyeColor.LIGHT_BLUE, PatternType.GRADIENT_UP));
        meta.addPattern(new Pattern(DyeColor.LIME, PatternType.STRIPE_BOTTOM));
        meta.addPattern(new Pattern(DyeColor.BROWN, PatternType.TRIANGLES_BOTTOM));

        meta.setDisplayName(ChatColorConverter.convert(PeaceBannerConfig.getItemName()));
        meta.setLore(PeaceBannerConfig.getItemLore().stream()
                .map(ChatColorConverter::convert).toList());
        meta.getPersistentDataContainer().set(PEACE_BANNER_KEY, PersistentDataType.BYTE, (byte) 1);
        meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);

        banner.setItemMeta(meta);
        return banner;
    }

    /**
     * Checks whether the given ItemStack is a Peace Banner by looking for the PDC tag.
     */
    public static boolean isPeaceBanner(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer()
                .has(PEACE_BANNER_KEY, PersistentDataType.BYTE);
    }

    /**
     * Registers the Peace Banner crafting recipe with the server.
     * The recipe accepts ANY banner color via {@link RecipeChoice.MaterialChoice}.
     * Does nothing if crafting is disabled in config.
     */
    public static void registerRecipe() {
        if (!PeaceBannerConfig.isCraftable()) return;

        ShapedRecipe recipe = new ShapedRecipe(RECIPE_KEY, createPeaceBanner());
        List<String> shape = PeaceBannerConfig.getRecipeShape();
        recipe.shape(shape.toArray(new String[0]));

        for (Map.Entry<String, String> entry : PeaceBannerConfig.getRecipeIngredients().entrySet()) {
            String value = entry.getValue();
            if (value.equals("ANY_BANNER")) {
                recipe.setIngredient(entry.getKey().charAt(0),
                        new RecipeChoice.MaterialChoice(ALL_BANNERS));
            } else {
                recipe.setIngredient(entry.getKey().charAt(0), Material.valueOf(value));
            }
        }

        Bukkit.addRecipe(recipe);
    }

    /**
     * Removes the Peace Banner crafting recipe from the server.
     */
    public static void unregisterRecipe() {
        Bukkit.removeRecipe(RECIPE_KEY);
    }
}
