package es.elzoo.tradingshops_14;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class Utils {
	public static Plugin plugin = Bukkit.getPluginManager().getPlugin("TradingShops");
	
	public static boolean hasStock(Player player, ItemStack item) {
		return player.getInventory().containsAtLeast(item, item.getAmount());
	}
	
	public static boolean hasStock(Tienda tienda, ItemStack item) {
		if(tienda.isAdmin()) {
			return true;
		}
		
		int max = TradingShops.config.getInt("stockPages");
		for(int i=0; i<max; i++) {
			Optional<StockTienda> stockTienda = StockTienda.getStockTiendaByOwner(tienda.getOwner(), i);
			if(!stockTienda.isPresent()) {
				continue;
			}
			if(stockTienda.get().getInventario().containsAtLeast(item, item.getAmount())) {
				return true;
			}
		}
		
		return false;
	}
}
