package com.novamens.content.web.base.page.component;

import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.markup.repeater.data.ListDataProvider;


@Deprecated
public class BreadcrumbPanel extends Panel {
	private static final long serialVersionUID = 2651430036732713082L;
	static final int MAX = 56;
	private boolean is_visisble = true;
	public BreadcrumbPanel(String id, List<BreadcrumbElement> list) {
		super(id);
		
		this.is_visisble = list.isEmpty();
		
		DataView<BreadcrumbElement>  data = new DataView<BreadcrumbElement>("breadcrumb-element",new ListDataProvider<BreadcrumbElement>(list)) {
		
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(Item<BreadcrumbElement> item){
				BreadcrumbElement element = item.getModelObject();
				WebMarkupContainer url = new WebMarkupContainer("link");
				if (element.getUrl()==null) 
					url.add(new AttributeModifier("href","#"));
				else  
					url.add(new AttributeModifier("href",element.getUrl()));
				url.add(new Label("label", getCrumb(element.getLabel())));
				item.add(url);
				Label arrow = new Label("arrow", ">");
				arrow.setVisible(item.getIndex()>0);
				item.add(arrow);
				
  			}
		};
		 add(data);
 	}
	private String getCrumb(String str) {
		if (str!=null && str.length()<MAX)
			return str;
		return str.substring(0, MAX)+"...";
	}

	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public boolean isVisible() {
		return this.is_visisble;
	}
	
}
