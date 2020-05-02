package es.elzoo.tradingshops.inventarios;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import es.elzoo.tradingshops.Mensajes;
import es.elzoo.tradingshops.StockTienda;
import es.elzoo.tradingshops.TradingShops;
import es.elzoo.tradingshops.gui.GUI;

public class InvStock extends GUI {
	private static List<InvStock> inventarios = new ArrayList<InvStock>();  
	
	private UUID owner;
	private int pag;
	
	private InvStock(UUID owner) {
		super(54, Mensajes.TIENDA_TITLE_STOCK.toString());
		
		inventarios.add(this);
		
		this.owner = owner;
		this.pag = 0;
	}
	
	public static InvStock getInvStock(UUID owner) {
		return inventarios.parallelStream().filter(inv -> {
			return inv.owner.equals(owner);
		}).findFirst().orElse(new InvStock(owner));
	}
	
	@Override
	public void onClick(InventoryClickEvent event) {
		super.onClick(event);
		
		if(event.getSlot() >= 45) {
			return;
		}
		
		event.setCancelled(false);
	}
	
	public void refrescarItems() {		
		Optional<StockTienda> stockOpt = StockTienda.getStockTiendaByOwner(owner, pag);
		StockTienda stock = null;
		if(!stockOpt.isPresent()) {
			stock = new StockTienda(owner, pag);
		} else {
			stock = stockOpt.get();
		}
		
		Inventory inv = stock.getInventario();
		
		for(int i=0; i<45; i++) {
			ItemStack item = inv.getItem(i);
			ponerItem(i, item);
		}
		
		for(int i=45; i<54; i++) {
			if(i == 47 && pag > 0) {
				ItemStack item = GUI.crearItem(Material.ARROW, Mensajes.TIENDA_PAGE.toString()+" " + (pag));
				ponerItem(i, item, p -> {
					abrirPagina(p, pag-1);
				});
			} else if(i == 51 && pag < TradingShops.config.getInt("stockPages")-1) {
				ItemStack item = GUI.crearItem(Material.ARROW, Mensajes.TIENDA_PAGE.toString()+" " + (pag+2));
				ponerItem(i, item, p -> {
					abrirPagina(p, pag+1);
				});
			} else {
				ItemStack item = GUI.crearItem(Material.BLACK_STAINED_GLASS_PANE, "");
				ponerItem(i, item);
			}
		}
	}
	
	private void abrirPagina(Player player, int pag) {
		for(int i=45; i<54; i++) {
			ponerItem(i, new ItemStack(Material.AIR));
		}
		player.closeInventory();
		this.pag = pag;
		this.abrir(player);
	}
	
	@Override
	public void abrir(Player player) {
		refrescarItems();
		super.abrir(player);
	}
	
	@Override
	public void onClose(InventoryCloseEvent event) {
		Inventory inventario = event.getInventory();
		
		Optional<StockTienda> stock = StockTienda.getStockTiendaByOwner(owner, pag);
		if(!stock.isPresent()) {
			return;
		}
		
		stock.get().setInventario(inventario);
	}
	
	public void setPag(int pag) {
		this.pag = pag;
	}
}
