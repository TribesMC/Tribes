package me.rey.clans.gui;

import me.rey.core.utils.UtilText;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.Plugin;

import me.rey.clans.items.Glow;
import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Deprecated
public abstract class Gui implements Listener {

    Inventory inventory;

    // This is for some UIs that need to differentiate between
    // different UIs of the same type
    private final String inventoryId;

    private String name;
    private int rows, size;
    private Plugin plugin;
    private Map<Integer, GuiItem> events = new HashMap<Integer, GuiItem>();
    private InventoryType type;
    private InventoryClosed closed = null;
    private boolean dontExecuteClosed = false;

    public Gui(String name, int rows, Plugin plugin) {
        this.inventoryId = UtilText.randomString(16);
        this.name = name;
        this.rows = rows;
        this.size = rows*9;
        this.plugin = plugin;
        this.create();
    }

    public Gui(InventoryType type, String name, int rows, Plugin plugin) {
        this.inventoryId = UtilText.randomString(16);
        this.type = type;
        this.name = name;
        this.rows = rows;
        this.size = rows*9-1;
        this.plugin = plugin;
        this.create();
    }

    private void create() {
        inventory = Bukkit.createInventory(null, 9*rows, ChatColor.translateAlternateColorCodes('&', name));
        if(type != null) inventory = Bukkit.createInventory(null, type);

        Bukkit.getPluginManager().registerEvents(this, plugin);

        init();
    }


    public abstract void init();

    protected void addItem(GuiItem guiItem) {
        if(this.getInventory().firstEmpty() == -1) return;
        setItem(guiItem, this.getInventory().firstEmpty());
    }

    protected void removeItem(int slot, int... slots) {
        for(int query : slots) {
            if(!events.containsKey(query)) continue;
            events.remove(query);
            this.inventory.setItem(query, new ItemStack(Material.AIR));
        }

        if(events.containsKey(slot)) {
            events.remove(slot);
            this.inventory.setItem(slot, new ItemStack(Material.AIR));
        }
    }

    protected void setItem(GuiItem item, int slot, int... slots) {
        for(int query : slots) {
            if(events.containsKey(query)) continue;
            this.events.put(query, item);
            this.inventory.setItem(query, item.get());
        }

        if(events.containsKey(slot)) return;
        this.events.put(slot, item);
        this.inventory.setItem(slot, item.get());

    }

    protected void setItemOverride(GuiItem item, int slot, int... slots) {
        for(int query : slots) {
            this.events.put(query, item);
            this.inventory.setItem(query, item.get());
        }

        this.events.put(slot, item);
        this.inventory.setItem(slot, item.get());

    }

    protected void setOnClose(InventoryClosed closed) {
        this.closed = closed;
    }

    protected void pauseOnClose(boolean dontExecuteClosed) {
        this.dontExecuteClosed = dontExecuteClosed;
    }

    protected void removeOnClose(InventoryClosed closed) {
        this.closed = null;
    }

    protected void fillEmptySlots(GuiItem guiItem) {
        for(int i = 0; i < this.getInventory().getSize() - 1; i++) {
            this.addItem(guiItem);
        }
    }

    public void borderise(GuiItem item) {

        for (int i = 0; i < 9; i++) {
            setItemOverride(item, i);
            setItemOverride(item, i + inventory.getSize() - 9);
            events.remove(i);
            events.remove( + inventory.getSize() - 9);
        }

        for (int i = 0; i < inventory.getSize(); i++) {
            if (i % 9 == 0 || (i + 1) % 9 == 0) {
                setItemOverride(item, i);
                events.remove(i);
            }
        }
    }

    public void open(Player player) {
        player.openInventory(this.inventory);
    }

    public String getInventoryId() {
        return inventoryId;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public String getName() {
        return name;
    }

    public int getRows() {
        return rows;
    }

    public int getSize() {
        return size;
    }

    public GuiItem getItem(int slot) {
        if(events.containsKey(slot)) {
            return events.get(slot);
        }
        return null;
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();

        if(e.getInventory().equals(this.inventory)) {
            if (closed != null && !dontExecuteClosed) {
                closed.onClose(player, e);
            }
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();


        if(e.getInventory().equals(this.inventory)) {
            e.setCancelled(true);
            if(!this.events.containsKey(e.getSlot())) return;

            if(e.getClickedInventory() == null) return;
            if(e.getClickedInventory().getItem(e.getSlot()) == null) return;
            if(e.getView().getBottomInventory() == null) return;
            if((e.getClickedInventory().getHolder() instanceof Player)) return;

            GuiItem item = this.events.get(e.getSlot());
            item.onUse(player, e.getClick(), e.getSlot());
        }
    }

    public static class Item {

        protected boolean glow;
        protected Material material;
        protected int data;
        protected int amount = 1;
        protected int durability = 0;
        protected String name;
        protected List<String> lore;
        protected ItemStack item;
        protected Map<Enchantment, Integer> enchantements;


        public Item(Material material){
            this.material = material;
            this.lore = new ArrayList<>();
            this.enchantements = new HashMap<>();
            this.glow = false;
        }

        public Item(ItemStack item){
            this.item = item;
            this.amount = item.getAmount();
            this.durability = item.getDurability();
            this.material = item.getType();
            this.lore = item.hasItemMeta() && item.getItemMeta().hasLore() ? item.getItemMeta().getLore() : new ArrayList<>();
            this.enchantements = item.getEnchantments();
            this.name = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : null;
            this.glow = false;
        }

        public Item setName(String name){
            this.name = ChatColor.translateAlternateColorCodes('&', "&r&f" + name);
            return this;
        }

        public Item setLore(List<String> lore){
            List<String> newLore = new ArrayList<String>();
            for(String line : lore) {
                newLore.add(ChatColor.translateAlternateColorCodes('&', line));
            }
            this.lore = newLore;
            return this;
        }

        public Item addLore(String lore){
            this.lore.add(ChatColor.translateAlternateColorCodes('&', lore));
            return this;
        }

        public Item setData(int data) {
            this.data = data;
            return this;
        }

        public Item setDefaultLore(String... lore) {
            this.addLore("&8&m-------------------");
            this.addLore("&r");
            for(String query : lore) {
                this.addLore(query);
            }
            this.addLore("&r");
            this.addLore("&8&m-------------------");
            return this;
        }

        public Item setAmount(int amount){
            this.amount = amount;
            return this;
        }

        public Item setDurability(int durability) {
            this.durability = durability;
            return this;
        }

        public Item setGlow(boolean glow) {
            this.glow = glow;
            return this;
        }

        public boolean hasGlow() {
            return this.get().containsEnchantment(new Glow(255));
        }

        public Item addEnchantment(Enchantment enchantment, int level){
            this.enchantements.remove(enchantment);
            this.enchantements.put(enchantment, level);
            return this;
        }

        public String getName() {
            return name != null ? name : me.rey.clans.utils.UtilText.formatName(material.name());
        }

        @SuppressWarnings("deprecation")
        public ItemStack get(){
            ItemStack item;
            if(this.item != null) {
                item = this.item.clone();
            } else {
                item = new ItemStack(material, amount, (byte) durability);
                item.setData(new MaterialData(material, (byte)data));
            }

            if (this instanceof PlayerSkull) {
                PlayerSkull skull = (PlayerSkull) this;
                SkullMeta meta = (SkullMeta) item.getItemMeta();
                meta.setOwner(skull.target.getName());
                if(name != null) meta.setDisplayName(name);
                if(lore != null && !lore.isEmpty()) meta.setLore(lore);
                if(glow) {
                    Glow glow = new Glow(255);
                    meta.addEnchant(glow, 1, true);
                }
                if(enchantements != null && !enchantements.isEmpty()){
                    for(Enchantment enchant : enchantements.keySet()){
                        item.addEnchantment(enchant, enchantements.get(enchant));
                    }
                }

                item.setItemMeta(meta);
                return item;
            }

            ItemMeta meta = item.getItemMeta();
            if(name != null) meta.setDisplayName(name);
            if(lore != null && !lore.isEmpty()) meta.setLore(lore);
            if(glow) {
                Glow glow = new Glow(255);
                meta.addEnchant(glow, 1, true);
            }
            if(enchantements != null && !enchantements.isEmpty()){
                for(Enchantment enchant : enchantements.keySet()){
                    item.addEnchantment(enchant, enchantements.get(enchant));
                }
            }
            item.setItemMeta(meta);
            return item;
        }

    }

    public static class PlayerSkull extends Item {

        public OfflinePlayer target;

        public PlayerSkull(OfflinePlayer target) {
            super(Material.SKULL_ITEM);
            this.target = target;
            item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        }

        public void setSkullOwner(OfflinePlayer target) {
            this.target = target;
        }
    }

    public static abstract class GuiItem {

        private ItemStack itemStack;
        private Item item;


        public GuiItem(Item item){
            this.itemStack = item.get();
            this.item = item;
        }

        public GuiItem(ItemStack item) {
            this.itemStack = item;
            this.item = new Item(item.getType());
        }


        public ItemStack get() {
            return itemStack;
        }

        public Item getFromItem() {
            return item;
        }


        public abstract void onUse(Player player, ClickType type, int slot);

    }

    public interface InventoryClosed {
        void onClose(Player player, InventoryCloseEvent e);
    }
}
