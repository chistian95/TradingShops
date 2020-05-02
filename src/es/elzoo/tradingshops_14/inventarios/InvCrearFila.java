package es.elzoo.tradingshops_14.inventarios;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import es.elzoo.tradingshops_14.FilaTienda;
import es.elzoo.tradingshops_14.Mensajes;
import es.elzoo.tradingshops_14.Tienda;
import es.elzoo.tradingshops_14.gui.GUI;

public class InvCrearFila extends GUI {
	private ItemStack itemIn;
	private ItemStack itemOut;
	
	public InvCrearFila(Tienda tienda, int index) {
		super(9*3, Mensajes.TIENDA_TITLE_CREATESHOP.toString());
		
		for(int i=0; i<9*3; i++) {
			if(i == 2) {
				ponerItem(i, GUI.crearItem(Material.OAK_SIGN, ChatColor.GREEN + Mensajes.TIENDA_TITLE_SELL.toString()));
			} else if(i == 6) {
				ponerItem(i, GUI.crearItem(Material.OAK_SIGN, ChatColor.RED + Mensajes.TIENDA_TITLE_BUY.toString()));
			} else if(i == 11) { //Item in
				//ponerItem(i, GUI.crearItem(Material.AIR, ""));
			} else if(i == 13) {
				ponerItem(i, GUI.crearItem(Material.LIME_DYE, ChatColor.BOLD + Mensajes.TIENDA_TITLE_CREATE.toString()), p -> {
					if(itemIn == null || itemIn.getType().equals(Material.AIR)) {
						return;
					}
					if(itemOut == null || itemOut.getType().equals(Material.AIR)) {
						return;
					}
					
					tienda.getFilas()[index] = new FilaTienda(itemOut, itemIn, false);
					InvAdminTienda inv = new InvAdminTienda(tienda);
					inv.abrir(p);
				});
			} else if(i == 15) { //Item out
				//ponerItem(i, GUI.crearItem(Material.AIR, ""));
			} else {
				ponerItem(i, GUI.crearItem(Material.BLACK_STAINED_GLASS_PANE, ""));
			}
		}
	}
	
	public void onDrag(InventoryDragEvent event) {		
		super.onDrag(event);
		
		Inventory inv = event.getInventory();
		if(inv.getType().equals(InventoryType.CHEST) && event.getView().getTitle().contains(Mensajes.TIENDA_TITLE_CREATESHOP.toString())) {
			event.setCancelled(true);
		}
	}
	
	@Override
	public void onClick(InventoryClickEvent event) {
		super.onClick(event);
		
		if(!event.getAction().equals(InventoryAction.PLACE_ALL) && !event.getAction().equals(InventoryAction.PICKUP_ALL)) {
			return;
		}
		
		event.setCancelled(false);
		
		Inventory inv = event.getClickedInventory();
		if(inv.getType().equals(InventoryType.CHEST) && event.getView().getTitle().contains(Mensajes.TIENDA_TITLE_CREATESHOP.toString())) {
			event.setCancelled(true);
			
			if(event.getSlot() == 11 || event.getSlot() == 15) {				
				ItemStack item =  event.getCursor().clone();
				
				if(event.getClick().isRightClick()) {				
					item.setAmount(1);
					ponerItem(event.getSlot(), item);
				} else {
					ponerItem(event.getSlot(), item);
				}
				
				if(event.getSlot() == 11) {
					itemOut = item;
				} else if(event.getSlot() == 15) {
					itemIn = item;
				}
			}			
		}
	}
}
