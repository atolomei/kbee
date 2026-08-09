package kbee.web.editor;

import java.util.List;

import com.novamens.content.model.AttributeTemplate;
import com.novamens.content.model.Classifier;
import com.novamens.content.model.DataSetMember;
import com.novamens.content.model.ModelElementTemplate;
import com.novamens.wicket.markup.html.editor.ObjectEditorPanel;

@SuppressWarnings("serial")
public class MemberClassificationEditor extends ObjectEditorPanel<DataSetMember> {
	private static final long serialVersionUID = 1L;
 
	public MemberClassificationEditor() {
			this(false);
	}
	
	public MemberClassificationEditor(boolean isReadOnly) {
		super("classification");
		
		setOutputMarkupId(true);
		setReadOnly(isReadOnly);
		
		add(new ClassificationEditor<DataSetMember>(isReadOnly()) {
			@Override
			public List<Classifier> getClassifiers() {
				return ((MemberClassificationEditor.this.getModelObject()).getDataSet()).getClassifiers();
			}
			@Override
			public List<AttributeTemplate> getAttributes() {
				return ((MemberClassificationEditor.this.getModelObject()).getDataSet()).getAttributes();
			}
			@Override
			public List<ModelElementTemplate> getStructure() {
				return ((MemberClassificationEditor.this.getModelObject()).getDataSet()).getStructure();
			}

		});
	}
}
