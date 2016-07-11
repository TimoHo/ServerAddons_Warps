package me.tmods.serveraddons;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.tmods.serverutils.Methods;

public class Warps extends JavaPlugin implements Listener{
	public File file = new File("plugins/TModsServerUtils/data.yml");
	public FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
	public FileConfiguration maincfg = YamlConfiguration.loadConfiguration(new File("plugins/TModsServerUtils","config.yml"));
	@Override
	public void onEnable() {
		try {
		if (cfg.getConfigurationSection("Navi") == null || cfg.getConfigurationSection("Navi").getKeys(false).size() < 1) {
			cfg.set("Navi.temporarywarp", "temporaryvalue");
			try {
				cfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
			cfg.set("Navi.temporarywarp", null);
			try {
				cfg.save(file);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Bukkit.getPluginManager().registerEvents(this, this);
		} catch (Exception e) {
			Methods.log(e);
		}
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		try {
		for (int i = 0;i < event.getPlayer().getInventory().getContents().length;i++) {
			if (event.getPlayer().getInventory().getItem(i) != null) {
				if (event.getPlayer().getInventory().getItem(i).getType() == Material.COMPASS) {
					if (event.getPlayer().getInventory().getItem(i).hasItemMeta()) {
						if (event.getPlayer().getInventory().getItem(i).getItemMeta().getDisplayName().equalsIgnoreCase("Navi")) {
							event.getPlayer().getInventory().setItem(i, new ItemStack(Material.AIR));
						}
					}
				}
			}
		}
		ItemStack navi = new ItemStack(Material.COMPASS);
		ItemMeta meta = navi.getItemMeta();
		meta.setDisplayName("Navi");
		navi.setItemMeta(meta);
		if (event.getPlayer().hasPermission("ServerAddons.warp")) {
			event.getPlayer().getInventory().addItem(navi);
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	@EventHandler
	public void onInteract (PlayerInteractEvent event) {
		try {
		if (Methods.getItemInHand(event.getPlayer()) != null) {
			if (Methods.getItemInHand(event.getPlayer()).hasItemMeta()) {
				if (Methods.getItemInHand(event.getPlayer()).getItemMeta().getDisplayName() != null) {
					if (Methods.getItemInHand(event.getPlayer()).getItemMeta().getDisplayName().equalsIgnoreCase("navi")) {
						if (!event.getPlayer().hasPermission("ServerAddons.warp")) {
							event.getPlayer().sendMessage("You don't have access to that command!");
						} else {
							if (cfg.getConfigurationSection("Navi").getKeys(false).size() > 0) {
								int warps = cfg.getConfigurationSection("Navi").getKeys(false).size();
								int rows = warps / 9;
								if (warps % 9 > 0) {
									rows++;
								} 
								Inventory navicomp = Bukkit.createInventory(null, rows * 9,"Navi");
								for (int i = 0;i < cfg.getConfigurationSection("Navi").getKeys(false).size();i++) {
									ItemStack naviitem = new ItemStack(Material.getMaterial(cfg.getString("Navi." + cfg.getConfigurationSection("Navi").getKeys(false).toArray()[i] + ".material")));
									ItemMeta meta = naviitem.getItemMeta();
									meta.setDisplayName((String) cfg.getConfigurationSection("Navi").getKeys(false).toArray()[i]);
									naviitem.setItemMeta(meta);
									navicomp.setItem(i, naviitem);
								}
								event.getPlayer().openInventory(navicomp);
							} else {
								event.getPlayer().sendMessage("There are currently no Warps");
							}
						}
					}
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	
	@EventHandler
	public void onInventoryKlick(InventoryClickEvent event) {
		try {
		if (event.getClickedInventory() == null) {
			return;
		}
		if (event.getClickedInventory().getTitle() == null) {
			return;
		}
		if (event.getClickedInventory().getTitle().equalsIgnoreCase("Navi")) {
			event.setCancelled(true);
			if (event.getClickedInventory().getItem(event.getRawSlot()) != null) {
				if (event.getClickedInventory().getItem(event.getRawSlot()).hasItemMeta()) {
					if (cfg.getConfigurationSection("Navi").getKeys(false).contains(event.getClickedInventory().getItem(event.getRawSlot()).getItemMeta().getDisplayName())) {
						Location tploc = new Location(Bukkit.getWorld(cfg.getString("Navi." + event.getClickedInventory().getItem(event.getRawSlot()).getItemMeta().getDisplayName() + ".world")),cfg.getDouble("Navi." + event.getClickedInventory().getItem(event.getRawSlot()).getItemMeta().getDisplayName() + ".x"),cfg.getDouble("Navi." + event.getClickedInventory().getItem(event.getRawSlot()).getItemMeta().getDisplayName() + ".y"),cfg.getDouble("Navi." + event.getClickedInventory().getItem(event.getRawSlot()).getItemMeta().getDisplayName() + ".z"));
						tploc.setYaw((float) cfg.getDouble("Navi." + event.getClickedInventory().getItem(event.getRawSlot()).getItemMeta().getDisplayName() + ".yaw"));
						event.getWhoClicked().closeInventory();
						event.getWhoClicked().teleport(tploc);
						((Player) event.getWhoClicked()).playSound(event.getWhoClicked().getLocation(),Sound.ENTITY_ENDERMEN_TELEPORT,1,1);
					}
				}
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		try {
		if (cmd.getName().equalsIgnoreCase("warp")) {
			if (!sender.hasPermission("ServerAddons.warp")) {
				sender.sendMessage("You don't have access to that command!");
				return true;
			}
			if (args.length != 1) {
				return false;
			}
			if (!cfg.getConfigurationSection("Navi").getKeys(false).contains(args[0])) {
				sender.sendMessage("This warp doesn't exist");
				return true;
			}
			if (sender instanceof Player) {
				Location tploc = new Location(Bukkit.getWorld(cfg.getString("Navi." + args[0] + ".world")),cfg.getDouble("Navi." + args[0] + ".x"),cfg.getDouble("Navi." + args[0] + ".y"),cfg.getDouble("Navi." + args[0] + ".z"));
				tploc.setYaw((float) cfg.getDouble("Navi." + args[0] + ".yaw"));
				((Player)sender).closeInventory();
				((Player)sender).teleport(tploc);
				((Player) sender).playSound(((Player) sender).getLocation(), Sound.ENTITY_ENDERMEN_TELEPORT, 1, 1);
			}	
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("warplist")) {
			if (cfg.getConfigurationSection("Navi").getKeys(false).size() > 0) {
				for (String s:cfg.getConfigurationSection("Navi").getKeys(false)) {
					sender.sendMessage(s);
				}
			} else {
				sender.sendMessage("There are currently no warps.");
			}
			return true;
		}
		if (cmd.getName().equalsIgnoreCase("navigator")) {
			if (!sender.hasPermission("ServerAddons.navigator")) {
				sender.sendMessage("You don't hava access to that command!");
				return true;
			}
			if (args.length != 3 && !(args.length == 2 && args[0].equalsIgnoreCase("remove"))) {
				return false;
			}
			if (args[0].equalsIgnoreCase("add")) {
				if (Material.getMaterial(args[2]) == null) {
					sender.sendMessage("No such material");
					return true;
				}
				if (cfg.getConfigurationSection("Navi").getKeys(false).size() > 53) {
					sender.sendMessage("The highest amount of warps is reached");
					return true;
				}
				cfg.set("Navi." + args[1] + ".x", ((Player)sender).getLocation().getX());
				cfg.set("Navi." + args[1] + ".y", ((Player)sender).getLocation().getY());
				cfg.set("Navi." + args[1] + ".z", ((Player)sender).getLocation().getZ());
				cfg.set("Navi." + args[1] + ".yaw", ((Player)sender).getLocation().getYaw());
				cfg.set("Navi." + args[1] + ".world", ((Player)sender).getLocation().getWorld().getName());
				cfg.set("Navi." + args[1] + ".material", args[2]);
				sender.sendMessage("Warp set.");
				try {
					cfg.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("remove")) {
				cfg.set("Navi." + args[1], null);
				sender.sendMessage("Warp removed.");
				try {
					cfg.save(file);
				} catch (IOException e) {
					e.printStackTrace();
				}
				return true;
			}
		}
		} catch (Exception e) {
			Methods.log(e);
		}
		return false;
	}
	
}
