package com.novamens.kbee.wicket.editor;

import java.util.List;

import com.novamens.content.model.Classificable;
import com.novamens.content.model.Classification;

public interface ClassificableEditor<T extends Classificable> extends Editor<T> {
	public List<Classification> getClassification();
}
