package kbee.web.console.grid;


import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.springframework.security.core.userdetails.User;

import com.novamens.content.base.Content;
import com.novamens.content.service.LabelsService;
import com.novamens.content.user.UserLabel;
import com.novamens.kbee.wicket.markup.html.event.WicketEventListener;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.event.wicket.LabelEvent;
						
public class LabelTagPanel<T extends Content> extends Panel {
		
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(LabelTagPanel.class.getName());

	
	private static final long serialVersionUID = 1L;
	
	private List<IModel<UserLabel>> labels;
	private IModel<T> model;


	public class LabelModel implements IModel<UserLabel> {
		
		private static final long serialVersionUID = 1L;
		
		private UserLabel label;
		private IModel<UserLabel> model;
		private String title, color;
		
		public LabelModel(UserLabel label) {
			model = new ObjectModel<UserLabel>(label);
			title = label.getLabel();
			color = label.getCss();
			this.label = label;
		} 
		public UserLabel getObject() {
			if (label==null) {
				label = model.getObject();
				label.setLabel(title);
				label.setCss(color);
			}	
			return label;
		}
		public void setObject(UserLabel label) {
		}
		
		public void detach() {
			if (label!=null) {
				title = label.getLabel();
				color = label.getCss();
				model.detach();
				this.label = null;
			}
		}
	}


	/**
	 * 
	 * 
	 * @param id
	 * @param model
	 */
	@SuppressWarnings("deprecation")
	public LabelTagPanel(String id, IModel<T> model) {
		super(id, model);
		
		this.model=model;
		
		setOutputMarkupId(true);
		
		getLabels();
		addLabelList();
										
		add(new WicketEventListener<LabelEvent>(LabelEvent.class) {
			private static final long serialVersionUID = 1L;
			@Override
			public void onEvent(LabelEvent event) {
				labels = null;
				getLabels();
				event.getRequestTarget().add(LabelTagPanel.this.getParent());
			}
		});

  }
	
	public boolean isVisible() {
		return !getLabels().isEmpty();
	}
	
	@Override
	public void onDetach() {
		if (labels!=null) {
			for (IModel<UserLabel> label: getLabels()) 
				label.detach();
		}
		this.model.detach();
		this.labels=null;
		super.onDetach();
	}
	

	protected List<IModel<UserLabel>> getLabels() {
		
		if (this.labels==null) {
			this.labels=new ArrayList<IModel<UserLabel>>();
			List<UserLabel> arr = this.model.getObject().getService(LabelsService.class).getUserLabels();
			for (UserLabel label: arr) 
				try {
					this.labels.add(new ObjectModel<UserLabel>(label));
				} catch (Exception e) {
					logger.error( getSessionUser().getUsername() + " | " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
					
					if (e instanceof org.hibernate.ObjectNotFoundException) {
						logger.info("Removing label that does not exists " + ((org.hibernate.ObjectNotFoundException) e).getIdentifier());
						try {
								this.model.getObject().getService(LabelsService.class).removeUserLabelById(((org.hibernate.ObjectNotFoundException) e).getIdentifier().toString());
						} catch (Exception e1) {
							logger.error(getSessionUser().getUsername() + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());	
						}
					}
				}
		}
		return this.labels;
	}

	private User getSessionUser() {
		return (User) ServiceLocator.getService(SecurityService.class).getSessionUser();
	}

	private void addLabelList() {
		 
		WebMarkupContainer list = new WebMarkupContainer("label-list"); 
		add(list);
		list.setOutputMarkupId(true);
		list.add(new ListView<IModel<UserLabel>>("label-element", 
				 new PropertyModel<List<IModel<UserLabel>>>(this, "labels")) {
					private static final long serialVersionUID = 1L;
						protected void populateItem(ListItem<IModel<UserLabel>> item) {
							try {
								WebMarkupContainer icon = new WebMarkupContainer("icon");
								icon.add(new AttributeModifier("class", "far fa-tag " + item.getModel().getObject().getObject().getCss()));
								item.add(icon);
								Label label = new Label("label", item.getModel().getObject().getObject().getLabel());
								item.add(label);
								
							} catch (Exception e) {
								logger.error(getSessionUser().getUsername() + " | " + e.getClass().getName() + " | " +  Thread.currentThread().getStackTrace()[1].getMethodName());
								logger.error("Likely the Label was deleted and a Content still has a reference to it.");
								WebMarkupContainer icon = new WebMarkupContainer("icon");
								icon.setVisible(false);
								item.add(icon);
								item.add((new Label("label")).setVisible(false));
							}
				}
		 });
	 }


}
