package kbee.web.panel;

import java.time.Instant;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.model.IModel;

import com.novamens.kbee.wicket.markup.html.event.WicketEvent;

public class ClickItemEvent<T> implements WicketEvent {

	 IModel<T> model;
	 int index;
	 AjaxRequestTarget target;
		 
	 public ClickItemEvent(IModel<T> model, int index) {
		this.model=model;
		this.index=index;
	 }
	 
	 public ClickItemEvent(AjaxRequestTarget target, IModel<T> model, int index) {
		this.model=model;
		this.index=index;
		this.target = target;
	 }
	
	 public int getIndex() {
		 return index;
	 }
	
	 public  IModel<T> getModel() {
		return this.model;
	 }
	
	 public T getModelObject() {
		 return this.model.getObject();
	 }
	
	 
		@Override
		public Instant getTime() {
			return Instant.now();
		}

	 public AjaxRequestTarget getTarget() {
		return target;
	 }

	 public void setTarget(AjaxRequestTarget target) {
		this.target = target;
	 }

	 @Override
	 public Object getObject() {
		 return model!=null?model.getObject():null;
	 }
}