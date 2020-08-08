package es.elzoo.tradingshops;

import java.util.Optional;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Utils {
	
	public static boolean hasStock(Player player, ItemStack item) {
		return player.getInventory().containsAtLeast(item, item.getAmount());
	}
	
	public static boolean hasStock(Shop shop, ItemStack item) {
		if(shop.isAdmin())
			return true;
		
		int max = TradingShops.config.getInt("stockPages");
		for(int i=0; i<max; i++) {
			Optional<StockShop> stockStore = StockShop.getStockShopByOwner(shop.getOwner(), i);
			if(!stockStore.isPresent()) {
				continue;
			}
			if(stockStore.get().getInventory().containsAtLeast(item, item.getAmount())) {
				return true;
			}
		}
		return false;
	}
}
