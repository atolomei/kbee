package com.novamens.content.web.treefile.markup;

import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.novamens.content.document.TreeFile;
import com.novamens.content.web.nav.markup.GlobalNavigationBar;
import com.novamens.wicket.model.ObjectModel;

import kbee.web.error.ErrorPanel;
import kbee.web.page.AbstractApplicationPage;

public class TreeFilePage  extends AbstractApplicationPage<TreeFile> {
	private static final long serialVersionUID = 1L;

	public TreeFilePage(PageParameters parameters) {
		TreeFile treefile = getTreeFile(parameters);
		if (treefile!=null) {
			setTopNavigation(new GlobalNavigationBar<TreeFile>("navigation"));
			add(new TreeFileExplorer("editor", new ObjectModel<TreeFile>(treefile)));
		}
		else {
			add(new ErrorPanel("tree", "tree error", ""));
		}
	}
	
	public TreeFilePage(IModel<TreeFile> model) {
		setTopNavigation(new GlobalNavigationBar<TreeFile>("navigation"));
		add(new TreeFileExplorer("editor", model));
	}
	
	private TreeFile getTreeFile(PageParameters parameters) {
		TreeFile treefile = null;
		StringValue id = parameters.get("id");
		if (!id.isNull() && !id.isEmpty()) {
			treefile = getContentDao().findTreeFileById(id.toLong());
			if (treefile!=null && !treefile.getDomain().equals(getDomain())) {
				treefile = null;
			}
		}	
		return treefile;
	}
}
