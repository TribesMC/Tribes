package me.rey.clans.clans;

import me.rey.clans.Tribes;
import me.rey.clans.database.SQLManager;
import me.rey.clans.enums.CommandType;
import me.rey.clans.siege.Siege;
import me.rey.clans.utils.UtilFocus;
import me.rey.core.Warriors;
import me.rey.core.players.combat.PlayerHitCache;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ClansPlayer {

    private final UUID uuid;
    /* TEMP VARIABLES FOR CONFIRMATION GUI */
    public Clan confirm_toPromote;
    public ClansPlayer confirm_toProm;
    public String confirm_name;
    public ClansRank confirm_origin;
    private Player player;
    private OfflinePlayer offline;
    private final Tribes plugin;
    private final SQLManager sql;

    public ClansPlayer(final Player p) {
        this.uuid = p.getUniqueId();
        this.player = p;
        this.offline = null;
        this.plugin = Tribes.getInstance();

        this.sql = this.plugin.getSQLManager();
    }

    public ClansPlayer(final UUID uuid) {
        this.uuid = uuid;
        this.player = Bukkit.getServer().getPlayer(uuid);
        this.offline = null;

        if (this.player == null || !this.player.isOnline()) {
			this.player = null;
            this.offline = Bukkit.getServer().getOfflinePlayer(uuid);
        }
        this.plugin = Tribes.getInstance();

        this.sql = this.plugin.getSQLManager();
    }

    public UUID getUniqueId() {
        return this.uuid;
    }

    public boolean compare(final ClansPlayer player) {
        return this.getUniqueId().equals(player.getUniqueId());
    }

    public Player getPlayer() {
        return this.player;
    }

    public OfflinePlayer getOfflinePlayer() {
        return this.offline;
    }

    public boolean isOnline() {
        return this.player != null && this.player.isOnline();
    }

    public boolean hasClan() {
        return this.isInFakeClan() || this.sql.hasClan(this.uuid);
    }

    public Clan getClan() {
        return this.isInFakeClan() ? this.getFakeClan() : this.getRealClan();
    }

    public boolean leaveClan() {
        if (!this.hasClan()) {
			return false;
		}

        final Clan toSave = this.getClan();
        this.sendMessageWithPrefix("Tribe", "You have left &s" + toSave.getName() + "&r.");
        if (this.isInFakeClan()) {
            Tribes.adminFakeClans.remove(this.getUniqueId());
            return true;
        }

        if (toSave.getPlayerRank(this.getUniqueId()) == ClansRank.LEADER) {
			return false;
		}

        toSave.kickPlayer(this.getUniqueId());
        this.sql.saveClan(toSave);
        this.save();
        return true;
    }

    public void kick() {
        this.sql.setPlayerData(this.getUniqueId(), "clan", null);
    }

    public boolean disbandClan() {
        if (!this.hasClan()) {
			return false;
		}

        if (this.getClan().isBeingSieged()) {
            final ArrayList<Siege> raidingSelf = new ArrayList<>();

            for (final Siege siege : this.getClan().getClansSiegingSelf()) {
                raidingSelf.add(siege);
            }

            for (final Siege siege : raidingSelf) {
                siege.end();
            }
        }

        if (this.getClan().isSiegingOther()) {
            final ArrayList<Siege> siegingOther = new ArrayList<>();

            for (final Siege siege : this.getClan().getClansSiegedBySelf()) {
                siegingOther.add(siege);
            }

            for (final Siege siege : siegingOther) {
                siege.end();
            }
        }

        this.sql.deleteClan(this.getClan().getUniqueId());
        this.sendMessageWithPrefix("Success", "You have disbanded your Clan.");
        return true;
    }

    public void sendMessageWithPrefix(final CommandType type, final String message) {
		this.sendMessageWithPrefix(type.getName(), message);
    }

    public void sendMessageWithPrefix(final String prefix, final String message) {
		this.sendMessage(Text.format(prefix, message));
    }

    public void sendMessage(final String message) {
        if (this.player == null) {
			return;
		}
		this.player.sendMessage(Text.color("&7" + message));
    }

    public void save() {
        this.sql.savePlayer(this.getUniqueId());
    }

    public Clan getRealClan() {
        String uuid = null;
        try {
            uuid = (String) Tribes.playerdata.get(this.getUniqueId()).get("clan");
        } catch (final NullPointerException e) {
            return null;
        }
        if (uuid == null) {
			return null;
		}

        final Clan toGive = Tribes.getInstance().getClan(UUID.fromString(uuid));
        return toGive;
    }

    public Clan getFakeClan() {
        return !this.isInFakeClan() ? null : Tribes.getInstance().getClan(Tribes.adminFakeClans.get(this.getUniqueId()));
    }

    public boolean isInFakeClan() {
        return Tribes.adminFakeClans.containsKey(this.getUniqueId());
    }

    public int getBalance() {
        this.sql.createPlayer(this.getPlayer());
        return (int) Tribes.playerdata.getOrDefault(this.getUniqueId(), new HashMap<>()).getOrDefault("balance", 0);
    }

    public void setBalance(final int balance) {
        this.sql.setPlayerData(this.getUniqueId(), "balance", balance);
    }

    public Clan getClanInTerritory() {
        return Tribes.getInstance().getClanFromTerritory(this.getPlayer().getLocation().getChunk());
    }

    public boolean isInSafeZone() {
        return Tribes.getInstance().isInSafezone(this.getPlayer().getLocation());
    }

    public boolean isInCombat() {
        final PlayerHitCache cache = Warriors.getInstance().getHitCache();
        return cache.hasCombatTimer(this.getPlayer());
    }

    public void unfocus() {
        UtilFocus.focus(this.getPlayer(), null);
    }

    public void focus(final Player focus) {
        UtilFocus.focus(this.getPlayer(), focus);
    }

    public boolean hasFocus() {
        return this.getFocus() != null;
    }

    public Player getFocus() {
        return UtilFocus.getFocus(this.getPlayer());
    }

}
