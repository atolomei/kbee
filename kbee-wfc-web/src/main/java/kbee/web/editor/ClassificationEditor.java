package kbee.web.editor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import com.novamens.beans.BeansService;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.ClassifierTemplate;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.service.ServiceLocator;
import com.novamens.wicket.model.ObjectModel;

@Deprecated
@SuppressWarnings("serial")
 public class ClassificationEditor<T extends Classificable> extends Panel  {
	private static final long serialVersionUID = 1L;

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(ClassificationEditor.class.getName());
	
	private List<Panel> memberspanels;
	private boolean readonly = false;
	
	
	public ClassificationEditor(boolean readOnly) {
		this("classification", readOnly);
	}
	
	public ClassificationEditor() {
		this("classification", false);
	}
	
	public ClassificationEditor(String id, boolean isReadOnly) {
		super(id);
		setOutputMarkupId(true);
		this.readonly=isReadOnly;
	}
	
	
	public List<Panel> getMembersPanels() {
		
		if (this.memberspanels!=null)
			return this.memberspanels;
		 
		this.memberspanels = new ArrayList<Panel>();
	
		for (ModelElementTemplate template : getStructure()) {
			if (template!=null) {
				if (template instanceof AttributeTemplate && ((AttributeTemplate)template).getAttribute()!=null) {
					try {
						this.memberspanels.add(new AttributeEditor<T>("members", new ObjectModel<AttributeTemplate>((AttributeTemplate)template)));
					} 
					catch (Exception e) {
						logger.error(e);
					}
				}
				if (template instanceof ClassifierTemplate && ((ClassifierTemplate)template).getClassifier()!=null) {
					try {
						MembersEditor<T> memberspanel = new MembersEditor<T>("members", new ObjectModel<ClassifierTemplate>((ClassifierTemplate)template), this.readonly) {
							@Override
							@SuppressWarnings("unchecked")
							public void onUpdate(AjaxRequestTarget target) {
								super.onUpdate(target);
								for (Panel panel : getMembersPanels()) {
									if (panel instanceof MembersEditor && !panel.equals(this)) {
										((MembersEditor<T>)panel).onUpdate(target, getClassifier());
									}
//									if (panel instanceof AttributeEditor) {
//										((AttributeEditor<T>)panel).onUpdate(target, getClassifier());
//									}
								}
							}
							@Override
							public List<DataSetMember> getClassification(Classifier classifier) {
								List<DataSetMember> values = new ArrayList<DataSetMember>();
								for (Panel panel : getMembersPanels()) {
									if (panel instanceof MembersEditor && ((MembersEditor)panel).getClassifier().equals(classifier)) {
										values.addAll(((MembersEditor<T>)panel).getMembers());
									}
								}
								return values;
							}
						};
						this.memberspanels.add(memberspanel);
					} 
					catch (Exception e) {
						logger.error(e);
					}
				}
			}
		}
		
		return this.memberspanels;
	}
	
	@Override
	public void onBeforeRender() {
		super.onBeforeRender();
		if (get("classification")==null) {
			add(new ListView<Panel>("classification", getMembersPanels()) {
				protected void populateItem(ListItem<Panel> item){
					try {
						item.add(item.getModelObject());
						item.setOutputMarkupId(true);
						item.setVisible(item.getModelObject().isVisible());
						item.detach();
					} 
					catch (Exception e) {
						logger.error(e);
					}
				}
			});	
		}
	}	
	
	public List<Classifier> getClassifiers() {
		return null;
	}
	
	public List<AttributeTemplate> getAttributes() {
		return null;
	}
	
	public List<ModelElementTemplate> getStructure() {
		return null;
	}
	
	public ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	public boolean isReadOnly() {
		return this.readonly;
	}
	
	public void setReadOnly(boolean re) {
		this.readonly=re;
	}
}
