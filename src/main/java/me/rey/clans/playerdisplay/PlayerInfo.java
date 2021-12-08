package me.rey.clans.playerdisplay;

import me.rey.clans.Tribes;
import me.rey.clans.clans.Clan;
import me.rey.clans.clans.ClanRelations;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.currency.CurrencyHandler;
import me.rey.clans.utils.UtilFocus;
import me.rey.core.Warriors;
import me.rey.core.packets.Nametag;
import me.rey.core.players.User;
import me.rey.core.players.combat.PlayerHitCache;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.HashMap;

public class PlayerInfo implements Listener {

    final String[] scoreboardTitles = {
            "&b&lTribes Beta",
            "&f&lT&b&lribes Beta",
            "&b&lT&f&lr&b&libes Beta",
            "&b&lTr&f&li&b&lbes Beta",
            "&b&lTri&f&lb&b&les Beta",
            "&b&lTrib&f&le&b&ls Beta",
            "&b&lTribe&f&ls &b&lBeta",
            "&b&lTribes&f&l &b&lBeta",
            "&b&lTribes &f&lB&b&leta",
            "&b&lTribes B&f&le&b&lta",
            "&b&lTribes Be&f&lt&b&la",
            "&b&lTribes Bet&f&la",
            "&b&lTribes Beta",
            "&b&lTribes Beta",
            "&f&lTribes Beta",
            "&f&lTribes Beta",
            "&b&lTribes Beta",
            "&b&lTribes Beta",
            "&f&lTribes Beta",
            "&f&lTribes Beta",
            "&b&lTribes Beta",
            "&b&lTribes Beta",
            "&f&lTribes Beta",
            "&f&lTribes Beta"
    };
    private final PlayerHitCache cache = Warriors.getInstance().getHitCache();
    private final HashMap<Player, CustomScoreboard> scoreboardCache = new HashMap<>();

    public void updateName(final Player p) {
//        final RankedPlayer rp = new RankedPlayer(p.getUniqueId());
//        p.setPlayerListName(rp.getRank().getPrefix() + ChatColor.RESET + p.getName());
//        final EntityPlayer entityPlayer = ((CraftPlayer)p).getHandle();
//        entityPlayer.displayName = rp.getRank().getPrefix().trim() + " &r" + p.getName();
//        for (final Player a : Bukkit.getOnlinePlayers()) {
//            ((CraftPlayer)a).getHandle().playerConnection.sendPacket((Packet)new PacketPlayOutNamedEntitySpawn((EntityHuman)entityPlayer));
//        }
    }

    /*
     *  SCOREBOARD
     */
    public void setupSidebar(final Player p) {
        if (!this.scoreboardCache.containsKey(p)) {
			this.scoreboardCache.put(p, new CustomScoreboard(p, this.scoreboardTitles[0], Arrays.copyOfRange(this.scoreboardTitles, 1, this.scoreboardTitles.length)));
		}

		this.scoreboardCache.get(p).init();
    }

    @EventHandler
    public void updateScoreboard(final UpdateScoreboardEvent e) {
        final ClansPlayer cp = new ClansPlayer(e.getScoreboard().getBound());
        final Chunk standing = e.getScoreboard().getBound().getLocation().getChunk();
        final boolean hasClan = cp.getRealClan() != null;
        final Clan c = cp.getRealClan();

        final String clan = hasClan ? c.getName() : "None";
		final String mimicClan = cp.getFakeClan() == null ? "" : " &c(" + cp.getFakeClan().getName() + ")";
		final String online = hasClan ? c.getOnlinePlayers(false).size() + "/" + c.getPlayers(false).size() : "N/A";
        final String energy = hasClan ? c.getEnergyString() : "N/A";
        final String home = hasClan && c.getHome() != null ? String.format("(%s, %s, %s)",
                c.getHome().getBlockX(),
                c.getHome().getBlockY(),
                c.getHome().getBlockZ()
        ) : "&fN/A";

        final String balance = Integer.toString(cp.getBalance());

        String territory = Tribes.getInstance().getClanFromTerritory(standing) == null ? ChatColor.GRAY.toString() + "Wilderness" :
                (!hasClan ? ClanRelations.NEUTRAL.getPlayerColor().toString() : c.getClanRelation(Tribes.getInstance().getClanFromTerritory(standing)
                        .getUniqueId()).getPlayerColor().toString()) + Tribes.getInstance().getClanFromTerritory(standing).getName();
        territory = cp.isInSafeZone() ? String.format("&f(%s&f) %s", cp.isInCombat() ? ChatColor.RED.toString() + "UNSAFE" :
                ChatColor.AQUA.toString() + "SAFE", ChatColor.stripColor(territory)) : territory;


        // String.format("%.1f", cache.getCombatTimer(u.getPlayer()).getRemaining(System.currentTimeMillis())) + " Seconds"
        final User u = new User(cp.getPlayer());
        final String combat = "&f(" + (u.isInCombat()
                ? "&cUNSAFE"
                : "&aSAFE") + "&f)";

        //    			"&eOnline &f" + online,
//    			"&eHome &a" + home,
        final String[] lines = {
                String.format("&3Online: %s/%s", Bukkit.getOnlinePlayers().size(), Bukkit.getMaxPlayers()),
                "",
                "&eClan &f" + clan + mimicClan,
                "&eEnergy &f" + energy,
                "&e" + CurrencyHandler.CURRENCY_NAME + " &f" + balance,
                "&eCombat &f" + combat,
                "",
                (Tribes.getInstance().getClanFromTerritory(standing) != null && Tribes.getInstance().getClanFromTerritory(standing).isServerClan() ? "&f" : "") + territory,
                "",
                null, // Event (x, z)
                null, // EVENT NAME
                null,
                null, // Sieged (clan)
                null, // TIME
                null
        };


        /*
         * SIEGES
         */
        if (hasClan && (c.isBeingSieged() || c.isSiegingOther())) {
            final String action;
			final String timeLeft;
			final String clanActing;

			if (c.isBeingSieged()) {
                action = "Sieged";
                timeLeft = c.getClansSiegingSelf().get(0).getRemainingString(System.currentTimeMillis());
                clanActing = c.getClansSiegingSelf().get(0).getClanSieging().getName();
            } else {
                action = "Sieging";
                timeLeft = c.getClansSiegedBySelf().get(0).getRemainingString(System.currentTimeMillis());
                clanActing = c.getClansSiegedBySelf().get(0).getClanSieged().getName();
            }

            lines[13] = "&d&l" + action + " " + String.format("&f(%s&f)", ChatColor.RED + clanActing);
            lines[14] = "&f" + timeLeft;
        }

        e.getScoreboard().setLines(lines);
    }

    @EventHandler
    public void onLeave(final PlayerQuitEvent e) {
        if (this.scoreboardCache.containsKey(e.getPlayer())) {
            this.scoreboardCache.get(e.getPlayer()).stop();
            this.scoreboardCache.remove(e.getPlayer());
        }
    }

    /*
     * NAME TAGS
     */
    public void updateNameTagsForAll() {
        for (final Player p : Bukkit.getOnlinePlayers()) {
            for (final Player ps : Bukkit.getOnlinePlayers()) {
                this.updateNameTags(p, ps);
                if (p != ps) {
					this.updateNameTags(ps, p);
				}
            }
        }
    }

    public void updateNameTags(final Player player, final Player playersToSee) {
        final ClansPlayer cp = new ClansPlayer(player);
        final ClansPlayer toSee = new ClansPlayer(playersToSee);

        final Clan clan = cp.getRealClan();
        final Clan otherclan = toSee.getRealClan();
        final boolean clanless = clan == null;

        String clanprefix = "", clanname = "", nameprefix = ClanRelations.NEUTRAL.getPlayerColor().toString();


        if (!clanless) {
            clanname = clan.getName() + " ";
            clanprefix = ClanRelations.NEUTRAL.getClanColor().toString();

            if (otherclan != null) {
                final ClanRelations r = clan.getClanRelation(otherclan.getUniqueId());
                clanprefix = r.getClanColor().toString();
                nameprefix = r.getPlayerColor().toString();
            }
        }

        boolean focus = false;
        if (toSee.hasFocus() && toSee.getFocus().equals(player)) {
            // Is Focusing Him
            clanprefix = UtilFocus.CLAN_FOCUS.toString();
            nameprefix = UtilFocus.PLAYER_FOCUS.toString();
            focus = true;
        }

        final Nametag packet = new Nametag(player, focus ? "Focus" : clan == null ? "None" : clan.getName(), clanprefix + clanname + nameprefix);
        packet.send(playersToSee);
    }

    // END


    /*
     * TAB LIST
     */
    public void updateTabListForAll() {
        for (final Player p : Bukkit.getOnlinePlayers()) {
			this.updateTab(p);
		}
    }

    public void updateTab(final Player p) {
        // TODO: Display custom tablist
    }
}