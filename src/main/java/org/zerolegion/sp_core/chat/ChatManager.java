package org.zerolegion.sp_core.chat;

import org.bukkit.entity.Player;
import org.zerolegion.sp_core.SP_CORE;
import org.bukkit.ChatColor;
import com.mongodb.client.MongoCollection;
import org.bson.Document;
import java.util.HashMap;
import java.util.UUID;
import java.util.HashSet;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import org.zerolegion.sp_core.permissions.Group;
import org.zerolegion.sp_core.clans.SpaceClan;
import org.zerolegion.sp_core.clans.ClanRole;

public class ChatManager {
    private final SP_CORE plugin;
    private final MongoCollection<Document> playersCollection;
    private final HashMap<UUID, String> playerPrefixes;
    private final HashSet<UUID> playersInGlobalChat;
    private final double LOCAL_CHAT_RADIUS = 50.0; // Raio de 50 blocos para chat local

    public ChatManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.playersCollection = plugin.getDatabase().getCollection("players");
        this.playerPrefixes = new HashMap<>();
        this.playersInGlobalChat = new HashSet<>();
    }

    public String formatMessage(Player player, String message, boolean isGlobal) {
        String prefix = getPlayerPrefix(player);
        String chatPrefix = isGlobal ? "&8[&7G&8]" : "&8[&eL&8]";
        String clanTag = getClanTag(player);
        String format = chatPrefix + " %prefix% " + clanTag + " &7%player% &8» " + (isGlobal ? "&7%message%" : "&f%message%");
        
        return ChatColor.translateAlternateColorCodes('&', format
                .replace("%prefix%", prefix)
                .replace("%player%", player.getName())
                .replace("%message%", message));
    }

    private String getClanTag(Player player) {
        SpaceClan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan != null) {
            ClanRole role = clan.getMembers().get(player.getUniqueId());
            if (role == ClanRole.LEADER) {
                return "&8[&b" + clan.getTag() + "&8]";
            }
        }
        return "";
    }

    public void sendLocalMessage(Player sender, String message) {
        String formattedMessage = formatMessage(sender, message, false);
        Location location = sender.getLocation();
        List<Player> nearbyPlayers = new ArrayList<>();

        for (Entity entity : sender.getNearbyEntities(LOCAL_CHAT_RADIUS, LOCAL_CHAT_RADIUS, LOCAL_CHAT_RADIUS)) {
            if (entity instanceof Player) {
                Player nearbyPlayer = (Player) entity;
                nearbyPlayers.add(nearbyPlayer);
                nearbyPlayer.sendMessage(formattedMessage);
            }
        }

        sender.sendMessage(formattedMessage); // Enviar para o próprio jogador

        // Se não houver jogadores próximos (exceto o próprio sender)
        if (nearbyPlayers.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Não há ninguém por perto para ver sua mensagem.");
        }
    }

    public void sendGlobalMessage(Player sender, String message) {
        String formattedMessage = formatMessage(sender, message, true);
        plugin.getServer().broadcastMessage(formattedMessage);
    }

    public void toggleGlobalChat(Player player) {
        UUID uuid = player.getUniqueId();
        if (playersInGlobalChat.contains(uuid)) {
            playersInGlobalChat.remove(uuid);
            player.sendMessage(ChatColor.GREEN + "Você saiu do chat global!");
        } else {
            playersInGlobalChat.add(uuid);
            player.sendMessage(ChatColor.GREEN + "Você entrou no chat global!");
        }
    }

    public boolean isInGlobalChat(Player player) {
        return playersInGlobalChat.contains(player.getUniqueId());
    }

    public String getPlayerPrefix(Player player) {
        UUID uuid = player.getUniqueId();
        if (!playerPrefixes.containsKey(uuid)) {
            // Pega os grupos do jogador e encontra o de maior peso
            List<Group> groups = plugin.getPermissionManager().getPlayerGroups(uuid);
            String highestPrefix = "&7"; // Prefix padrão se não tiver grupo
            
            if (!groups.isEmpty()) {
                // Encontra o grupo com maior peso
                Group highestGroup = groups.stream()
                    .max((g1, g2) -> Integer.compare(g1.getWeight(), g2.getWeight()))
                    .orElse(null);
                
                if (highestGroup != null) {
                    String prefix = highestGroup.getPrefix();
                    if (prefix != null && !prefix.isEmpty()) {
                        highestPrefix = prefix;
                    }
                }
            }
            
            playerPrefixes.put(uuid, highestPrefix);
        }
        return playerPrefixes.get(uuid);
    }

    public void updatePlayerPrefix(Player player) {
        UUID uuid = player.getUniqueId();
        playerPrefixes.remove(uuid); // Remove o cache para forçar recalcular
        getPlayerPrefix(player); // Recalcula o prefix
    }

    public void clearPlayerCache(UUID uuid) {
        playerPrefixes.remove(uuid);
        playersInGlobalChat.remove(uuid);
    }
} 