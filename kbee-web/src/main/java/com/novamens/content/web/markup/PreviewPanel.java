package com.novamens.content.web.markup;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetType;

public abstract class PreviewPanel<T extends Content> extends Panel {
 
	private static final long serialVersionUID = -472535668550638776L;

	public PreviewPanel(String id, IModel<T> model) {
		super(id);
		setModel(model);
	}

	IModel<T> model;
	
	public Page getGalleryReturnPage() 			{return null;}
	public void setGalleryReturnPage(Page page) {} 
	public void onGalleryReturn() 				{}
	
	public void setModel(IModel<T> model)
	{
		this.model=model;
	}
	
	public IModel<T> getModel() {
		return model;
	}
	
	
	protected List<String> getMetadataAsView(Content content) {
		
		DateFormat dateformat = DateFormat.getDateInstance(DateFormat.LONG, getLocale());
		
		List<String> description;
		description = new ArrayList<String>();
		
		boolean por = false;
		
		Map<String, List<String>> classificationmap = new HashMap<String, List<String>>();
		for (Classification classification : content.getClassification()) {
			if (classification.getClassifier().isDisplayable()) {
				String classifierlabel = classification.getClassifier().getName();
				List<String> values = classificationmap.get(classifierlabel);
				if (values == null) {
					values = new ArrayList<String>();
					classificationmap.put(classifierlabel, values);
				}
				if (classification.getClassifier().getDataSetType()==DataSetType.DATE)
					values.add(dateformat.format(classification.getDateValue()));
				else {
					
					if (classification.getClassifier().getName().toLowerCase().startsWith("aut") && ! por) {
						values.add("por " + classification.getAlternativeDisplayValue());
						por= true;
					}
					else
						values.add(classification.getAlternativeDisplayValue());
				}
			}
		}
		for (String classifierlabel : classificationmap.keySet()) {
			StringBuilder label = new StringBuilder(); 
			List<String> values = classificationmap.get(classifierlabel);
			int i = 0;
			for (String value : values) {
				if (i>0) 
					label.append(", ");
				label.append(value);
				i++;
			}
			description.add(label.toString());
		}
		
		return description;
	}

	
}
