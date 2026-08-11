package com.novamens.kbee.content.webapi.type;

import java.util.ArrayList;
import java.util.List;

import com.novamens.content.base.Resource;
import com.novamens.kbee.content.workflow.KbeeActivityProgressNote;
import com.novamens.workflow.ActivityProgressNote;

import kbee.api.model.INote;
import kbee.api.model.ApiResource;

public class IProgressNoteAdapter implements Adapter<ActivityProgressNote, INote> {
	
	public IProgressNoteAdapter() {
	}
	
	public INote adapt(ActivityProgressNote note) {
		INote inote = new INote();
		inote.setId(String.valueOf(note.getId()));
		inote.setText(note.getText());
		inote.setAuthor(new ApiUserProxy(note.getLastModifiedUser()));
		inote.setTime(note.getLastModifiedOffsetDateTime());
		inote.setState(note.getState().name());
		List<ApiResource> resources = new ArrayList<>();
		for (Resource resource : ((KbeeActivityProgressNote)note).getResources()) {
			ApiResource iresource = (new IResourceAdapter()).adapt(resource);
			resources.add(iresource);
		}
		inote.setResources(resources);
		return inote;
	}
} 
