package me.rey.clans.items.special.miscs;

import me.rey.clans.Tribes;
import me.rey.clans.items.special.ClansTool;
import me.rey.core.players.User;
import me.rey.core.utils.UtilItem;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DebugStickListener extends MiscItem {

    public DebugStickListener(final Tribes plugin) {
        super(plugin, "DEBUG_STICK", MiscType.DEBUG_STICK, ClansTool.DEBUG_STICK, ClansTool.DEBUG_STICK);
    }

    @Override
    public void update() {
    }

    @Override
    public void serverShutdown() {
    }

    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        if (!this.isOfArchetype(event.getItem(), this.misc)) {
            return;
        }
        final User user = new User(event.getPlayer());
        user.sendMessage(ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
        final Block block = event.getClickedBlock();
        final UtilItem.MaterialInfo info = UtilItem.getItemByMaterial(block.getType(), block.getData());
        if (info != null) {
            user.sendMessage("&s Item: &r" + info.name);
            user.sendMessage("&s Material: &r" + info.material.name());
            user.sendMessage("&s State ID: &r" + info.specificDurability);
        } else {
            user.sendMessage("&s Item: &rUnknown");
            user.sendMessage("&s Material: &r" + block.getType().name());
            user.sendMessage("&s State ID: &r" + block.getData());
        }

        user.sendMessage("");
        user.sendMessage("&s World: &r" + block.getWorld().getName());
        user.sendMessage("&s Location: &rX: " + block.getLocation().getX() + ", Y: " + block.getY() + ", Z: " + block.getZ());
        user.sendMessage(ChatColor.STRIKETHROUGH + "-----------------------------------------------------");
    }
}