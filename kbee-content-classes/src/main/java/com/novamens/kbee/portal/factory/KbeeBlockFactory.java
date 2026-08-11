package com.novamens.kbee.portal.factory;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.novamens.kbee.sql.SqlPlatform;
import com.novamens.kbee.sql.SqlPlatformFactory;
import com.novamens.portal.factory.BlockFactory;
import com.novamens.portal.factory.PortalObjectFactory;
import com.novamens.portal6.model.Block;
import com.novamens.service.FactoryService;
import com.novamens.util.KbeeRuntimeException;


/***
 * KbeeBlockFactory
 * 
 * {@link KbeeBlockEditorFactory}
 * {@link KbeePortalObjectInternalPanelFactory}
 * 
 * 
 * <bean id="block-panel-billboard" class="kbee.web.portal6.factory.KbeePortalObjectInternalPanelFactory">
		<property name="id" value="block-panel-billboard"/>
		<property name="className" value="kbee.web.alert.BillboardPanel"/>
		<property name="key" value="block-billboard"/>
		<property name="title" value="billboard block panel"/>
	</bean>
 * 
 */
public class KbeeBlockFactory implements BlockFactory, FactoryService {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeBlockFactory.class.getName());
	
	private String id;
	private String title;
	private String key;
	private String className;
	private String usageInfoKey;
	

	public KbeeBlockFactory() {
	}
	
	public KbeeBlockFactory(String id, String title,  String key,  String clazz, String usageinfokey) {
		this.id=id;
		this.className=clazz;
		this.key=key;
		this.id=id;
		this.title=title;
		this.usageInfoKey=usageinfokey;
	}

	@Override
	public Block create() {
		
		try {
			
			Block block;
			block = (Block) Class.forName(className).getDeclaredConstructor().newInstance();
			block.setTitle(this.title);
			block.setKey(this.key);
			block.setUsageInfoKey(getUsageInfoKey());
			return block;
			
		}
		
		catch (InvocationTargetException | NoSuchMethodException e	) {	logger.error(e);} 
		catch (InstantiationException e								) {	logger.error(e);} 
		catch (IllegalAccessException e								) {	logger.error(e);} 
		catch (RuntimeException e									) {	logger.error(e);} 
		catch (ClassNotFoundException e								) {	logger.error(e);}
		
		return null;
	}
	
	
	
	public String getclassName() {
		return className;
	}

	public void setclassName(String clazz) {
		this.className = clazz;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDisplayName() {
		return title;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getKey() {
		return key;
	}
	
	@Override
	public String getUsageInfoKey() {
		return usageInfoKey;
	}

	public void setUsageInfoKey(String usageinfokey) {
		this.usageInfoKey = usageinfokey;
	}


}
