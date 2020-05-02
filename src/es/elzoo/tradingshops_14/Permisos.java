package es.elzoo.tradingshops_14;

public enum Permisos {
	TIENDA_CREAR("tradingshop.create"),
	TIENDA_ADMIN("tradingshop.admin"),
	TIENDA_LIMITE_PREFIX("tradingshop.create.limit."),
	TIENDA_LIMITE_BYPASS("tradingshop.create.limit.bypass"),
	TIENDA_ROMPER_STOCK("tradingshop.breakstock");
	
	private String permiso;
	
	private Permisos(String permiso) {
		this.permiso = permiso;
	}
	
	@Override
	public String toString() {
		return permiso;
	}
}
