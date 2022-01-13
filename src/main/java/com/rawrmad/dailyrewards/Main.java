package com.rawrmad.dailyrewards;

import com.rawrmad.dailyrewards.commands.AdminCommands;
import com.rawrmad.dailyrewards.commands.RewardCommands;
import com.rawrmad.dailyrewards.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main extends JavaPlugin implements Listener {

    public SettingsManager settings = SettingsManager.getInstance();
    public static boolean papi;
    public static Connection connection;
    public static String host;
    public static String database;
    public static String username;
    public static String password;
    public int port;

    public void onEnable() {
        getCommand("dailyrewards").setExecutor((CommandExecutor) new AdminCommands(this));
        getCommand("reward").setExecutor((CommandExecutor) new RewardCommands());
        this.settings.setup((Plugin) this);
        registerEvents();
        if (SettingsManager.getConfig().getBoolean("mysql.enabled")) {
            mysqlSetup();
            MySQLManager.createTable();
            for (Player player : Bukkit.getOnlinePlayers())
                MySQLManager.createPlayer(player);
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            papi = true;
            new PAPIExtensions().register();
        } else {
            papi = false;
        }
    }

    private void registerEvents() {
        Bukkit.getPluginManager().registerEvents(new JoinManager(), this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }



    public void mysqlSetup() {
        host = SettingsManager.getConfig().getString("mysql.host-name");
        this.port = SettingsManager.getConfig().getInt("mysql.port");
        database = SettingsManager.getConfig().getString("mysql.database");
        username = SettingsManager.getConfig().getString("mysql.username");
        password = SettingsManager.getConfig().getString("mysql.password");
        try {
            synchronized (this) {
                if (getConnection() != null && !getConnection().isClosed())
                    return;
                Class.forName("com.mysql.jdbc.Driver");
                setConnection(DriverManager.getConnection("jdbc:mysql://" + host + ":" + this.port + "/" + database,
                        username, password));
                Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN + "Daily Rewards MySQL: Successfully Connected");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Daily Rewards MySQL: Failed To Connected");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Daily Rewards MySQL: Error 'SQLException'");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Daily Rewards MySQL: Your MySQL Configuration Information Is Invalid");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Daily Rewards MySQL: Failed To Connect");
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Daily Rewards MySQL: Error 'ClassNotFoundException'");
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        Main.connection = connection;
    }

}

