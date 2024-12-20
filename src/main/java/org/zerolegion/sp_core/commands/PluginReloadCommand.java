package org.zerolegion.sp_core.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.zerolegion.sp_core.SP_CORE;
import org.bukkit.command.TabCompleter;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.File;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.UnknownDependencyException;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.io.InputStreamReader;
import org.yaml.snakeyaml.Yaml;
import java.util.Map;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;

public class PluginReloadCommand implements CommandExecutor, TabCompleter {
    private final SP_CORE plugin;

    public PluginReloadCommand(SP_CORE plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("sensitive.admin")) {
            sender.sendMessage(ChatColor.RED + "⚠ Você não possui autorização para executar este comando!");
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        String action = args[0].toLowerCase();

        // Se o comando for "perms", delega para o PermissionCommand
        if (action.equals("perms")) {
            if (sender.hasPermission("sensitive.permissions")) {
                // Remove o primeiro argumento (perms) e passa o resto para o PermissionCommand
                String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
                PermissionCommand permCommand = new PermissionCommand(plugin);
                return permCommand.onCommand(sender, command, label, newArgs);
            } else {
                sender.sendMessage(ChatColor.RED + "⚠ Você não possui autorização para gerenciar permissões!");
                return true;
            }
        }

        switch (action) {
            case "reload":
                if (args.length < 2) {
                    handleCoreReload(sender);
                } else {
                    handleReload(sender, args[1], plugin.getServer().getPluginManager());
                }
                break;
            case "enable":
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }
                handleEnable(sender, args[1], plugin.getServer().getPluginManager());
                break;
            case "disable":
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }
                handleDisable(sender, args[1], plugin.getServer().getPluginManager());
                break;
            case "load":
                if (args.length < 2) {
                    sendHelp(sender);
                    return true;
                }
                handleLoad(sender, args[1], plugin.getServer().getPluginManager());
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("sensitive.admin")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>(Arrays.asList("reload", "enable", "disable", "load"));
            if (sender.hasPermission("sensitive.permissions")) {
                completions.add("perms");
            }
            return completions;
        }

        // Se o primeiro argumento for "perms", delega para o PermissionCommand
        if (args[0].equalsIgnoreCase("perms") && sender.hasPermission("sensitive.permissions")) {
            String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
            PermissionCommand permCommand = new PermissionCommand(plugin);
            return permCommand.onTabComplete(sender, command, alias, newArgs);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "reload":
                case "enable":
                case "disable":
                    List<String> plugins = new ArrayList<>();
                    for (Plugin p : plugin.getServer().getPluginManager().getPlugins()) {
                        plugins.add(p.getName());
                    }
                    return plugins;
                case "load":
                    List<String> files = new ArrayList<>();
                    File pluginsDir = new File("plugins");
                    if (pluginsDir.exists() && pluginsDir.isDirectory()) {
                        for (File file : pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"))) {
                            files.add(file.getName().replace(".jar", ""));
                        }
                    }
                    return files;
            }
        }

        return new ArrayList<>();
    }

    private void handleCoreReload(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Recarregando SP_CORE...");
        
        try {
            // Desativa todos os sistemas
            plugin.onDisable();
            
            // Recarrega a configuração
            plugin.reloadConfig();
            
            // Reinicia todos os sistemas
            plugin.reloadSystems();
            
            sender.sendMessage(ChatColor.GREEN + "SP_CORE recarregado com sucesso!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Erro ao recarregar o SP_CORE: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "Comandos disponíveis:");
        sender.sendMessage(ChatColor.YELLOW + "/sensitive reload " + ChatColor.GRAY + "- Recarrega o SP_CORE");
        sender.sendMessage(ChatColor.YELLOW + "/sensitive reload <plugin> " + ChatColor.GRAY + "- Recarrega um plugin específico");
        sender.sendMessage(ChatColor.YELLOW + "/sensitive enable <plugin> " + ChatColor.GRAY + "- Ativa um plugin");
        sender.sendMessage(ChatColor.YELLOW + "/sensitive disable <plugin> " + ChatColor.GRAY + "- Desativa um plugin");
        sender.sendMessage(ChatColor.YELLOW + "/sensitive load <plugin> " + ChatColor.GRAY + "- Carrega um novo plugin do arquivo .jar");
    }

    private Plugin findPlugin(String name, PluginManager pluginManager) {
        // Primeiro tenta encontrar pelo nome exato
        Plugin plugin = pluginManager.getPlugin(name);
        if (plugin != null) return plugin;

        // Se não encontrar, procura ignorando case
        for (Plugin p : pluginManager.getPlugins()) {
            if (p.getName().equalsIgnoreCase(name)) {
                return p;
            }
        }

        return null;
    }

    private void handleReload(CommandSender sender, String pluginName, PluginManager pluginManager) {
        Plugin targetPlugin = findPlugin(pluginName, pluginManager);

        if (targetPlugin == null) {
            sender.sendMessage(ChatColor.RED + "Plugin '" + pluginName + "' não encontrado!");
            return;
        }

        if (targetPlugin.getName().equals(plugin.getName())) {
            sender.sendMessage(ChatColor.RED + "Você não pode recarregar o plugin principal!");
            return;
        }

        try {
            reloadPlugin(targetPlugin);
            sender.sendMessage(ChatColor.GREEN + "Plugin '" + targetPlugin.getName() + "' recarregado com sucesso!");
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Erro ao recarregar o plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleEnable(CommandSender sender, String pluginName, PluginManager pluginManager) {
        Plugin targetPlugin = findPlugin(pluginName, pluginManager);

        if (targetPlugin == null) {
            sender.sendMessage(ChatColor.RED + "Plugin '" + pluginName + "' não encontrado!");
            return;
        }

        if (targetPlugin.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Plugin '" + targetPlugin.getName() + "' já está ativado!");
            return;
        }

        pluginManager.enablePlugin(targetPlugin);
        sender.sendMessage(ChatColor.GREEN + "Plugin '" + targetPlugin.getName() + "' ativado com sucesso!");
    }

    private void handleDisable(CommandSender sender, String pluginName, PluginManager pluginManager) {
        Plugin targetPlugin = findPlugin(pluginName, pluginManager);

        if (targetPlugin == null) {
            sender.sendMessage(ChatColor.RED + "Plugin '" + pluginName + "' não encontrado!");
            return;
        }

        if (targetPlugin.getName().equals(plugin.getName())) {
            sender.sendMessage(ChatColor.RED + "Você não pode desativar o plugin principal!");
            return;
        }

        if (!targetPlugin.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Plugin '" + targetPlugin.getName() + "' já está desativado!");
            return;
        }

        pluginManager.disablePlugin(targetPlugin);
        sender.sendMessage(ChatColor.GREEN + "Plugin '" + targetPlugin.getName() + "' desativado com sucesso!");
    }

    private String getPluginNameFromJar(File jarFile) {
        try (JarFile jar = new JarFile(jarFile)) {
            JarEntry pluginYml = jar.getJarEntry("plugin.yml");
            if (pluginYml != null) {
                LoaderOptions options = new LoaderOptions();
                Yaml yaml = new Yaml(new Constructor(String.valueOf(options)));
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) yaml.load(new InputStreamReader(jar.getInputStream(pluginYml)));
                return data != null ? (String) data.get("name") : null;
            }
        } catch (Exception e) {
            // Ignora erros e retorna null
        }
        return null;
    }

    private void handleLoad(CommandSender sender, String pluginName, PluginManager pluginManager) {
        File pluginsDir = new File("plugins");
        File pluginFile = new File(pluginsDir, pluginName + (pluginName.endsWith(".jar") ? "" : ".jar"));

        // Se não encontrar o arquivo diretamente, procura por qualquer arquivo .jar
        if (!pluginFile.exists()) {
            File[] files = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
            if (files != null) {
                for (File file : files) {
                    String realPluginName = getPluginNameFromJar(file);
                    if (realPluginName != null && realPluginName.equalsIgnoreCase(pluginName)) {
                        pluginFile = file;
                        break;
                    }
                }
            }
        }

        if (!pluginFile.exists()) {
            sender.sendMessage(ChatColor.RED + "Arquivo do plugin não encontrado!");
            return;
        }

        try {
            Plugin targetPlugin = pluginManager.loadPlugin(pluginFile);
            if (targetPlugin != null) {
                pluginManager.enablePlugin(targetPlugin);
                sender.sendMessage(ChatColor.GREEN + "Plugin '" + targetPlugin.getName() + "' carregado e ativado com sucesso!");
            } else {
                sender.sendMessage(ChatColor.RED + "Erro ao carregar o plugin: arquivo inválido");
            }
        } catch (InvalidPluginException e) {
            sender.sendMessage(ChatColor.RED + "Arquivo de plugin inválido: " + e.getMessage());
        } catch (UnknownDependencyException e) {
            sender.sendMessage(ChatColor.RED + "Dependência não encontrada: " + e.getMessage());
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + "Erro ao carregar o plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void reloadPlugin(Plugin targetPlugin) {
        PluginManager pluginManager = plugin.getServer().getPluginManager();
        pluginManager.disablePlugin(targetPlugin);
        pluginManager.enablePlugin(targetPlugin);
    }
} 