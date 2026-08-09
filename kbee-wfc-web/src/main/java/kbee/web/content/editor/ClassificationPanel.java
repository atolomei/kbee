package kbee.web.content.editor;

import java.io.Serializable;
import java.util.List;

import com.novamens.content.base.Content;
import com.novamens.content.model.Attribute;
import com.novamens.content.model.Classification;
import com.novamens.content.model.Classifier;

public interface ClassificationPanel<T extends Content> extends Serializable {
	public List<Classification> getClassification();
	public boolean includes(Classifier classifier);
	public boolean includes(Attribute attribute);
	public boolean isUpdated();
	public void update(T content);
	public void validate();
	public boolean isReadOnly();
	public List<String> getAttributeValue(Attribute attribute);
	public List<Classifier> getClassifiers();
}
