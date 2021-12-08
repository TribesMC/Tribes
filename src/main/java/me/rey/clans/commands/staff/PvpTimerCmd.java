package me.rey.clans.commands.staff;

import me.rey.clans.Tribes;
import me.rey.clans.clans.ClansPlayer;
import me.rey.clans.clans.ClansRank;
import me.rey.clans.commands.ClansCommand;
import me.rey.clans.commands.SubCommand;
import me.rey.clans.enums.CommandType;
import me.rey.clans.features.PvpTimer;
import me.rey.clans.gui.LegacyGuiEditable;
import me.rey.clans.gui.StainedMaterialColor;
import me.rey.clans.gui.anvil.AnvilGUI;
import me.rey.clans.utils.ErrorCheck;
import me.rey.clans.utils.UtilTime;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class PvpTimerCmd extends ClansCommand {

    private final static Map<UUID, UIData> times;

    static {
        times = new HashMap<>();
    }

    public PvpTimerCmd() {
        super("pvptimer", "Configures the pvp timer of a player", "/pvptimer <player>", ClansRank.NONE, CommandType.STAFF, true);
        super.setStaff(true);
    }

    @Override
    public void run(final CommandSender sender, final String[] args) {
        OfflinePlayer player = (OfflinePlayer) sender;

        if (args == null || args.length == 0) {
            this.sendUsageError(this.usage());
            return;
        }

        if (sender.isOp()) {
            try {
                player = Bukkit.getOfflinePlayer(args[0]);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        if (player == null) {
            ErrorCheck.inexistentPlayer(sender);
            return;
        }

        final ConfirmGui gui = new ConfirmGui(player, (Player) sender);
        gui.init();
        gui.setup();
        gui.open((Player) sender);
    }

    @Override
    public SubCommand[] getChilds() {
        return new SubCommand[]{};
    }

    private static class ConfirmGui extends LegacyGuiEditable {

        private final OfflinePlayer target;
        private final Player opener;

        public ConfirmGui(final OfflinePlayer target, final Player opener) {
            super(target.getName() + "'s Pvp Timer", 6, Tribes.getInstance().getPlugin());
            this.target = target;
            this.opener = opener;
        }

        @Override
        public void setup() {
            final UIData data = times.getOrDefault(this.opener.getUniqueId(), new UIData(0L, false, true));
            final long time = data.time;
            final boolean invalid = data.invalid;
            final boolean playerRemovable = data.playerRemovable;

            this.setItemOverride(new GuiItem(new PlayerSkull(this.target).setName(ChatColor.GOLD + "" + ChatColor.BOLD + this.target.getName()).setLore(Arrays.asList(
                    "",
                    ChatColor.GRAY + "You are configuring the PVP timer of",
                    ChatColor.YELLOW + this.target.getName() + ChatColor.GRAY + "!"
            ))) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                }
            }, 0);

            this.setItemOverride(new GuiItem(new Item(Material.BOOK).setGlow(true).setName(ChatColor.GOLD + "" + ChatColor.BOLD + "PVP Timer Manager").setLore(Arrays.asList(
                    "",
                    ChatColor.BLUE + "" + ChatColor.BOLD + "LOADING..."
            ))) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                }
            }, 4);

            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!ConfirmGui.this.opener.isOnline()) {
                        this.cancel();
                    }

                    if (ConfirmGui.this.opener.getOpenInventory() == null) {
                        this.cancel();
                    }

                    if (ConfirmGui.this.opener.getOpenInventory().getType() != ConfirmGui.this.getInventory().getType()) {
                        this.cancel();
                    }

                    if (!ConfirmGui.this.opener.getOpenInventory().getTopInventory().equals(ConfirmGui.this.getInventory())) {
                        this.cancel();
                    }

                    final long headloretime = Tribes.getInstance().getPvpTimer().getLivePvpTimer(ConfirmGui.this.target);
                    final List<String> headLore = new ArrayList<>(Arrays.asList(
                            "",
                            ChatColor.GRAY + "Status: " + (headloretime >= 0 ? ChatColor.GREEN + "" + ChatColor.BOLD + "ACTIVE" : ChatColor.RED + "" + ChatColor.BOLD + "INACTIVE")
                    ));

                    if (headloretime >= 0) {
                        final UtilTime.Breakdown breakdown = UtilTime.getBreakdown(headloretime);
                        headLore.add("");
                        headLore.add(ChatColor.GRAY + "Time Left: ");
                        headLore.addAll(ConfirmGui.this.getDisplayedTimes(breakdown));
                        if (PvpTimer.getRemovables().containsKey(ConfirmGui.this.target.getUniqueId())) {
                            if (!PvpTimer.getRemovables().get(ConfirmGui.this.target.getUniqueId())) {
                                headLore.addAll(Arrays.asList(
                                        "",
                                        ChatColor.RED + "" + ChatColor.BOLD + "WARNING",
                                        ChatColor.RED + "Player cannot remove this PVP",
                                        ChatColor.RED + "timer themselves! It must be",
                                        ChatColor.RED + "removed by a staff member or",
                                        ChatColor.RED + "through its expiry!"
                                ));
                            }
                        }
                    }

                    ConfirmGui.this.setItemOverride(new GuiItem(new Item(Material.BOOK).setGlow(true).setName(ChatColor.GOLD + "" + ChatColor.BOLD + "PVP Timer Manager").setLore(headLore)) {
                        @Override
                        public void onUse(final Player player, final ClickType type, final int slot) {
                        }
                    }, 4);
                }
            }.runTaskTimer(Tribes.getInstance().getPlugin(), 20, 20);

            final UtilTime.Breakdown breakdown = UtilTime.getBreakdown(time * 1000);
            final List<String> watchLore = new ArrayList<>(Arrays.asList(
                    "",
                    ChatColor.YELLOW + this.target.getName() + ChatColor.GRAY + "'s PVP timer",
                    ChatColor.GRAY + "duration will be set to",
                    ChatColor.GRAY + "the following:"
            ));

            watchLore.add("");
            watchLore.addAll(this.getDisplayedTimes(breakdown));

            watchLore.addAll(Arrays.asList("",
                    ChatColor.GOLD + "" + ChatColor.BOLD + "Click" + ChatColor.GRAY + " to set custom time!",
                    ChatColor.GOLD + "" + ChatColor.BOLD + "Shift-Click" + ChatColor.GRAY + " to reset!"
            ));

            this.setItemOverride(new GuiItem(new Item(Material.WATCH).setName(ChatColor.GOLD + "" + ChatColor.BOLD + "DURATION").setLore(watchLore)) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                    if (ConfirmGui.this.opener.equals(player)) {
                        if (type.isShiftClick()) {
                            times.put(ConfirmGui.this.opener.getUniqueId(), new UIData(0L, false, true));
                            ConfirmGui.this.setup();
                            ConfirmGui.this.open(ConfirmGui.this.opener);
                        } else {
                            ConfirmGui.this.pauseOnClose(true);
                            final AnvilGUI anvil = new AnvilGUI(Tribes.getInstance().getPlugin(), ConfirmGui.this.opener, (event) -> {
                                final long seconds;
                                try {
                                    seconds = Long.parseLong(event.getName());
                                    data.time = seconds;
                                } catch (final NumberFormatException e) {
                                    data.invalid = true;
                                }
                                times.put(ConfirmGui.this.opener.getUniqueId(), data);
                                Bukkit.getScheduler().runTask(Tribes.getInstance().getPlugin(), () -> {
                                    ConfirmGui.this.setup();
                                    ConfirmGui.this.open(ConfirmGui.this.opener);
                                    ConfirmGui.this.pauseOnClose(false);
                                });
                            });
                            anvil.setSlot(AnvilGUI.AnvilSlot.INPUT_LEFT, new Item(Material.PAPER).setName(ChatColor.GREEN + "").get());
                            final ItemStack i = new ItemStack(Material.PAPER);
                            anvil.setSlot(AnvilGUI.AnvilSlot.INPUT_LEFT, i);
                            anvil.setSlotName(AnvilGUI.AnvilSlot.INPUT_LEFT, "Time in seconds");
                            anvil.setTitle("Input Time");
                            anvil.open();
                        }
                    } else {
                        player.closeInventory();
                    }
                }
            }, 13);

            // green = removable
            this.setItemOverride(new GuiItem(new Item(playerRemovable ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK).setName((playerRemovable ? ChatColor.GREEN : ChatColor.RED) + "" + ChatColor.BOLD + "PLAYER REMOVABLE").setLore(playerRemovable ? Arrays.asList(
                    "",
                    ChatColor.GRAY + "Player will be able to remove",
                    ChatColor.GRAY + "this timer themselves.",
                    "",
                    ChatColor.GREEN + "" + ChatColor.BOLD + "Click" + ChatColor.GRAY + " to stop player being able",
                    ChatColor.GRAY + "to remove the timer themselves!"
            ) : Arrays.asList(
                    "",
                    ChatColor.GRAY + "Player will not be able to remove",
                    ChatColor.GRAY + "this timer themselves.",
                    "",
                    ChatColor.RED + "" + ChatColor.BOLD + "Click" + ChatColor.GRAY + " to make removable!"
            ))) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                    if (ConfirmGui.this.opener.equals(player)) {
                        data.playerRemovable = !playerRemovable;
                        times.put(ConfirmGui.this.opener.getUniqueId(), data);
                        ConfirmGui.this.setup();
                        ConfirmGui.this.open(ConfirmGui.this.opener);
                    } else {
                        player.closeInventory();
                    }
                }
            }, 16);

            this.setTimedButton(28, 3600, UtilTime.TimeUnit.HOURS);
            this.setTimedButton(29, 60, UtilTime.TimeUnit.MINUTES);
            this.setTimedButton(30, 1, UtilTime.TimeUnit.SECONDS);

            this.setTimedButton(32, -1, UtilTime.TimeUnit.SECONDS);
            this.setTimedButton(33, -60, UtilTime.TimeUnit.MINUTES);
            this.setTimedButton(34, -3600, UtilTime.TimeUnit.HOURS);

            if (!invalid) {
                final List<String> lore = new ArrayList<>(Collections.singletonList(""));
                if (time == 0) {
                    lore.addAll(Arrays.asList(
                            ChatColor.RED + "" + ChatColor.BOLD + "WARNING",
                            ChatColor.RED + "Time is set to 0 seconds which",
                            ChatColor.RED + "means the players PVP timer will",
                            ChatColor.RED + "be completely removed.",
                            ""
                    ));
                }
                lore.addAll(Arrays.asList(
                        ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "INFO",
                        ChatColor.DARK_AQUA + "This will go into immediate effect!",
                        "",
                        ChatColor.GREEN + "" + ChatColor.BOLD + "Click" + ChatColor.GRAY + " to update!"
                ));
                this.setItemOverride(new GuiItem(new Item(Material.EMERALD_BLOCK).setName(ChatColor.GREEN + "" + ChatColor.BOLD + "UPDATE").setLore(lore).setGlow(true)) {
                    @Override
                    public void onUse(final Player player, final ClickType type, final int slot) {
                        if (time == 0) {
                            Tribes.getInstance().getPvpTimer().removePvpTimer(ConfirmGui.this.target, true);
                            if (ConfirmGui.this.opener.isOnline()) {
                                new ClansPlayer(ConfirmGui.this.opener).sendMessageWithPrefix("PVP Timer", String.format("Successfully removed &s%s&r's PVP timer!", ConfirmGui.this.target.getName()));
                            }
                            if (ConfirmGui.this.target.isOnline()) {
                                new ClansPlayer(ConfirmGui.this.target.getPlayer()).sendMessageWithPrefix("PVP Timer", "Your PVP timer has been removed!");
                            }
                        } else {
                            Tribes.getInstance().getPvpTimer().applyPvpTimer(ConfirmGui.this.target, time * 1000, playerRemovable);
                            if (ConfirmGui.this.opener.isOnline()) {
                                new ClansPlayer(ConfirmGui.this.opener).sendMessageWithPrefix("PVP Timer", String.format("Successfully set &s%s&r's PVP timer to &s%s&r!", ConfirmGui.this.target.getName(), UtilTime.convert(time * 1000, 0, UtilTime.getBestUnit(time * 1000)) + " " + UtilTime.getBestUnit(time * 1000).name().toLowerCase()));
                            }
                            if (ConfirmGui.this.target.isOnline()) {
                                new ClansPlayer(ConfirmGui.this.target.getPlayer()).sendMessageWithPrefix("PVP Timer", String.format("Your PVP timer has been set to &s%s&r!", UtilTime.convert(time * 1000, 0, UtilTime.getBestUnit(time * 1000)) + " " + UtilTime.getBestUnit(time * 1000).name().toLowerCase()));
                            }
                        }
                        player.closeInventory();
                    }
                }, 49);
            } else {
                this.setItemOverride(new GuiItem(new Item(Material.COAL_BLOCK).setName(ChatColor.RED + "" + ChatColor.BOLD + "UPDATE").setLore(Arrays.asList(
                        "",
                        ChatColor.RED + "" + ChatColor.BOLD + "ERROR",
                        ChatColor.RED + "You submitted an invalid custom",
                        ChatColor.RED + "time for this players PVP timer!",
                        ChatColor.RED + "You need to hover over the clock",
                        ChatColor.RED + "and shift-click it to reset it and",
                        ChatColor.RED + "start the process of creating the",
                        ChatColor.RED + "time again.",
                        "",
                        ChatColor.RED + "Custom times MUST be numeric and contain",
                        ChatColor.RED + "no non-numerical characters, this",
                        ChatColor.RED + "includes commas and mathematical symbols.",
                        "",
                        ChatColor.RED + "" + ChatColor.BOLD + "Click" + ChatColor.GRAY + " to close!"
                )).setGlow(true)) {
                    @Override
                    public void onUse(final Player player, final ClickType type, final int slot) {
                        player.closeInventory();
                    }
                }, 49);
            }

            this.setOnClose((p, e) -> Bukkit.getScheduler().runTaskLater(Tribes.getInstance().getPlugin(), () -> {
                if (this.opener.getOpenInventory() != null) {
                    times.remove(p.getUniqueId());
                }
            }, 5));
        }

        @Override
        public void init() {
            final GuiItem item = new GuiItem(new Item(Material.STAINED_GLASS_PANE).setName(ChatColor.BLACK + "_").setDurability(StainedMaterialColor.CYAN.getGlassColorId())) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {

                }
            };
            this.borderise(item);
        }

        private void setTimedButton(final int slot, long time, final UtilTime.TimeUnit unit) {
            final UIData data = times.getOrDefault(this.opener.getUniqueId(), new UIData(0L, false, true));

            boolean negative = false;
            if (time < 0) {
                negative = true;
                time = time * -1;
            }

            if (negative) {
                if (data.time - time < 0) {
                    this.setItemOverride(new GuiItem(new Item(Material.COAL_BLOCK).setName(ChatColor.RED + "" + ChatColor.BOLD + "Remove" + " " + ((time * 1000) / unit.divider) + " " + unit.name().toLowerCase().substring(0, unit.name().length() - 1)).setLore(Arrays.asList(
                            "",
                            ChatColor.RED + "" + ChatColor.BOLD + "UNAVAILABLE",
                            ChatColor.RED + "This button would send the players",
                            ChatColor.RED + "PVP timer below 0, so it cannot be",
                            ChatColor.RED + "used with this low of a PVP timer!"
                    )).setGlow(true)) {
                        @Override
                        public void onUse(final Player player, final ClickType type, final int slot) {

                        }
                    }, slot);
                    return;
                }
            }

            final long finalTime = time;
            final boolean finalNegative = negative;
            this.setItemOverride(new GuiItem(new Item(!finalNegative ? Material.EMERALD_BLOCK : Material.REDSTONE_BLOCK).setName((!finalNegative ? ChatColor.GREEN : ChatColor.RED) + "" + ChatColor.BOLD + (!finalNegative ? "Add" : "Remove") + " " + ((finalTime * 1000) / unit.divider) + " " + unit.name().toLowerCase().substring(0, unit.name().length() - 1)).setLore(Arrays.asList(
                    "",
                    ChatColor.YELLOW + Long.toString((finalTime * 1000) / unit.divider) + " " + unit.name().toLowerCase().substring(0, unit.name().length() - 1) + ChatColor.GRAY + " will be " + (!finalNegative ? "added to" : "removed from"),
                    ChatColor.GRAY + "the players PVP timer!"
            ))) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                    if (finalNegative) {
                        data.time = data.time - finalTime;
                    } else {
                        data.time = data.time + finalTime;
                    }
                    times.put(ConfirmGui.this.opener.getUniqueId(), data);
                    ConfirmGui.this.setup();
                    ConfirmGui.this.open(ConfirmGui.this.opener);
                }
            }, slot);
        }

        private List<String> getDisplayedTimes(final UtilTime.Breakdown breakdown) {
            final List<String> lines = new ArrayList<>();
            boolean include = false;

            if (breakdown.months > 0) {
                include = true;
                lines.add(ChatColor.GRAY + "Months: " + ChatColor.YELLOW + breakdown.months);
            }
            if (breakdown.days > 0 || include) {
                include = true;
                lines.add(ChatColor.GRAY + "Days: " + ChatColor.YELLOW + breakdown.days);
            }
            if (breakdown.hours > 0 || include) {
                include = true;
                lines.add(ChatColor.GRAY + "Hours: " + ChatColor.YELLOW + breakdown.hours);
            }
            if (breakdown.minutes > 0 || include) {
                include = true;
                lines.add(ChatColor.GRAY + "Minutes: " + ChatColor.YELLOW + breakdown.minutes);
            }
            if (breakdown.seconds > 0 || include) {
                include = true;
                lines.add(ChatColor.GRAY + "Seconds: " + ChatColor.YELLOW + breakdown.seconds);
            }
            if (!include) {
                lines.add(ChatColor.GRAY + "Seconds: " + ChatColor.YELLOW + breakdown.seconds);
            }
            return lines;
        }
    }

    private static class UIData {
        public long time;
        public boolean invalid;
        public boolean playerRemovable;

        public UIData(final long time, final boolean invalid, final boolean playerRemovable) {
            this.time = time;
            this.invalid = invalid;
            this.playerRemovable = playerRemovable;
        }

        @Override
        public String toString() {
            final Map<String, Object> map = new HashMap<>();
            map.put("time", this.time);
            map.put("invalid", this.invalid);
            map.put("playerRemovable", this.playerRemovable);
            return map.toString();
        }
    }
}
