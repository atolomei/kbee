package com.novamens.portal.favorites;

import java.util.List;

import com.novamens.portal6.model.Site;
import com.novamens.security.User;

public interface SiteFavorites {

	public User getUser();

	public List<Site> getFavorites();
	public List<Site> getList();

	public void addFavoriteSite(Site site);
	public void removeFavoriteSite(Site site);
	
}
