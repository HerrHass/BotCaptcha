package de.herrhass.botcaptcha.utils.itembuilder;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ItemBuilder {

    private ItemStack itemStack;
    private ItemMeta itemMeta;

    public ItemBuilder(Material material, short subId) {
        itemStack = new ItemStack(material, 1, subId);
        itemMeta = itemStack.getItemMeta();
    }

    public ItemBuilder(Material material) {
        this(material, (short) 0);
    }

    public static void fillEmptySlots(Inventory inventory) {
        ItemStack fillMaterial = new ItemBuilder(Material.STAINED_GLASS_PANE, (short) 7).setName(" ").build();

        for (int slots = 0; slots < inventory.getSize(); slots++) {
            if (inventory.getItem(slots) == null) {
                inventory.setItem(slots, fillMaterial);
            }

        }

    }

    public ItemBuilder setName(String displayName) {
        itemMeta.setDisplayName(displayName);
        return this;
    }

    public ItemBuilder setAmount(Integer amount) {
        itemStack.setAmount(amount);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        itemMeta.setLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder hideAttributes() {
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        return this;
    }

    public ItemStack build() {
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
