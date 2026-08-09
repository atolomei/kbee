package kbee.web.report;

import java.util.List;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import com.novamens.content.base.Content;
import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classification;
import com.novamens.content.model.DataSetMember;
import com.novamens.indexer.query.SearchResult;

public class AttributeColumn extends ReportColumn {
	private static final long serialVersionUID = 1L;

	public AttributeColumn(String id, IModel<String> displayModel, String sortProperty) {
		super(id, displayModel, sortProperty);
	}
	
	@Override
	protected IModel<String> getLabelModel(SearchResult result) {
		
		if (result.getObject()==null) 
			return new Model<String>("err");
		
		Content content = (Content)result.getObject();
		
		for (Classification classification : content.getClassification()) {
			if (classification.getClassifier().getName().toLowerCase().equals(getId())) {
				DataSetMember member = classification.getDataSetMember();
				String label = member!=null ? member.getDisplayName() : "";
				return new Model<String>(label);
			}
		}
		
		
		for (AttributeTemplate template : getAttributes(content)) {
			if (template.getAttribute().getName().toLowerCase().equals(getId())) {
				List<String> values = content.getAttributeValues(template.getAttribute());
				String label = "";
				for (String value : values) {
					if (!"".equals(label))
						label += ", ";
					label += value;
				}
				return new Model<String>(label);
			}
		}
		
		return new Model<String>("err");
	}

	protected List<AttributeTemplate> getAttributes(Content content) {
		return ((Content)content).getContentTemplate().getAttributes();
	}
}
