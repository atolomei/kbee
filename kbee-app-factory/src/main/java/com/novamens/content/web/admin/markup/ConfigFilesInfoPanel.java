package com.novamens.content.web.admin.markup;

import java.util.ArrayList;
import java.util.List;



public class ConfigFilesInfoPanel extends FileSystemXplorerPanel {

	private static final long serialVersionUID = 1L;

	public ConfigFilesInfoPanel(String id) {
		super(id);

		List<String> extensions = new ArrayList<String>();
		extensions.add("*");
		/*
		extensions.add("log");
		extensions.add("txt");
		extensions.add("bat");
		extensions.add("xml");
		extensions.add("ini");
		extensions.add("sql");
		extensions.add("tar.gz");
		extensions.add("zip");
		*/
		setExtensions(extensions);
		
		
		List<String> dirs = new ArrayList<String>();
		dirs.add("resources");
		dirs.add("config");
		dirs.add("build");
		dirs.add("index");
		dirs.add("webapps/root/WEB-INF");
		
		setDirs(dirs);
		
	}

}
