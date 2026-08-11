package kbee.web.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.pages.RedirectPage;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;

import com.novamens.content.model.ContentTemplate;
import com.novamens.content.model.DataSet;
import com.novamens.content.model.ModelReference;
import com.novamens.content.service.DataSetService;
 
import com.novamens.kbee.content.model.KbeeModelReference;

@SuppressWarnings("serial")
public class DataSetUsedByPanel<T extends DataSet> extends Panel {
	private static final long serialVersionUID = 1L;

	private IModel<T> model;

	List<ModelReference> references;
	
	public DataSetUsedByPanel(String id, IModel<T> model) {
		super(id);
		
		setModel(model);
		
		add(new ListView<ModelReference>("references", new PropertyModel<List<ModelReference>>(this, "references")) {
			public void populateItem(final ListItem<ModelReference> item) {
				Link<Void> link = new Link<Void>("link") {
					public void onClick() {
						setResponsePage(new RedirectPage(item.getModelObject().getUrl()));
					}
				};
				link.add(new Label("label", item.getModelObject().getDescription()));
				link.add(new Label("clazz", "("+item.getModelObject().getModelElementClass()+")"));
				item.add(link);
			}
		});
	}
	

	public List<ModelReference> getReferences() {
		if (this.references==null) {
			this.references = new ArrayList<ModelReference>();
			for (Object object : getModel().getObject().getService(DataSetService.class).getReferences()) {
				KbeeModelReference reference = new KbeeModelReference(); 
				if (object instanceof DataSet) {
					reference.setDescription(((DataSet)object).getDisplayName());
					reference.setUrl("/model/datasets/"+((DataSet)object).getId());
					reference.setModelElementClass(((DataSet)object).getModelObjectClassName());
					
				}
				else if (object instanceof ContentTemplate) {
					reference.setDescription(((ContentTemplate)object).getDisplayName());
					reference.setUrl("/model/contentclass/"+((ContentTemplate)object).getId());
					reference.setModelElementClass(((ContentTemplate)object).getModelObjectClassName());
				}
				this.references.add(reference);
			}
		}
		
		this.references.sort(new Comparator<ModelReference>() {

			@Override
			public int compare(ModelReference o1, ModelReference o2) {
				try {
				return o1.getDescription().compareToIgnoreCase(o2.getDescription());
				} catch (Exception e) {
					return 0;
				}
			}
			
		});
		
		
		return this.references;
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		this.references = null;
	}

	private void setModel(IModel<T> model) {
		this.model = model;
	}
	
	private IModel<T> getModel() {
		return this.model;
	}
}
