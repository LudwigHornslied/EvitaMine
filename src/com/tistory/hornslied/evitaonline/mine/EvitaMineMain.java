package com.tistory.hornslied.evitaonline.mine;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.tistory.hornslied.evitaonline.commands.MineCommand;
import com.tistory.hornslied.evitaonline.core.EvitaCoreMain;
import com.tistory.hornslied.evitaonline.db.DB;
import com.tistory.hornslied.evitaonline.mine.block.MineBlockManager;

public class EvitaMineMain extends JavaPlugin {
	private static EvitaMineMain instance;

	private FileConfiguration config;
	
	public static EvitaMineMain getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		
		loadConfig();
		createTables();
		MineBlockManager.getInstance();
		
		initCommands();
	}

	private void loadConfig() {
		if (!new File(getDataFolder(), "config.yml").exists())
			saveDefaultConfig();
		config = getConfig();
	}
	
	private void createTables() {
		DB db = EvitaCoreMain.getInstance().getDB();
		
		db.query("CREATE TABLE IF NOT EXISTS mineblocks (" + "id int NOT NULL PRIMARY KEY AUTO_INCREMENT,"
				+ "x long NOT NULL," + "y long NOT NULL," + "z long NOT NULL," + "world varchar(50) NOT NULL,"
				+ "town varchar(50) DEFAULT NULL" + ");");
	}
	
	private void initCommands() {
		MineCommand mineCommand = new MineCommand();
		
		getCommand("mineblock").setExecutor(mineCommand);
	}
	
	public FileConfiguration getConfiguration() {
		return config;
	}
}
