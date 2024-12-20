package org.zerolegion.sp_core.tablist;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.zerolegion.sp_core.SP_CORE;
import java.util.HashMap;
import java.util.UUID;
import java.util.List;
import java.lang.reflect.Constructor;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.stream.Collectors;
import org.bukkit.scheduler.BukkitTask;
import java.util.Set;
import org.zerolegion.sp_core.permissions.Group;
import org.zerolegion.sp_core.clans.SpaceClan;
import org.zerolegion.sp_core.clans.ClanRole;

public class TabListManager {
    private final SP_CORE plugin;
    private final HashMap<UUID, Team> playerTeams;
    private final Scoreboard scoreboard;
    private final FileConfiguration config;
    private BukkitTask updateTask;

    // Reflection
    private Class<?> packetPlayOutPlayerListHeaderFooter;
    private Class<?> chatComponentText;
    private Class<?> iChatBaseComponent;
    private Constructor<?> headerFooterConstructor;
    private String version;

    public TabListManager(SP_CORE plugin) {
        this.plugin = plugin;
        this.playerTeams = new HashMap<>();
        this.scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        this.config = plugin.getConfig();
        setupReflection();
        setupTeams();
        startUpdateTask();
    }

    private void setupReflection() {
        try {
            version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            packetPlayOutPlayerListHeaderFooter = Class.forName("net.minecraft.server." + version + ".PacketPlayOutPlayerListHeaderFooter");
            chatComponentText = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
            iChatBaseComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");
            headerFooterConstructor = packetPlayOutPlayerListHeaderFooter.getConstructor(iChatBaseComponent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupTeams() {
        // Limpa times existentes
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }

        // Time para jogadores com OP
        Team adminTeam = scoreboard.registerNewTeam("000Admin");
        String adminPrefix = config.getString("tablist.teams.admin.prefix", "&4⚔ &c");
        String adminSuffix = config.getString("tablist.teams.admin.suffix", " &7[Admin]");
        adminTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', adminPrefix));
        adminTeam.setSuffix(ChatColor.translateAlternateColorCodes('&', adminSuffix));

        // Time para jogadores normais
        Team playerTeam = scoreboard.registerNewTeam("999Player");
        String playerPrefix = config.getString("tablist.teams.player.prefix", "&7");
        String playerSuffix = config.getString("tablist.teams.player.suffix", "");
        playerTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', playerPrefix));
        playerTeam.setSuffix(ChatColor.translateAlternateColorCodes('&', playerSuffix));
    }

    public void updatePlayerTeam(Player player) {
        // Pega os grupos do jogador
        List<Group> groups = plugin.getPermissionManager().getPlayerGroups(player.getUniqueId());
        String teamName = "999Player"; // Time padrão
        String prefix = "&7"; // Prefix padrão
        String suffix = ""; // Suffix padrão
        
        // Adiciona a tag do clã se o jogador for líder
        SpaceClan clan = plugin.getClanManager().getPlayerClan(player.getUniqueId());
        if (clan != null && clan.getMembers().get(player.getUniqueId()) == ClanRole.LEADER) {
            suffix = ChatColor.translateAlternateColorCodes('&', " &8[&b" + clan.getTag() + "&8]");
        }
        
        if (player.isOp()) {
            teamName = "000Admin";
        } else if (!groups.isEmpty()) {
            // Pega o grupo com maior peso
            Group highestGroup = groups.stream()
                .max((g1, g2) -> Integer.compare(g1.getWeight(), g2.getWeight()))
                .orElse(null);
            
            if (highestGroup != null) {
                // Usa o peso do grupo para determinar o nome do time
                teamName = String.format("%03d%s", 999 - highestGroup.getWeight(), highestGroup.getName());
                String groupPrefix = highestGroup.getPrefix();
                if (groupPrefix != null && !groupPrefix.isEmpty()) {
                    prefix = groupPrefix;
                }
            }
        }
        
        // Remove o jogador de qualquer time anterior
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
        
        // Cria ou obtém o time específico para este jogador
        Team newTeam = scoreboard.getTeam(teamName + player.getName());
        if (newTeam == null) {
            newTeam = scoreboard.registerNewTeam(teamName + player.getName());
        }
        
        // Configura o time
        newTeam.setPrefix(ChatColor.translateAlternateColorCodes('&', prefix));
        newTeam.setSuffix(suffix);
        newTeam.addEntry(player.getName());
        playerTeams.put(player.getUniqueId(), newTeam);
    }

    private void startUpdateTask() {
        // Cancela task anterior se existir
        if (updateTask != null) {
            updateTask.cancel();
        }

        // Inicia nova task
        updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateTabList(player);
            }
        }, 20L, 20L); // Atualiza a cada 1 segundo
    }

    private String formatText(List<String> lines) {
        return lines.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', 
                        line.replace("{online}", String.valueOf(Bukkit.getOnlinePlayers().size()))))
                .collect(Collectors.joining("\n"));
    }

    public void updateTabList(Player player) {
        try {
            // Pega as mensagens da config
            List<String> headerLines = config.getStringList("tablist.header");
            List<String> footerLines = config.getStringList("tablist.footer");

            // Formata as mensagens
            String header = formatText(headerLines);
            String footer = formatText(footerLines);

            // Cria os componentes
            Object headerComponent = chatComponentText.getConstructor(String.class).newInstance(header);
            Object footerComponent = chatComponentText.getConstructor(String.class).newInstance(footer);
            
            // Cria e envia o pacote
            Object packet = packetPlayOutPlayerListHeaderFooter.getConstructor(iChatBaseComponent).newInstance(headerComponent);
            
            // Define o footer usando reflection
            java.lang.reflect.Field footerField = packet.getClass().getDeclaredField("b");
            footerField.setAccessible(true);
            footerField.set(packet, footerComponent);

            // Envia o pacote
            Object entityPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = entityPlayer.getClass().getField("playerConnection").get(entityPlayer);
            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"))
                    .invoke(playerConnection, packet);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // Atualiza o scoreboard do jogador
        player.setScoreboard(scoreboard);
        
        // Atualiza o time do jogador
        updatePlayerTeam(player);
    }

    public void removePlayer(Player player) {
        Team team = playerTeams.remove(player.getUniqueId());
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }

    public void onDisable() {
        // Cancela a task de atualização
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
        }

        // Limpa os times
        for (Team team : scoreboard.getTeams()) {
            team.unregister();
        }
        playerTeams.clear();

        // Remove o scoreboard dos jogadores
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
        }
    }
} 