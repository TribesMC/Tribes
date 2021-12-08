package me.rey.clans.database;

import me.rey.clans.Tribes;
import me.rey.clans.clans.*;
import me.rey.clans.playerdisplay.PlayerInfo;
import me.rey.clans.utils.References;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SQLManager {

    private final JavaPlugin plugin;
    private final ConnectionPoolManager pool;
    private final String clansDataTable, clansPlayerDataTable, clansSettingsTable;
    private final String safeZoneSetting = "safezone";
    private final String resetIdSetting = "resetId";

    public SQLManager(final Tribes plugin) {
        this.plugin = plugin.getPlugin();
        this.pool = new ConnectionPoolManager(this.plugin);
        this.clansDataTable = this.plugin.getConfig().getConfigurationSection("mysql").getString("clans_data_table");
        this.clansPlayerDataTable = this.plugin.getConfig().getConfigurationSection("mysql").getString("clans_player_data_table");
        this.clansSettingsTable = this.plugin.getConfig().getConfigurationSection("mysql").getString("clans_settings_table");
        this.makeTable();
    }

    public void onDisable() {
        this.pool.closePool();
    }

    private void makeTable() {
        Connection conn = null;
        PreparedStatement ps = null, ps2 = null, ps3 = null;

        try {
            conn = this.pool.getConnection();
            ps = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `" + this.clansDataTable + "` " +
                            "(" +
                            "uuid TEXT, name TEXT, founder TEXT, energy BIGINT, home TEXT, members TEXT, territory TEXT, relations TEXT, warpoints TEXT" +
                            ")"
            );
            ps.executeUpdate();

            ps2 = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `" + this.clansPlayerDataTable + "` " +
                            "(" +
                            "name TEXT, uuid TEXT, balance INT, clan TEXT" +
                            ")"
            );
            ps2.executeUpdate();

            ps3 = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS `" + this.clansSettingsTable + "` " +
                            "(" +
                            "name TEXT, value TEXT" +
                            ")"
            );
            ps3.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, null);
            this.pool.close(null, ps2, null);
            this.pool.close(null, ps3, null);
        }
    }

    public boolean settingExists(final String setting) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();
            final String stmt = "SELECT * FROM " + this.clansSettingsTable + " WHERE name=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, setting);
            res = ps.executeQuery();

            if (res.next()) {
                return true;
            }

            return false;

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
        }
        return false;
    }

    public boolean createSetting(final String setting) {
        Connection conn = null;
        PreparedStatement ps = null, insert = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.clansSettingsTable + " WHERE name = ?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, setting);
            res = ps.executeQuery();

            res.next();
            if (!this.settingExists(setting)) {
                final String stmt2 = "INSERT INTO " + this.clansSettingsTable
                        + "(name,value) VALUE(?,?)";
                insert = conn.prepareStatement(stmt2);
                insert.setString(1, setting);
                insert.setString(2, null);
                insert.executeUpdate();
                return true;
            }
            return false;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
            this.pool.close(null, insert, null);
        }
        return false;
    }

    public String getSetting(final String setting) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            this.createSetting(setting);
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.clansSettingsTable + " WHERE name=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, setting);
            rs = ps.executeQuery();

            while (rs.next()) {
                return rs.getString("value");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, rs);
        }
        return null;
    }

    public boolean clanExists(final String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();
            final String stmt = "SELECT * FROM " + this.clansDataTable + " WHERE name=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, name);
            res = ps.executeQuery();

            if (res.next()) {
                return true;
            }

            return false;

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
        }
        return false;
    }

    public boolean clanExists(final UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();
            final String stmt = "SELECT * FROM " + this.clansDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, uuid.toString());
            res = ps.executeQuery();

            if (res.next()) {
                return true;
            }

            return false;

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
        }
        return false;
    }

    public boolean playerExists(final UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();
            final String stmt = "SELECT * FROM " + this.clansPlayerDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, uuid.toString());
            res = ps.executeQuery();

            if (res.next()) {
                return true;
            }

            return false;

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
        }
        return false;
    }

    public boolean playerExists(final Player player) {
        return this.playerExists(player.getUniqueId());
    }

    public boolean playerExists(final String name) {
        return this.getPlayerFromName(name) != null;
    }

    public boolean createPlayer(final Player player) {
        final boolean success = this.createPlayer(player.getUniqueId());
        if (success) {
            this.setPlayerData(player.getUniqueId(), "name", player.getUniqueId());
        }
        return success;
    }

    public boolean createPlayer(final UUID player) {
        if (this.playerExists(player)) {
            return false;
        }

        Connection conn = null;
        PreparedStatement ps = null, insert = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.clansPlayerDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, player.toString());
            res = ps.executeQuery();

            res.next();

            final String stmt2 = "INSERT INTO " + this.clansPlayerDataTable
                    + "(name,uuid,balance,clan) VALUE(?,?,?,?)";
            insert = conn.prepareStatement(stmt2);
            insert.setString(1, null);
            insert.setString(2, player.toString());
            insert.setInt(3, 16000);
            insert.setString(4, null);
            insert.executeUpdate();

            final HashMap<String, Object> data = new HashMap<String, Object>();
            data.put("clan", null);
            data.put("balance", 16000);
            Tribes.playerdata.put(player, data);

            return true;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
            this.pool.close(null, insert, null);
        }
        return false;
    }

    public boolean createClan(final UUID uuid, final String name, final String founder, final Player leader) {

        Connection conn = null;
        PreparedStatement ps = null, insert = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.clansDataTable + " WHERE name=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, name);
            res = ps.executeQuery();

            res.next();
            if (!this.clanExists(name)) {
                final HashMap<Integer, ArrayList<String>> members = new HashMap<>();
                if (leader != null) {
                    final ArrayList<String> array = new ArrayList<String>();
                    array.add(leader.getUniqueId().toString());
                    members.put(ClansRank.LEADER.getId(), array);
                }

                final String stmt2 = "INSERT INTO " + this.clansDataTable
                        + "(uuid,name,founder,energy,home,members,territory,relations,warpoints) VALUE(?,?,?,?,?,?,?,?,?)";
                insert = conn.prepareStatement(stmt2);
                insert.setString(1, uuid.toString());
                insert.setString(2, name);
                insert.setString(3, founder);
                insert.setInt(4, References.DEFAULT_ENERGY);
                insert.setString(5, null);
                insert.setString(6, new JSONObject(members).toJSONString());
                insert.setString(7, null);
                insert.setString(8, null);
                insert.setString(9, null);
                insert.executeUpdate();

                if (leader != null) {
                    this.createPlayer(leader);
                    this.setPlayerData(leader.getUniqueId(), "clan", uuid.toString());
                }

            } else {
                return false;
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
            this.pool.close(null, insert, null);
        }

        boolean existsInEnergy = false;
        final Clan self = this.getClan(uuid);
        for (final Clan clan : Tribes.clans) {
            if (clan.compare(self)) {
                existsInEnergy = true;
            }
        }

        if (!existsInEnergy) {
            Tribes.clans.add(this.getClan(uuid));
        }


        final PlayerInfo info = new PlayerInfo();
        info.updateNameTagsForAll();

        return true;
    }

    public boolean createClan(final UUID uuid, final String name, final Player founder) {
        return this.createClan(uuid, name, founder.getName(), founder);
    }

    public boolean saveClan(final Clan clan) {

        Connection conn = null;
        PreparedStatement ps = null;
        final PreparedStatement insert = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.clansDataTable + " WHERE name=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, clan.getName());
            res = ps.executeQuery();

            res.next();
            if (!this.clanExists(clan.getName())) {
                this.createClan(clan.getUniqueId(), clan.getName(), null);
            }

            final UUID uuid = clan.getUniqueId();
            String home = clan.getHome() == null ? null : "";

            /*
             * CLAN HOME
             */
            if (clan.getHome() != null) {
                home += clan.getHome().getBlockX() + ";" + clan.getHome().getBlockY() + ";" + clan.getHome().getBlockZ();
            }

            /*
             * MEMBERS
             */
            final HashMap<Integer, ArrayList<String>> members = new HashMap<>();
            for (final UUID u : clan.getPlayers(false).keySet()) {
                final ClansPlayer m = new ClansPlayer(u);
                final ClansRank rank = clan.getPlayers(false).get(u);

                final ArrayList<String> membersUUID = members.get(rank.getId()) == null ? new ArrayList<>() : members.get(rank.getId());
                membersUUID.add(m.getUniqueId().toString());

                members.put(rank.getId(), membersUUID);
                this.setPlayerData(m.getUniqueId(), "clan", clan.getUniqueId().toString());
            }

            /*
             * TERRITORY
             */
            final ArrayList<String> territory = new ArrayList<String>();
            for (final Chunk chunk : clan.getTerritory()) {
                territory.add(String.format("%s;%s", chunk.getX(), chunk.getZ()));
            }
            final HashMap<String, ArrayList<String>> chunks = new HashMap<>();
            chunks.put("territory", territory);

            /*
             *  CLAN RELATIONS
             */
            final HashMap<Integer, ArrayList<String>> relations = new HashMap<>();
            for (final UUID related : clan.getRelations().keySet()) {
                final ClanRelations relation = clan.getClanRelation(related);
                if (!relation.shouldSave()) {
                    continue;
                }

                final ArrayList<String> clansUUID = relations.get(relation.getId()) == null ? new ArrayList<>() : relations.get(relation.getId());
                clansUUID.add(related.toString());

                relations.put(relation.getId(), clansUUID);
            }

            /*
             * WARPOINTS
             */
            final HashMap<String, HashMap<String, Long>> warpoints = new HashMap<>();
            final HashMap<String, Long> positive = new HashMap<>();
            final HashMap<String, Long> negative = new HashMap<>();
            for (final UUID opponent : clan.getWarpoints().keySet()) {
                final long wp = clan.getWarpointsOnClan(opponent);
                if (wp >= 0) {
                    positive.put(opponent.toString(), clan.getWarpointsOnClan(opponent));
                    warpoints.put("positive", positive);
                } else {
                    negative.put(opponent.toString(), clan.getWarpointsOnClan(opponent));
                    warpoints.put("negative", negative);
                }
            }

            this.setClanData(uuid, "name", clan.getName());
            this.setClanData(uuid, "founder", clan.getFounder());
            this.setClanData(uuid, "energy", clan.getEnergy());
            this.setClanData(uuid, "home", home);
            this.setClanData(uuid, "members", new JSONObject(members).toJSONString());
            this.setClanData(uuid, "territory", new JSONObject(chunks).toJSONString());
            this.setClanData(uuid, "relations", new JSONObject(relations).toJSONString());
            this.setClanData(uuid, "warpoints", new JSONObject(warpoints).toJSONString());

            return true;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
            this.pool.close(null, insert, null);
        }


        final PlayerInfo info = new PlayerInfo();
        info.updateNameTagsForAll();

        return false;
    }

    public Clan getClan(final UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null;
        final PreparedStatement insert = null;
        ResultSet res = null;

        try {
            if (!this.clanExists(uuid)) {
                return null;
            }

            conn = this.pool.getConnection();
            final String stmt = "SELECT * FROM " + this.clansDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, uuid.toString());
            res = ps.executeQuery();

            while (res.next()) {
                final String name;
                final String founder;
                long energy = 0;
                final HashMap<UUID, ClansRank> members = new HashMap<>();
                final ArrayList<Chunk> territory = new ArrayList<>();
                final HashMap<UUID, ClanRelations> relations = new HashMap<>();
                final HashMap<UUID, Long> warpoints = new HashMap<>();
                Location home = null;

                // setting name
                name = res.getString("name");

                // setting founder
                founder = res.getString("founder");

                // setting energy;
                energy = res.getLong("energy");

                // setting home
                if (res.getString("home") != null) {
                    final String[] coords = res.getString("home").split(";");
                    final int x = Integer.parseInt(coords[0]);
                    final int y = Integer.parseInt(coords[1]);
                    final int z = Integer.parseInt(coords[2]);
                    home = new Location(Tribes.getInstance().getClansWorld(), x, y, z);
                }

                // setting members
                final JSONObject objMembers = (JSONObject) new JSONParser().parse(res.getString("members"));
                for (final Object o : objMembers.keySet()) {
                    final int rankId = Integer.parseInt((String) o);
                    final ClansRank rank = ClansRank.getRankFromId(rankId);

                    final JSONArray jsonMembers = (JSONArray) objMembers.get((String) o);
                    for (final Object string : jsonMembers) {
                        final UUID memberUuid = UUID.fromString((String) string);

                        members.put(memberUuid, rank);
                    }
                }

                //territory
                if (res.getString("territory") != null) {
                    final JSONObject objTerritory = (JSONObject) new JSONParser().parse(res.getString("territory"));
                    final JSONArray arrayTerritory = (JSONArray) objTerritory.get("territory");
                    for (final Object entry : arrayTerritory) {
                        final String[] coords = ((String) entry).split(";");
                        final Chunk chunk = Tribes.getInstance().getClansWorld().getChunkAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));

                        territory.add(chunk);
                    }
                }


                //relations
                if (res.getString("relations") != null) {
                    final JSONObject objRelations = (JSONObject) new JSONParser().parse(res.getString("relations"));
                    for (final Object o : objRelations.keySet()) {
                        final int relationId = Integer.parseInt((String) o);
                        final ClanRelations relation = ClanRelations.getRelationFromId(relationId);

                        final JSONArray jsonRelations = (JSONArray) objRelations.get((String) o);
                        for (final Object string : jsonRelations) {
                            final UUID clanUuid = UUID.fromString((String) string);
                            relations.put(clanUuid, relation);
                        }
                    }
                }

                //warpoints
                if (res.getString("warpoints") != null) {
                    final JSONObject objWP = (JSONObject) new JSONParser().parse(res.getString("warpoints"));
                    for (final Object o : objWP.keySet()) {

                        final JSONObject entries = (JSONObject) objWP.get((String) o);
                        for (final Object string : entries.keySet()) {
                            final UUID clanUuid = UUID.fromString((String) string);
                            final long warpoint = (long) entries.get((String) string);
                            warpoints.put(clanUuid, warpoint);
                        }
                    }
                }

                final Clan toGive = new Clan(name, founder, uuid, members);
                toGive.setEnergy(energy);
                toGive.setHome(home);
                toGive.setWarpointsMap(warpoints);
                toGive.setRelationsMap(relations);
                toGive.addTerritory(territory);
                return toGive;
            }

            return null;
        } catch (final SQLException | ParseException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
            this.pool.close(null, insert, null);
        }
        return null;
    }

    public Clan getClan(final String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        final PreparedStatement insert = null;
        ResultSet res = null;

        try {
            if (!this.clanExists(name)) {
                return null;
            }

            conn = this.pool.getConnection();
            final String stmt = "SELECT * FROM " + this.clansDataTable + " WHERE name=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, name);
            res = ps.executeQuery();

            while (res.next()) {
                final UUID uuid;
                final String founder;
                final String namex;
                long energy = 0;
                final HashMap<UUID, ClansRank> members = new HashMap<>();
                final ArrayList<Chunk> territory = new ArrayList<>();
                final HashMap<UUID, ClanRelations> relations = new HashMap<>();
                final HashMap<UUID, Long> warpoints = new HashMap<>();
                Location home = null;

                // setting name
                namex = res.getString("name");

                // setting uuid
                uuid = UUID.fromString(res.getString("uuid"));

                // setting founder
                founder = res.getString("founder");

                // setting energy;
                energy = res.getLong("energy");

                // setting home
                if (res.getString("home") != null) {
                    final String[] coords = res.getString("home").split(";");
                    final int x = Integer.parseInt(coords[0]);
                    final int y = Integer.parseInt(coords[1]);
                    final int z = Integer.parseInt(coords[2]);
                    home = new Location(Tribes.getInstance().getClansWorld(), x, y, z);
                }


                // setting members
                final JSONObject objMembers = (JSONObject) new JSONParser().parse(res.getString("members"));
                for (final Object o : objMembers.keySet()) {
                    final int rankId = Integer.parseInt((String) o);
                    final ClansRank rank = ClansRank.getRankFromId(rankId);

                    final JSONArray jsonMembers = (JSONArray) objMembers.get((String) o);
                    for (final Object string : jsonMembers) {
                        final UUID memberUuid = UUID.fromString((String) string);

                        members.put(memberUuid, rank);
                    }
                }

                //territory
                if (res.getString("territory") != null) {
                    final JSONObject objTerritory = (JSONObject) new JSONParser().parse(res.getString("territory"));
                    final JSONArray arrayTerritory = (JSONArray) objTerritory.get("territory");
                    for (final Object entry : arrayTerritory) {
                        final String[] coords = ((String) entry).split(";");
                        final Chunk chunk = Tribes.getInstance().getClansWorld().getChunkAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]));

                        territory.add(chunk);
                    }
                }


                //relations
                if (res.getString("relations") != null) {
                    final JSONObject objRelations = (JSONObject) new JSONParser().parse(res.getString("relations"));
                    for (final Object o : objRelations.keySet()) {
                        final int relationId = Integer.parseInt((String) o);
                        final ClanRelations relation = ClanRelations.getRelationFromId(relationId);

                        final JSONArray jsonRelations = (JSONArray) objRelations.get((String) o);
                        for (final Object string : jsonRelations) {
                            final UUID clanUuid = UUID.fromString((String) string);
                            relations.put(clanUuid, relation);
                        }
                    }
                }

                //warpoints
                if (res.getString("warpoints") != null) {
                    final JSONObject objWP = (JSONObject) new JSONParser().parse(res.getString("warpoints"));
                    for (final Object o : objWP.keySet()) {

                        final JSONObject entries = (JSONObject) objWP.get((String) o);
                        for (final Object string : entries.keySet()) {
                            final UUID clanUuid = UUID.fromString((String) string);
                            final long warpoint = (long) entries.get((String) string);
                            warpoints.put(clanUuid, warpoint);
                        }
                    }
                }

                final Clan toGive = new Clan(namex, founder, uuid, members);
                toGive.setEnergy(energy);
                toGive.setHome(home);
                toGive.setWarpointsMap(warpoints);
                toGive.setRelationsMap(relations);
                toGive.addTerritory(territory);
                return toGive;
            }

            return null;
        } catch (final SQLException | ParseException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
            this.pool.close(null, insert, null);
        }
        return null;
    }

    public void deleteClan(final UUID uuid) {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            final Clan clan = Tribes.getInstance().getClan(uuid);
            if (clan == null) {
                return;
            }


            Clan toRemove = null;
            for (final Clan clanL : Tribes.clans) {
                if (uuid.equals(clanL.getUniqueId())) {
                    toRemove = clan;
                }
            }

            if (toRemove != null) {
                Tribes.clans.remove(toRemove);
            }


            for (final Chunk chunk : clan.getTerritory()) {
                Tribes.getInstance().territory.remove(chunk);
            }

            final ArrayList<UUID> playersToRemove = new ArrayList<UUID>();
            for (final UUID player : Tribes.adminFakeClans.keySet()) {
                if (Tribes.adminFakeClans.get(player).equals(clan.getUniqueId())) {
                    playersToRemove.add(player);
                }
            }

            for (final UUID player : playersToRemove) {
                Tribes.adminFakeClans.remove(player);
            }

            if (clan.getHome() != null) {
                clan.getHome().getBlock().setType(Material.AIR);
            }

            conn = this.pool.getConnection();
            final String stmt = "DELETE FROM " + this.clansDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, uuid.toString());
            ps.executeUpdate();

            for (final UUID cp : clan.getPlayers().keySet()) {
                this.setPlayerData(cp, "clan", null);
            }

            for (final UUID uR : clan.getRelations().keySet()) {
                final Clan related = Tribes.getInstance().getClan(uR);
                if (related == null || related.compare(clan)) {
                    continue;
                }
                related.removeRelation(clan.getUniqueId());
                this.saveClan(related);
            }

            for (final UUID uR : clan.getWarpoints().keySet()) {
                final Clan enemy = Tribes.getInstance().getClan(uR);
                if (enemy == null || enemy.compare(clan)) {
                    continue;
                }
                enemy.setWarpoint(clan.getUniqueId(), 0);
                this.saveClan(enemy);
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, null);
        }

        final PlayerInfo info = new PlayerInfo();
        info.updateNameTagsForAll();

    }

    public void setClanData(final UUID uuid, final String column, final Object data) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "UPDATE " + this.clansDataTable + " SET " + column + "=?  WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setObject(1, data);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, null);
        }
    }

    public Object getClanData(final UUID uuid, final String column) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.clansDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                return rs.getObject(column);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, rs);
        }
        return null;
    }

    public void setPlayerData(final UUID player, final String column, final Object data) {

        Connection conn = null;
        PreparedStatement ps = null;

        try {
            this.createPlayer(player);
            conn = this.pool.getConnection();

            final String stmt = "UPDATE " + this.clansPlayerDataTable + " SET " + column + "=?  WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setObject(1, data);
            ps.setString(2, player.toString());
            ps.executeUpdate();

            Tribes.playerdata.get(player).replace(column, data);

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, null);
        }
    }

    public HashMap<UUID, HashMap<String, Object>> getAllPlayerData() {

        final HashMap<UUID, HashMap<String, Object>> pd = new HashMap<UUID, HashMap<String, Object>>();

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.clansPlayerDataTable + " WHERE uuid IS NOT NULL";
            ps = conn.prepareStatement(stmt);

            rs = ps.executeQuery();

            while (rs.next()) {
                final HashMap<String, Object> data = new HashMap<String, Object>();

                data.put("clan", rs.getString("clan"));
                data.put("balance", rs.getInt("balance"));

                pd.put(UUID.fromString((String) rs.getObject("uuid")), data);
            }
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, rs);
        }

        return pd;
    }

	/*
	public Object getPlayerData(UUID player, String column) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			createPlayer(player);
			conn = pool.getConnection();
			UUID uuid = player;
			
			String stmt = "SELECT * FROM " + clansPlayerDataTable + " WHERE uuid=?";
			ps = conn.prepareStatement(stmt);
			
			ps.setString(1, uuid.toString());
			rs = ps.executeQuery();
			
			while(rs.next()) {
				return rs.getObject(column);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, rs);
		}
		return null;
	}


	public Object getPlayerData(Player player, String column) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			createPlayer(player);
			conn = pool.getConnection();
			UUID uuid = player.getUniqueId();
			
			String stmt = "SELECT * FROM " + clansPlayerDataTable + " WHERE uuid=?";
			ps = conn.prepareStatement(stmt);
			
			ps.setString(1, uuid.toString());
			rs = ps.executeQuery();
			
			while(rs.next()) {
				return rs.getObject(column);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			pool.close(conn, ps, rs);
		}
		return null;
	}
	*/

    public void savePlayer(final UUID player) {
        if (this.hasClan(player)) {
            final Clan old = new ClansPlayer(player).getRealClan();
            if (Tribes.getInstance().getClan(old.getUniqueId()).getMatchingPlayer(player) == null) {
                this.setPlayerData(player, "clan", null);
                Tribes.playerdata.get(player).replace("clan", null);
            }
        }
    }

    public boolean hasClan(final UUID player) {

        if (!this.playerExists(player)) {
            return false;
        }

        final Connection conn = null;
        final PreparedStatement ps = null;
        final ResultSet res = null;

        try {

            final String clan = (String) Tribes.playerdata.get(player).get("clan");
            if (clan == null) {
                return false;
            }

            if (this.clanExists(UUID.fromString(clan))) {
                return true;
            }
            return false;

        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
        }
        return false;
    }

    public ClansPlayer getPlayerFromName(final String name) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();
            final String stmt = "SELECT * FROM " + this.clansPlayerDataTable + " WHERE uuid IS NOT NULL";
            ps = conn.prepareStatement(stmt);

            res = ps.executeQuery();

            while (res.next()) {
                final String found = res.getString("name");
                if (found == null || !found.equalsIgnoreCase(name)) {
                    continue;
                }

                return new ClansPlayer(UUID.fromString(res.getString("uuid")));
            }

            return null;

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
        }
        return null;
    }

    public HashMap<Chunk, UUID> loadTerritories() {

        final HashMap<Chunk, UUID> chunks = new HashMap<Chunk, UUID>();
        for (final Clan toLoad : this.getClans()) {

            for (final Chunk chunk : toLoad.getTerritory()) {
                chunks.put(chunk, toLoad.getUniqueId());
            }
        }
        return chunks;
    }

    public ArrayList<Clan> getClans() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet res = null;

        try {
            conn = this.pool.getConnection();
            final String stmt = "SELECT * FROM " + this.clansDataTable + " WHERE uuid IS NOT NULL";
            ps = conn.prepareStatement(stmt);

            res = ps.executeQuery();
            final ArrayList<Clan> clans = new ArrayList<>();

            while (res.next()) {
                final UUID uuid = UUID.fromString(res.getString("uuid"));
                final Clan clan = this.getClan(uuid);
                clans.add(clan);
            }


            return clans;
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, res);
        }
        return new ArrayList<>();
    }

    public void loadServerClans() {
        for (final ServerClan type : ServerClan.values()) {
            if (this.clanExists(type.getName())) {
                continue;
            }

            this.createClan(UUID.randomUUID(), type.getName(), "Server", null);
        }
    }

    public Clan getServerClan(final ServerClan type) {
        this.loadServerClans();
        return this.getClan(type.getName());
    }

    public void saveSafeZones(final Set<String> coords) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            this.createSetting(this.safeZoneSetting);
            conn = this.pool.getConnection();

            final String stmt = "UPDATE " + this.clansSettingsTable + " SET value=?  WHERE name=?";
            ps = conn.prepareStatement(stmt);

            /*
             * COORDS
             */
            int highest = 1;
            final HashMap<Integer, ArrayList<String>> toSave = new HashMap<>();
            if (this.getSetting(this.safeZoneSetting) != null) {
                final JSONObject setting = (JSONObject) new JSONParser().parse(this.getSetting(this.safeZoneSetting));
                for (final Object o : setting.keySet()) {

                    final JSONArray keys = (JSONArray) setting.get((String) o);
                    final ArrayList<String> alreadySaved = new ArrayList<String>();
                    for (int i = 0; i < keys.size(); i++) {
                        alreadySaved.add((String) keys.get(i));
                    }
                    final int index = Integer.parseInt((String) o);
                    if (index >= highest) {
                        highest = index + 1;
                    }
                    toSave.put(index, alreadySaved);
                }
            }

            final ArrayList<String> tryingToSave = new ArrayList<String>(coords);
            toSave.put(highest, tryingToSave);

            ps.setString(1, new JSONObject(toSave).toJSONString());
            ps.setString(2, this.safeZoneSetting);
            ps.executeUpdate();

        } catch (final SQLException | ParseException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, null);
        }
    }

    public Set<String> getSafeZones() {
        final Set<String> coords = new HashSet<String>();

        try {
            if (this.getSetting(this.safeZoneSetting) != null) {
                final JSONObject setting = (JSONObject) new JSONParser().parse(this.getSetting(this.safeZoneSetting));
                for (final Object o : setting.keySet()) {

                    final JSONArray keys = (JSONArray) setting.get((String) o);
                    for (int i = 0; i < keys.size(); i++) {
                        coords.add((String) keys.get(i));
                    }
                }
            }
        } catch (final ParseException e) {
            e.printStackTrace();
        }

        return coords;
    }

    public void saveResetId(final String id) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            this.createSetting(this.resetIdSetting);
            conn = this.pool.getConnection();

            final String stmt = "UPDATE " + this.clansSettingsTable + " SET value = ? WHERE name = ?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, id);
            ps.setString(2, this.resetIdSetting);
            ps.executeUpdate();

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, null);
        }
    }

    public String getResetId() {
        final String setting = this.getSetting(this.resetIdSetting);
        return setting != null ? setting : "0";
    }
}
