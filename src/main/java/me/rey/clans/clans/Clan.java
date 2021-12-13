package me.rey.clans.clans;

import me.rey.clans.Tribes;
import me.rey.clans.siege.Siege;
import me.rey.clans.utils.References;
import me.rey.clans.utils.UtilText;
import me.rey.parser.Text;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class Clan {

    private final UUID uuid;
    String name;
    String founder;
    HashMap<UUID, ClansRank> players;
    ArrayList<Chunk> territory;
    HashMap<UUID, Long> warpoints;
    HashMap<UUID, ClanRelations> clanRelations;
    Location home;
    private long energy = 0;

    public Clan(final String name, final String founder, final UUID uuid, final ClansPlayer[] players) {
        this.name = name;
        this.founder = founder;
        this.uuid = uuid;
        this.warpoints = new HashMap<UUID, Long>();
        this.clanRelations = new HashMap<UUID, ClanRelations>();
        this.home = null;

        this.players = new HashMap<UUID, ClansRank>();
        for (final ClansPlayer p : players) {
            this.players.put(p.getUniqueId(), ClansRank.RECRUIT);
        }

        this.territory = new ArrayList<Chunk>();
    }

    public Clan(final String name, final String founder, final UUID uuid, final HashMap<UUID, ClansRank> players) {
        this.name = name;
        this.founder = founder;
        this.uuid = uuid;
        this.warpoints = new HashMap<UUID, Long>();
        this.clanRelations = new HashMap<UUID, ClanRelations>();
        this.home = null;

        this.players = players;
        this.territory = new ArrayList<Chunk>();
    }

    public void announceToClan(final String text, final ClansPlayer... exclude) {
        for (final ClansPlayer o : this.getOnlinePlayers().keySet()) {
            boolean e = false;
            for (final ClansPlayer ex : exclude) {
                if (o.getUniqueId() == ex.getUniqueId()) {
					e = true;
				}
            }
            if (!e) {
				o.sendMessageWithPrefix("Tribe", text);
			}
        }
    }

    public void announceToClan(final String text, final boolean prefix, final ClansPlayer... exclude) {
        for (final ClansPlayer o : this.getOnlinePlayers().keySet()) {
            boolean e = false;
            for (final ClansPlayer ex : exclude) {
                if (o.getUniqueId() == ex.getUniqueId()) {
					e = true;
				}
            }
            if (!e && prefix) {
				o.sendMessageWithPrefix("Tribe", text);
			}
            if (!e && !prefix) {
				o.sendMessage(text);
			}
        }
    }

    public boolean isServerClan() {
        for (final ServerClan type : ServerClan.values()) {
            if (type.getName().equalsIgnoreCase(this.getName())) {
				return true;
			}
        }
        return false;
    }

    public void shoutToRelation(final ClanRelations relation, final Player shouter, final String message) {
        final ClanRelations r = relation;
        final ChatColor playerColor;
		final ChatColor messageColor;
		final String prefix = UtilText.getPrefix(shouter);

        switch (r) {
            case SELF:
                playerColor = r.getPlayerColor();
                messageColor = r.getClanColor();
                break;
            default:
                playerColor = r.getClanColor();
                messageColor = r.getPlayerColor();
                break;
        }

        final String text = Text.color(prefix + playerColor + (r.getId() == ClanRelations.SELF.getId() ? "" : this.getName() + " ") + shouter.getName() + " " + messageColor + message);

        for (final UUID uuid : this.getRelations().keySet()) {
            if (this.getClanRelation(uuid).getId() != r.getId()) {
				continue;
			}

            final Clan related = Tribes.getInstance().getClan(uuid);

            for (final ClansPlayer toShout : related.getOnlinePlayers().keySet()) {
                toShout.getPlayer().sendMessage(text);
            }
        }

        for (final ClansPlayer inside : this.getOnlinePlayers().keySet()) {
            inside.getPlayer().sendMessage(text);
        }

    }

    public Location getHome() {
        return this.home;
    }

    public void setHome(final Location home) {
        this.home = home;
    }

    public boolean compare(final Clan clan) {
        if (clan == null) {
			return false;
		}
        return this.getUniqueId().equals(clan.getUniqueId());
    }

    public long getEnergy() {
        return this.energy;
    }

    public Clan setEnergy(final long energy) {
        this.energy = energy;
        return this;
    }

    public double getEnergyDays() {
        return this.getEnergy() * References.MAX_ENERGY_DAYS / References.MAX_ENERGY;
    }

    public String getEnergyString() {
        final double count = this.getEnergyDays();
        final String countString = String.format("%.1f", count < 1.0 ? count * 24 : count);
        return count <= 0 ? "N/A" : countString + " " + (count < 1.0 ? "Hours" : "Days");
    }

    public String getFounder() {
        return this.founder;
    }

    public String getName() {
        return this.name;
    }

    public Clan setName(final String name) {
        this.name = name;
        return this;
    }

    public UUID getUniqueId() {
        return this.uuid;
    }


    /*
     *  PLAYERS SECTION
     */

    public HashMap<UUID, ClansRank> getPlayers(final boolean withFakes) {
        final HashMap<UUID, ClansRank> clone = new HashMap<>();
		final HashMap<UUID, ClansRank> toSend = new HashMap<UUID, ClansRank>();
		final List<UUID> exclude = new ArrayList<>();
        this.players.forEach((k, v) -> clone.put(k, v));

        if (withFakes) {
            for (final UUID player : Tribes.adminFakeClans.keySet()) {
                if (Tribes.adminFakeClans.get(player).equals(this.getUniqueId())) {
					clone.put(player, ClansRank.ADMIN);
				}
            }

            for (final UUID uuid : clone.keySet()) {
                if (new ClansPlayer(uuid).isInFakeClan() && new ClansPlayer(uuid).getRealClan() != null && new ClansPlayer(uuid).getRealClan().compare(this)) {
					exclude.add(uuid);
				}
            }
        }

        for (final UUID uuid : clone.keySet()) {
            if (exclude.contains(uuid)) {
				continue;
			}
            toSend.put(uuid, clone.get(uuid));
        }
        return toSend;
    }

    public HashMap<UUID, ClansRank> getPlayers() {
        return this.getPlayers(true);
    }

    public boolean hasMaxMembers() {
        return this.getPlayers(false).size() >= References.MAX_MEMBERS;
    }

    public ArrayList<UUID> getPlayersFromRank(final ClansRank rank, final boolean fakes) {
        final ArrayList<UUID> players = new ArrayList<>();
        for (final UUID p : this.getPlayers(fakes).keySet()) {
            if (this.getPlayerRank(p, fakes) == rank) {
				players.add(p);
			}
        }
        return players;
    }

    public ArrayList<UUID> getPlayersFromRank(final ClansRank rank) {
        return this.getPlayersFromRank(rank, true);
    }

    public boolean setRank(final UUID player, final ClansRank rank) {
        if (this.isInClan(player)) {
            this.players.put(player, rank);
            return true;
        }
        return false;
    }

    public ClansRank getPlayerRank(final UUID player, final boolean fakes) {
        if (this.isInClan(player, fakes)) {
            for (final UUID cp : this.getPlayers(fakes).keySet()) {
                if (cp.equals(player)) {
					return this.getPlayers(fakes).get(cp);
				}
            }
        }
        return null;
    }

    public ClansRank getPlayerRank(final UUID player) {
        return this.getPlayerRank(player, true);
    }

    public boolean isInClan(final UUID player, final boolean fakes) {
        return this.getMatchingPlayer(player, fakes) != null;
    }

    public boolean isInClan(final UUID player) {
        return this.isInClan(player, true);
    }

    public boolean isInClan(final String name) {
        return this.getPlayer(name) != null;
    }

    public ClansPlayer getPlayer(final String name) {
        for (final UUID uuid : this.getPlayers().keySet()) {
            final ClansPlayer cp = new ClansPlayer(uuid);
            if (cp.isOnline() && cp.getPlayer().getName().equalsIgnoreCase(name)) {
				return cp;
			}
            if (!cp.isOnline() && cp.getOfflinePlayer().getName().equalsIgnoreCase(name)) {
				return cp;
			}
        }
        return null;
    }

    public boolean kickPlayer(final UUID player) {
        if (this.isInClan(player)) {
            if (new ClansPlayer(player).isInFakeClan() && new ClansPlayer(player).getFakeClan().compare(this)) {
				return false;
			}
            this.players.remove(player);
            return true;
        }
        return false;
    }

    public boolean demote(final UUID player) {
        if (this.isInClan(player)) {
            ClansRank rank = null;
            final int index = this.getPlayerRank(player).ordinal();

            for (final ClansRank r : ClansRank.values()) {
                if (r == ClansRank.NONE) {
					continue;
				}
                if (r.ordinal() == index - 1) {
                    rank = r;
                    break;
                }
            }

            if (rank != null) {
                this.setRank(player, rank);
                return true;
            }

        }
        return false;
    }

    public boolean promote(final UUID player) {
        if (this.isInClan(player)) {
            ClansRank rank = null;
            final int index = this.getPlayerRank(player).ordinal();

            for (final ClansRank r : ClansRank.values()) {
                if (r == ClansRank.NONE) {
					continue;
				}
                if (r.ordinal() == index + 1) {
                    rank = r;
                    break;
                }
            }

            if (rank != null) {
                this.setRank(player, rank);
                return true;
            }

        }
        return false;
    }

    public boolean promotable(final UUID player) {
        if (this.isInClan(player)) {
            ClansRank rank = null;
            final int index = this.getPlayerRank(player).ordinal();

            for (final ClansRank r : ClansRank.values()) {
                if (r == ClansRank.NONE) {
					continue;
				}
                if (r.ordinal() == index + 1) {
                    rank = r;
                    break;
                }
            }

            if (rank != null) {
                return true;
            }

        }
        return false;
    }

    public HashMap<ClansPlayer, ClansRank> getOnlinePlayers(final boolean fakes) {
        final HashMap<ClansPlayer, ClansRank> online = new HashMap<ClansPlayer, ClansRank>();
        for (final UUID uuid : this.getPlayers(fakes).keySet()) {
            final ClansPlayer cp = new ClansPlayer(uuid);
            if (cp.getPlayer() != null && cp.getPlayer().isOnline()) {
				online.put(cp, this.getPlayers(fakes).get(uuid));
			}
        }
        return online;
    }

    public HashMap<ClansPlayer, ClansRank> getOnlinePlayers() {
        return this.getOnlinePlayers(true);
    }

    public void addPlayer(final UUID uuid, final ClansRank rank) {
        if (this.getMatchingPlayer(uuid) != null) {
			return;
		}

        this.players.put(uuid, rank);
    }

    public ClansPlayer getMatchingPlayer(final UUID player, final boolean fakes) {
        for (final UUID cp : this.getPlayers(fakes).keySet()) {
            if (cp.equals(player)) {
                return new ClansPlayer(cp);
            }
        }
        return null;
    }

    public ClansPlayer getMatchingPlayer(final UUID player) {
        return this.getMatchingPlayer(player, true);
    }

    /*
     *  TERRITORY SECTION
     */

    public ArrayList<Chunk> getTerritory() {
        return this.territory;
    }

    public boolean hasMaxTerritory() {
        return this.getTerritory().size() >= References.MAX_TERRITORY || this.getTerritory().size() >= this.getPossibleTerritory();
    }

    public int getPossibleTerritory() {
        return this.getPlayers(false).size() + 2;
    }

    public boolean addTerritory(final Chunk chunk) {
        if (this.territory.contains(chunk)) {
			return false;
		}
        Tribes.getInstance().territory.put(chunk, this.getUniqueId());

        this.territory.add(chunk);
        return true;
    }

    public boolean removeTerritory(final Chunk chunk) {
        if (!this.territory.contains(chunk)) {
			return false;
		}
        if (Tribes.getInstance().getClanFromTerritory(chunk) != null) {
			Tribes.getInstance().removeTerritory(chunk);
		}

        this.territory.remove(chunk);
        return true;
    }

    public void addTerritory(final ArrayList<Chunk> chunks) {
        for (final Chunk chunk : chunks) {
            this.addTerritory(chunk);
        }
    }

    public void unclaimAll() {
        final Iterator<Chunk> chunks = this.getTerritory().iterator();
        while (chunks.hasNext()) {
            final Chunk next = chunks.next();
            if (Tribes.getInstance().getClanFromTerritory(next) != null) {
				Tribes.getInstance().removeTerritory(next);
			}
        }
        this.territory.clear();
    }

    /*
     *  WARPOINTS SECTION
     */

    public long setWarpoint(final UUID clan, final long warpoints) {
        if (warpoints == 0 && this.warpoints.containsKey(clan)) {
            this.warpoints.remove(clan);
            return 0;
        }
        this.warpoints.put(clan, warpoints);
        return warpoints;
    }

    public long getWarpointsOnClan(final UUID clan) {
        if (!this.warpoints.containsKey(clan)) {
			return 0;
		}
        return this.warpoints.get(clan);
    }

    public HashMap<UUID, Long> getWarpoints() {
        return this.warpoints;
    }

    public void setWarpointsMap(final HashMap<UUID, Long> wps) {
        this.warpoints = wps;
    }

    /*
     *  CLAN RELATIONS SECTION
     */

    public void setRelation(final UUID clan, final ClanRelations relation) {
        if (!relation.shouldSave()) {
			return;
		}
        this.clanRelations.put(clan, relation);
    }

    public void removeRelation(final UUID clan) {
        this.clanRelations.remove(clan);
    }

    public ClanRelations getClanRelation(final UUID clan) {
        if (clan.equals(this.getUniqueId())) {
			return ClanRelations.SELF;
		}

        if (!this.clanRelations.containsKey(clan)) {
            if (this.getWarpointsOnClan(clan) >= 10 || this.getWarpointsOnClan(clan) <= -10) {
				return ClanRelations.ENEMY;
			}
            return ClanRelations.NEUTRAL;
        }
        return this.clanRelations.get(clan);
    }

    public HashMap<UUID, ClanRelations> getRelations() {
        return this.clanRelations;
    }

    public boolean hasMaxAllies() {
        int allies = 0;
        for (final UUID uuid : this.getRelations().keySet()) {
            if (this.getClanRelation(uuid).equals(ClanRelations.ALLY)) {
				allies++;
			}
        }
        return allies >= References.MAX_ALLIES;
    }

    public boolean isAlliedTo(Clan clan) {
        ClanRelations relation = getClanRelation(clan.uuid);
        return relation == ClanRelations.ALLY;
    }

    //  ------------------------------------------- REMOVED
//	public boolean hasMaxTruces() {
//		int allies = 0;
//		for(UUID uuid : this.getRelations().keySet()) {
//			if(getClanRelation(uuid).equals(ClanRelations.TRUCE))
//				allies++;
//		}
//		return allies >= References.MAX_TRUCES;
//	}
//	
    public void setRelationsMap(final HashMap<UUID, ClanRelations> relations) {
        this.clanRelations = relations;
    }

    /*
     * SIEGE SYSTEM
     */

    public boolean isBeingSieged() {
        return this.getClansSiegingSelf() != null && !this.getClansSiegingSelf().isEmpty();
    }

    public ArrayList<Siege> getClansSiegingSelf() {
        final ArrayList<Siege> siegersOnSelf = new ArrayList<Siege>();
        for (final UUID siegers : Siege.sieges.keySet()) {
            final ArrayList<Siege> siegerSieging = Siege.sieges.get(siegers) == null ? new ArrayList<>() : Siege.sieges.get(siegers);
            final Iterator<Siege> siegerIterator = siegerSieging.iterator();
            while (siegerIterator.hasNext()) {
                final Siege found = siegerIterator.next();
                if (found.getClanSieged().getUniqueId().equals(this.getUniqueId())) {
                    siegersOnSelf.add(found);
                }
            }
        }
        return siegersOnSelf;
    }

    public boolean isSiegingOther() {
        return this.getClansSiegedBySelf() != null && !this.getClansSiegedBySelf().isEmpty();
    }

    public ArrayList<Siege> getClansSiegedBySelf() {
        return Siege.sieges.get(this.getUniqueId()) == null ? new ArrayList<>() : Siege.sieges.get(this.getUniqueId());
    }

    // for debugging
    @Override
    public String toString() {
        final Map<String, Object> map = new HashMap<>();
        map.put("name", this.name);
        map.put("founder", this.founder);
        map.put("uuid", this.uuid);
        map.put("players", this.players);
        map.put("territory", this.territory);
        map.put("warpoints", this.warpoints);
        map.put("clanRelations", this.clanRelations);
        map.put("home", this.home);
        map.put("energy", this.energy);
        return map.toString();
    }
}
