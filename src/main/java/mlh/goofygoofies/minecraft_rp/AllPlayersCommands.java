package mlh.goofygoofies.minecraft_rp;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


import static org.bukkit.Bukkit.getServer;

public class AllPlayersCommands implements CommandExecutor, TabCompleter, Listener {
    public CommandSender sender;
    static public Player robber = null; // set to null if no one is robbing the ship
    static int robbing_task_id = 0; // set to 0 if no one is robbing the ship
    public static Location SHIP_LOCATION = new Location(getServer().getWorlds().get(0), 6910, 67, -4819);

    static Plugin plugin;

    // Sends a message to all nearby players, describing the action that the sender did (args[0]).
    public boolean describeAction(String[] args){
        if (args.length == 0) {
            return false;
        }
        int distance = 50 * 50;
        String actionDescription = String.join(" ", args);
        for (Player p : getServer().getOnlinePlayers()) {
            Player senderPlayer = (Player) sender;
            if (p.getWorld() ==  senderPlayer.getWorld() && p.getLocation().distanceSquared(senderPlayer.getLocation()) <= distance) {
                p.sendMessage(ChatColor.BLUE + sender.getName() + " " + actionDescription);
            }
        }
        return true;
    }

    // Sends a message to all nearby players, describing the event that happened (args[0]).
    public boolean describeEvent(String[] args){
        if (args.length == 0) {
            return false;
        }
        int distance = 50 * 50;
        String eventDescription = String.join(" ", args);

        for (Player p : getServer().getOnlinePlayers()) {
            Player senderPlayer = (Player) sender;
            if (p.getWorld() ==  senderPlayer.getWorld() && p.getLocation().distanceSquared(senderPlayer.getLocation()) <= distance) {
                p.sendMessage(ChatColor.GREEN + eventDescription);
            }
        }
        return true;
    }

    //generates a random number between 100 and 0 (inclusive), to determine if an action has succeeded or not.
    public boolean rollDice(){
        Random r = new Random();
        int result = r.nextInt(101);
        int distance = 50 * 50;
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Player senderPlayer = (Player) sender;
            if (p.getWorld() ==  senderPlayer.getWorld() && p.getLocation().distanceSquared(senderPlayer.getLocation()) <= distance) {
                p.sendMessage(ChatColor.GRAY + sender.getName() + " rolled the dice and obtained a " + result);
            }
        }
        return true;
    }

    public boolean showID(){
        int distance = 50 * 50;

        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Player senderPlayer = (Player) sender;
            if (p.getWorld() ==  senderPlayer.getWorld() && p.getLocation().distanceSquared(senderPlayer.getLocation()) <= distance) {
                p.sendMessage(ChatColor.GREEN + "ID indicates: \n Name: " + sender.getName());
            }
        }
        return true;
    }

    public boolean rob(){

        //Check if ship is already being looted
        if (robber != null) {
            sender.sendMessage("The ship is already being looted...");
            return true;
        }

        //checks that player is inside the ship's vault (which is a chunk)
        Player p = (Player) sender;

        //TODO change to distance radius
        if (p.getLocation().distance(SHIP_LOCATION) > 50 ){
            sender.sendMessage("You have to be inside the ship in order to loot it!");
            return true;
        }

        //Start robbing the ship (with scheduler/multi-threading to deal with the timer)
        robber = p;
        BukkitScheduler scheduler = getServer().getScheduler();
        int [] time_left = {8400}; //TODO CHANGE 1200 to 8400ticks = 20ticks * 60 * 7 = 7minutes

        robbing_task_id = scheduler.scheduleSyncRepeatingTask(plugin, new Runnable() {
            @Override
            public void run() {
                //decrement time left
                time_left[0] -= 20;

                //if time left is not 0, and is an integer (units: minutes), tells the players how much time is left
                //for the robbery
                if (time_left[0] > 0){
                float x = (float)time_left[0]/1200;
                if (x == Math.ceil(x)) sender.sendMessage(ChatColor.RED + String.valueOf(x) + "minutes left!");
                }
                else{
                        sender.sendMessage("You successfully looted the ship!");
                        //Gives 64 gold ingots to players
                        ItemStack[] gold_ingot_stack = {new ItemStack(Material.GOLD_INGOT, 64)};
                        robber.getInventory().addItem(gold_ingot_stack);

                        Bukkit.broadcastMessage(ChatColor.RED + "THE SHIP HAS BEEN LOOTED!");
                        robber = null;
                        int old_robbing_task_id = robbing_task_id;
                        robbing_task_id = 0;
                        Bukkit.getServer().getScheduler().cancelTask(old_robbing_task_id);


                }
            }
        }, 0L, 20L);


    return true;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        this.sender = sender;
        // Doesn't work for ConsoleCommandSender/BlockCommandSender
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command only supports being run by a player");
            return true;
        }

        switch (label.toLowerCase()) {
            case "me":
                return describeAction(args);
            case "it":
                return describeEvent(args);
            case "roll":
                return rollDice();
            case "id":
                return showID();
            case "rob":
                return rob();
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // None of these commands take arguments so far
        return new ArrayList<String>();
    }
}
