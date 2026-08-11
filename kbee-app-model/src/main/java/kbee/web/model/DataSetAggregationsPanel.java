package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.DataSet;
import com.novamens.content.service.DataSetService;
import com.novamens.datetime.DateTimeService;
import com.novamens.kbee.content.service.KbeeDataSetService;
import com.novamens.security.acl.KbeeGlobalRole;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.markup.html.form.Field;
import com.novamens.wicket.markup.html.form.Form;
import com.novamens.wicket.markup.html.form.Form.Disposition;
import com.novamens.wicket.markup.html.modal.ConfirmationDialog;
import com.novamens.wicket.markup.html.modal.Dialog;
import com.novamens.wicket.markup.html.modal.Dialog.Button;
import com.novamens.wicket.markup.html.form.TextField;
import com.novamens.wicket.model.ObjectModel;

import kbee.util.NumberFormatter;
import kbee.web.editor.DomainObjectEditor;

@SuppressWarnings("serial")
public class DataSetAggregationsPanel<T extends DataSet> extends DomainObjectEditor<T> {
	private static final long serialVersionUID = 1L;
	
	static private Logger logger = LogManager.getLogger(KbeeDataSetService.class);
	
	final boolean role_admin = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.DOMAIN_ADMIN.getId());
	final boolean role_model = ServiceLocator.getService(com.novamens.service.SecurityService.class).isMember(KbeeGlobalRole.INFORMATION_MODEL.getId());
	
	private List<IModel<DataSet>> aggregations = null;
	
	public class Browser extends Fragment {
		public Browser() {
			super("browser", "browser-fragment", DataSetAggregationsPanel.this);
			setOutputMarkupId(true);
			add(new ListView<IModel<DataSet>>("aggregation", ()->getAggregations()) {
				public void populateItem(final ListItem<IModel<DataSet>> item) {
					DataSet aggregation = item.getModelObject().getObject();
					Link<?> datasetlink = new Link<Void>("dataset-link") {
						public void onClick() {
							setResponsePage(new RedirectPage("/model/datasets/"+item.getModelObject().getObject().getId()));
						}
					};
					datasetlink.add(new Label("name", aggregation.getDisplayName()));
					item.add(datasetlink);
					item.add(new Label("type", aggregation.getDataSetType().getLabel()));
					String modifiedlabel = ServiceLocator.getService(DateTimeService.class).getDateDisplayString(aggregation.getLastModifiedOffsetDateTime());
					item.add(new Label("modified", modifiedlabel));
					item.add(new Label("members", NumberFormatter.formatNumber(getContentDao().getTotalElements(aggregation), getSessionUser().getLocale())));
					AjaxLink<?> deletelink = new AjaxLink<Void>("delete-link") {
						public void onClick(AjaxRequestTarget target) {
							ConfirmationDialog dialog = (ConfirmationDialog)Browser.this.get("confirmation-dialog");
							DataSet dataset = item.getModelObject().getObject();
							dialog.open(target, getLabel("confirmation.delete", dataset.getDisplayName()), Dialog.Delete, new Dialog.Handler() {
								@Override
								public void onClick(AjaxRequestTarget target, Button button) {
									if (button.key().equals(Dialog.Delete.key())) {
										try {
											getDataSet().getService(DataSetService.class).deleteAggregation(item.getModelObject().getObject());
											DataSetAggregationsPanel.this.detach();
											target.add(Browser.this);
										} 
										catch (Exception e) {
											logger.error(e);
										}
									}
								}
							});
						}
						public boolean isVisible() {
							return item.getModelObject().getObject().getService(DataSetService.class).getTotalMembers() == 0;
						}
					};
					item.add(deletelink);
				}
			});
			add(new ConfirmationDialog("confirmation-dialog"));
		}
		public boolean isVisible() {
			return !getAggregations().isEmpty();
		}
	}
	
	public class CreationWizard extends Fragment {
		private String aggregationName;
		private int step = 1;
		public CreationWizard() {
			super("wizard", "creation-wizard-fragment", DataSetAggregationsPanel.this);
			
			setOutputMarkupId(true);
			
			WebMarkupContainer creationdata = new WebMarkupContainer("data") {
				public boolean isVisible() {
					return step==2;
				}
			};
			
			add(new AjaxLink<Void>("new-button") {
				public void onClick(AjaxRequestTarget target) {
					step = 2;
					setEditionEnabled(true);
					((Field<?>)creationdata.get("newname")).onBeforeRender();
					target.focusComponent(((Field<?>)creationdata.get("newname")).getInput());
					target.add(CreationWizard.this);
				}
				public boolean isVisible() {
					return step==1;
				}
				@Override
				public boolean isEnabled() {
					return role_admin || role_model;
				}
			});
			
			creationdata.add(new TextField<String>("newname", new PropertyModel<String>(this, "aggregationName")));
			creationdata.add(new AjaxLink<Void>("cancel-button") {
				public void onClick(AjaxRequestTarget target) {
					step = 1;
					setEditionEnabled(false);
					target.add(CreationWizard.this);
				}
			});
			
			creationdata.add(new SubmitButton("create-button", getForm()) {
				protected void onSubmit(AjaxRequestTarget target) {
					step = 1;
					getDataSet().getService(DataSetService.class).createAggregation(getAggregationName());
					setEditionEnabled(false);
					target.add(DataSetAggregationsPanel.this);
				}
			});

			
			add(creationdata);
 		}
		public String getAggregationName() {
			return aggregationName;
		}
		public void setAggregationName(String name) {
			this.aggregationName = name;
		}
	}	
	
	public DataSet getDataSet() {
		return getModelObject();
	}
	
	public DataSetAggregationsPanel(String id, IModel<T> model) {
		super(id, model);
		setOutputMarkupId(true);
		Form<?> form = new Form<Void>("form", Disposition.VERTICAL);
		form.add(new Browser());
		form.add(new CreationWizard());
		add(form);
	}
	
	public List<IModel<DataSet>> getAggregations() {
		if (aggregations!=null) {
			return aggregations;
		}
		aggregations = new ArrayList<IModel<DataSet>>();
		for (DataSet dataset : getDataSet().getService(DataSetService.class).getAggregations()) {
			aggregations.add(new ObjectModel<DataSet>(dataset));
		}
		return aggregations;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		aggregations = null;
	}
}