package com.novamens.content.web.markup;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import org.jsoup.Jsoup;


import com.novamens.content.base.Content;
import com.novamens.content.service.LabelsService;
import com.novamens.content.user.UserLabel;
import com.novamens.util.KbeeRuntimeException;


public class HightlightHitPanel extends Panel {
	
	public HightlightHitPanel(String id) {
		super(id);
		throw new KbeeRuntimeException ("deprecated in 6.3");
	}

	private static final long serialVersionUID = 1L;
	
	

}
