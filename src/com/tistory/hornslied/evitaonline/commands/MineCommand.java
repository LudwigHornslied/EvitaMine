package com.tistory.hornslied.evitaonline.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.tistory.hornslied.evitaonline.mine.block.MineBlockManager;
import com.tistory.hornslied.evitaonline.utils.Resources;

public class MineCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender.hasPermission("evita.mod"))) {
			sender.sendMessage(Resources.messagePermission);
			return true;
		}
		
		switch(cmd.getLabel()) {
		case "mineblock":
			if (!(sender instanceof Player)) {
				sender.sendMessage(Resources.messageConsole);
				break;
			}
			
			((Player) sender).getInventory().addItem(MineBlockManager.MINEBLOCK);
			sender.sendMessage(Resources.tagServer + ChatColor.AQUA + "광산 블록이 지급되었습니다.");
			
			break;
		}
		return true;
	}

}
