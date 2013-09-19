package net.menking.alter_vue.mc.tools;

import java.util.HashMap;
import java.util.List;

import net.menking.ben.mc.objs.BorderDef;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class WorldBorderConnector {
	private JavaPlugin plugin;
	private HashMap<String, BorderDef> worldParams;
	
	public WorldBorderConnector(JavaPlugin plugin) {
		this.plugin = plugin;
		
		Plugin jp = plugin.getServer().getPluginManager().getPlugin("WorldBorder");
		
		if( jp != null ) {
			this.setupWorldBorders(jp);
		}
		else {
			// oh well, no matter
			//log.info("[RandomPort] Did not find WorldBorder plugin");
		}

	}
	
	public boolean isInstalled() {
		return (plugin != null);
	}
	
	public BorderDef getWorldBorder(World world ) {
		return worldParams.get(world.getName());
	}
	
	private void setupWorldBorders(Plugin jp) {
		// we have WorldBorder!
		//plugin.getServer().getLogger().info("[RandomPort] Detected WorldBorder");
		
		FileConfiguration config = jp.getConfig();
		List<World> worlds = null;
		
		worlds = plugin.getServer().getWorlds();	

		for( World world : worlds ) {
			try {
				BorderDef b = new BorderDef(world, config);
				worldParams.put(world.getName(),  b);
				//plugin.getServer().getLogger().info("[RandomPort] Added definition for world " + world.getName());
			}
			catch( Exception e ) {
				// do nothing, world didn't exist
				//plugin.getServer().getLogger().info("[RandomPort] world " + world.getName() + " did not have a defintion in WorldBorder");
			}
		}
		
	}	
}
