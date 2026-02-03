package com.xLSC;

import com.bekvon.bukkit.residence.Residence;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

public class ResidenceListener implements Listener {
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Block block = e.getBlock();
        Location l = block.getLocation();
        Residence residence = Residence.getInstance();
        if(l.getX() >= -1.5 && l.getX() <= 4495 && l.getZ() >= 14 && l.getZ() <= 4097) {
            if (residence != null && residence.getResidenceManager().isOwnerOfLocation(player, l)) {
                if (item.getType() == Material.NETHERITE_SHOVEL ||
                        item.getType() == Material.NETHERITE_AXE ||
                        item.getType() == Material.NETHERITE_HOE ||
                        item.getType() == Material.NETHERITE_PICKAXE) {
                    return;
                }
            }
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            } else {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        Block block = e.getBlock();
        Location l = block.getLocation();
        Residence residence = Residence.getInstance();
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        ItemStack mainHandItem = player.getInventory().getItemInMainHand();
        Location lo = player.getLocation();

        if (lo.getX() >= -1.5 && lo.getX() <= 4495 && lo.getZ() >= 14 && lo.getZ() <= 4097) {
            if (residence != null && residence.getResidenceManager().isOwnerOfLocation(player, l)) {
                if (offHandItem.getType() == Material.COMPASS) {
                    return;
                }
            }
            if (player.getGameMode() == GameMode.CREATIVE) {
                return;
            }
            e.setCancelled(true);
            player.getInventory().addItem(mainHandItem.clone());
            mainHandItem.setAmount(mainHandItem.getAmount() - 1);
            player.getInventory().setItemInMainHand(mainHandItem);
        } else {
            e.setCancelled(false);
        }
    }
}
