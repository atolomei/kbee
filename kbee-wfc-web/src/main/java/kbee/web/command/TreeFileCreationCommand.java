package kbee.web.command;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.novamens.beans.BeansService;
import com.novamens.content.base.Content;
import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceContainer;
import com.novamens.content.dao.ContentDao;
import com.novamens.content.document.TreeFile;
import com.novamens.content.document.TreeFileDir;
import com.novamens.content.document.TreeFileKBFile;
import com.novamens.content.model.ObjectId;
import com.novamens.content.service.TreeFileService;
import com.novamens.dom.ObjectState;
import com.novamens.kbee.content.command.AsyncCommand;
import com.novamens.kbee.content.document.KbeeTreeFile;
import com.novamens.kbee.content.document.KbeeTreeFileDir;
import com.novamens.kbee.content.resource.KbeeTreeFileResource;
import com.novamens.security.User;
import com.novamens.service.SecurityService;
import com.novamens.service.ServiceLocator;
import com.novamens.transaction.Transaction;
import com.novamens.transaction.TransactionService;

import kbee.util.logging.Logger;

public class TreeFileCreationCommand extends AsyncCommand {

	private static Logger logger = Logger.getLogger(TreeFileCreationCommand.class.getName());

	Map<String, Number> metrics = new HashMap<String, Number>();
	int totalfiles = 0;
	Content content;
	
	protected void executeAsync() {
		
		Transaction transaction = null;
		
		try {
			com.novamens.hibernate.session.Session.open();
			
			ServiceLocator.getService(SecurityService.class).authenticate("root@aerolineas");

			transaction = beginTransaction();
			
//			for (int f=0; f<getTotalItems(); f++) {
//				synchronized (this) {
//					AtomicInteger files=((AtomicInteger) metrics.get("files"));
//					if (files==null) {
//						files = new AtomicInteger();
//						metrics.put("files", files);
//					}
//					files.addAndGet(1);
//					Thread.sleep(1000);
//				}
//			}
			
			Content content = getContent();
			
			TreeFile treefile = createTreeFile();
			
			treefile.getService(TreeFileService.class).addDirectory(getRootDirectory(), metrics);
			
			List<TreeFile> indexes = setIndexes(treefile);
			
			getContentDao().save(treefile);
			
			setResources(content, indexes);
			
			getContentDao().save(content);
			
			transaction.commit();
			
			indexes.forEach((index) -> getContentDao().refresh(index));;
			getContentDao().refresh(treefile);
			
			end();
		}
		catch (Exception e) {
			logger.error(e);
			stop();
			transaction.rollback();
		}
		finally {
			com.novamens.hibernate.session.Session.close();
		}	
	}
	
	@Override
	public double getProgress() {
		try {
			Number files = null;
			synchronized (this) {
				files = metrics.get("files");
			}
			return files!=null && files.intValue()>0 && getTotalItems()>0 ? files.doubleValue()/(double) getTotalItems() * 100 : 0.0;
		}
		catch (Exception e) {
			return 0;
		}
	}
	
	@Override
	public long getTotalItems() {
		if (totalfiles == 0) {
			totalfiles = getTotalFiles(getRootDirectory());
		}
		return totalfiles;
	}
	
	@Override
	public long getTotalItemsProcessed() {
		Number files = null;
		synchronized (this) {
			files = metrics.get("files");
		}
		return files!=null ? files.longValue() : 0;
	}
	
	protected List<TreeFile> setIndexes(TreeFile treefile) {
		List<TreeFile> indexes = new ArrayList<TreeFile>();
		for (File file : getIndexes()) {
			TreeFile index = setIndex(treefile, file);
			indexes.add(index);
		}
		return indexes;
	}
	
	protected TreeFile setIndex(TreeFile treefile, File file) {
		TreeFile index = null;
		for (TreeFile child : treefile.getChildren()) {
			if (child.getName().equals(file.getName())) {
				child.setAccessPoint(true);
				index = child;
				break;
			}
			else {
				setIndex(child, file);
			}
		}
		return index;
	}
	
	protected void setResources(Content content, List<TreeFile> indexes) {
		List<Resource> resources = new ArrayList<Resource>();
		for (TreeFile index : indexes) {
			if (index instanceof TreeFileKBFile) {
				KbeeTreeFileResource resource = new KbeeTreeFileResource((KbeeTreeFile)index);
				getContentDao().save(resource);
				resources.add(resource);
			}
		}
		((ResourceContainer)content).setResources(resources);
	}
	
	protected TreeFile createTreeFile() {
		
		TreeFileDir treefile = KbeeTreeFileDir.createRoot();
		
		treefile.setCreationOffsetDateTime(OffsetDateTime.now());
		treefile.setLastModifiedUser(getSessionUser());
		treefile.setState(ObjectState.ENABLED);
		treefile.setLastModifiedOffsetDateTime(OffsetDateTime.now());
		treefile.setPosition(0);
		
		treefile.setDomain(getContent().getDomain());

		getContentDao().saveTreeFile(treefile);
		
		return treefile;
	}
	
	protected List<File> getIndexes() {
		List<File> indexes = new ArrayList<File>();
		String value = (String)getParameter("indexes");
		StringTokenizer tokenizer = new StringTokenizer(value, ",");
		while (tokenizer.hasMoreTokens()) {
			File index = new File(tokenizer.nextToken().trim());
			indexes.add(index);
		}
		return indexes;
	}
	
	protected File getRootDirectory() {
		String path = (String)getParameter("folder");
		File file = new File(path);
		return file;
	}
	
	protected Content getContent() {
		ObjectId contentId = (ObjectId)getParameter("content");
		Content content = (Content)getContentDao().findObjectById(contentId);
		return content;
	}
	
	protected int getTotalFiles(File file) {
		if (!file.exists()) {
			return 0;
		}
		
		if (!file.isDirectory()) {
			return 1;
		}
		else {
			int total = 0;
			File childs[] = file.listFiles();
			for (int c=0; c<childs.length; c++) {
				total += getTotalFiles(childs[c]);
			}
		return total;
		}
	}
	
	protected Transaction beginTransaction()  {
		return ServiceLocator.getService(TransactionService.class).beginTransaction(false);
	}
	
	protected ContentDao getContentDao() {
		return (ContentDao)ServiceLocator.getService(BeansService.class).getBean("contentDao");
	}
	
	protected User getSessionUser() {
		return ServiceLocator.getService(SecurityService.class).getSessionUser();
	}
}
