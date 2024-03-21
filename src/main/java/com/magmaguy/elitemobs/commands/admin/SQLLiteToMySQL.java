package com.magmaguy.elitemobs.commands.admin;

import com.magmaguy.elitemobs.playerdata.database.PlayerData;
import org.bukkit.command.CommandSender;

import java.sql.Connection;

public class SQLLiteToMySQL {
    public static void convert(CommandSender sender) {
        try {
            Connection sqliteConnection = PlayerData.getSQLiteConnect();
            Connection mysqlConnection = PlayerData.getMySQLConnect();
            PlayerData.convertDatabase(sqliteConnection, mysqlConnection);
            sender.sendMessage("Successfully converted databases");
        } catch (Exception e) {
            sender.sendMessage("Could not convert databases");
        }
    }
}
