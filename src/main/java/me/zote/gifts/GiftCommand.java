package me.zote.gifts;

import com.google.common.collect.Lists;
import me.zote.gifts.items.GiftHeads;
import me.zote.gifts.items.menus.ItemBuilder;
import me.zote.gifts.items.menus.types.PaginatedGUI;
import me.zote.gifts.utils.InventorySave;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class GiftCommand extends Command {

    public GiftCommand() {
        super("gift", "Gift main command", "gifts.create", Lists.newArrayList());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {

        if(!(sender instanceof Player player))
            return false;

        if (args.length != 1) {
            sender.sendMessage("You must provide a player!");
            return false;
        }

        String target = args[0];

        PaginatedGUI menu = new PaginatedGUI("Choose a wrapper");
        String secretData = InventorySave.toBase64(player.getInventory().getContents());

        for (GiftHeads heads : GiftHeads.values()) {
            ItemBuilder builder = heads.asBuilder();
            builder.hideData("content", secretData);
            builder.hideData("owner", target);
            menu.addButton(builder.buildButton()
                    .setListener(l -> player.getInventory().addItem(builder.name("Gift for " + target).build())));
        }

        player.openInventory(menu.getInventory());
        return true;
    }

}
