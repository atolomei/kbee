package kbee.web.nav;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.wicket.markup.html.panel.KBPanel;

public class ApplicationPageHeader extends KBPanel {

	
	private static final long serialVersionUID = 1L;
	IModel<String> title_h1 ;
	
	public ApplicationPageHeader(String id, IModel<String> title, Panel breadcrumb) {
		super(id);
		title_h1=title;
		super.setOutputMarkupId(true);
		add(breadcrumb);
		add(new Label("title_h1", title_h1!=null?title_h1:new Model<String>(this.getClass().getSimpleName())));
	}
	
	
	@Override
	public void onInitialize() {
		super.onInitialize();
	}

	
}



/**

    <div>
        <div class="console-page-header page-header">
            <div class="page-header-content">
                <h1 wicket:id="console-name"></h1>
                <div wicket:id="breadcrumb" />
            </div>
        </div>
        <div style="height:46px;">&nbsp; </div>
    </div>


*/