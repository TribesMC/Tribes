package me.rey.core.gui;

import me.rey.core.Warriors;
import me.rey.core.classes.ClassType;
import me.rey.core.database.SQLManager;
import me.rey.core.effects.SoundEffect;
import me.rey.core.gui.anvil.AnvilGUI;
import me.rey.core.gui.anvil.AnvilGUI.AnvilClickEvent;
import me.rey.core.gui.anvil.AnvilGUI.AnvilSlot;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;
import me.rey.core.utils.Text;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public class GuiBuildOptions extends GuiEditable {

    private final Player player;
    private final Build b;
    private final ClassType classType;
    private final SQLManager sql;

    public GuiBuildOptions(final Player p, final Build build, final ClassType classType) {
        super("&8Editing: &4" + build.getName(), 3, Warriors.getInstance().getPlugin());

        this.player = p;
        this.b = build;
        this.classType = classType;
        this.sql = Warriors.getInstance().getSQLManager();
    }

    @Override
    public void setup() {

        final User u = new User(this.player);

        /*
         * RENAME
         */
		this.setItem(new GuiItem(new Item(Material.NAME_TAG).setName("&e&lRENAME")) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

                final AnvilGUI gui = new AnvilGUI(Warriors.getInstance().getPlugin(), u.getPlayer(), new AnvilGUI.AnvilClickEventHandler() {

                    @Override
                    public void onAnvilClick(final AnvilClickEvent event) {
                        if (GuiBuildOptions.this.b == null) {
							return;
						}
                        event.setWillClose(event.getSlot() == AnvilSlot.OUTPUT);
                        event.setWillDestroy(event.getSlot() == AnvilSlot.OUTPUT);

                        if (event.getSlot() == AnvilSlot.OUTPUT && event.getName() != null && event.getName().trim() != "") {

                            if (ChatColor.stripColor(Text.color(event.getName().toString())).length() > 10) {
                                u.sendMessageWithPrefix("Build", "Invalid name!");
                                return;
                            }

                            if (event.getName().trim().equals(GuiBuildOptions.this.b.getRawName().trim())) {
                                u.sendMessageWithPrefix("Build", "Invalid name!");
                                return;
                            }

                            for (final Build query : GuiBuildOptions.this.sql.getPlayerBuilds(u.getUniqueId(), GuiBuildOptions.this.classType)) {
                                if (query.getNameWithoutColors().trim().equals(ChatColor.stripColor(Text.color(event.getName().toString())))) {
                                    u.sendMessageWithPrefix("Build", "Invalid name!");
                                    return;
                                }
                            }

                            if (ChatColor.stripColor(Text.color(event.getName().toString())).startsWith("Build ")) {
                                u.sendMessageWithPrefix("Build", "Invalid name!");
                                return;
                            }

                            final String oldName = GuiBuildOptions.this.b.getName();
                            final Build newBuild = GuiBuildOptions.this.b;
                            newBuild.setName(event.getName());
                            u.sendMessageWithPrefix("Build", "Sucessfully renamed &e" + oldName + " &7to &e" + newBuild.getName() + "&7!");
                            u.editBuild(GuiBuildOptions.this.b, newBuild, GuiBuildOptions.this.classType);

                            new SoundEffect(Sound.ANVIL_USE, 1.0F).play(player);
                        }
                    }

                });

                final Item text = new Item(Material.BOOK_AND_QUILL).setName(GuiBuildOptions.this.b.getName());
                gui.setSlot(AnvilSlot.INPUT_LEFT, text.get());

                try {
                    gui.open();
                } catch (final Exception e) {
                    e.printStackTrace();
                }

            }
        }, 11);

        /*
         * DELETE
         */
		this.setItem(new GuiItem(new Item(Material.STAINED_CLAY).setDurability(14).setName("&c&lDELETE")) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

                u.sendMessageWithPrefix("Build", "Sucessfully deleted &e" + GuiBuildOptions.this.b.getName() + "&7.");
				GuiBuildOptions.this.sql.deletePlayerBuild(player.getUniqueId(), GuiBuildOptions.this.b, GuiBuildOptions.this.classType);
                player.closeInventory();

            }
        }, 15);

        /*
         * EMPTY SLOTS
         */
        this.fillEmptySlots(new GuiItem(new Item(Material.STAINED_GLASS_PANE).setDurability(15).setName("&r")) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {
                // IGNORE
            }
        });
    }

    @Override
    public void init() {
        // ignore
    }

}
