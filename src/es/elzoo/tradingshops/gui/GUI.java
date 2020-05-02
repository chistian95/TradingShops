package es.elzoo.tradingshops.gui;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;

public abstract class GUI {
	static Plugin plugin = Bukkit.getPluginManager().getPlugin("ZooSurvival");
	public static Map<String, GUI> inventariosAbiertos = new HashMap<>();
	
	private UUID uuid;
	private Inventory inventario;
	private Map<Integer, GUIAccion> acciones;
	
	public GUI(int invTam, String invNombre) {
		uuid = UUID.randomUUID();
		inventario = Bukkit.createInventory(null, invTam, invNombre);
		acciones = new HashMap<>();
	}
	
	public void onClick(InventoryClickEvent e) {
		e.setCancelled(true);
	}
	
	public void onDrag(InventoryDragEvent e) {
		
	}
	
	public void onClose(InventoryCloseEvent e) {
		return;
	}
	
	public static ItemStack crearItem(Material material, String nombre, short data) {
		ItemStack item = new ItemStack(material);
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.RESET + nombre);
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
		meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_PLACED_ON);
		meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		item.setItemMeta(meta);
		
		return item;
	}
	
	public static void enviarComandoBungee(Player player, String cmd) {
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				DataOutputStream out = new DataOutputStream(stream);
				out.writeUTF(player.getName());
				out.writeUTF(cmd);
				player.sendPluginMessage(plugin, "elzoo:zoosurvival", stream.toByteArray());
			} catch(Exception e) {
				e.printStackTrace();
			}
		});		
	}
	
	public static ItemStack crearItem(Material material, String nombre) {
		return crearItem(material, nombre, (short) 0);
	}
	
	public void ponerItem(int slot, ItemStack item, GUIAccion accion) {
		inventario.setItem(slot, item);
		if(accion != null) {
			acciones.put(slot, accion);
		}
	}
	
	public void ponerItem(int slot, ItemStack item) {
		ponerItem(slot, item, null);
	}
	
	public void abrir(Player player) {
		player.openInventory(inventario);
		inventariosAbiertos.put(player.getName(), this);
	}
	
	public Inventory getInventario() {
		return inventario;
	}
	
	public UUID getUuid() {
		return uuid;
	}
	
	public Map<Integer, GUIAccion> getAcciones() {
		return acciones;
	}
}
