package me.rey.clans.database.local;

import me.rey.clans.Tribes;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.sql.*;

public class LocalSQLiteManager {
    public LocalSQLiteManager() {
        try {
            this.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS PvpTimers (" +
                    "`uuid` TEXT NOT NULL, " +
                    "`initial` TEXT NOT NULL, " +
                    "`remaining` TEXT NOT NULL, " +
                    "`removable` TEXT NOT NULL, " +
                    "`timeApplied` TEXT NOT NULL, " +
                    "`resetId` TEXT NOT NULL, " +
                    "PRIMARY KEY (`uuid`));").execute();
            this.getConnection().prepareStatement("CREATE TABLE IF NOT EXISTS PlayerServerData (" +
                    "`uuid` TEXT NOT NULL, " +
                    "`lastResetPlayedId` TEXT NOT NULL, " +
                    "PRIMARY KEY (`uuid`));").execute();
        } catch (final SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        Connection connection = null;
        try {
            final File sql = new File(Tribes.getInstance().getPlugin().getDataFolder(), "local_data.db");
            if (!sql.exists()) {
                try {
                    if (sql.createNewFile()) {
                        Bukkit.getLogger().warning("Created " + sql.getPath());
                    }
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }

            try {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection("jdbc:sqlite:" + sql);
            } catch (final SQLException e) {
                e.printStackTrace();
                Bukkit.getLogger().warning("A critical error occurred whilst trying to load the local database!");
            }
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    public boolean isLocallyIdentified(final OfflinePlayer player) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getConnection().prepareStatement("SELECT * FROM PlayerServerData WHERE uuid = ?");
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();
            return rs.next();
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void loadPlayer(final Player player) {
        if (!this.isLocallyIdentified(player)) {
            this.resetPlayerLocalData(player);
        }
    }

    public void resetPlayerLocalData(final OfflinePlayer player) {
        PreparedStatement ps = null;

        try {
            if (this.isLocallyIdentified(player)) {
                ps = this.getConnection().prepareStatement("UPDATE PlayerServerData SET lastResetPlayedId = ? WHERE uuid = ?");
                ps.setString(1, Tribes.getInstance().getSQLManager().getResetId());
                ps.setString(2, player.getUniqueId().toString());
            } else {
                ps = this.getConnection().prepareStatement("INSERT INTO PlayerServerData (uuid, lastResetPlayedId) VALUES (?,?)");
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, Tribes.getInstance().getSQLManager().getResetId());
            }
            ps.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public String getPlayerLastResetPlayedId(final OfflinePlayer player) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getConnection().prepareStatement("SELECT * FROM PlayerServerData WHERE uuid = ?");
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("uuid").equals(player.getUniqueId().toString())) {
                    return rs.getString("lastResetPlayedId");
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isOnPvpTimerList(final OfflinePlayer player) {
        return this.isOnPvpTimerList(player, true);
    }

    public boolean isOnPvpTimerList(final OfflinePlayer player, final boolean includeWrongServers) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = this.getConnection().prepareStatement("SELECT * FROM PvpTimers WHERE uuid = ?");
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();
            if (includeWrongServers) {
                while (rs.next()) {
                    if (rs.getString("resetId").equals(Tribes.getInstance().getSQLManager().getResetId())) {
                        return true;
                    }
                }
            } else {
                return rs.next();
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public long getPlayerPvpTimer(final OfflinePlayer player) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        if (this.getPlayerLastResetPlayedId(player) == null) {
            return Tribes.getInstance().getPvpTimer().getDefault();
        }


        if (!this.getPlayerLastResetPlayedId(player).equals(Tribes.getInstance().getSQLManager().getResetId())) {
            return Tribes.getInstance().getPvpTimer().getDefault();
        }

        try {
            ps = this.getConnection().prepareStatement("SELECT * FROM PvpTimers WHERE uuid = ?");
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("uuid").equals(player.getUniqueId().toString())) {
                    return Long.parseLong(rs.getString("remaining"));
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
        return Tribes.getInstance().getPvpTimer().getDefault();
    }

    public boolean isPvpTimerRemovable(final OfflinePlayer player) {
        PreparedStatement ps = null;
        ResultSet rs = null;

        if (this.getPlayerLastResetPlayedId(player) == null) {
            return true;
        }


        if (!this.getPlayerLastResetPlayedId(player).equals(Tribes.getInstance().getSQLManager().getResetId())) {
            return true;
        }

        try {
            ps = this.getConnection().prepareStatement("SELECT * FROM PvpTimers WHERE uuid = ?");
            ps.setString(1, player.getUniqueId().toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                if (rs.getString("uuid").equals(player.getUniqueId().toString())) {
                    return Boolean.parseBoolean(rs.getString("removable"));
                }
            }
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }

                if (rs != null) {
                    rs.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public void setPlayerPvpTimer(final OfflinePlayer player, final long time, final boolean removable) {
        PreparedStatement ps = null;

        try {
            if (this.isOnPvpTimerList(player, false)) {
                ps = this.getConnection().prepareStatement("UPDATE PvpTimers SET remaining = ?, removable = ?, initial = ?, resetId = ?, timeApplied = ? WHERE uuid = ?");
                ps.setString(1, Long.toString(time));
                ps.setString(2, Boolean.toString(removable));
                ps.setString(3, Long.toString(time));
                ps.setString(4, Tribes.getInstance().getSQLManager().getResetId());
                ps.setString(5, Long.toString(System.currentTimeMillis()));
                ps.setString(6, player.getUniqueId().toString());
            } else {
                this.resetPlayerLocalData(player);
                ps = this.getConnection().prepareStatement("INSERT INTO PvpTimers (uuid, initial, remaining, removable, timeApplied, resetId) VALUES (?,?,?,?,?,?)");
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, Long.toString(time));
                ps.setString(3, Long.toString(time));
                ps.setString(4, Boolean.toString(removable));
                ps.setString(5, Long.toString(System.currentTimeMillis()));
                ps.setString(6, Tribes.getInstance().getSQLManager().getResetId());
            }
            ps.executeUpdate();
        } catch (final SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (final SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
