package net.zetaeta.plugins.donationpackagemanager;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;



public class CommandVariables {
	public String format, requiredPackage, permission;
	public List<String> outCommands;
	int playerIndex;
	public CommandVariables(String f, String rP, List<String> oC, int pi, String perm) {
		format = f;
		requiredPackage = rP;
		outCommands = oC;
		playerIndex = pi;
		permission = perm;
	}
	public static CommandVariables getFromConfig(String path) {
		ConfigurationSection config = DonationPackageManager.inst.config.getConfigurationSection(path);
		String f = config.getString("format", "error");
		String rp = config.getString("required_package", null);
		String perm = config.getString("permission", null);
		List<String> oc = config.getStringList("out_commands");
		int pi = config.getInt("player_index", -1);
		return new CommandVariables(f, rp, oc, pi, perm);
	}
}
