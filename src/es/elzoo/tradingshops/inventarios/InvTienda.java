package es.elzoo.tradingshops.inventarios;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import es.elzoo.tradingshops.FilaTienda;
import es.elzoo.tradingshops.Mensajes;
import es.elzoo.tradingshops.Tienda;
import es.elzoo.tradingshops.gui.GUI;

public class InvTienda extends GUI {
	public InvTienda(Tienda tienda) {
		super(54, getShopName(tienda));
		
		for(int x=0; x<9; x++) {
			for(int y=0; y<6; y++) {
				if(x == 1) {
					if(y == 0 || y == 5) {
						ponerItem(y*9+x, GUI.crearItem(Material.GREEN_STAINED_GLASS_PANE, ChatColor.GREEN+Mensajes.TIENDA_TITLE_SELL.toString()));
					} else {
						Optional<FilaTienda> fila = tienda.getFila(y-1);
						if(fila.isPresent()) {
							ponerItem(y*9+x, fila.get().getItemOut());
						}
					}
				} else if(x == 4) {
					if(y == 0 || y == 5) {
						ponerItem(y*9+x, GUI.crearItem(Material.RED_STAINED_GLASS_PANE, ChatColor.RED+Mensajes.TIENDA_TITLE_BUY.toString()));
					} else {
						Optional<FilaTienda> fila = tienda.getFila(y-1);
						if(fila.isPresent()) {
							ponerItem(y*9+x, fila.get().getItemIn());
						}
					}
				} else if(x == 7 && y >= 1 && y <= 4) {
					Optional<FilaTienda> fila = tienda.getFila(y-1);
					if(fila.isPresent()) {
						final int index = y-1;
						ponerItem(y*9+x, GUI.crearItem(Material.LIME_DYE, ChatColor.BOLD+Mensajes.TIENDA_TITLE_BUYACTION.toString()), p -> {
							p.closeInventory();
							tienda.comprar(p, index);
						});
					} else {
						ponerItem(y*9+x, GUI.crearItem(Material.GRAY_DYE, ""));
					}
				} else {
					ponerItem(y*9+x, GUI.crearItem(Material.BLACK_STAINED_GLASS_PANE, ""));
				}
			}
		}
	}
	
	private static String getShopName(Tienda tienda) {
		if(tienda.isAdmin()) {
			return Mensajes.TIENDA_TITLE_ADMIN_SHOP.toString();
		}
		
		String msg = Mensajes.TIENDA_TITLE_NORMAL_SHOP.toString();
		
		OfflinePlayer pl = Bukkit.getOfflinePlayer(tienda.getOwner());
		if(pl == null) {
			return msg.replaceAll("%player%", "<unknown>");
		}
		
		return msg.replaceAll("%player%", pl.getName());
	}
}
