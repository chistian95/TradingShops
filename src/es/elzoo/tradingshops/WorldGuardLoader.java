package es.elzoo.tradingshops;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.block.Block;

import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

public class WorldGuardLoader {
	private StateFlag FLAG_TRADE = null;
	
	public WorldGuardLoader() {
		FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();	
		try {
			StateFlag flag = new StateFlag("trade-shop", false);
			registry.register(flag);
			FLAG_TRADE = flag;
		} catch(FlagConflictException e) {
			e.printStackTrace();
		}
	}
	
	public boolean checkRegion(Block bloque) {
		AtomicBoolean isTiendaLoc = new AtomicBoolean(false);
		
		if(FLAG_TRADE != null && TradingShops.config.getBoolean("enableWorldGuardFlag")) {
			RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
			RegionManager regiones = container.get(new BukkitWorld(bloque.getWorld()));
			regiones.getRegions().forEach((id, region) -> {
				if(region.getId().equalsIgnoreCase(ProtectedRegion.GLOBAL_REGION) || region.contains(bloque.getX(), bloque.getY(), bloque.getZ())) {
					if(region.getFlag(TradingShops.wgLoader.getFlagTrade().get()) != null && region.getFlag(TradingShops.wgLoader.getFlagTrade().get()).equals(StateFlag.State.ALLOW)) {
						isTiendaLoc.set(true);
					}
				}
			});
		} else {
			isTiendaLoc.set(true);
		}
		
		return isTiendaLoc.get();
	}
	
	public Optional<StateFlag> getFlagTrade() {
		return Optional.ofNullable(FLAG_TRADE);
	}
}
