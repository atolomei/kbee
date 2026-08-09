package kbee.web.searcher.panel;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.service.ContentService;
import com.novamens.dom.ObjectState;
import com.novamens.portal6.model.Site;

  
public class SearcherDetailTitlePanel<T extends Content> extends SearcherDetailPanel<T> {
	private static final long serialVersionUID = 1L;

	static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(SearcherDetailTitlePanel.class.getName());

	public boolean isConsole;
	private boolean show_abstract = true;

	public SearcherDetailTitlePanel(String id, IModel<T> model, IModel<Site> site_model) {
		this(id, model, site_model, false);
	}

	public SearcherDetailTitlePanel(String id, IModel<T> model, IModel<Site> site_model, boolean isConsole) {
		super(id, model, site_model);
		this.isConsole = isConsole;
	}

	public boolean isConsole() {
		return this.isConsole;
	}

	@Override
	public void onInitialize() {
		super.onInitialize();

		WebMarkupContainer title_container = new WebMarkupContainer("title-container") {
			@Override
			public boolean isVisible() {
				return getModel().getObject().getTitle() != null;
			}
		};
		add(title_container);

		WebMarkupContainer lock = new WebMarkupContainer("lock") {
			@Override
			public boolean isVisible() {
				return isConsole() && getModel().getObject().isLocked();
			}
		};
		title_container.add(lock);

		/**
		 * WebMarkupContainer previous_version = new
		 * WebMarkupContainer("previous-version") { private static final long
		 * serialVersionUID = 1L;
		 * 
		 * @Override public boolean isVisible() { if (getContent().getWorkspace()!=null)
		 *           return false; if (getContent().getState()==ObjectState.DELETED)
		 *           return false; return !getContent().isHeadVersion() &&
		 *           !getContent().getService(ContentService.class).isValidVersion(); }
		 *           };
		 */

		WebMarkupContainer deleted = new WebMarkupContainer("deleted") {
			@Override
			public boolean isVisible() {
				if (getContent().getState() == ObjectState.DELETED)
					return true;
				return false;
			}
		};
		// previous_version.add(new AttributeModifier("title", "Deleted"));
		title_container.add(deleted);

		// previous_version.add(new AttributeModifier("title", "Version
		// "+String.valueOf(getModel().getObject().getVersion())));
		// title_container.add(previous_version);

		Label xtitle = new Label("title", getModel().getObject().getTitle() != null ? getModel().getObject().getTitle() : "");
		title_container.add(xtitle);

		IModel<String> subtitlemodel = new Model<String>() {
			public String getObject() {
				return getModelObject().getService(ContentService.class).getConsoleSubtitle();
			}
		};
		Label subtitle = new Label("subtitle", subtitlemodel) {
			public boolean isVisible() {
				return subtitlemodel.getObject() != null && !"".equals(subtitlemodel.getObject());
			}
		};
		subtitle.setEscapeModelStrings(false);
		title_container.add(subtitle);
	}

	public void setShowAbstract(boolean b) {
		this.show_abstract = b;
	}

	public boolean isShowAbstract() {
		return this.show_abstract;
	}
}
