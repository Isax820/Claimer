package org.claimer.claimer;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class ClaimListener implements Listener {

    private final ClaimManager manager;
    private final Claimer plugin;

    public ClaimListener(ClaimManager manager, Claimer plugin) {
        this.manager = manager;
        this.plugin = plugin;
    }

    private boolean allowed(org.bukkit.entity.Player player, org.bukkit.Chunk chunk) {
        return manager.getOwner(chunk).equals(player.getUniqueId())
                || manager.isMember(chunk, player.getUniqueId());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        var chunk = e.getBlock().getChunk();
        var player = e.getPlayer();

        if (manager.isClaimed(chunk) && !allowed(player, chunk)) {
            e.setCancelled(true);
            player.sendMessage(plugin.getConfig().getString("messages.claim-protected"));
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        var chunk = e.getBlock().getChunk();
        var player = e.getPlayer();

        if (manager.isClaimed(chunk) && !allowed(player, chunk)) {
            e.setCancelled(true);
            player.sendMessage(plugin.getConfig().getString("messages.claim-protected"));
        }
    }
}