package es.elzoo.tradingshops.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import es.elzoo.tradingshops.inventories.InvStock;

public class GUIEvent implements Listener {
	@EventHandler
	public void onClick(InventoryClickEvent e) {
		if(!(e.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) e.getWhoClicked();

		GUI gui = GUI.inventoriesOpen.get(player.getName());
		if(gui == null)
			return;

		GUIAction action = gui.getActions().get(e.getSlot());
		gui.onClick(e);
		
		if(action != null)
			action.click(player);
	}
	
	@EventHandler
	public void onDrag(InventoryDragEvent e) {
		if(!(e.getWhoClicked() instanceof Player))
			return;

		Player player = (Player) e.getWhoClicked();
		GUI gui = GUI.inventoriesOpen.get(player.getName());
		if(gui == null)
			return;
		
		gui.onDrag(e);
	}
	
	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Player player = (Player) e.getPlayer();
		GUI gui = GUI.inventoriesOpen.get(player.getName());
		if(gui != null)
			gui.onClose(e);

		GUI.inventoriesOpen.remove(player.getName());
		InvStock.inShopInv.remove(e.getPlayer());
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		Player player = e.getPlayer();
		GUI.inventoriesOpen.remove(player.getName());
		InvStock.inShopInv.remove(e.getPlayer());
	}
}