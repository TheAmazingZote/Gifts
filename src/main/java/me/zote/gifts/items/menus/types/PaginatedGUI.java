package me.zote.gifts.items.menus.types;

import com.google.common.collect.Maps;
import me.zote.gifts.items.menus.ItemBuilder;
import me.zote.gifts.items.menus.Paginator;
import me.zote.gifts.items.menus.buttons.GUIButton;
import me.zote.gifts.items.menus.buttons.InventoryListenerGUI;
import me.zote.gifts.items.menus.config.GUIConfig;
import me.zote.gifts.utils.Util;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class PaginatedGUI implements InventoryHolder {

	private final Paginator<GUIButton> paginator = new Paginator<>(45);

	private static InventoryListenerGUI inventoryListenerGUI;
	private final GUIConfig configuration;
	private final Map<Integer, GUIButton> items = Maps.newConcurrentMap();
	private final Map<Integer, GUIButton> toolbarItems = Maps.newHashMap();
	private Map<Integer, GUIButton> currentToolbar;
	private String name;
	@Getter
	@Setter
	private boolean clickAllowed;
	@Getter
	@Setter
	private Consumer<InventoryCloseEvent> closeListener;

	public PaginatedGUI(String name) {
		this(name, new GUIConfig());
	}

	public PaginatedGUI(String name, GUIConfig config) {
		this.configuration = config;
		this.name = Util.title(name);
	}

	public static void prepare(JavaPlugin plugin) {
		if (inventoryListenerGUI == null) {
			inventoryListenerGUI = new InventoryListenerGUI();
			plugin.getServer().getPluginManager().registerEvents(inventoryListenerGUI, plugin);
		}
	}

	public void setDisplayName(String name) {
		this.name = Util.color(name);
	}

	public GUIButton getButton(int slot) {
		if (hasToolbar()) {
			if (slot / 9 == getSize() / 9)
				return currentToolbar.get(slot % 9);
		}
		if (configuration.isPagination()) {
			if (slot >= paginator.getPage().size())
				return null;
			return paginator.getPage().get(slot);
		}
		return items.get(slot);
	}

	public void fill(GUIButton button) {
		int size = getSize();
		for (int slot = 0; slot < size; slot++)
			if (getButton(slot) == null)
				setButton(slot, button);
	}

	public void fillRow(int row, GUIButton button) {
		if (configuration.isPagination())
			throw new UnsupportedOperationException("This action is not available for paginated gui's");

		if (row < 0 || row > 5) {
			throw new IllegalArgumentException(
					"The desired row is outside the bounds of the inventory slot range. [0-5]");
		}
		int startSlot = row * 9;
		for (int i = 0; i < 9; i++) {
			if (getButton(startSlot + i) == null)
				setButton(startSlot + i, button);
		}
	}

	public void setRow(int row, GUIButton button) {
		if (configuration.isPagination())
			throw new UnsupportedOperationException("This action is not available for paginated gui's");
		if (row < 0 || row > 5) {
			throw new IllegalArgumentException(
					"The desired row is outside the bounds of the inventory slot range. [0-5]");
		}
		int startSlot = row * 9;
		for (int i = 0; i < 9; i++)
			setButton(startSlot + i, button);
	}

	public void addButton(GUIButton button) {
		if (configuration.isPagination()) {
			this.paginator.addElement(button);
			return;
		}

		int size = getSize();
		for (int slot = 0; slot < size; slot++) {
			if (!items.containsKey(slot)) {
				items.put(slot, button);
				break;
			}
		}
	}

	public void setButton(int slot, GUIButton button) {
		if (configuration.isPagination())
			paginator.addElement(button);
		else
			items.put(slot, button);
	}

	public void removeButton(int slot) {
		if (configuration != null && configuration.isPagination())
			throw new UnsupportedOperationException("This action is not available for paginated gui's");
		items.remove(slot);
	}

	public void fillToolbar(GUIButton button) {
		for (int i = 0; i < 9; i++)
			if (!toolbarItems.containsKey(i))
				toolbarItems.put(i, button);
	}

	public void setToolbarItem(int slot, GUIButton button) {
		if (slot < 0 || slot > 8)
			throw new IllegalArgumentException(
					"The desired slot is outside the bounds of the toolbar slot range. [0-8]");

		toolbarItems.put(slot, button);
	}

	public void removeToolbarItem(int slot) {
		if (slot < 0 || slot > 8)
			throw new IllegalArgumentException(
					"The desired slot is outside the bounds of the toolbar slot range. [0-8]");

		toolbarItems.remove(slot);
	}

	public void refreshInventory(HumanEntity holder) {
		holder.openInventory(getInventory());
	}

	public void setToolbar() {
		currentToolbar = Maps.newHashMap();
		currentToolbar.putAll(toolbarItems);

		if (hasPages()) {

			GUIButton backButton = new GUIButton(
					ItemBuilder.start(Material.ARROW).name(configuration.getPreviousPage()).build());
			GUIButton pageIndicator = new GUIButton(ItemBuilder.start(Material.NAME_TAG)
					.name(configuration.getCurrentPage()
							.replaceAll(Pattern.quote("{currentPage}"), String.valueOf(paginator.getCurrent()))
							.replaceAll(Pattern.quote("{maxPages}"), String.valueOf(paginator.getTotalPages())))
					.build());
			GUIButton nextButton = new GUIButton(
					ItemBuilder.start(Material.ARROW).name(configuration.getNextPage()).build());

			backButton.setListener(event -> {
				PaginatedGUI menu = (PaginatedGUI) event.getClickedInventory().getHolder();
				menu.paginator.setCurrent(menu.paginator.getPrev());
				refreshInventory(event.getWhoClicked());
			});

			nextButton.setListener(event -> {
				PaginatedGUI menu = (PaginatedGUI) event.getClickedInventory().getHolder();
				menu.paginator.setCurrent(menu.paginator.getNext());
				refreshInventory(event.getWhoClicked());
			});

			if (paginator.hasPrev())
				currentToolbar.put(3, backButton);

			currentToolbar.put(4, pageIndicator);

			if (paginator.hasNext())
				currentToolbar.put(5, nextButton);
		}

	}

	@Override
	public Inventory getInventory() {
		int size = getSize();
		Inventory inventory = Bukkit.createInventory(this, hasToolbar() ? size + 9 : size, name);

		if (configuration.isPagination()) {
			List<GUIButton> butts = paginator.getPage();
			for (int i = 0; i < butts.size(); i++)
				inventory.setItem(i, butts.get(i).getItem());
		} else {
			for (Map.Entry<Integer, GUIButton> ent : items.entrySet()) {
				inventory.setItem(ent.getKey(), ent.getValue().getItem());
			}
		}

		if (hasToolbar()) {
			setToolbar();
			for (Map.Entry<Integer, GUIButton> ent : currentToolbar.entrySet()) {
				int rawSlot = ent.getKey() + size;
				inventory.setItem(rawSlot, ent.getValue().getItem());
			}
		}

		return inventory;
	}

	public boolean hasToolbar() {
		return !toolbarItems.isEmpty() || hasPages();
	}

	public boolean hasPages() {
		return configuration.isPagination() && paginator.getTotalPages() > 1;
	}

	public int getSize() {

		if (hasPages())
			return 45;

		if (configuration.isPagination())
			return getSize(paginator.getPage().size());

		if (configuration.getSize() != 0)
			return getSize(configuration.getSize());

		int itemSize = items.isEmpty() ? 9 : items.keySet().stream().mapToInt(i -> i).max().getAsInt();
		return getSize(itemSize);
	}

	public static int getSize(int size) {
		if (size <= 0)
			size = 9;
		double d = Math.ceil(size / 9.0);
		if (d <= 0.0)
			d = 1.0;
		return (int) (d * 9);
	}

}