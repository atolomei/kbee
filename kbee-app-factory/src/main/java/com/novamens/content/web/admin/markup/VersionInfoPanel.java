package com.novamens.content.web.admin.markup;

import com.novamens.kbee.wicket.markup.html.areainfo.AreaInfoPanel;
import com.novamens.kbee.wicket.markup.html.areainfo.GridInfoPanel;
import com.novamens.util.WildcardResourceResolver;
import com.novamens.wicket.util.BCElement;

import kbee.util.Tuple;
import kbee.web.console.AbstractConsole;

import org.apache.wicket.model.Model;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class VersionInfoPanel extends AbstractSystemInfoPanel {
			
	private static kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(VersionInfoPanel.class.getName());

	private static final long serialVersionUID = 1L;

	public VersionInfoPanel() {
		this("info-panel");
	}

	public VersionInfoPanel(String id) {
		super(id);
	}
	
	/**
	 * 
	 */
	public void onInitialize() {
		super.onInitialize();
		
 
		
		AreaInfoPanel area = new AreaInfoPanel("info");
		add(area);
		area.setSections(AreaInfoPanel.ONE_SECTION);
		area.setCss("col-lg-12");
		area.addPanel(new GridInfoPanel("element", this.versionInfo(), new Model<String>("JAR Version"), true));
	
		
	}
	
	protected BCElement getPageBCElement() {
		return new BCElement(new Model<String>("Version"));
	}


	private List<Tuple> versionInfo() {
		List<Tuple> data = new ArrayList<Tuple>();

		try {
			URL[] resources = new WildcardResourceResolver().getResources("META-INF/git.properties");
			for(URL url: resources) {
				Properties p=new Properties();
				try( InputStream inStream = url.openStream()) {
					p.load(inStream);

					String mainVersion = p.getProperty("git.build.version");
					String commit_time = p.getProperty("git.commit.time");
					String commitIdAbbrev = p.getProperty("git.commit.id.abbrev");

					String fullVersion = mainVersion + "(" + commit_time + "." + commitIdAbbrev + ")";
					data.add(new Tuple(p.getProperty("git.remote.origin.url"), fullVersion));
				}
			}
		
			
			Collections.sort(data, new Comparator<Tuple>() {
				@Override
				public int compare(Tuple o1, Tuple o2) {
					try {
						return o1.label.compareToIgnoreCase(o2.label);
					} catch (Exception e) {
						return 0;
					}
				}
				
			});
		
		} catch (Exception e) {
			logger.error(e);
			data.add(new Tuple( "Error ",  	e.getClass().getName() + ". " + e.getMessage()));
			
		}

		return data;
	}



	/**
	 * list files in the given directory and subdirs (with recursion)
	 * @param paths
	 * @return
	 */
	public List<File> getFiles(String paths) {
		List<File> filesList = new ArrayList<File>();
		for (final String path : paths.split(File.pathSeparator)) {
			final File file = new File(path);
			recurse(filesList, file);

		}
		return filesList;
	}

	private void recurse(List<File> filesList, File f) {
		if( !f.isDirectory()) {
				if(f.getName().endsWith(".jar"))
					filesList.add(f);
			return;
		}

		File list[] = f.listFiles();
		for (File file : list) {
			recurse(filesList, file);
		}
	}


	public boolean hasClassesInPackage(String jarPath, String packageName) throws IOException {
		JarFile jarFile = new JarFile(jarPath);
		Enumeration<JarEntry> e = jarFile.entries();
		while (e.hasMoreElements()) {
			JarEntry entry = (JarEntry)e.nextElement();

			String name = entry.getName();
			if(name.equals("git.properties"))
				return true;
		}
		return false;
	}

}
