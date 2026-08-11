package kbee.web.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.AttributeModifier;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSet;
import com.novamens.content.user.UserService;
 
import com.novamens.dom.Domain;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

@SuppressWarnings("serial")
public class DataSetClassifiersPanel<T extends DataSet> extends Panel {
	private static final long serialVersionUID = 1L;

	private IModel<T> model;

	List<Classifier> classifiers;
	
	public DataSetClassifiersPanel(String id, IModel<T> model) {
		super(id);
		
		setModel(model);
		
		add(new ListView<Classifier>("classifiers", new PropertyModel<List<Classifier>>(this, "classifiers")) {
			public void populateItem(final ListItem<Classifier> item) {
				Link<Classifier> link = new Link<Classifier>("link") {
					private static final long serialVersionUID = 1L;
					public void onClick() {
						setResponsePage(new ClassifierModelPage(new ObjectModel<Classifier>(item.getModelObject()), false, false));
					}
				};
				link.add(new AttributeModifier("target", "_blank"));
				String label = item.getModelObject().getName();
				if (label==null) 
					label = "Classifier " + item.getModelObject().getId().toString();
				link.add(new Label("label", label));
				item.add(link);
			}
		});
	}
	
	public List<Classifier> getClassifiers() {
		if (this.classifiers==null) {
			this.classifiers = new ArrayList<Classifier>();
			List<Classifier> list = getContentDao().getClassifiers(getDomain());
			for (Classifier classifier: list) {
				if (classifier.getDataSet()!=null && classifier.getDataSet().getId().equals(getModel().getObject().getId())) 
					this.classifiers.add(classifier);
			}
		}
		return this.classifiers;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.classifiers = null;
	}

	private void setModel(IModel<T> model) {
		this.model = model;
	}
	
	private IModel<T> getModel() {
		return this.model;
	}
	
	private ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	private Domain getDomain() {
		return (Domain)ServiceLocator.getService(UserService.class).getDomain();
	}
}
