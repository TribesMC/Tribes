package me.rey.clans.commands.staff;

import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.players.User;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Legendary extends ClansCommand {

    public Legendary() {
        super("legendary", "Allows you to spawn a legendary item", "/legendary <legend>", ClansRank.NONE, CommandType.STAFF, true);
//        super.setStaff(true);
    }

    @Override
    public void run(CommandSender sender, String[] args) {
        if (args != null && args.length > 0) {
            ClansTool tool;
            int amount = 1;
            Player player = (Player) sender;
            try {
                tool = ClansTool.valueOf(args[0]);
            } catch (IllegalArgumentException e) {
                new User(player).sendMessageWithPrefix("Item", "No item by the name of &s" + args[0] + "&r appears to exist!");
                return;
            }

            int slot;

            if ((slot = player.getInventory().firstEmpty()) >= 0) {
                ItemStack stack = tool.stack.clone();
                stack.setAmount(amount);
                player.getInventory().setItem(slot, stack);

                new User(player).sendMessageWithPrefix("Legendaries", "You have spawned a &s" + stack.getItemMeta().getDisplayName() + "&r!");
            } else {
                new User(player).sendMessageWithPrefix("Item", "Your inventory is full, safely summoning the item into your inventory is not possible.");
            }
        } else {
            new User((Player) sender).sendMessageWithPrefix("Item", "You have not specified an item to summon!");
        }
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[0];
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length == 1) {
            return Arrays.stream(ClansTool.values())
                    .map(Enum::name)
                    .filter(rankName -> rankName.toLowerCase().startsWith(strings[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return null;
    }
}

//String permission = "cwr.itemsummon";
//
//public ItemCommand(ClansItems core) {
//    super(core,
//            "item",
//            "Spawns one of the custom clans items",
//            "/item <toolname> [amount] [player]",
//            "cwr.itemsummon");
//}
//
//@Override
//public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
//    if (!(sender instanceof Player)) {
//        if (args.length < 3) {
//            new Message("Item", "Summoning items through console must have a player argument!").send(sender);
//            return true;
//        }
//    }
//
//    if (sender.hasPermission(permission)) {
//
//        if (args == null) {
//            new Message("Item", "You have not specified an item to summon!").send(sender);
//            return true;
//        }
//
//        if (args.length >= 1) {
//            ClansTool tool;
//            int amount = 1;
//            Player player = sender instanceof Player ? (Player) sender : null;
//            try {
//                tool = ClansTool.valueOf(args[0]);
//            } catch (IllegalArgumentException e) {
//                new Message("Item", "No item by the name of " + Message.VARIABLE + args[0] + Message.DEFAULT + " appears to exist!").send(sender);
//                return true;
//            }
//            if (args.length > 1) {
//                try {
//                    amount = Integer.parseInt(args[1]);
//                } catch (NumberFormatException e) {
//                    new Message("Item", Message.VARIABLE + args[1] + Message.DEFAULT + " is not a valid number!").send(sender);
//                    return true;
//                }
//            }
//            if (args.length > 2) {
//                OfflinePlayer target = Bukkit.getOfflinePlayer(args[2]);
//                if (target == null || !target.hasPlayedBefore()) {
//                    new Message("Item", "Player by the name of " + Message.VARIABLE + (target != null ? target.getName() : args[2]) + Message.DEFAULT + " has either never played before, or does not exist!").send(sender);
//                    return true;
//                }
//
//                if (!target.isOnline()) {
//                    new Message("Item", Message.VARIABLE + target.getName() + Message.DEFAULT + " must be online to receive the item, but they are offline!").send(sender);
//                    return true;
//                }
//
//                player = target.getPlayer();
//            }
//
//            int slot;
//
//            if (player == null) {
//                new Message("Item", "Could not verify the player, try this again or contact a developer!").send(sender);
//                return true;
//            }
//
//            if ((slot = player.getInventory().firstEmpty()) >= 0) {
//                ItemStack stack = tool.stack.clone();
//                stack.setAmount(amount);
//                player.getInventory().setItem(slot, stack);
//            } else {
//                new Message("Item", (player.equals(sender) ? "Your" : player.getName().toLowerCase().endsWith("s") ? Message.VARIABLE + player.getName() + Message.DEFAULT + "'" : Message.VARIABLE + player.getName() + Message.DEFAULT + "'s") + " inventory is full, safely summoning the item into " + (player.equals(sender) ? "your" : "their") + " inventory is not possible.").send(sender);
//                return true;
//            }
//        } else {
//            new Message("Item", "You have not specified an item to summon!").send(sender);
//            return true;
//        }
//        return true;
//    } else {
//        sendNoPermissionMessage(sender);
//    }
//
//    return true;
//}
//
//@Override
//public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
//    if (args.length == 1) {
//        return Arrays.stream(ClansTool.values())
//                .map(Enum::name)
//                .filter(rankName -> rankName.toLowerCase().startsWith(args[0].toLowerCase()))
//                .collect(Collectors.toList());
//    } else if (args.length == 2) {
//        return Bukkit.getOnlinePlayers().stream()
//                .map(Player::getName)
//                .filter(playerName -> playerName.toLowerCase().startsWith(args[1].toLowerCase()))
//                .collect(Collectors.toList());
//    }
//    return null;
//}