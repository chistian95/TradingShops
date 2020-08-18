package es.elzoo.tradingshops;

import java.net.URL;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import es.elzoo.tradingshops.inventories.InvAdminShop;
import es.elzoo.tradingshops.inventories.InvStock;

public class CommandShop implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if(!(sender instanceof Player)) {
			return false;
		}
		Player player = (Player) sender;
		
		if(args.length == 0) {
			listSubCmd(player, label);
		} else if(args[0].equalsIgnoreCase("create")) {
			createStore(player);
		} else if(args[0].equalsIgnoreCase("adminshop")) {
			adminShop(player);
		} else if(args[0].equalsIgnoreCase("delete")) {
			deleteShop(player);
		} else if(args[0].equalsIgnoreCase("stock")) {
			stockShop(player);
		} else if(args[0].equalsIgnoreCase("reload")) {
			reloadShop(player);
		} else if(args[0].equalsIgnoreCase("list") && args.length == 1) {
			Bukkit.getServer().getScheduler().runTaskAsynchronously(TradingShops.getPlugin(), () -> listShops(player, null));
		} else if(args[0].equalsIgnoreCase("list") && args.length >= 2) {
			Bukkit.getServer().getScheduler().runTaskAsynchronously(TradingShops.getPlugin(), () -> listShops(player, args[1]));
		} else if(args[0].equalsIgnoreCase("view") && args.length >= 2) {
			viewShop(player, args);
		} else { listSubCmd(player, label); }
		
		return true;
	}
	
	private void listSubCmd(Player player, String label) {
		player.sendMessage(ChatColor.GOLD + "Available TradingShops Commands:");
		player.sendMessage(ChatColor.GRAY + "/"+label+" adminshop");
		player.sendMessage(ChatColor.GRAY + "/"+label+" stock");
		player.sendMessage(ChatColor.GRAY + "/"+label+" create");
		player.sendMessage(ChatColor.GRAY + "/"+label+" delete");
		player.sendMessage(ChatColor.GRAY + "/"+label+" list");
		player.sendMessage(ChatColor.GRAY + "/"+label+" view <id>");
		if(player.hasPermission(Permission.SHOP_ADMIN.toString()))
			player.sendMessage(ChatColor.GRAY + "/"+label+" list player");
	}
	
	private void createStore(Player player) {
		if(!player.hasPermission(Permission.SHOP_CREATE.toString())) {
			player.sendMessage(Messages.NO_PERMISSION.toString());
			return;
		}

		if(!EventShop.playersCreating.contains(player.getName()))
			EventShop.playersCreating.add(player.getName());

		EventShop.playersDeleting.remove(player.getName());
		player.sendMessage(Messages.SHOP_CREATE.toString());
	}
	
	private void adminShop(Player player) {
		if(!player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			player.sendMessage(Messages.NO_PERMISSION.toString());
			return;
		}

		if(!EventShop.playersCreatingAdmin.contains(player.getName()))
			EventShop.playersCreatingAdmin.add(player.getName());

		EventShop.playersDeleting.remove(player.getName());
		player.sendMessage(Messages.SHOP_CREATE.toString());
	}
	
	private void deleteShop(Player player) {
		if(!EventShop.playersDeleting.contains(player.getName()))
			EventShop.playersDeleting.add(player.getName());

		EventShop.playersCreating.remove(player.getName());
		player.sendMessage(Messages.SHOP_CLEAR.toString());
	}

	private void listShops(Player player, String argsShopList) {
		if(argsShopList != null && !player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			player.sendMessage(Messages.NO_PERMISSION.toString());
			return;
		}

		UUID sOwner;
		if(argsShopList == null) {
			sOwner = player.getUniqueId();
			argsShopList = player.getDisplayName();
		} else {
			try {
				sOwner = getUUID(argsShopList);
			} catch (Exception e) {
				player.sendMessage(Messages.NO_PLAYER_SHOP.toString());
				Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "TradingShops Exception Error: Player name does not exist! Cannot retrieve shop!");
				return;
			}
		}
		Shop.getShopList(player, sOwner, argsShopList);
	}
	
	private void stockShop(Player player) {
		if(!TradingShops.config.getBoolean("enableStockCommand")) {
			player.sendMessage(Messages.STOCK_COMMAND_DISABLED.toString());
			return;
		}

		if(InvStock.inShopInv.containsValue(player.getUniqueId())) {
			player.sendMessage(Messages.SHOP_BUSY.toString());
			return;
		} else { InvStock.inShopInv.put(player, player.getUniqueId()); }

		InvStock inv = InvStock.getInvStock(player.getUniqueId());
		inv.open(player);
	}
	
	private void reloadShop(Player player) {
		if(!player.hasPermission(Permission.SHOP_ADMIN.toString())) {
			player.sendMessage(Messages.NO_PERMISSION.toString());
			return;
		}
		
		TradingShops plugin = (TradingShops) Bukkit.getPluginManager().getPlugin("TradingShops");
		if(plugin != null)
			plugin.createConfig();
		player.sendMessage(Messages.SHOP_RELOAD.toString());
	}

	private static UUID getUUID(String name) throws Exception {
		Scanner scanner = new Scanner(new URL("https://api.mojang.com/users/profiles/minecraft/" + name).openStream());
		String input = scanner.nextLine();
		scanner.close();

		JSONObject UUIDObject = (JSONObject) JSONValue.parseWithException(input);
		String uuidString = UUIDObject.get("id").toString();
		String uuidSeparation = uuidString.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5");
		return UUID.fromString(uuidSeparation);
	}

	private static void viewShop(Player player, String[] args) {
		if(!TradingShops.config.getBoolean("remoteManage")) {
			player.sendMessage(Messages.NO_PERMISSION.toString());
			return;
		}
		
		int shopId = -1;
		
		try {
			shopId = Integer.parseInt(args[1]);
		} catch(Exception e) {
			shopId = -1;
		}
		
		if(shopId < 0) {
			player.sendMessage(Messages.SHOP_NO_SELF.toString());
			return;
		} 
		
		Optional<Shop> shop = Shop.getShopById(shopId);
		if(!shop.isPresent() || !shop.get().isOwner(player.getUniqueId())) {
			player.sendMessage(Messages.SHOP_NO_SELF.toString());
			return;
		}
		
		if(InvStock.inShopInv.containsValue(shop.get().getOwner())) {
			player.sendMessage(Messages.SHOP_BUSY.toString());
			return;
		}
		
		InvAdminShop inv = new InvAdminShop(shop.get());
		inv.open(player, shop.get().getOwner());
	}
}
