package kbee.web.portal6.factory;


import java.lang.reflect.Constructor;

import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.portal6.model.Block;


import kbee.web.portal6.IBlockWebPanel;
import kbee.web.portal6.panel.PortalErrorPanel;

public class KbeePortalObjectInternalPanelFactory implements PortalObjectInternalPanelFactory {
			
	//, IDetachable
	
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeePortalObjectInternalPanelFactory.class.getName());
	
	private static final long serialVersionUID = 1L;


	String id;
	String className;
	String title;
	String key;
	
	//IModel<Block> model;
	
	public KbeePortalObjectInternalPanelFactory() {
	}
	
			
	public KbeePortalObjectInternalPanelFactory(String id, String className, String key, String title) {
		//this.model=model;
		this.id=id;
		this.key=key;
		this.title=title;
		this.className=className;
	}
	
	@Override
	public Panel create() {
		return create("body");
	}
	
	@Override
	public Panel create(String pid) {
			try {
				Constructor<?> co = Class.forName(className).getDeclaredConstructor(new Class[] {String.class});
				Panel panel = (Panel) co.newInstance(pid);
				return panel;
			}
			
			catch (Exception e	) {	
				logger.error(e);
				return new PortalErrorPanel<Block>(pid, e);
			} 
			

	}


	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getTitle() {
		return null;
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

	//@Override
	//public void detach() {
	//	if (model!=null)
	//		model.detach();
	//}

}


























//@Override
//public List<BlockFactory> getBlockFactories() {
//	return getPortalDao().getBlockFactories(site.getDomain(), site);
//}
//public List<BlockFactory> getBlockFactories();

/**
*
<bean id="search-external" class="com.novamens.kbee.portal.model.factory.KbeeBlockFactory">
<property name="id" value="search-external"/>
<property name="name" value="Buscador externo"/>
<property name="className" value="com.novamens.kbee.portal.model.KbeeBlockSearchExternal"/>
<property name="usage" value="Buscador externo."/>
<property name="block_intro_visible" value="false"/>
<property name="block_title_visible" value="false"/>
</bean>

**/