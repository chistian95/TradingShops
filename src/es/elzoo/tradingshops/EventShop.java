package es.elzoo.tradingshops;

import es.elzoo.tradingshops.inventories.InvAdminShop;
import es.elzoo.tradingshops.inventories.InvShop;
import es.elzoo.tradingshops.inventories.InvStock;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class EventShop implements Listener {
	public static final List<String> playersCreating = new ArrayList<>();
	public static final List<String> playersCreatingAdmin = new ArrayList<>();
	public static final List<String> playersDeleting = new ArrayList<>();
	private static boolean isShopLoc = false;
	
	@EventHandler
	public void onPlayerInteractStock(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		String stockBlock = TradingShops.config.getString("stockBlock");

		Material match = Material.matchMaterial(Objects.requireNonNull(stockBlock));
		if(match == null) {
			try {
				match = Material.matchMaterial(stockBlock.split("minecraft:")[1].toUpperCase());
			} catch(Exception e) {
				match = null;
			}

			if(match == null) {
				match = Material.JUKEBOX;
			}
		}

		if(block == null || !block.getType().equals(match))
			return;

		// WorldGuard Check
		if(TradingShops.wgLoader != null)
			isShopLoc = TradingShops.wgLoader.checkRegion(block);
		else
			isShopLoc = true;

		if(!isShopLoc)
			return;

		if(event.getPlayer().isSneaking())
			return;

		event.setCancelled(true);

		if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
			if(InvStock.inShopInv.containsValue(event.getPlayer().getUniqueId())) {
				event.getPlayer().sendMessage(Messages.SHOP_BUSY.toString());
				return;
			} else { InvStock.inShopInv.put(event.getPlayer(), event.getPlayer().getUniqueId()); }

			InvStock inv = InvStock.getInvStock(event.getPlayer().getUniqueId());
			inv.setPag(0);
			inv.open(event.getPlayer());
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		Block block = event.getClickedBlock();
		String shopBlock = TradingShops.config.getString("shopBlock");

		Material match = Material.matchMaterial(Objects.requireNonNull(shopBlock));
		if(match == null) {
			try {
				match = Material.matchMaterial(shopBlock.split("minecraft:")[1].toUpperCase());
			} catch(Exception e) {
				match = null;
			}

			if(match == null) {
				match = Material.BARREL;
			}
		}

		if(block == null || !block.getType().equals(match)) {
			playersCreating.remove(event.getPlayer().getName());
			playersCreatingAdmin.remove(event.getPlayer().getName());
			playersDeleting.remove(event.getPlayer().getName());
			return;
		}

		if(TradingShops.wgLoader != null)
			isShopLoc = TradingShops.wgLoader.checkRegion(block);
		else
			isShopLoc = true;

		Optional<Shop> shop = Shop.getShopByLocation(block.getLocation());
		if(!shop.isPresent()) {
			if(!isShopLoc)
				return;

			if(playersCreating.contains(event.getPlayer().getName())) {
				event.setCancelled(true);

				playersCreating.remove(event.getPlayer().getName());

				int maxShops = 0;
				String permPrefix = Permission.SHOP_LIMIT_PREFIX.toString();
				for(PermissionAttachmentInfo attInfo : event.getPlayer().getEffectivePermissions()) {
					String perm = attInfo.getPermission();
					if(perm.startsWith(permPrefix)) {
						int num = 0;
						try {
							num = Integer.parseInt(perm.substring(perm.lastIndexOf(".")+1));
						} catch(Exception e) {
							num = 0;
						}

						if(num > maxShops) {
							maxShops = num;
						}
					}
				}

				boolean limitShops = true;
				int numShops = Shop.getNumShops(event.getPlayer().getUniqueId());

				if(TradingShops.config.getBoolean("usePermissions")) {
					limitShops = numShops >= maxShops;
				} else {
					int numConfig = TradingShops.config.getInt("defaultShopLimit");
					limitShops = numShops >= numConfig && numConfig >= 0;
				}

				if(event.getPlayer().hasPermission(Permission.SHOP_LIMIT_BYPASS.toString()))
					limitShops = false;

				if(limitShops) {
					event.getPlayer().sendMessage(Messages.SHOP_MAX.toString());
					return;
				}

				double cost = TradingShops.config.getDouble("createCost");
				Optional<Economy> economy = TradingShops.getEconomy();
				if(cost > 0 && economy.isPresent()) {
					OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId());
					EconomyResponse res = economy.get().withdrawPlayer(offPlayer, cost);

					if(!res.transactionSuccess()) {
						event.getPlayer().sendMessage(Messages.SHOP_CREATE_NO_MONEY.toString()+cost);
						return;
					}
				}

				Shop newShop = Shop.createShop(block.getLocation(), event.getPlayer().getUniqueId());
				event.getPlayer().sendMessage(Messages.SHOP_CREATED.toString());
				InvAdminShop inv = new InvAdminShop(newShop);
				inv.open(event.getPlayer(), newShop.getOwner());
			} else if(playersCreatingAdmin.contains(event.getPlayer().getName())) {
				event.setCancelled(true);

				playersCreatingAdmin.remove(event.getPlayer().getName());

				if(!event.getPlayer().hasPermission(Permission.SHOP_ADMIN.toString())) {
					event.getPlayer().sendMessage(Messages.NO_PERMISSION.toString());
					return;
				}

				Shop newShop = Shop.createShop(block.getLocation(), event.getPlayer().getUniqueId(), true);
				event.getPlayer().sendMessage(Messages.SHOP_CREATED.toString());
				InvAdminShop inv = new InvAdminShop(newShop);
				inv.open(event.getPlayer(), newShop.getOwner());
			}

			playersCreating.remove(event.getPlayer().getName());
			playersCreatingAdmin.remove(event.getPlayer().getName());
			playersDeleting.remove(event.getPlayer().getName());
			return;
		}

		event.setCancelled(true);

		if(playersDeleting.contains(event.getPlayer().getName())) {
			playersCreating.remove(event.getPlayer().getName());
			playersCreatingAdmin.remove(event.getPlayer().getName());
			playersDeleting.remove(event.getPlayer().getName());

			if(shop.get().isAdmin()) {
				if(!event.getPlayer().hasPermission(Permission.SHOP_ADMIN.toString())) {
					event.getPlayer().sendMessage(Messages.NO_PERMISSION.toString());
					return;
				}
			} else if(!shop.get().isOwner(event.getPlayer().getUniqueId()) && !event.getPlayer().hasPermission(Permission.SHOP_ADMIN.toString())) {
				event.getPlayer().sendMessage(Messages.SHOP_NO_SELF.toString());
				return;
			}

			double cost = TradingShops.config.getDouble("returnAmount");
			Optional<Economy> economy = TradingShops.getEconomy();
			if(cost > 0 && economy.isPresent()) {
				OfflinePlayer offPlayer = Bukkit.getOfflinePlayer(event.getPlayer().getUniqueId());
				economy.get().depositPlayer(offPlayer, cost);
			}

			shop.get().deleteShop();
			event.getPlayer().sendMessage(Messages.SHOP_DELETED.toString());
			return;
		}

		if(InvStock.inShopInv.containsValue(shop.get().getOwner()) && (event.getAction().equals(Action.LEFT_CLICK_BLOCK) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK))) {
			event.getPlayer().sendMessage(Messages.SHOP_BUSY.toString());
			return;
		}

		if((shop.get().isAdmin() && event.getPlayer().hasPermission(Permission.SHOP_ADMIN.toString())) || shop.get().isOwner(event.getPlayer().getUniqueId())) {
			InvAdminShop inv = new InvAdminShop(shop.get());
			inv.open(event.getPlayer(), shop.get().getOwner());
		} else {
			InvShop inv = new InvShop(shop.get());
			inv.open(event.getPlayer(), shop.get().getOwner());
		}

		playersCreating.remove(event.getPlayer().getName());
		playersCreatingAdmin.remove(event.getPlayer().getName());
		playersDeleting.remove(event.getPlayer().getName());
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		playersCreating.remove(event.getPlayer().getName());
		playersCreatingAdmin.remove(event.getPlayer().getName());
		playersDeleting.remove(event.getPlayer().getName());
	}
}
