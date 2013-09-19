package net.menking.ben.mc.objs;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

public class BorderDef {
	private int x, z, radiusX, radiusZ;
	private boolean wrapping;
	
	public BorderDef() {
		x = z = radiusX = radiusZ = 0;
		wrapping = false;
	}
	
	public BorderDef(int x, int z, int radiusX, int radiusZ, boolean wrapping) {
		this.x = x;
		this.z = z;
		this.radiusX = radiusX;
		this.radiusZ = radiusZ;
		this.wrapping = wrapping;
	}

	public BorderDef(World world, FileConfiguration config) throws Exception {
		String path = "worlds." + world.getName();
		
		if( !config.contains(path) ) {
			throw new Exception("That world was not defined in the configuration specified");
		}
		
		path = "worlds." + world.getName() + ".x";
		this.x = config.getInt(path);
		path = "worlds." + world.getName() + ".z";
		this.z = config.getInt(path);
		path = "worlds." + world.getName() + ".radiusX";
		this.radiusX = config.getInt(path);
		path = "worlds." + world.getName() + ".radiusZ";
		this.radiusZ = config.getInt(path);
		path = "worlds." + world.getName() + ".wrapping";
		this.wrapping = config.getBoolean(path);
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}

	public int getRadiusX() {
		return radiusX;
	}

	public void setRadiusX(int radiusX) {
		this.radiusX = radiusX;
	}

	public int getRadiusZ() {
		return radiusZ;
	}

	public void setRadiusZ(int radiusZ) {
		this.radiusZ = radiusZ;
	}

	public boolean isWrapping() {
		return wrapping;
	}

	public void setWrapping(boolean wrapping) {
		this.wrapping = wrapping;
	}
}
