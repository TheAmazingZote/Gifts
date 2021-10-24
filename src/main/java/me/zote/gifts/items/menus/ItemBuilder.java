package me.zote.gifts.items.menus;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import me.zote.gifts.Gifts;
import me.zote.gifts.items.menus.buttons.GUIButton;
import me.zote.gifts.utils.Util;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class ItemBuilder {

    private static final NamespacedKey key = new NamespacedKey(Gifts.getInstance(), "properties");

    private final ItemStack stack;

    public ItemBuilder(ItemStack stack) {
        this.stack = stack;
    }

    public static ItemBuilder start(Material material) {
        return new ItemBuilder(new ItemStack(material));
    }

    public static GUIButton filler(DyeColor data) {
        return filler("&c", data);
    }

    public static GUIButton filler(String name, DyeColor color) {
        String material = color.name() + "_STAINED_GLASS_PANE";
        return ItemBuilder.start(Material.matchMaterial(material)).name(name).buildButton();
    }

    public static ItemBuilder start(ItemStack stack) {
        return new ItemBuilder(stack);
    }

    public ItemBuilder name(String name) {
        ItemMeta stackMeta = stack.getItemMeta();
        stackMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        stack.setItemMeta(stackMeta);
        return this;
    }

    public ItemBuilder title(String title) {
        return name(Util.title(title));
    }

    public ItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder owner(PlayerProfile profile) {
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setPlayerProfile(profile);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder property(Properties properties) {
        String json = new Gson().toJson(properties);
        ItemMeta meta = stack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(key, PersistentDataType.STRING, json);
        stack.setItemMeta(meta);
        return this;
    }

    public Properties properties() {
        PersistentDataContainer container = stack.getItemMeta().getPersistentDataContainer();
        if (!container.has(key, PersistentDataType.STRING))
            return null;
        return new Gson().fromJson(container.get(key, PersistentDataType.STRING), Properties.class);
    }

    public ItemBuilder lore(String... lore) {
        return lore(Arrays.asList(lore));
    }

    public ItemBuilder lore(List<String> add) {
        ItemMeta meta = stack.getItemMeta();
        List<String> lore = meta.hasLore() ? meta.getLore() : Lists.newArrayList();

        add = add.stream().map(Util::color).collect(Collectors.toList());
        lore.addAll(add);
        meta.setLore(lore);
        stack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder hideData(String key, String value) {
        ItemMeta meta = stack.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(new NamespacedKey(Gifts.getInstance(), key), PersistentDataType.STRING, value);
        stack.setItemMeta(meta);
        return this;
    }

    public boolean hasHiddenData(String key) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return false;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(Gifts.getInstance(), key);
        return container.has(namespacedKey, PersistentDataType.STRING);
    }

    public String getHiddenData(String key) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null)
            return null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey namespacedKey = new NamespacedKey(Gifts.getInstance(), key);
        if (!container.has(namespacedKey, PersistentDataType.STRING))
            return null;
        return container.get(namespacedKey, PersistentDataType.STRING);
    }

    public String getName() {
        ItemMeta meta = stack.getItemMeta();
        return meta.getDisplayName();
    }

    /**
     * Returns the class' internally modified {@link ItemStack} object.
     *
     * @return The updated ItemStack.
     */
    public ItemStack build() {
        return stack;
    }

    public GUIButton buildButton() {
        return new GUIButton(build());
    }
}
