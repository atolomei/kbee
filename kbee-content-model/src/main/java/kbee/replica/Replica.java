package kbee.replica;

import java.io.Serializable;

import com.novamens.dom.Domain;
import com.novamens.event.Event;

import kbee.api.service.ApiService;

public interface Replica  {
	public Serializable getId();
	public String getServer();
	public ReplicaType getType();
	public String getUser();
	public String getPassword();
	public Domain getDomain();
	public ApiService getApi();
	public void handle(Event event);
}