package me.rey.clans.utils;

import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClanRelations;
import me.rey.clans.clans.ClansPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class UtilText {

    public static void echo(String prefix, String msg){
        Bukkit.getConsoleSender().sendMessage(String.format("[%s] %s", prefix.toUpperCase(), msg));
    }

    public static String formatName(String text) {
        String[] name = text.replaceAll("_", " ").toLowerCase().split(" ");
        StringBuilder message = new StringBuilder();
        for(String s : name) {
            s = s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            message.append(s).append(" ");
        }
        return message.toString().trim();
    }

    public static String formatClanColors(Player player, Player receiver) {
        ClansPlayer cp = new ClansPlayer(player);

        boolean hasClan = cp.getRealClan() != null;

        ClansPlayer toSend = new ClansPlayer(receiver);

        if(!hasClan)
            return ClanRelations.NEUTRAL.getPlayerColor() + player.getName();

        ClanRelations relation = toSend.getRealClan() == null ? ClanRelations.NEUTRAL : toSend.getRealClan().getClanRelation(cp.getRealClan().getUniqueId());
        Clan from = cp.getRealClan();

        return relation.getClanColor() + from.getName() + " " + relation.getPlayerColor() + player.getName();
    }

    public static boolean announceToServer(String prefix, String message) {
        for(Player online : Bukkit.getOnlinePlayers()) {
            new ClansPlayer(online).sendMessageWithPrefix(prefix, message);
        }
        return true;
    }

    public static boolean announceToServer(String message) {
        for(Player online : Bukkit.getOnlinePlayers()) {
            new ClansPlayer(online).sendMessage(message);
        }
        return true;
    }

    public static String capitalize(String text) {
        if(text == null || text.isEmpty())
            return text;

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    public static boolean isAlphanumeric(String str)
    {
        char[] charArray = str.toCharArray();
        for(char c:charArray)
        {
            if (!Character.isLetterOrDigit(c))
                return false;
        }

        String[] charrArray2 = {"�", "�", "�", "�", "�"};
        for(String s : charrArray2) {
            if(str.contains(s))
                return false;
        }
        return true;
    }

    public static String getPrefix(Player player) {
		/* TODO: PREFIXES
		UserRank rank = new User(player).getRank();
		String rankPrefix = rank == UserRank.MEMBER ? "" : rank.getColor() + "[" + rank.getName().toUpperCase() + "] " + ChatColor.RESET;
		 */

        return player.isOp() ? ChatColor.BLUE + ChatColor.BLUE.toString() + "OP " + ChatColor.YELLOW : ChatColor.YELLOW.toString();
    }


}
