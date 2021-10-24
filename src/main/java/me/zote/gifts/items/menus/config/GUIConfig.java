package me.zote.gifts.items.menus.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class GUIConfig extends InventoryConfiguration {

    private String noPreviousPages = "There are no previous pages.";

    private String noAdditionalPages = "There are no additional pages.";

    private String previousPage = "&c&lPrevious Page";

    private String currentPage = "&c&lPage {currentPage} of {maxPages}";

    private String nextPage = "&c&lNext Page";
}

