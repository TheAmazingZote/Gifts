package me.zote.gifts;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import me.zote.gifts.items.menus.ItemBuilder;
import me.zote.gifts.items.menus.types.PaginatedGUI;
import me.zote.gifts.utils.InventorySave;
import me.zote.gifts.utils.Util;
import org.apache.commons.lang.text.StrSubstitutor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.Map;

public final class Gifts extends JavaPlugin implements Listener {

    private static Gifts instance;

    public static Gifts getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        PaginatedGUI.prepare(this);
        getServer().getCommandMap().register("gifts", new GiftCommand());

        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        HandlerList.unregisterAll((JavaPlugin) this);
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();

        if (item.getType() != Material.PLAYER_HEAD)
            return;

        ItemBuilder builder = new ItemBuilder(item);

        if (!builder.hasHiddenData("content"))
            return;

        Block block = event.getBlockPlaced();
        Skull skull = (Skull) block.getState();
        PersistentDataContainer container = skull.getPersistentDataContainer();
        String owner = builder.getHiddenData("owner");
        container.set(key("owner"), PersistentDataType.STRING, owner);
        container.set(key("content"), PersistentDataType.STRING, builder.getHiddenData("content"));

        skull.update();

        Hologram hologram = HologramsAPI.createHologram(this, block.getLocation().add(0.5, 1.0, 0.5));
        hologram.appendTextLine(Util.color("&cRegalo para &r" + owner));
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        boolean result = handleOpen(event.getPlayer(), event.getBlock());
        event.setCancelled(event.isCancelled() | result);
    }

    @EventHandler
    public void clickListener(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getHand() != EquipmentSlot.HAND)
            return;

        handleOpen(event.getPlayer(), event.getClickedBlock());
    }

    private boolean handleOpen(Player player, Block block) {
        if (block == null || block.getType() != Material.PLAYER_HEAD)
            return false;

        Skull skull = (Skull) block.getState();
        PersistentDataContainer container = skull.getPersistentDataContainer();
        String owner = container.get(key("owner"), PersistentDataType.STRING);

        if (!player.getName().equalsIgnoreCase(owner)) {
            player.sendMessage("Este regalo no es tuyo!");
            return false;
        }

        String data = container.get(key("content"), PersistentDataType.STRING);
        ItemStack[] arr = InventorySave.fromBase64(data);

        for (ItemStack item : arr) {
            if (item != null) {
                if (item.getType() == Material.COMMAND_BLOCK) {
                    runCmd(player, item);
                } else {
                    player.getInventory().addItem(item);
                }
            }
        }

        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0F, 1F);
        block.setType(Material.AIR);

        block.getLocation().getWorld().spawn(block.getLocation(), Firework.class, fw -> {
            FireworkMeta meta = fw.getFireworkMeta();
            meta.setPower(0);
            meta.addEffect(FireworkEffect.builder().withFlicker().withTrail()
                    .withColor(Color.RED, Color.WHITE).with(FireworkEffect.Type.BALL_LARGE).build());
            fw.setFireworkMeta(meta);
            fw.detonate();
        });
        HologramsAPI.getHolograms(this).forEach(Hologram::delete);
        return true;
    }

    public NamespacedKey key(String key) {
        return new NamespacedKey(this, key);
    }

    private void runCmd(Player player, ItemStack item) {
        String cmd = ItemBuilder.start(item).getName();
        Map<String, Object> placeholders = Collections.singletonMap("player", player.getName());
        cmd = replace(cmd, placeholders);
        cmd = cmd.startsWith("/") ? cmd.substring(1) : cmd;
        Bukkit.dispatchCommand(getServer().getConsoleSender(), cmd);
    }

    private String replace(String string, Map<String, Object> placeholders) {
        return StrSubstitutor.replace(string, placeholders, "%", "%");
    }

}
