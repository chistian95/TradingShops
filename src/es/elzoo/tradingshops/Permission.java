package es.elzoo.tradingshops;

public enum Permission {
	SHOP_CREATE("tradingshop.create"),
	SHOP_ADMIN("tradingshop.admin"),
	SHOP_LIMIT_PREFIX("tradingshop.create.limit."),
	SHOP_LIMIT_BYPASS("tradingshop.create.limit.bypass");
	
	private final String perm;
	
	Permission(String perms) {
		this.perm = perms;
	}
	
	@Override
	public String toString() {
		return perm;
	}
}
