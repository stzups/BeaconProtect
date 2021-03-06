package net.stzups.beaconprotect.Cmds;

import com.sun.istack.internal.NotNull;
import net.stzups.beaconprotect.BeaconProtect;
import net.stzups.beaconprotect.Group;
import org.bukkit.ChatColor;
import org.bukkit.command.*;

import java.util.*;
import java.util.List;

public class CmdGroups extends Cmd implements CommandExecutor, TabCompleter {
    public CmdGroups(BeaconProtect plugin) {
        super(plugin);
        PluginCommand pluginCommand = plugin.getCommand("groups");
        if (pluginCommand != null) {
            pluginCommand.setExecutor(this);
            pluginCommand.setTabCompleter(this);
        }
        List<String> list = new ArrayList<>();
        list.add("/groups - list all groups");
        list.add("/groups top <method> - list all groups sorted by a certain method");
        list.add("Valid sort methods are by members");
        usages.put("groups", list);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("beaconprotect.groups")) {
            if (args.length == 0) {
                sender.sendMessage("Groups:");
                for (Group group : plugin.groups.values()) {
                    sender.sendMessage(group.getName() + ": " + group.getMembersSize());
                }
                return true;
            } else if (args.length == 1) {
                if (args[0].equalsIgnoreCase("top")) {
                    sender.sendMessage("Groups by alphabetical order");
                    List<Group> groups = new ArrayList<>(plugin.groups.values());
                    groups.sort(Comparator.comparing(Group::getName));
                    Collections.reverse(groups);
                    List<String> msg = new ArrayList<>();
                    for (Group group : groups) {
                        msg.add(group.getName());
                    }
                    sender.sendMessage(msg.toArray(new String[0]));
                    return true;
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("top")) {
                    if (args[1].equalsIgnoreCase("members")) {
                        sender.sendMessage("Groups by members:");
                        List<Group> groups = new ArrayList<>(plugin.groups.values());
                        groups.sort(Comparator.comparing(Group::getMembersSize));
                        Collections.reverse(groups);
                        List<String> msg = new ArrayList<>();
                        for (Group group : groups) {
                            msg.add(group.getName() + ": " + group.getMembersSize());
                        }
                        sender.sendMessage(msg.toArray(new String[0]));
                    } else if (args[1].equalsIgnoreCase("date")) {
                        sender.sendMessage("Groups by creation date:");
                        List<Group> groups = new ArrayList<>(plugin.groups.values());
                        groups.sort(Comparator.comparing(Group::getCreationDate));
                        Collections.reverse(groups);
                        List<String> msg = new ArrayList<>();
                        for (Group group : groups) {
                            msg.add(group.getName() + ": " + new Date(group.getCreationDate()));
                        }
                        sender.sendMessage(msg.toArray(new String[0]));
                    } else if (args[1].equalsIgnoreCase("beacons")) {
                        sender.sendMessage("Groups by amount of beacons");
                        List<Group> groups = new ArrayList<>(plugin.groups.values());
                        groups.sort(Comparator.comparing(Group::getBeaconsAmount));
                        Collections.reverse(groups);
                        List<String> msg = new ArrayList<>();
                        for (Group group : groups) {
                            msg.add(group.getName() + ": " + group.getBeaconsAmount());
                        }
                        sender.sendMessage(msg.toArray(new String[0]));
                    } else {
                        sender.sendMessage("Can't sort groups by " + args[1] + ". Valid methods are by members, date, or by amount of beacons");
                    }
                    return true;
                }
            }
            usage(sender, "groups");
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use that command");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (sender.hasPermission("beaconprotect.groups")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                if (checkCompletions("top", args[0])) {
                    completions.add("top");
                }
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("top")) {
                    if (checkCompletions("members", args[1])) {
                        completions.add("members");
                    }
                    if (checkCompletions("date", args[1])) {
                        completions.add("date");
                    }
                    if (checkCompletions("beacons", args[1])) {
                        completions.add("beacons");
                    }
                }
            }
            return completions;
        }
        return null;
    }
}
