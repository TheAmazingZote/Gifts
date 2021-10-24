package me.zote.gifts.items.menus.buttons;

import me.zote.gifts.items.menus.types.PaginatedGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;

public class InventoryListenerGUI implements Listener {

	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		// Determine if the Inventory was a PaginatedGUI

		InventoryHolder holder = event.getInventory().getHolder();

		if (event.getCurrentItem() == null)
			return;

		if (event.getSlot() != event.getRawSlot())
			return;

		if (holder instanceof PaginatedGUI) {

			// Get the instance of the PaginatedGUI that was clicked.
			PaginatedGUI paginatedGUI = (PaginatedGUI) holder;

			event.setCancelled(true);
			// Then, assume the slot holds a GUIButton and attempt to get the button.
			GUIButton button = paginatedGUI.getButton(event.getSlot());

			// Finally, if the slot did actually hold a GUIButton (that has a listener)...
			if (button != null && button.getListener() != null) {
				// ...execute that button's listener.
				button.getListener().onClick(event);
			}

			((Player) event.getWhoClicked()).updateInventory();
		}

	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		InventoryHolder holder = event.getInventory().getHolder();

		if (holder instanceof PaginatedGUI) {
			PaginatedGUI paginatedGUI = (PaginatedGUI) holder;
			if (paginatedGUI.getCloseListener() != null)
				paginatedGUI.getCloseListener().accept(event);
		}
	}

}
