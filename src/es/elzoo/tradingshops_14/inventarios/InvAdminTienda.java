package es.elzoo.tradingshops_14.inventarios;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;

import es.elzoo.tradingshops_14.FilaTienda;
import es.elzoo.tradingshops_14.Mensajes;
import es.elzoo.tradingshops_14.Tienda;
import es.elzoo.tradingshops_14.gui.GUI;

public class InvAdminTienda extends GUI {
	private Tienda tienda;
	
	public InvAdminTienda(Tienda tienda) {
		super(54, getShopName(tienda));
		
		this.tienda = tienda;
		updateItems();
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
	
	private void updateItems() {
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
					final int index = y-1;
					
					if(fila.isPresent()) {						
						ponerItem(y*9+x, GUI.crearItem(Material.TNT, ChatColor.BOLD+Mensajes.TIENDA_TITLE_DELETE.toString()), p -> {
							tienda.borrar(p, index);
							InvAdminTienda inv = new InvAdminTienda(tienda);
							inv.abrir(p);
						});
					} else {
						ponerItem(y*9+x, GUI.crearItem(Material.LIME_DYE, ChatColor.BOLD+Mensajes.TIENDA_TITLE_CREATE.toString()), p -> {
							InvCrearFila inv = new InvCrearFila(tienda, index);
							inv.abrir(p);
						});
					}
				} else if(x == 8 && y >= 1 && y <= 4 && tienda.isAdmin()) {
					final Optional<FilaTienda> fila = tienda.getFila(y-1);
					if(fila.isPresent()) {
						if(fila.get().broadcast) {
							ponerItem(y*9+x, GUI.crearItem(Material.REDSTONE_TORCH, Mensajes.TIENDA_TITLE_BROADCAST_ON.toString(), (short) 15), p -> {
								fila.get().toggleBroadcast();
								updateItems();
							});
						} else {
							ponerItem(y*9+x, GUI.crearItem(Material.LEVER, Mensajes.TIENDA_TITLE_BROADCAST_OFF.toString(), (short) 15), p -> {
								fila.get().toggleBroadcast();
								updateItems();
							});
						}
					} else {
						ponerItem(y*9+x, GUI.crearItem(Material.BLACK_STAINED_GLASS_PANE, ""));
					}
				} else {
					ponerItem(y*9+x, GUI.crearItem(Material.BLACK_STAINED_GLASS_PANE, ""));
				}
			}
		}
	}
}
