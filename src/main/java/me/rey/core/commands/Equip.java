package me.rey.core.commands;

import me.rey.core.classes.ClassType;
import me.rey.core.gui.Item;
import me.rey.core.utils.Text;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class Equip implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if((sender instanceof Player) && command.getName().equalsIgnoreCase("equip")) {
			
			Player p = (Player) sender;
//			if(!p.isOp()) {
//				p.sendMessage(Text.color("&cOnly operators can do that!"));
//				return true;
//			}
			
			if(args.length < 1) {
				p.sendMessage(Text.color("&cUsage: /equip <class>"));
				p.sendMessage(Text.color("&cValid classes: " + Arrays.toString(ClassType.values())));
				return true;
			}
			
			String inputClassName = args[0];
			ClassType classType = ClassType.getClassTypeFromString(inputClassName);

			if(classType == null) {
				p.sendMessage(Text.color("&cValid classes: " + Arrays.toString(ClassType.values())));
				return true;
			}

			ItemStack helmet = me.rey.core.utils.Utils.updateItem(classType.getHelmet().get()),
					chestplate = me.rey.core.utils.Utils.updateItem(classType.getChestplate().get()),
					leggings = me.rey.core.utils.Utils.updateItem(classType.getLeggings().get()),
					boots = me.rey.core.utils.Utils.updateItem(classType.getBoots().get());

			p.getInventory().addItem(new Item(Material.DIAMOND_SWORD).get());
			p.getInventory().addItem(new Item(Material.GOLD_AXE).get());

			switch(classType) {
				case LEATHER:
					p.getInventory().addItem(new Item(Material.BOW).get());
					p.getInventory().addItem(new Item(Material.ARROW).setAmount(64).get());
					break;

				case CHAIN:
				case GOLD:
				case IRON:
				case DIAMOND:
				case BLACK:
				case GREEN:
					p.getInventory().addItem(new Item(Material.IRON_SPADE).get());
					break;
			}

			p.getInventory().setHelmet(helmet);
			p.getInventory().setChestplate(chestplate);
			p.getInventory().setLeggings(leggings);
			p.getInventory().setBoots(boots);
			
			p.sendMessage(Text.color("&aEquipped: &6" + classType.getName()));
		}
		
		
		return true;
	}

}
