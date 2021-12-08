package me.rey.clans.shops;

import java.util.*;

import me.rey.clans.currency.CurrencyHandler;
import me.rey.core.gui.Gui;
import me.rey.core.gui.GuiItem;
import me.rey.core.gui.Item;
import me.rey.core.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.items.CustomItems;

public class ShopItem extends GuiItem {

	private static final String CURRENCY = CurrencyHandler.CURRENCY_NAME.toLowerCase();

	private int sellCost, buyCost;
	private Item item;
	
	public ShopItem(Material item, int buyCost, int sellCost) {
		super(new Item(item).setName("&a&l" + Text.formatName(item.name())).setLore(new ArrayList<String>(Arrays.asList(
				"&7Buy: &a" + buyCost,
				"&7Sell: &c" + sellCost
				))));
		
		this.item = new Item(item);
		this.sellCost = sellCost;
		this.buyCost = buyCost;
	}
	
	public ShopItem(ItemStack item, int buyCost, int sellCost, boolean stripColor) {
		super(new Item(item)
				.setName("&a&l" + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? !stripColor ? item.getItemMeta().getDisplayName() :
					ChatColor.stripColor(item.getItemMeta().getDisplayName()) : Text.formatName(item.getType().name())))
				.setLore(new ArrayList<String>(Arrays.asList(
					"&7Buy: &a" + buyCost,
					"&7Sell: &c" + sellCost
					))));
		
		this.item = new Item(item);
		this.sellCost = sellCost;
		this.buyCost = buyCost;
	}

	@Override
	public void onUse(Player player, ClickType type, int slot) {
		ClansPlayer cp = new ClansPlayer(player);
		int balance = cp.getBalance();
		ItemStack toGive = CustomItems.updateItem(item.get());
		Item clicked = this.getFromItem();
		
		switch(type) {
		case LEFT: // BUY - 1
			if(balance < buyCost) {
				cp.sendMessageWithPrefix("Error", String.format("You have insufficient funds to buy &s1 %s&r!", clicked.getName()));
				break;
			}
			
			cp.setBalance(balance - buyCost);
			cp.sendMessageWithPrefix("Shop", String.format("You bought &s1 %s&r!", clicked.getName()));
			
			toGive.setAmount(1);
			if(player.getInventory().firstEmpty() != -1)
				player.getInventory().addItem(toGive);
			else
				player.getLocation().getWorld().dropItem(player.getEyeLocation(), toGive);
			
			break;
		case SHIFT_LEFT: // BUY - 64
			int maxSize = this.item.get().getMaxStackSize();

			if(balance < (buyCost * maxSize)) {
				cp.sendMessageWithPrefix("Error", String.format("You have insufficient funds to buy &s%s %s&r!", maxSize, clicked.getName()));
				break;
			}
			
			cp.setBalance(balance - (buyCost * maxSize));
			cp.sendMessageWithPrefix("Shop", String.format("You bought &s%s %s&r!", maxSize, clicked.getName()));
			
			toGive.setAmount(maxSize);
			if(player.getInventory().firstEmpty() != -1)
				player.getInventory().addItem(toGive);
			else
				player.getLocation().getWorld().dropItem(player.getEyeLocation(), toGive);
			break;
		case RIGHT: // SELL - 1
			
			if(!player.getInventory().contains(this.item.get().getType())) {
				cp.sendMessageWithPrefix("Error", String.format("You don't have &s1 %s&r!", clicked.getName()));
				break;
			}
			
			cp.setBalance(balance + sellCost);
			cp.sendMessageWithPrefix("Shop", String.format("You sold &s1 %s&r for &s%s &r" + CURRENCY +"!", clicked.getName(), sellCost));
			
			ItemStack stack = player.getInventory().getItem(player.getInventory().first(this.item.get().getType()));
			int toSet = stack.getAmount()-1;
			if(toSet <= 0)
				player.getInventory().remove(stack);
			else 
				stack.setAmount(toSet);
			break;
			
		case SHIFT_RIGHT: // SELL ALL
			
			if(!player.getInventory().contains(clicked.get().getType())) {
				cp.sendMessageWithPrefix("Error", String.format("You don't have &s%s&r!", clicked.getName()));
				break;
			}
		
			Iterator<ItemStack> items = player.getInventory().iterator();
			int count = 0;
			while(items.hasNext()) {
				ItemStack found = items.next();
				if(found == null) continue;
				if(found.getType().equals(this.item.get().getType())) {
					player.getInventory().setItem(player.getInventory().first(found), new ItemStack(Material.AIR));
					count += found.getAmount();
				}
			}
			
			cp.setBalance(balance + (sellCost * count));
			cp.sendMessageWithPrefix("Shop", String.format("You sold &s%s %s&r for &s%s &r" + CURRENCY + "!", count, clicked.getName(), (sellCost * count)));
			
			break;
		default:
			break;
		}
	}
	
}