package com.xLSC;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import com.bekvon.bukkit.residence.Residence;

import java.util.Arrays;
import java.util.List;

public class lp implements Listener {
    private final Residence residence = Residence.getInstance();
    private final List<Material> netheriteTools = Arrays.asList(
            Material.NETHERITE_SHOVEL,
            Material.NETHERITE_PICKAXE,
            Material.NETHERITE_AXE,
            Material.NETHERITE_HOE
    );
    private final Material compassItem = Material.COMPASS;

    @EventHandler
    public void onPlayerEnterResidence(PlayerMoveEvent event) {
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        Player player = event.getPlayer();
        if (residence != null && residence.getResidenceManager().isOwnerOfLocation(player, event.getTo()) && player.getGameMode() == GameMode.SURVIVAL) {
            giveCompass(player);
            giveNetheriteTools(player);
        }
    }

    @EventHandler
    public void onPlayerLeaveResidence(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location l = player.getLocation();
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        if (residence != null && !residence.getResidenceManager().isOwnerOfLocation(player, l) && player.getGameMode() == GameMode.SURVIVAL) {
            removeNetheriteTools(player);
            removeCompass(player);
            removeCompassFromInventory(player);
        }
    }

    private void giveCompass(Player player) {
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem == null || offHandItem.getType() == Material.AIR) {
            player.getInventory().setItemInOffHand(new ItemStack(compassItem));
        }
    }

    private void removeCompass(Player player) {
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        if (offHandItem != null && offHandItem.getType() == compassItem) {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }
    }

    private void removeCompassFromInventory(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == compassItem) {
                item.setAmount(0);
                contents[i] = item;
            }
        }
        player.getInventory().setContents(contents);
    }

    private void giveNetheriteTools(Player player) {
        for (Material material : netheriteTools) {
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() == material) {
                    return;
                }
            }
        }
        for (Material material : netheriteTools) {
            player.getInventory().addItem(new ItemStack(material));
        }
    }

    private void removeNetheriteTools(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && netheriteTools.contains(item.getType())) {
                item.setAmount(0);
                contents[i] = item;
            }
        }
        player.getInventory().setContents(contents);
    }

}
