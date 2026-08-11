package com.novamens.kbee.content.service.datamanagement;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import com.novamens.content.base.Content;
import com.novamens.content.model.RelationTemplate;
import com.novamens.util.KbeeFileUtils;


public class KbeeRelationshipExporter extends KbeeBaseFileSystemExporter {
			

	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(KbeeRelationshipExporter.class.getName());

	String source_target;
	RelationTemplate relation;
	
	public KbeeRelationshipExporter(Serializable uid, RelationTemplate relation, String source_target) {
		super(uid);
		this.source_target= source_target;
		this.relation=relation;
	}

	
	
	@Override
	protected void exportResourceList(Content content, int index, String home_dir) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void exportAttributes(Content content, int index, String home_dir) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void exportAuditTrail(Content content, int index, String home_dir) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void exportCustomTags(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void exportNotes(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void exportPrivateNotes(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void exportText(Content content, int index, String content_dir) {
		// TODO Auto-generated method stub

	}
	
	@Override
	protected String getHomeResourcesDir(String home_dir) {
		return home_dir;
	}
	
	@Override
	public void export(Content content) {
		super.export(content, this.relation, this.source_target, -1);
	}
	
	
	@Override
	public void export(Content content, int index) {
		super.export(content, this.relation, this.source_target, index);
	}
	
	

}
