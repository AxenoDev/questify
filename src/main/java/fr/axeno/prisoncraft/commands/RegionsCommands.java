package fr.axeno.prisoncraft.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.axeno.prisoncraft.Main;

public class RegionsCommands implements CommandExecutor {

    private Main main;
    public RegionsCommands(Main main) {
        this.main = main;
        main.getCommand("regions").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Cette commande est réservé au joueur.");
            return true;
        }

        Player player = (Player) sender;

        if(!player.hasPermission("prisoncraft.regions.set")) {
            player.sendMessage("Vous n'avez pas la permission d'éxécuté cette commande.");
            return true;
        }

        if(args[0].equalsIgnoreCase("set")) {
            if(args.length < 2) {
                player.sendMessage("Vous devez séléctionner vos position. (/regions set pos1) ou (/regions set pos2)");
                return true;
            }

            if(args[1].equalsIgnoreCase("pos1")) {
                player.sendMessage("La position 1 à été positionné à votre position.");
                return true;
            } else if(args[1].equalsIgnoreCase("pos2")) {
                player.sendMessage("La position 2 à été positiionné à votre position.");
                return true;
            }
        }
        

        return true;
    }

    
    
}
