package me.rey.clans.features;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import me.rey.clans.Tribes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class StaffChatGateway implements PluginMessageListener {

    public StaffChatGateway() {
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(Tribes.getInstance().getPlugin(), "staffchat:gateway", this);
    }

    @Override
    public void onPluginMessageReceived(final String channel, final Player player, final byte[] bytes) {
        if (!channel.equalsIgnoreCase("staffchat:gateway")) {
            return;
        }

        final ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        final String subChannel = in.readUTF();
        if (subChannel.equalsIgnoreCase("Ping")) {
            Bukkit.getLogger().info("received a ping");
            //todo play sound
        }
    }
}
