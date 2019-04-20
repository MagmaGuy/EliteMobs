package com.magmaguy.elitemobs.economy;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;

public class VaultCompatibility {

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    public static void setupEconomy() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return;
        }
        econ = rsp.getProvider();
    }

    public boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    public boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }

    public static void addVaultCurrency(UUID user, double amount) {
        econ.depositPlayer(Bukkit.getOfflinePlayer(user), amount);
    }

    public static void subtractCurrency(UUID user, double amount) {
        econ.withdrawPlayer(Bukkit.getOfflinePlayer(user), amount);
    }

    public static void setCurrency(UUID user, double amount) {
        Bukkit.getLogger().warning("[EliteMobs] Someone just attempted to set the Vault currency through EliteMobs." +
                " For safety reasons, this command is off. Please use your dedicated economy plugin for this.");
    }

    public static double checkCurrency(UUID user) {
        Bukkit.getLogger().warning(Bukkit.getOfflinePlayer(user).toString());
        return econ.getBalance(Bukkit.getOfflinePlayer(user));

    }

    public static Economy getEconomy() {
        return econ;
    }

    public static Permission getPermissions() {
        return perms;
    }

    public static Chat getChat() {
        return chat;
    }

}
