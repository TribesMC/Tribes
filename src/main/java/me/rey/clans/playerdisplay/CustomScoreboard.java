package me.rey.clans.playerdisplay;

import me.rey.clans.Tribes;
import me.rey.core.utils.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.*;

public class CustomScoreboard {

    private static final int MAX_LINES = 15;

    private BukkitTask task;
    private final Player bound;
    private final Scoreboard sb;
    private final Objective obj;
    private String[] titles, lines;
    private int currentTitle;

    public CustomScoreboard(final Player bound, final String title, final String... extraTitles) {
        this.bound = bound;
        this.lines = new String[15];
        this.titles = new String[]{title};
        this.titles[0] = Text.color(title);

        final ScoreboardManager manager = Bukkit.getScoreboardManager();
        this.sb = manager.getNewScoreboard();

        this.obj = this.sb.registerNewObjective("bar", "dummy");
		this.obj.setDisplayName(Text.color(title));
		this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        for (int i = 0; i < MAX_LINES; i++) {
			this.sb.registerNewTeam("team" + (i + 1));
		}

        if (extraTitles != null && extraTitles.length > 0) {
			for (final String s : extraTitles) {
				this.addTitle(s);
			}
		}

        this.currentTitle = 1;
    }

    public Player getBound() {
        return this.bound;
    }

    public CustomScoreboard init() {
        this.bound.setScoreboard(this.sb);
		this.update();

		this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (CustomScoreboard.this.bound == null || !CustomScoreboard.this.bound.isOnline()) {
                    this.cancel();
                    return;
                }

				CustomScoreboard.this.callEvent();

				CustomScoreboard.this.currentTitle = CustomScoreboard.this.currentTitle + 1 > CustomScoreboard.this.getTitles().length ? 1 : CustomScoreboard.this.currentTitle + 1;
				CustomScoreboard.this.update();
            }
        }.runTaskTimerAsynchronously(Tribes.getInstance().getPlugin(), 0, 2);
        return this;
    }

    public CustomScoreboard stop() {
        if (this.task != null) {
			this.task.cancel();
		}
        return this;
    }

    public String[] getTitles() {
        return this.titles;
    }

    public CustomScoreboard setTitles(final String... titles) {
        for (int i = 0; i < titles.length; i++) {
			titles[i] = titles[i] == null ? null : Text.color(titles[i]);
		}
        this.titles = titles;
        return this;
    }

    public String[] getLines() {
        return this.lines;
    }

    public CustomScoreboard setLines(final String[] lines) {
        for (int i = 0; i < lines.length; i++) {
			lines[i] = lines[i] == null ? null : Text.color(lines[i]);
		}
        this.lines = lines;
        return this;
    }

    public CustomScoreboard addTitle(final String title) {
        final String[] query = new String[this.titles.length + 1];
        for (int i = 0; i < this.titles.length; i++) {
			query[i] = this.titles[i];
		}
        query[query.length - 1] = Text.color(title);
        this.titles = query;
        return this;
    }

    public CustomScoreboard addLine(final String text) {
        if (this.lines.length >= MAX_LINES) {
			return this;
		}
		this.lines[this.lines.length] = Text.color(text);
        return this;
    }

    private void update() {

        if (this.bound.getScoreboard() == null) {
			this.bound.setScoreboard(this.sb);
		}

        final Scoreboard scoreboard = this.bound.getScoreboard();
        scoreboard.getObjective(DisplaySlot.SIDEBAR).setDisplayName(this.getTitles()[this.currentTitle - 1]);

        int count = 0;
        for (int i = 0; i < this.getLines().length; i++) {
            if (this.getLines()[i] == null) {
                if (this.getEntryFromScore(this.obj, MAX_LINES - i) != null) {
					scoreboard.resetScores(this.getEntryFromScore(this.obj, MAX_LINES - i));
				}
                continue;
            }

            String line = this.getLines()[i];
			final int score = MAX_LINES - i;
            if (line.equals("")) {
                line = ChatColor.values()[count].toString();
                count++;
            }

            final Team team = scoreboard.getTeam("team" + Math.min(MAX_LINES, i + 1));

            final String[] allChars = new String[3];
            allChars[1] = line;

            if (line.length() > 16) {
                allChars[0] = line.substring(0, 16);
                allChars[1] = line.substring(16, Math.min(32, line.length()));
                if (line.length() > 32) {
					allChars[2] = line.substring(32, Math.min(48, line.length()));
				}
            }

            team.setPrefix(allChars[0] == null ? "" : allChars[0]);
			this.replaceScore(this.obj, team, score, allChars[1]);
            team.setSuffix(allChars[2] == null ? "" : allChars[2]);

			this.obj.getScore(line).setScore(score);
        }
    }

    private void callEvent() {
        final UpdateScoreboardEvent event = new UpdateScoreboardEvent(this);
        Bukkit.getServer().getPluginManager().callEvent(event);
    }

    private String getEntryFromScore(final Objective o, final int score) {
        if (o == null) {
			return null;
		}
        if (!this.hasScoreTaken(o, score)) {
			return null;
		}
        for (final String s : o.getScoreboard().getEntries()) {
            if (o.getScore(s).getScore() == score) {
				return o.getScore(s).getEntry();
			}
        }
        return null;
    }

    private boolean hasScoreTaken(final Objective o, final int score) {
        for (final String s : o.getScoreboard().getEntries()) {
            if (o.getScore(s).getScore() == score) {
				return true;
			}
        }
        return false;
    }

    private void replaceScore(final Objective o, final Team team, final int score, String name) {
        name = team == null ? name : team.getPrefix() + name + team.getSuffix();

        if (this.hasScoreTaken(o, score)) {
            final Scoreboard s = o.getScoreboard();
            String found = this.getEntryFromScore(o, score);
            if (s.getEntryTeam(found) != null) {
				found = s.getEntryTeam(found).getPrefix() + found + s.getEntryTeam(found).getSuffix();
			}


            if (found.equals(name)) {
				return;
			} else {
				o.getScoreboard().resetScores(this.getEntryFromScore(o, score));
			}
        }
        o.getScore(name).setScore(score);
    }

}
