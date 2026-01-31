package org.claimer.claimer;

import org.bukkit.Chunk;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ClaimManager {

    private final Map<String, UUID> owners = new HashMap<>();
    private final Map<String, Set<UUID>> members = new HashMap<>();
    private final Claimer plugin;

    public ClaimManager(Claimer plugin) {
        this.plugin = plugin;
        loadClaims();
    }

    private String key(Chunk chunk) {
        return chunk.getWorld().getName() + ";" + chunk.getX() + ";" + chunk.getZ();
    }

    /* ===== GETTERS ===== */

    public boolean isClaimed(Chunk chunk) {
        return owners.containsKey(key(chunk));
    }

    public UUID getOwner(Chunk chunk) {
        return owners.get(key(chunk));
    }

    public boolean isMember(Chunk chunk, UUID uuid) {
        return members.getOrDefault(key(chunk), Set.of()).contains(uuid);
    }

    public long getClaimCount(UUID uuid) {
        return owners.values().stream().filter(o -> o.equals(uuid)).count();
    }

    /* ===== ACTIONS ===== */

    public void claim(Chunk chunk, UUID owner) {
        String key = key(chunk);
        owners.put(key, owner);
        members.put(key, new HashSet<>());
        saveClaims();
    }

    public void unclaim(Chunk chunk) {
        String key = key(chunk);
        owners.remove(key);
        members.remove(key);
        saveClaims();
    }

    public void addMember(Chunk chunk, UUID uuid) {
        members.get(key(chunk)).add(uuid);
        saveClaims();
    }

    public void removeMember(Chunk chunk, UUID uuid) {
        members.get(key(chunk)).remove(uuid);
        saveClaims();
    }

    /* ===== SAVE / LOAD ===== */

    private void saveClaims() {
        FileConfiguration config = plugin.getConfig();
        config.set("claims", null);

        owners.forEach((key, owner) -> {
            config.set("claims." + key + ".owner", owner.toString());

            List<String> list = members.get(key).stream()
                    .map(UUID::toString)
                    .toList();

            config.set("claims." + key + ".members", list);
        });

        plugin.saveConfig();
    }

    private void loadClaims() {
        FileConfiguration config = plugin.getConfig();
        if (!config.contains("claims")) return;

        for (String key : config.getConfigurationSection("claims").getKeys(false)) {
            UUID owner = UUID.fromString(config.getString("claims." + key + ".owner"));
            owners.put(key, owner);

            List<String> list = config.getStringList("claims." + key + ".members");
            Set<UUID> set = new HashSet<>();

            for (String uuid : list) {
                set.add(UUID.fromString(uuid));
            }

            members.put(key, set);
        }
    }
}