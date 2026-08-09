package kbee.web.nav;

public interface NavigablePage<T> {
	public Navigator<T> getNavigator();
	public void setNavigator(Navigator<T> navigator);
}
