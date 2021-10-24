package me.zote.gifts.items.menus.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class InventoryConfiguration {

	private String chatPrefix = "&c&lGUI  &c";
	private int size;
	private boolean pagination = true;

}
