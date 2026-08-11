package com.novamens.kbee.content.form;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.novamens.content.form.EFormChoice;
import com.novamens.content.form.EFormComponent;
import com.novamens.content.form.EFormMultipleChoice;

@JsonTypeName("multiple choice")
public class KbeeEMultipleChoice extends EFormAbstractComponent implements EFormMultipleChoice {
	private static final long serialVersionUID = 1L;
	
	private String text;
	private List<EFormChoice> choices = new ArrayList<EFormChoice>();

	public KbeeEMultipleChoice() {
	}
	
	@Override
	public List<EFormChoice> getChoices() {
		return choices;
	}
	
	@Override
	public List<EFormComponent> getComponents() {
		List<EFormComponent> childs = new ArrayList<EFormComponent>();
		childs.addAll(getChoices());
		return childs;
	}
	
	public void setComponents(List<EFormComponent> components) {
	}
	
	public void addChoice(EFormChoice choice) {
		this.choices.add(choice);
	}
	
	
	public String getText() {
		return text;
	}
	
	public void setText(String label) {
		this.text = label;
	}
}