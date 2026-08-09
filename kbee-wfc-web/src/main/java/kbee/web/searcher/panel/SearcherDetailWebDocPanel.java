package kbee.web.searcher.panel;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.kbee.wicket.util.InvisiblePanel;
import com.novamens.portal6.model.Site;


public class SearcherDetailWebDocPanel<T extends Content> extends SearcherDetailPanel<T> {
			
	private static final long serialVersionUID = 1L;
	
	private String content;
	private String footer;
	private boolean isHeader = true;
			
	
	public SearcherDetailWebDocPanel(String id, IModel<T> model,  IModel<Site> site_model, String content, String footer) {
		super(id, model, site_model);
		setContent(content);
		setFooter(footer);
	}
	
	public void setContent( String content) {
		this.content=content;
	}
	public void setFooter( String footer) {
		this.footer=footer;
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
		
		{
			Label la=new Label("content", content);
			la.setEscapeModelStrings(false);
			add(la);
		}
		
		
		{
			Label la=new Label("footer", footer);
			la.setVisible(footer!=null);
			la.setEscapeModelStrings(false);
			add(la);
		}
		
		if (isHeader()) {
			add(new HeaderFragment("header"));
		}
		else
			add(new InvisiblePanel("header"));
	}
	
	public boolean isHeader() {
		return isHeader;
	}

	public void setHeader(boolean isHeader) {
		this.isHeader = isHeader;
	}

	public class HeaderFragment extends Fragment {
		private static final long serialVersionUID = 1L;
		public HeaderFragment(String id) {
			super(id, "header-fragment", SearcherDetailWebDocPanel.this);
			
			
		}
	}
			
			
	
	
}
