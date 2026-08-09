package kbee.web.model.util;

import org.apache.wicket.model.IModel;

import com.novamens.content.base.Content;
import com.novamens.content.base.Relation;
import com.novamens.content.model.RelationTemplate;
import com.novamens.kbee.content.model.KbeeRelation;

public class RelationModel implements IModel<Relation> {
	private static final long serialVersionUID = 1L;
	
	IModel<Content> contentmodel;
	private IModel<RelationTemplate> templatemodel;
	
	public RelationModel(IModel<Content> contentmodel, IModel<RelationTemplate> templatemodel) {
		this.contentmodel = contentmodel;
		this.templatemodel = templatemodel;
	}
	
	public void setObject(Relation relation) {
	}
	
	public Relation getObject() {
		KbeeRelation relation = new KbeeRelation();
		relation.setTarget(contentmodel.getObject());
		relation.setTemplate(templatemodel.getObject());
		return relation;
	}
	
	public void detach() {
		templatemodel.detach();
		contentmodel.detach();
	}
}