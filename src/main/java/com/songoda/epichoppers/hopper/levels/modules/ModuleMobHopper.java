package com.songoda.epichoppers.hopper.levels.modules;

import com.songoda.core.utils.TextUtils;
import com.songoda.epichoppers.EpicHoppers;
import com.songoda.epichoppers.hopper.Hopper;
import com.songoda.epichoppers.utils.StorageContainerCache;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleMobHopper extends Module {
    private final int amount;
    private final Map<Block, Integer> blockTick = new HashMap<>();


    public ModuleMobHopper(EpicHoppers plugin, int amount) {
        super(plugin);
        this.amount = amount;
    }

    @Override
    public String getName() {
        return "MobHopper";
    }

    @Override
    public void run(Hopper hopper, StorageContainerCache.Cache hopperCache) {
        Block block = hopper.getLocation().getBlock();

        if (!this.blockTick.containsKey(block)) {
            this.blockTick.put(block, 1);
            return;
        }
        int tick = this.blockTick.get(block);
        int put = tick + 1;
        this.blockTick.put(block, put);
        if (tick < this.amount || !isEnabled(hopper)) {
            return;
        }

        hopper.getWorld().getNearbyEntities(hopper.getLocation(), 5, 5, 5).stream()
                .filter(entity -> entity instanceof LivingEntity && !(entity instanceof Player) &&
                        !(entity instanceof ArmorStand)).limit(1).forEach(entity -> {
                    Location location = hopper.getLocation().add(.5, 1, .5);
                    if (location.getBlock().getType() != Material.AIR) {
                        return;
                    }

                    entity.teleport(location);
                    ((LivingEntity) entity).damage(99999999);
                });
        this.blockTick.remove(block);
    }

    @Override
    public ItemStack getGUIButton(Hopper hopper) {
        ItemStack block = new ItemStack(Material.ROTTEN_FLESH, 1);
        ItemMeta blockMeta = block.getItemMeta();
        blockMeta.setDisplayName(this.plugin.getLocale().getMessage("interface.hopper.mobtitle").getMessage());
        ArrayList<String> loreBlock = new ArrayList<>();
        String[] parts = this.plugin.getLocale().getMessage("interface.hopper.moblore").processPlaceholder("enabled",
                isEnabled(hopper) ? this.plugin.getLocale().getMessage("general.word.enabled").getMessage()
                        : this.plugin.getLocale().getMessage("general.word.disabled").getMessage()).getMessage().split("\\|");
        for (String line : parts) {
            loreBlock.add(TextUtils.formatText(line));
        }
        blockMeta.setLore(loreBlock);
        block.setItemMeta(blockMeta);
        return block;
    }

    @Override
    public void runButtonPress(Player player, Hopper hopper, ClickType type) {
        saveData(hopper, "mobhopper", !isEnabled(hopper));
    }

    @Override
    public List<Material> getBlockedItems(Hopper hopper) {
        return null;
    }

    @Override
    public String getDescription() {
        return this.plugin.getLocale()
                .getMessage("interface.hopper.mobhopper")
                .processPlaceholder("ticks", this.amount)
                .getMessage();
    }

    public boolean isEnabled(Hopper hopper) {
        Object isMobHopper = getData(hopper, "mobhopper");
        return isMobHopper != null && (boolean) isMobHopper;
    }
}
