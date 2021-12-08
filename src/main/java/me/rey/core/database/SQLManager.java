package me.rey.core.database;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.pvp.Build;
import me.rey.core.pvp.Build.BuildSet;
import me.rey.core.utils.Text;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SQLManager {

    private final JavaPlugin plugin;
    private final ConnectionPoolManager pool;
    private final String playerDataTable;

    public SQLManager(final Warriors plugin) {
        this.plugin = plugin.getPlugin();
        this.pool = new ConnectionPoolManager(this.plugin);
        this.playerDataTable = this.plugin.getConfig().getConfigurationSection("mysql").getString("player_data_table");
        this.makeTable();
    }

    private void makeTable() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            String jsonList = "";
            final String[] classNames = new String[ClassType.values().length];
            int count = 0;
            for (final ClassType type : ClassType.values()) {
                jsonList += ", " + type.name().toLowerCase() + "_buildset TEXT";
                classNames[count] = type.name().toLowerCase() + "_buildset";
                count++;
            }

            /*
             * CREATING WHOLE TABLE
             */
            final String statement = "CREATE TABLE IF NOT EXISTS `" + this.playerDataTable + "` " +
                    "(" +
                    "uuid TEXT" + jsonList +
                    ")";

            conn = this.pool.getConnection();
            ps = conn.prepareStatement(statement);
            ps.executeUpdate();


            /*
             * ADDING ROW IF NOT EXIST
             */
            for (final String name : classNames) {

                final String sql = "SELECT * FROM " + this.playerDataTable;
                rs = ps.executeQuery(sql);
                final ResultSetMetaData metaData = rs.getMetaData();
                final int rowCount = metaData.getColumnCount();

                boolean isMyColumnPresent = false;
                final String myColumnName = name;
                for (int i = 1; i <= rowCount; i++) {
                    if (myColumnName.equals(metaData.getColumnName(i))) {
                        isMyColumnPresent = true;
                    }
                }

                if (!isMyColumnPresent) {
                    final String myColumnType = "JSON";
                    ps.executeUpdate("ALTER TABLE " + this.playerDataTable + " ADD " + myColumnName + " " + myColumnType);
                    Text.log("Sucessfully created column: " + name);
                } else {
                    Text.log("Skipped creation of column: " + name);
                }

            }

            Text.log("Sucessfully executed statement: " + statement);
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, rs);
        }
    }

    public void onDisable() {
        this.pool.closePool();
    }

    public boolean playerExists(final UUID uuid) {

        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.playerDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();

            if (rs.next()) {
                return true;
            }

            return false;

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, rs);
        }

        return false;
    }


    public void createPlayer(final UUID uuid) {
        Connection conn = null;
        PreparedStatement ps = null, insert = null;
        ResultSet rs = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.playerDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();

            rs.next();
            if (!this.playerExists(uuid)) {
                final String stmt2 = "INSERT INTO " + this.playerDataTable
                        + "(uuid,chain_buildset,diamond_buildset,iron_buildset,gold_buildset,leather_buildset) VALUE(?,?,?,?,?,?)";
                insert = conn.prepareStatement(stmt2);
                insert.setString(1, uuid.toString());
                insert.setString(2, null);
                insert.setString(3, null);
                insert.setString(4, null);
                insert.setString(5, null);
                insert.setString(6, null);
                insert.executeUpdate();
            }

        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, rs);
            this.pool.close(null, insert, null);
        }

    }

    public BuildSet getPlayerBuilds(final UUID uuid, final ClassType classType) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.playerDataTable + " WHERE uuid=?";
            ps = conn.prepareStatement(stmt);

            ps.setString(1, uuid.toString());
            rs = ps.executeQuery();

            while (rs.next()) {

                if (rs.getObject(classType.name().toLowerCase() + "_buildset") == null) {
                    return new BuildSet();
                }

                final JSONObject obj = (JSONObject) new JSONParser().parse(rs.getString(classType.name().toLowerCase() + "_buildset"));

                final ArrayList<Build> builds = new ArrayList<Build>();
                for (final Object o : obj.keySet()) {
                    final String name = (String) o;
                    final JSONObject input = (JSONObject) obj.get(name);

                    final HashMap<Ability, Integer> abilities = new HashMap<Ability, Integer>();
                    final JSONObject abJson = (JSONObject) input.get("abilities");
                    for (final Object id : abJson.keySet()) {
                        for (final Ability a : Warriors.getInstance().getAbilitiesInCache()) {
                            if (Long.parseLong((String) id) == a.getIdLong()) {
                                abilities.put(a, ((Long) abJson.get(id)).intValue());
                            }
                        }
                    }

                    final Build build = new Build(name, UUID.fromString((String) input.get("uuid")), ((Long) input.get("position")).intValue(), abilities);
                    build.setCurrentState((boolean) input.get("selected"));
                    builds.add(build);
                }

                final Build[] finalBuilds = builds.toArray(new Build[builds.size()]);

                final BuildSet bs = new BuildSet(finalBuilds);
                return bs;

            }

        } catch (final NullPointerException e) {
            return new BuildSet();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, rs);
        }

        return new BuildSet();
    }


    public void setPlayerData(final UUID uuid, final String column, final Object data) {
        Connection conn = null;
        PreparedStatement ps = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "UPDATE " + this.playerDataTable + " SET " + column + "=?  WHERE uuid=?";
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

    public void createPlayerBuild(final UUID uuid, final Build b, final ClassType classType) {

        this.createPlayer(uuid);

        final BuildSet bs = this.getPlayerBuilds(uuid, classType);
        bs.add(b);

        final HashMap<String, HashMap<String, Object>> list = new HashMap<>();
        for (final Build build : bs.getArray()) {
            final HashMap<Long, Integer> query = new HashMap<Long, Integer>();

            for (final Ability ability : build.getAbilities().keySet()) {
                query.put(ability.getIdLong(), build.getAbilities().get(ability));
            }

            final HashMap<String, Object> toPut = new HashMap<String, Object>();
            toPut.put("position", build.getPosition());
            toPut.put("uuid", build.getUniqueId().toString());
            toPut.put("abilities", query);
            toPut.put("selected", build.getCurrentState());


            list.put(build.getRawName(), toPut);
        }


        this.setPlayerData(uuid, classType.name().toLowerCase() + "_buildset", new JSONObject(list).toJSONString());
    }

    public void deletePlayerBuild(final UUID uuid, final Build b, final ClassType classType) {

        this.createPlayer(uuid);

        if (this.getPlayerBuilds(uuid, classType).contains(b)) {

            final BuildSet bs = this.getPlayerBuilds(uuid, classType);
            bs.remove(b);

            final HashMap<String, HashMap<String, Object>> list = new HashMap<>();
            for (final Build build : bs.getArray()) {
                final HashMap<Long, Integer> query = new HashMap<Long, Integer>();

                for (final Ability ability : build.getAbilities().keySet()) {
                    query.put(ability.getIdLong(), build.getAbilities().get(ability));
                }

                final HashMap<String, Object> toPut = new HashMap<String, Object>();
                toPut.put("position", build.getPosition());
                toPut.put("uuid", build.getUniqueId().toString());
                toPut.put("abilities", query);
                toPut.put("selected", build.getCurrentState());

                list.put(build.getRawName(), toPut);
            }

            this.setPlayerData(uuid, classType.name().toLowerCase() + "_buildset", new JSONObject(list).toJSONString());

        }

    }

    public void saveBuild(final UUID uuid, final Build b, final ClassType classType) {

        this.createPlayer(uuid);

        final BuildSet bs = this.getPlayerBuilds(uuid, classType);

        final HashMap<String, HashMap<String, Object>> list = new HashMap<>();
        for (Build build : bs.getArray()) {
            if (build.getUniqueId().equals(b.getUniqueId())) {
                build = b;
            }

            final HashMap<Long, Integer> query = new HashMap<Long, Integer>();

            for (final Ability ability : build.getAbilities().keySet()) {
                query.put(ability.getIdLong(), build.getAbilities().get(ability));
            }

            final HashMap<String, Object> toPut = new HashMap<String, Object>();
            toPut.put("position", build.getPosition());
            toPut.put("uuid", build.getUniqueId().toString());
            toPut.put("abilities", query);
            toPut.put("selected", build.getCurrentState());

            list.put(build.getRawName(), toPut);
        }

        this.setPlayerData(uuid, classType.name().toLowerCase() + "_buildset", new JSONObject(list).toJSONString());

    }

    public Map<UUID, HashMap<ClassType, Build[]>> loadAllBuilds() {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            conn = this.pool.getConnection();

            final String stmt = "SELECT * FROM " + this.playerDataTable + " WHERE uuid IS NOT NULL";
            ps = conn.prepareStatement(stmt);
            rs = ps.executeQuery();
            final HashMap<UUID, HashMap<ClassType, Build[]>> toReturn = new HashMap<>();

            while (rs.next()) {

                final UUID uuid = UUID.fromString(rs.getString("uuid"));
                final HashMap<ClassType, Build[]> builds = new HashMap<>();

                for (final ClassType classType : ClassType.values()) {
                    final BuildSet buildSet = this.getPlayerBuilds(uuid, classType);
                    builds.put(classType, buildSet.getArray());
                }

                toReturn.put(uuid, builds);
            }

            return toReturn;
        } catch (final NullPointerException e) {
            return new HashMap<UUID, HashMap<ClassType, Build[]>>();
        } catch (final Exception e) {
            e.printStackTrace();
        } finally {
            this.pool.close(conn, ps, rs);
        }

        return new HashMap<UUID, HashMap<ClassType, Build[]>>();
    }

}