package com.novamens.logging;



import javax.persistence.Entity;

import com.novamens.content.document.TreeFile;
import com.novamens.content.model.ObjectId;
import com.novamens.security.audit.AuditSet;

@Entity
public class TreeFileEvent extends AbstractObjectEvent {
			

		public TreeFileEvent() {
			super();
			setAuditSet(AuditSet.TREEFILE);
		}
		
		public TreeFileEvent(TreeFileEvent tree_file) {
			super();
			setTreeFile(tree_file);
			setAuditSet(AuditSet.TREEFILE);
		}
		
		public TreeFileEvent(TreeFile tree_file, String description) {
			super();
			setTreeFile(tree_file);
			setAuditSet(AuditSet.TREEFILE);
			setParameters(description);
		}
		

		
		public void setTreeFile(Object object) {
		
			if (object instanceof TreeFile) {
				
				TreeFile tf = (TreeFile) object;
				
				setKbeeClass(TreeFile.class.getSimpleName());
				
				setObjectId((new ObjectId(tf)).toString());

				setDomain(tf.getDomain());
				
				if (tf.getDomain()!=null)
					setDomainId((Long)(tf.getId()));
				
				String title= tf.getName(); 
				
				if ((title!=null) && (title.length()>255))
					title=title.substring(0, 252)+"...";
				setTitle(title);
			}
		}
		
		
		@Override
		public String toString() {
			return getAction()+ " | " + getTarget();
		}
		
		public String getTarget() {
			return getKbeeClass() + " - "  + getObjectId();
		}
		
		@Override
		public String getType() {
			return "TreeFile";
		}
}
