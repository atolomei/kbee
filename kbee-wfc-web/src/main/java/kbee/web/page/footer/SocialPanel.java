package kbee.web.page.footer;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.util.KeyValue;

public class SocialPanel extends Panel {

	private static final long serialVersionUID = 1L;
	
	static List<KeyValue<String>> menu_options = new ArrayList<KeyValue<String>>();

	
	// JSON con la lista
	// -----------------
	// footer.legal {{gicon, url}, {gicon, url}}
	//
	
	static {

		menu_options.add(new KeyValue<String>("fab fa-facebook", "https://www.facebook.com/realpage"));
		menu_options.add(new KeyValue<String>("fab fa-twitter", "https://twitter.com/RealPage"));
		menu_options.add(new KeyValue<String>("fab fa-linkedin", "https://www.linkedin.com/company/realpage"));
		menu_options.add(new KeyValue<String>("fab fa-youtube", "https://www.youtube.com/user/RealPageMedia"));
		menu_options.add(new KeyValue<String>("fab fa-google-plus", "https://plus.google.com/+Realpage/posts"));
		
	}

	
	
	public SocialPanel(String id) {
		super(id);
	
	 
		ListView<KeyValue<String>> menu = new ListView<KeyValue<String>>("menu", menu_options) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<KeyValue<String>> item) {

				Link<Void> link = new Link<Void>("link") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
						setResponsePage( new RedirectPage(item.getModelObject().getValue()));
					}
					
				};

				item.add(link);
				
				WebMarkupContainer gicon = new WebMarkupContainer("icon");
				gicon.add(new AttributeModifier("class", item.getModelObject().getDisplayName()));
				link.add(gicon);
				
			}
		};
		
		add(menu);
	
	
	
	
	
	
	
	
	}
	

}
