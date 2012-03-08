package net.zetaeta.plugins.donationpackagemanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import static net.zetaeta.plugins.libraries.CEUtil.*;

public class DonationPackageManager extends JavaPlugin {
    
	PluginManager pm;
	public Logger logger;
	public static Set<String> commands;
	public FileConfiguration config;
	public static Map<String, CommandVariables> outCommandMap;
	public static DonationPackageManager inst;
	public static MultiLogger log;
	public DPMCommandExecutor ce;
	PrintWriter fileLogger;
	protected FileConfiguration pbConfig;
	protected File pbConfigFile;
	
	public void onDisable() {
        log.close();
    }

    public void onEnable() {
    	inst = this;
    	logger = getLogger();
    	File logFile = new File(getDataFolder(), "log.txt");
        try {
        	getDataFolder().mkdirs();
        	logFile.createNewFile();
			fileLogger = new PrintWriter(new FileOutputStream(logFile, true));
		} catch (FileNotFoundException e) {
			logger.severe("Could not create log file!");
			logger.severe(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.severe("Could not create log file!");
			logger.severe(e.getMessage());
			e.printStackTrace();
		}
        log = new MultiLogger(logger, fileLogger);
        ce = new DPMCommandExecutor(this);
        pm = getServer().getPluginManager();
        getCommand("dpmdonate").setExecutor(ce);
        getCommand("dpm").setExecutor(ce);
        {
        	File f = new File(getDataFolder(), "config.yml");
        	if(!f.exists()) {
//        		log.info("About to load default config!");
//        		loadDefaultConfig();
        	}
        }
        
        loadPB();
        
        getConfig().setDefaults(YamlConfiguration.loadConfiguration(getResource("config.yml")));
        getConfig().options().copyDefaults(true);
        getConfig().options().copyHeader(true);
        config = getConfig();
        saveConfig();
        reloadConfig();
        loadConfig();
        log.info(this + " is now enabled!");
    }

	private void loadPB() {
		pbConfigFile = new File("plugins\\PermissionsBukkit\\config.yml");
		if (!pbConfigFile.exists()) {
			log.severe("Could not load PermissionsBukkit config file.");
			return;
		}
		pbConfig = YamlConfiguration.loadConfiguration(pbConfigFile);
		
	}

	private void loadConfig() {
		log.info("Loading Config!");
		commands = config.getConfigurationSection("commands").getKeys(false);
        Iterator<String> cit = commands.iterator();
        outCommandMap = new HashMap<String, CommandVariables>();
        while(cit.hasNext()) {
        	String s = cit.next();
        	log.info("Loading command " + s);
        	outCommandMap.put(s, CommandVariables.getFromConfig("commands." + s));
        }
		
	}

	public static boolean doDonation(String[] args, CommandSender sndr) {
		if(args.length < 1)  {
			return false;
		}
		if(commands.contains(args[0])) {
			CommandVariables these = outCommandMap.get(args[0]);
			String format = these.format;
			List<String> outCommands = these.outCommands;
			String requiredPackage = these.requiredPackage;
			int playerIndex = these.playerIndex;
			String permission = these.permission;
			if (permission == null) {
				permission = "dpm.defaultperm";
			} else {
				Permission perm = new Permission(permission);
				perm.setDefault(PermissionDefault.OP);
				sndr.sendMessage(permission);
				sndr.sendMessage(perm.getName());
				if (!isPermissionRegistered(perm)) {
					inst.pm.addPermission(perm);
				}
				
			}
			if (!sndr.hasPermission(permission)) {
				sndr.sendMessage("§cYou are not allowed to do this.");
				return false;
			}
			String[] expectedArgs = format.split(" ");
			if (args.length != expectedArgs.length)
				return false;
			Map<String, String> varargs = new HashMap<String, String>();
			for (int i = 1; i < expectedArgs.length; i++) {
				if (expectedArgs[i].startsWith("+")) {
					varargs.put(expectedArgs[i].substring(1), args[i]);
				} 
				else {
					if(expectedArgs[i].equalsIgnoreCase(args[i])) {
						continue;
					}
					else {
						log.info("Invalid command args: " + args[i] + ", expected " + expectedArgs[i]);
						return false;
					}
				}
			}
			if(requiredPackage != null && !hasRequiredPackage(args[playerIndex], requiredPackage)) {
				StringBuilder totargs = new StringBuilder("");
				for (String s : args) {
					totargs.append(s).append(" ");
				}
				log.warning(( new StringBuilder().append("DONATION ERROR: ").append(args[playerIndex]).append(" did not have package ").append(requiredPackage).append(" for command ").append(arrayAsString(args)).toString() ));
				return false;
			}
			for (String outCommand : outCommands) {
				String[] outarray = outCommand.split(" ");
				String[] newoutarray = new String[outarray.length];
				for (int i = 0; i < outarray.length; i++) {
					if(outarray[i].startsWith("+")) {
						if(varargs.get(outarray[i].substring(1)) != null) {
							newoutarray[i] = varargs.get(outarray[i].substring(1));
						} else {
							log.warning("Variable " + outarray[i] + " does not have a matching variable in input command.");
						}
					} else {
						newoutarray[i] = outarray[i];
					}
				}
				StringBuilder output = new StringBuilder();
				for (String s : newoutarray) {
					output.append(s).append(" ");
				}
				
				inst.getServer().dispatchCommand(inst.getServer().getConsoleSender(), output.toString());
				log.info("Command " + arrayAsString(args) + " run!");
			}
			return true;
		}
		return false;
	}
	
/*	private static void setRequiredPackage(String player, String returnedPackage) {
		log.info("setRequiredPackage");
		log.info("Setting package of " + player + " to " + returnedPackage);
		inst.players.set("players." + player + ".package", returnedPackage);
		inst.savePlayers();
	}*/

	private static boolean isPermissionRegistered(Permission perm) {
		Set<Permission> perms = inst.pm.getPermissions();
		for (Permission p : perms) {
			if(p.getName().equalsIgnoreCase(perm.getName())) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasRequiredPackage(String player, String requiredPackage) {
		inst.loadPB();
		List<String> groups = inst.pbConfig.getStringList("users." + player + ".groups");
		for (String s : groups) {
			if (s.equalsIgnoreCase(requiredPackage)) {
				return true;
			}
		}
		
		return false;
		
	}

	@SuppressWarnings("unused")
	private void loadDefaultConfig() {
		log.info("Loading default config");
		BufferedReader in;
		PrintWriter out = null;
		in = new BufferedReader(new InputStreamReader(getResource("config.yml")));
		try {
			File f = new File(getDataFolder(), "config.yml");
			f.createNewFile();
			out = new PrintWriter(f);
		} catch (FileNotFoundException e1) {
			log.severe("Could not find config!");
			log.severe(e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e) {
			log.severe("Could not find config!");
			log.severe(e.getMessage());
			e.printStackTrace();
		}
		String s;
		try {
			while((s = in.readLine()) != null) {
				out.println(s);
				logger.info(s);
			}
		} catch (IOException e) {
			log.severe("Could not read from config.");
			e.printStackTrace();
		}
		try {
			in.close();
		} catch (IOException e) {
			log.severe("Input could not be closed.");
			e.printStackTrace();
		}
		out.close();
		try {
			in.close();
		} catch (IOException e) {
			log.severe("Could not close input stream!");
			log.severe(e.getMessage());
			e.printStackTrace();
		}
	}
	
}