package net.zetaeta.plugins.donationpackagemanager;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;


public class DPMCommandExecutor implements CommandExecutor {

	public DonationPackageManager plugin;

	public DPMCommandExecutor(DonationPackageManager plugin) {
		this.plugin = plugin;
	}
	
	@Override
	public boolean onCommand(CommandSender sndr, Command cmd, String cmdlbl, String[] args) {
		if(cmd.getName().equalsIgnoreCase("dpmdonate")) {
				return DonationPackageManager.doDonation(args, sndr);
		}
		else if (cmd.getName().equalsIgnoreCase("dpm")) {
			if(args.length != 1) return false;
			if (args[0].equalsIgnoreCase("reload")) {
				if (sndr.hasPermission("dpm.reload")) {
					plugin.reloadConfig();
					sndr.sendMessage("§aConfiguration reloaded!");
					DonationPackageManager.log.info("Configuration reloaded!");
					return true;
				}
				sndr.sendMessage("§cYou do not have access to that command!");
				return true;
			}
			
		}
		return false;
	}
	
}
