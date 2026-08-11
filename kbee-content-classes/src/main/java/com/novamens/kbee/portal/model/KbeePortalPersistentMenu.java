package com.novamens.kbee.portal.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.portal6.model.PortalMenu;
import com.novamens.portal6.model.PortalMenuItem;
import com.novamens.portal6.model.PortalObject;
import com.novamens.portal6.model.PortalPersistentMenu;
import com.novamens.portal6.model.Site;


/**
 * 
 * Menu
 * SiteMap
 * Breadcrumb
 * Tab
 *
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "po_id")
@Table(name = "PO_MENU")
public class KbeePortalPersistentMenu extends KbeePortalObject implements PortalPersistentMenu {
			
	@SuppressWarnings("unused")
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalPersistentMenu.class.getName());

	
	@Column(name = "json_menu")
	private String jsonmenu;

	@Column(name = "position")
	private int position;
	
	
	@Transient
	private List<PortalMenuItem> items = new ArrayList<PortalMenuItem>();


	@Transient
	private boolean initialized;

	
	@Column(name = "site_id", insertable=false, updatable=false)
	private Long site_id;
	
	
	public KbeePortalPersistentMenu() {
		initTest();
	}
	
	public KbeePortalPersistentMenu(String name) {
		setTitle(name);
		setName(name);
		initTest();
		
	}
	
	
	
	
	
	@Override
	public String treeString() {
		
		StringBuilder str = new StringBuilder();
		
		str.append("\n-----------------------------\n");
		str.append("Menu -> " + getLabel() + " \n");
		
		for (PortalMenuItem m: this.getMenuItems()) {
			str.append(m.treeString());
			str.append("\n");
		}
		str.append("-----------------------------\n");
		return str.toString();
	}
	
	
	 
	@Override
	public void setOrder(int n) {
		this.position=n;
	}
	@Override
	public int getOrder() {
		return this.position;
	}

	
	public String getJsonMenu() {
		return this.jsonmenu;
	}

	public void setJSonMenu(String m) {
		this.jsonmenu = m;
	}
	
	
	/**
	public Map<String, String> getContext() {
		
		if (map!=null)
			return map;
		 try {
			 ObjectMapper mapper = new Obje ctMapper();
			 map = mapper.readValue(context, Map.class);
		} catch (IOException e) {
			map = null;
			logger.error(e);
		}
		 return new HashMap<String, String>();
	}
	
	@Override
	public void setContext(Map<String, String> map) {
		
		this.map=null;
		
		if (map==null)
			context=null;
		else {
				ObjectMapper mapper = new Obj ectMapper();
				String json;
				try {
					json = mapper.writeValueAsString(map);
					context = json;
				} catch (JsonProcessingException e) {
					logger.error(e);
				}
		}
	}
*/
	
	
	
	
	@Override
	public String getMetadataAsString() {
		return null;
	}

	@Override
	public Map<String, String> getSpecificInfo() {
		return null;
	}

	@Override
	public String getClassKey() {
		return  PortalPersistentMenu.KEY;
	}

	
	@Override
	public PortalObject clone() {
		return null;
	}

	
	@Override
	public Site getSite() {
		if (site_id==null)
			return null;
		return getPortalDao().findSiteById(site_id);
	}
	
	@Override
	public PortalObject getParent() {
		if (site_id==null)
			return null;
		return getPortalDao().findSiteById(site_id);
	}

	@Override
	public void addMenuItem(PortalMenuItem item) {
	 
		items.add(item);
	}

	
	@Override
	public void add(PortalMenuItem item) {
		items.add(item);
	}

	@Override
	public List<PortalMenuItem> getMenuItems() {
		if (items==null) {
			items=new ArrayList<PortalMenuItem>();
		}
		return items;
	}

	
	private void initTest() {
		
		// 1. Servicios
		PortalMenu	   servicios = new KbeePortalMenu("Servicios");
		PortalMenuItem servicios_1 = new KbeePortalMenuItem("Servicios 1");
		PortalMenuItem servicios_2 = new KbeePortalMenuItem("Servicios 2");
		PortalMenuItem servicios_3 = new KbeePortalMenuItem("Servicios 3");
		servicios.add(servicios_1);
		servicios.add(servicios_2);
		servicios.add(servicios_3);

		PortalMenuItem i2 = new KbeePortalMenuItem("Cómo funciona");
		PortalMenuItem i3 = new KbeePortalMenuItem("Quienes Somos");
		PortalMenuItem i4 = new KbeePortalMenuItem("Precios");
		PortalMenuItem i5 = new KbeePortalMenuItem("Contacto");

		
		// 6. Productos
		PortalMenu productos 	= new KbeePortalMenu("Productos");
		PortalMenuItem productos_1 = new KbeePortalMenuItem("Producto 1");
		PortalMenuItem productos_2 = new KbeePortalMenuItem("Producto 2");
		PortalMenuItem productos_3 = new KbeePortalMenuItem("Producto 3");
		productos.add(productos_1);
		productos.add(productos_2);
		productos.add(productos_3);
		
		// 6.1. Subproductos
		PortalMenu productos_sub 	= new KbeePortalMenu("P1 Subproductos");
		PortalMenuItem productos_sub_1 = new KbeePortalMenuItem("P1 SubProducto 1");
		PortalMenuItem productos_sub_2 = new KbeePortalMenuItem("P1 SubProducto 2");
		PortalMenuItem productos_sub_3 = new KbeePortalMenuItem("P1 SubProducto 3");
		productos_sub.add(productos_sub_1);
		productos_sub.add(productos_sub_2);
		productos_sub.add(productos_sub_3);
		productos.add(productos_sub);
		
		
		add(servicios);
		add(i2);
		add(i3);
		add(i4);
		add(i5);
		add(productos);
		
	}
	
	
	/**
	 * 
	 */
	
	public String getLabel() {
		return this.getTitle();
	}

	
	
	
	
	public String displayPanelKey() {
		return null;
	}


	@Override
	public PortalMenu getPortalMenu() {
		PortalMenu menu = new KbeePortalMenu(this);
		return menu;
	}

	@Override
	public String getDataProviderInfo() {
		// TODO Auto-generated method stub
		return null;
	}

}
