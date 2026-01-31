package org.claimer.claimer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

public class Claimer extends JavaPlugin {

    private ClaimManager manager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        manager = new ClaimManager(this);

        // Events
        getServer().getPluginManager().registerEvents(
                new ClaimListener(manager, this), this
        );

        /* =======================
           /claim
           ======================= */
        getCommand("claim").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Commande réservée aux joueurs.");
                return true;
            }

            var chunk = player.getLocation().getChunk();

            // /claim
            if (args.length == 0) {

                if (manager.isClaimed(chunk)) {
                    player.sendMessage(getConfig().getString("messages.already-claimed"));
                    return true;
                }

                int maxClaims = getConfig().getInt("max-claims-per-player");
                long currentClaims = manager.getClaimCount(player.getUniqueId());

                if (currentClaims >= maxClaims) {
                    player.sendMessage(
                            getConfig().getString("messages.claim-limit-reached")
                                    .replace("%max%", String.valueOf(maxClaims))
                    );
                    return true;
                }

                manager.claim(chunk, player.getUniqueId());

                player.sendMessage(
                        getConfig().getString("messages.claim-success")
                                .replace("%current%", String.valueOf(currentClaims + 1))
                                .replace("%max%", String.valueOf(maxClaims))
                );
                return true;
            }

            // Le claim doit exister et le joueur doit être le propriétaire
            if (!manager.isClaimed(chunk)
                    || !manager.getOwner(chunk).equals(player.getUniqueId())) {
                player.sendMessage(getConfig().getString("messages.not-owner"));
                return true;
            }

            // /claim add <joueur>
            if (args[0].equalsIgnoreCase("add") && args.length == 2) {
                var target = Bukkit.getOfflinePlayer(args[1]);
                manager.addMember(chunk, target.getUniqueId());

                player.sendMessage(
                        getConfig().getString("messages.member-added")
                                .replace("%player%", target.getName())
                );
                return true;
            }

            // /claim remove <joueur>
            if (args[0].equalsIgnoreCase("remove") && args.length == 2) {
                var target = Bukkit.getOfflinePlayer(args[1]);
                manager.removeMember(chunk, target.getUniqueId());

                player.sendMessage(
                        getConfig().getString("messages.member-removed")
                                .replace("%player%", target.getName())
                );
                return true;
            }

            player.sendMessage("§cUsage: /claim | /claim add <joueur> | /claim remove <joueur>");
            return true;
        });

        /* =======================
           /unclaim
           ======================= */
        getCommand("unclaim").setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Commande réservée aux joueurs.");
                return true;
            }

            var chunk = player.getLocation().getChunk();

            if (!manager.isClaimed(chunk)) {
                player.sendMessage(getConfig().getString("messages.not-claimed"));
                return true;
            }

            if (!manager.getOwner(chunk).equals(player.getUniqueId())) {
                player.sendMessage(getConfig().getString("messages.not-owner"));
                return true;
            }

            manager.unclaim(chunk);
            player.sendMessage("§aChunk unclaim avec succès !");
            return true;
        });

        /* =======================
           /info
           ======================= */
        getCommand("info").setExecutor((sender, command, label, args) -> {

            sender.sendMessage("§8§m------------------------");
            sender.sendMessage("§6§lClaimer");
            sender.sendMessage("§7Version: §e" + getDescription().getVersion());
            sender.sendMessage("§7Auteur: §e" +
                    String.join(", ", getDescription().getAuthors()));
            sender.sendMessage("§7Description:");
            sender.sendMessage("§f" + getDescription().getDescription());
            sender.sendMessage("§8§m------------------------");

            return true;
        });
    }
}