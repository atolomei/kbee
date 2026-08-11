package com.novamens.kbee.portal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.kbee.security.KbeeUser;
import com.novamens.portal.favorites.SiteFavorites;
import com.novamens.portal6.model.Site;
import com.novamens.security.User;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "content")
@Table(name = "po_site_favorites")
@DynamicInsert
public class KbeeSiteFavorites implements SiteFavorites {
	
	@Id
	@SequenceGenerator(name = "portal_sequencer", sequenceName = "portalid_sequence")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "portal_sequencer")
	@Column(name = "id")
	private Long id;

	// 8 Junio. saque Cascade.ALL
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id")
	private User user;

	// 8 Junio. saque Cascade.ALL
	@ManyToMany(fetch = FetchType.LAZY, targetEntity = KbeeSite.class)
	@JoinTable(name = "po_site_favorites_list", joinColumns = { @JoinColumn(name = "list_id") }, inverseJoinColumns = {@JoinColumn(name = "site_oid") })
	private List<Site> favorites = new ArrayList<Site>();

	@Transient
	private boolean issorted = false;

	public KbeeSiteFavorites() {
	}

	public KbeeSiteFavorites(User user) {
		this.user = user;
	}

	@Override
	public List<Site> getFavorites() {
		return getList();
	}

	@Override
	public void addFavoriteSite(Site site) {
		favorites.add(site);
		issorted = false;
	}

	@Override
	public void removeFavoriteSite(Site site) {
		favorites.remove(site);
	}

	@Override
	public User getUser() {
		return user;
	}

	@Override
	public List<Site> getList() {
		if (!issorted) {
			Collections.sort(favorites, new Comparator<Site>() {
				@Override
				public int compare(Site a, Site b) {
					try {
						return a.getTitle().compareToIgnoreCase(b.getTitle());
					} catch (Exception e) {
						return 0;
					}
				}
			});

			issorted = true;
		}
		return favorites;
	}
}
