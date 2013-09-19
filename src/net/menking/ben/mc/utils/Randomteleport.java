package net.menking.ben.mc.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import net.menking.alter_vue.mc.tools.WorldBorderConnector;
import net.menking.ben.mc.objs.BorderDef;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class Randomteleport extends JavaPlugin implements Listener {
	private HashMap<String, Date> coolDown;
	private WorldBorderConnector wbc;
	private int setBack = 5;
	private int coolDownTime = 30;
	private boolean debug = false;
	
	@Override
	public void onEnable() {
		Logger log = getServer().getLogger();

		if( this.coolDown == null ) {
			this.coolDown = new HashMap<String, Date>();
		}
		else {
			this.coolDown.clear();
		}

		wbc = new WorldBorderConnector(this);
		
		if( !wbc.isInstalled() ) {
			getServer().getLogger().warning("[RandomTeleport] WorldBorder is not installed.  Disabling");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		// reload the config
		this.saveDefaultConfig();
		
		setBack = getConfig().getInt("set-back", 15);
		coolDownTime = getConfig().getInt("cool-down-seconds", 30);
		debug = getConfig().getBoolean("debug",  false);
	}
	
	@Override
	public void onDisable() {
		this.coolDown.clear();
		this.coolDown = null;
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( cmd.getName().equalsIgnoreCase("rp") ) {
			
			if( !(sender instanceof Player) ) {
				sender.sendMessage("This command can only be run by a player");
				return true;
			}

			Player player = (Player) sender;
			debug_message("rp called by " + sender.getName());

			if( args.length == 0 && player.hasPermission("randomport.rp")) {
				debug_message("args.length==0 && player.hasPermission('randomport.rp')");
				
				// make sure this player isn't cooling down from a previous /rp
				//
				if( !player.hasPermission("randomport.bypass.cooldown") ) {
					debug_message("Checking for cooldown");
					if( coolDown.containsKey(player.getName()) ) {
						debug_message("Player found in cooldown list");
						Date d = coolDown.get(player.getName());
						Date k = new Date();
						
						debug_message("Player saved cooldown time: " + d.toString());
						debug_message("Now time: " + k.toString());
						
						debug_message("Time diff: " + Long.toString(k.getTime() - d.getTime()));
						
						if( k.getTime() - d.getTime() < (this.coolDownTime * 1000)) {
							sender.sendMessage(ChatColor.YELLOW + "Sorry, you must wait " + Long.toString(coolDownTime - ((k.getTime() - d.getTime())/1000)+1) 
									+ " seconds before trying again" + ChatColor.RESET);
							debug_message("player has to wait");
							return true;
						}
						else {
							coolDown.remove(player.getName());
							debug_message("player cooldown time has expired");
						}
					}
				}
				else {
					debug_message("Bypassing cooldown check");
					
				}
				
				// what world is this player in?
				World world = player.getWorld();

				Block block = getRandomCoordinate(world);

				if( block == null ) {
					// if not, we don't allow teleporting
					sender.sendMessage(ChatColor.DARK_RED + "Sorry, this world does not allow random teleporting" + ChatColor.RESET);	
					debug_message("This world is not in the database for teleporting");
					return true;					
				}
				
				int i = 0;
				
				while( block.getTypeId() == 10 || block.getTypeId() == 11 || block.getTypeId() == 8
						|| block.getTypeId() == 9 || block.getTypeId() == 81 || block.getTypeId() == 0 ||
						block.getBiome().name().equalsIgnoreCase("ocean")) {
					block = getRandomCoordinate(world);

					i++;
					
					if( i > 100 ) break;
				}
				
				if( !player.hasPermission("randomport.bypass.cooldown") ) {
					coolDown.put(player.getName(),  new Date());
				}
				
				if( i > 100 ) {
					sender.sendMessage(ChatColor.DARK_RED + "Sorry, could not find a place to teleport you.  Please try again later." + ChatColor.RESET);
					debug_message("Could not find a place to teleport you");
					return true;
				}
				
				Location l = block.getLocation();
				l.setX(l.getX() + 0.5);
				l.setY(l.getY() + 2);
				l.setZ(l.getZ() + 0.5);

				sender.sendMessage(ChatColor.BLUE + "You have been randomly teleported to (" + Integer.toString(l.getBlockX()) 
						+ ", " + Integer.toString(l.getBlockY()) + ", " + Integer.toString(l.getBlockZ()) + ")" + ChatColor.RESET);
			
				if( !world.isChunkLoaded(l.getChunk().getX(), l.getChunk().getZ()) ) {
					debug_message("Loading chunk and pre-generating");
					world.loadChunk(l.getChunk().getX(), l.getChunk().getZ(), true);
				}
				
				player.teleport(l, TeleportCause.PLUGIN);
				
				debug_message("Player " + player.getName() + " base-block was " + block.toString());
				debug_message("BLock BIOME is '" + block.getBiome().name() + "'" );
						
			}
			else if( args.length == 1 ) {
				if( args[0].equalsIgnoreCase("reload") && player.hasPermission("randomport.reload") ) {
					this.onEnable();
					sender.sendMessage(ChatColor.AQUA + "RandomPort config has been reloaded");
				}
			}
			/*
			else {
				debug_message("args.length != 0 || 1");
				return false;
			}
			*/
			
			return true;
		}
		
		debug_message("returning false.  did not match rp");
		return false;
		
	}
	
	private Block getRandomCoordinate(World w) {
		BorderDef b = wbc.getWorldBorder(w);
		
		if( b != null ) {
			int minRadiusX = b.getX() - b.getRadiusX() + this.setBack;
			int maxRadiusX = b.getX() + b.getRadiusX() - this.setBack;
			int minRadiusZ = b.getZ() - b.getRadiusZ() + this.setBack;
			int maxRadiusZ = b.getZ() + b.getRadiusZ() - this.setBack;
			
			// min + (Math.random() * (Max - Min))
			int x = minRadiusX + (int)(Math.random() * (maxRadiusX - minRadiusX));
			int z = minRadiusZ + (int)(Math.random() * (maxRadiusZ - minRadiusZ));
	
			int y = w.getMaxHeight() - 1;
			Block b1 = w.getBlockAt(x,  y--,  z);
			
			// scan through blocks until we hit non-air
			while((b1.getTypeId() == 0 || b1.getTypeId() == 7) && y > 0 ) {
				b1 = w.getBlockAt(x, y--, z);
			}
			
			return b1;
			
		}
		else {
			return null;
		}
	}	
	
	private void debug_message(String txt) {
		if( this.debug ) getServer().getConsoleSender().sendMessage("[RandomTeleport] DEBUG: " + txt);
	}
	
	@EventHandler
	public void onTeleportEvent(PlayerTeleportEvent event) {
		
	}
}
