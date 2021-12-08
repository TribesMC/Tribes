package me.rey.core.gui;

import com.google.common.primitives.Ints;
import me.rey.core.Warriors;
import me.rey.core.classes.ClassCondition;
import me.rey.core.classes.ClassType;
import me.rey.core.classes.abilities.Ability;
import me.rey.core.classes.abilities.AbilityType;
import me.rey.core.database.SQLManager;
import me.rey.core.players.User;
import me.rey.core.pvp.Build;
import me.rey.core.utils.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GuiClassEditor extends GuiEditable {

    private final int[] buildPositions = {2, 4, 6, 8, 30, 32, 34};
    private final ClassType classType;
    private final Player p;
    private final SQLManager sql;

    public GuiClassEditor(final Warriors plugin, final ClassType classType, final Player player) {
        super("", 6, plugin.getPlugin());

        this.classType = classType;
        this.p = player;
        this.sql = Warriors.getInstance().getSQLManager();
    }

    private Player getPlayer() {
        return this.p;
    }

    @Override
    public void setup() {

        final User u = new User(this.getPlayer());

        final int column = 2;
        for (int i = 0; i < this.getRows(); i++) {

			this.setItem(new GuiItem(new Item(Material.STAINED_GLASS_PANE).setDurability(15).setName("&r")) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                    // ignore

                }
            }, column - 1 + (9 * i));
        }

		this.setItem(new GuiItem(new Item(Material.QUARTZ).setName("&6&lDefault").setGlow(new User(this.getPlayer()).getSelectedBuild(this.classType) == null)) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {
                if (!GuiClassEditor.this.getItem(slot).getFromItem().hasGlow()) {
                    u.selectBuild(null, GuiClassEditor.this.classType);
					GuiClassEditor.this.updateInventory();
                }
            }
        }, 9);

        final ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        final SkullMeta meta = (SkullMeta) head.getItemMeta();

        final List<String> lore = new ArrayList<>(Arrays.asList(
                Text.color("&r"),
                Text.color(String.format("&7Player: &e%s", this.getPlayer().getDisplayName())),
                Text.color(String.format("&7Loaded Builds: &e%s", this.sql.getPlayerBuilds(u.getUniqueId(), this.classType).size())),
                Text.color("&r"),
                Text.color("&8&m---------------")
        ));

        for (final ClassCondition classCondition : this.classType.getClassConditions()) {
            lore.add(Text.color("&r"));
            lore.add(Text.color(String.format("&e&lCLASS CONDITION &f%s", classCondition.getName())));

            if (classCondition.getDescription() != null) {
                for (final String line : classCondition.getDescription()) {
                    lore.add(Text.color("&7" + line));
                }
            }

            lore.add(Text.color("&r"));
            lore.add(Text.color("&8&m---------------"));
        }

        meta.setOwner(this.getPlayer().getName());
        meta.setDisplayName(Text.color("&8&m---------------"));
        meta.setLore(lore);
        head.setItemMeta(meta);


        final Item emptyBuild = new Item(Material.INK_SACK).setDurability(8).setName("&7Empty Build");
        final Item[] buildItems = new Item[]{

                new Item(Material.INK_SACK).setDurability(1),
                new Item(Material.INK_SACK).setDurability(11),
                new Item(Material.INK_SACK).setDurability(14),
                new Item(Material.INK_SACK).setDurability(10),
                new Item(Material.INK_SACK).setDurability(12),
                new Item(Material.INK_SACK).setDurability(6),
                new Item(Material.INK_SACK).setDurability(5)

        };


        for (int i = 0; i < this.sql.getPlayerBuilds(u.getUniqueId(), this.classType).size(); i++) {

            final Build b = this.sql.getPlayerBuilds(u.getUniqueId(), this.classType).get(i);
            final int position = b.getPosition() <= -1 ? i + 1 : b.getPosition();

            if (i > this.buildPositions.length - 1) {
				break;
			}

            final Item item = u.getSelectedBuild(this.classType) != null && b.getUniqueId().toString().trim().equals(u.getSelectedBuild(this.classType).getUniqueId().toString().trim())
                    ? buildItems[position - 1].setGlow(true) : buildItems[position - 1];

            item.addLore("&8&m-------------------").addLore("&r");
            for (final AbilityType type : AbilityType.values()) {
                item.addLore("&2" + type.getName() + ": &r" + (b.getAbility(type) == null ? "None" : b.getAbility(type).getName() + " Lvl" + b.getAbilityLevel(type)));
            }
            item.addLore("&r");

			this.setItem(new GuiItem(item.setName("&e" + b.getName())) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
                    if (!item.hasGlow()) {
                        u.selectBuild(b, GuiClassEditor.this.classType);

						GuiClassEditor.this.updateInventory();
                    }
                }

            }, this.buildPositions[position - 1]);

            this.setEditItem(u, b, this.buildPositions[position - 1] + 9, this.buildPositions[position - 1]);
            this.setOptionsItem(u, b, this.buildPositions[position - 1] + 9 * 2);
        }

        for (int i = 0; i < this.buildPositions.length; i++) {

            this.setEditItem(u, null, this.buildPositions[i] + 9, this.buildPositions[i]);
            this.setOptionsItem(u, null, this.buildPositions[i] + 9 * 2);

			this.setItem(new GuiItem(emptyBuild) {
                @Override
                public void onUse(final Player player, final ClickType type, final int slot) {
					GuiClassEditor.this.createBuild(player, slot);
                }
            }, this.buildPositions[i]);

        }


		this.setItem(new GuiItem(head) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {
            }
        }, 0);

		this.setItem(new GuiItem(this.classType.getHelmet().setName("&f&l" + this.classType.getName())) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {
            }
        }, 18);

		this.setItem(new GuiItem(this.classType.getChestplate().setName("&f&l" + this.classType.getName())) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {
            }
        }, 27);

		this.setItem(new GuiItem(this.classType.getLeggings().setName("&f&l" + this.classType.getName())) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {
            }
        }, 36);

		this.setItem(new GuiItem(this.classType.getBoots().setName("&f&l" + this.classType.getName())) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

            }
        }, 45);

    }

    @Override
    public void init() {
        // IGNORE
    }

    private Build createBuild(final Player player, final int slot) {
        final User u = new User(player);
        if (this.sql.getPlayerBuilds(player.getUniqueId(), this.classType).size() >= this.buildPositions.length) {
			return u.getSelectedBuild(this.classType);
		}

        final int position = Ints.indexOf(this.buildPositions, slot) + 1;
        final Build def = new Build("", UUID.randomUUID(), position, new HashMap<Ability, Integer>());
        final String name = "Build ";
        int number = position;

        for (int index = 0; index <= this.buildPositions.length; index++) {
            boolean repeated = false;
            for (final Build b : this.sql.getPlayerBuilds(u.getUniqueId(), this.classType).getArray()) {
                if ((name + index).equalsIgnoreCase(b.getRawName())) {
                    repeated = true;
                }
            }
            if (!repeated) {
                number = number + index;
                break;
            }
        }

        def.setName(name + number);

		this.sql.createPlayerBuild(player.getUniqueId(), def, this.classType);
        u.selectBuild(def, this.classType);

		this.updateInventory();
        return u.getSelectedBuild(this.classType);
    }

    private void setOptionsItem(final User u, final Build b, final int slot) {
		this.setItem(new GuiItem(new Item(Material.BOOK_AND_QUILL).setName("&7Options")) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

                if (b != null) {
                    final GuiBuildOptions gui = new GuiBuildOptions(u.getPlayer(), b, GuiClassEditor.this.classType);
                    gui.setup();
                    gui.open(u.getPlayer());
                }

            }
        }, slot);
    }

    private void setEditItem(final User u, final Build b, final int slot, final int buildSlot) {

		this.setItem(new GuiItem(new Item(Material.ANVIL).setName("&7Edit: &e" + (b == null ? "None" : b.getName()))) {
            @Override
            public void onUse(final Player player, final ClickType type, final int slot) {

                Build toEdit = b;
                if (toEdit == null) {
					toEdit = GuiClassEditor.this.createBuild(player, buildSlot);
				}

                final GuiClassAbilitiesEditor editor = new GuiClassAbilitiesEditor(player, GuiClassEditor.this.classType, toEdit);
                editor.setup();
                editor.open(u.getPlayer());

            }
        }, slot);
    }


}
