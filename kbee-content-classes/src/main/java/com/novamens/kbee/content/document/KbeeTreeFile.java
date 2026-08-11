package com.novamens.kbee.content.document;


import java.io.IOException;
import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;

import javax.persistence.Table;
import javax.persistence.Transient;
import javax.swing.tree.TreeNode;

import org.apache.commons.collections4.iterators.IteratorEnumeration;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.springframework.util.Assert;

import com.novamens.content.base.Resource;
import com.novamens.content.base.ResourceTag;
import com.novamens.content.document.IDoc;
import com.novamens.content.document.TreeFile;
import com.novamens.dom.Domain;
import com.novamens.kbee.content.resource.AbstractResource;
import com.novamens.kbee.dom.AbstractObject;
import com.novamens.security.audit.AuditSet;
import com.novamens.util.KbeeRuntimeException;


/**
 * <p>A TreeFile is either a <b>Directory name</b>(String) or a <b>Reference</b> to a <@link KBFile}
 * In the first case it can also contain 0-N Children, each of them a TreeFile.</p>
 */

//@Cache(usage=CacheConcurrencyStrategy.READ_WRITE, region="entity")

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "TYPE", discriminatorType =  javax.persistence.DiscriminatorType.STRING)
@Table(name = "kb_tree_file")
@DynamicInsert
public abstract class KbeeTreeFile extends AbstractObject implements TreeFile {
																								
	private static kbee.util.logging.Logger kblogger = kbee.util.logging.Logger.getLogger(KbeeTreeFile.class.getName());
	
		@Id
		@GenericGenerator(
			name = "resource_sequencer",
			strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
			parameters = {
				@Parameter(name = "sequence_name", value = "resourceid_sequence"),
				@Parameter(name = "increment_size", value = "50"), // it must be same as other mapped classes
				@Parameter(name = "optimizer", value = "pooled-lo")
			}
		)
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "resource_sequencer")
		@Column(name = "id")
		private Long id;
	
	
		// Version control is not implemented for Tree File 
		@Column(name = "VERSION")
		private int version = 0;
		
		@Column(name = "ISHEAD")
		private boolean ishead = true;
		
		@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST,  targetEntity = AbstractResource.class)
		@Fetch(FetchMode.SELECT)
		@JoinColumn(name ="PREV_VERSION")
		private Resource previousVersion;

		/** 
		 * OId is the ObjectId (all versions share the same id)
		 *  Id is the version id 
		 **/
		@Column(name = "OID")
		private Long oid = null;
		
		@Override
		public Long getOId() {
			return oid;
		}
		
		public void setOId(Long d) {
			this.oid=d;
		}
		
		
		
		// TreeIDoc that owns this TreeFile
		// Strictly speaking this field is Redundant 
		@OneToOne(fetch = FetchType.LAZY, targetEntity = KbeeIDoc.class)
		@Fetch(FetchMode.SELECT)
		@JoinColumn(name = "tree_idoc_id", updatable=false)
		private IDoc tree_idoc;

		@Column(name = "title")
	 	private String title;

	 	@Column(name = "isaccesspoint")
	 	private boolean isaccesspoint = false;

		@Column(name = "type",  insertable=false, updatable=false)
	 	private String type;

		@Column(name = "position")
	 	private int position = 0;
		
	 	
	     /** The parent of this TreeFile can be null if this is the root TreeFile. */
		@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeTreeFile.class)
		@Fetch(FetchMode.SELECT)
		@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="content")
		@JoinColumn(name = "parent_id", updatable=true, nullable=true)
		private TreeFile parent;


	    /**
	     * The children TreeFile of this TreeFile.
	     * 
	     * This is the inverse side of the parent relation.
	     * 
	     * <strong>It is the children responsibility to manage there parents children set!</strong>
	     * 	    @Nonnull
	     */
		@OneToMany(fetch = FetchType.LAZY, targetEntity = KbeeTreeFile.class)
		@Fetch(FetchMode.SELECT)
		@JoinColumn(name = "parent_id", nullable=true)
		@OrderColumn(name="position")
	    public Set<TreeFile> children = new LinkedHashSet<TreeFile>();


		@Column(name = "in_portal")
		private boolean in_portal = true;	


		
		@Transient
		private boolean is_dirty = true;
		
		@Transient
		private long total_size = 0;

		@Transient
		private int total_count = 0;
		
		
		/**
	     * Used by Hibernate
	     */
	    KbeeTreeFile() {
	    }

	    /**
	     * Instantiates a new TreeFile.
	     * The domain will be of the same state like the parent domain.
	     *
	     * @param parent the parent TreeFile
	     * @see Domain#createRoot()
	     */
	    public KbeeTreeFile(final TreeFile parent) {
	        if(parent==null) throw new IllegalArgumentException("parent required");

	        this.parent = parent;
	        //this.is_dirty = true;
	        registerInParentsChilds();
	    }
	    				

	    @Override
		public Serializable getId() {
			return this.id;
		}
		

	    /**
	     * Type is DIR, KBFILE 
	     * defined by Hibernate based on the class of the subclass
	     * 
	     */
		@Override
		public String getType() {
			return this.type;
		}

		
		@Override
		public boolean isInPortalVersion() {
			return this.in_portal;
		}
		
		@Override
		public void setInPortalVersion(boolean b) {
			this.in_portal=b;
		}

	 
		
		@Override 
		public abstract boolean isDirectory();
		

		@Override 
		public boolean isAccessPoint() {
			return this.isaccesspoint;
		}


		@Override 
		public void setAccessPoint(boolean b) {
			this.isaccesspoint=b;
		}

 
		
	    @Override
		public void addTreeFileChild(TreeFile tree_file) {
			tree_file.setPosition(children.size()+1);
			children.add(tree_file);
	        this.is_dirty = true;
		}
		
		@Override
		public String getDisplayName() {
			if (getTitle()!=null)
				return getTitle();
			if (getName()!=null)
				return getName();
			return "TreeFile "+  getId()!=null?String.valueOf(getId()):"";
		}

	    /** Register this TreeFile in the child list of its parent. */
	    private void registerInParentsChilds() {
	        this.parent.getChildren().add((TreeFile) this);
	        this.is_dirty = true;
	    }

	    /**
	     * Return the <strong>unmodifiable</strong> children of this TreeFile.
	     * 
	     * @return the child nodes.
	     */
	    @Override
	    public Set<TreeFile> getChildren() {
	        return Collections.unmodifiableSet(this.children);
	    }        

	    /**
	     * Move this TreeFile to an new parent TreeFile.
	     *
	     * @param newParent the new parent
	     */
	    @Override
	    public void move(final TreeFile newParent)  {
	        								
	    	Assert.notNull(newParent, "newParent is null");

	        if (!isProperMoveTarget(newParent) /* detect circles... */ ) { 
	            throw new IllegalArgumentException("move not a proper new parent");
	        }

	        this.parent.getChildren().remove((TreeFile) this);
	        this.parent = newParent;
	        registerInParentsChilds();
	        this.is_dirty = true;

	    }

	    
	    @Override
	    public boolean isRoot() {
	    	return getLevel()==0;
	    }
	    

	    @Override
	    public int getLevel() {
	    	if (parent==null) {
	    			return 0;
	    	}
	    	if (kblogger.isDebugEnabled()) {
	    		if ((parent.getLevel()+1)>100)
	    			throw new KbeeRuntimeException ("Loop in TreeFile. Level>100");
	    	}
	    	return parent.getLevel()+1;
	    }
	    
		@Override
		public void setId(Serializable id) {
			this.id=Long.valueOf((String) id); 
		}

		@Override
		public String getTitle() {
			if (this.title!=null)
				return this.title;
			return null;
			
		}
			
		@Override
		public String getName() {
			if (getId()!=null)
				return "TreeFile - "+String.valueOf(this.getId());
			return "TreeFile";
		}
 

		@Override
		public int getPosition() {
			return this.position;
		}

		@Override
		public void setPosition(int pos) {
			this.position=pos;
		}

		@Override
		public TreeFile getParent()  {
	    	return this.parent;
	    			
	    }

		
		@Override
		public String getLastModifiedOffsetDateTimeColloquial(String classago) {
			return super.getLastModifiedOffsetDateTimeColloquial(classago);
		}

		@Override
		public String getFontAwesomeFreeIcon() {
			return "fal fa-folder";
		}

		
		public int getTotalNodes() {
	        if (!this.is_dirty)
	        	return this.total_count;
	        calculate();
			return this.total_count; 
		}

		public long getTotalSize() {
	        if (!this.is_dirty)
	        	return this.total_size;
	        calculate();
	        return this.total_size;
		}

		private synchronized void calculate() {
	        this.total_count=countNodes(this);
	        this.total_size=sizeNodes(this);
	        this.is_dirty=false;
		}

		private int countNodes(TreeFile tf) {
			int total=1;
			for (TreeFile son: tf.getChildren())
				total+=countNodes(son);
			return total;
		}

		private long sizeNodes(TreeFile tf) {
			if (!tf.isDirectory())  
				return tf.getSize();
			long size=0;
			for (TreeFile son: tf.getChildren())
				size+=sizeNodes(son);
			return size;
		}
		
		@Override
		public String getDescription() {
			return null;
		}

		@Override
		public void setTitle(String title) {
				this.title=title;
		}

		
		@Override
		public long getSize() {
			return 0;
		}

		@Override
		public String getUrl() {
			return null;
		}
		
		@Override
		public String getPath() {
			String name = getName();
			TreeFile parent = getParent();
			while (parent!=null && parent.getParent()!=null) {
				name = parent.getName() + "/" + name; 
				parent = parent.getParent();
			}
			if (parent==null) parent = this;
			name = "fs" + parent.getId() + "/" + name;
			return name;			
		}
		
		@Override
		public boolean isBinaryFile() throws IOException {
			return false;
		}
		
		@Override
		public boolean isPublicArea() {
			return true;
		}


		@Override
		public ResourceTag getGroup() {
			return null;
		}

		@Override
		public String getMetadataAsString() {
			return null;
		}

		@Override
		public String getMetadataAsString(DateTimeFormatter df) {
			return null;
		}

		@Override
		public String getGlyphIcon() {
			return "fal fa-folder-tree";
		}


		
		/**
		 * TODO !!!!!!!!!!!!!!!!!!!!!!
		 *  
		 * This check is unfinished
		 * 
		 * @param newParent
		 * @return
		 */
		
	    private boolean isProperMoveTarget(TreeFile newParent) {
			
	    	int total_1 = this.getTotalNodes();
	    	int total_2 = newParent.getTotalNodes();
	    	
	    	 Graph graph = new Graph(total_1+total_2); 

	    	 Integer node_index = Integer.valueOf(0);
	    	 addEdges(node_index, graph, newParent);

	    	 node_index++;
	    	 graph.addEdge(0,node_index.intValue());
	    	 
	    	 addEdges(node_index, graph, this);
	    	 
	    	 boolean b= graph.isCyclic();
	    	 kblogger.debug("isCyclic " + (b?"yes":"no"));
	    	 
	    	 kblogger.debug(graph.toString());
	    	
	    	return true;
	    	
		}

		
		private void addEdges(Integer node_index, Graph graph, TreeFile node) {
			int current = node_index.intValue();
			for (TreeFile tn: node.getChildren()) {
				int son_index = node_index++;
				addEdges(node_index, graph, tn);
		    	 graph.addEdge(current, son_index);
			}
		}
		

	    private class Graph { 
	        
	        private final int V; 
	        private final List<List<Integer>> adj; 

	        
	        /***
	         * 
	         * @param V is the total nodes
	         */
	        public Graph(int V)  
	        { 
	            this.V = V; 
	            adj = new ArrayList<>(V); 
	              
	            for (int i = 0; i < V; i++) 
	                adj.add(new LinkedList<>()); 
	        } 

	        public String toString() {
	        	StringBuilder str = new  StringBuilder();
	        	for (int n=0; n<adj.size();n++) {
	        		List<Integer> list = adj.get(n);
	        		str.append(String.valueOf(n) + " -> ");
	        		boolean isfirst = true;
	        		for (Integer in: list) {
	        			if (!isfirst) 
	        				str.append(", ");
	        			else
	        				isfirst=false;
	        			str.append(in.toString());
		        	}
	        		str.append("\n");
	        	}
	        	return str.toString();
	        }

	        
	        // This function is a variation of DFSUytil() in  
	        // https://www.geeksforgeeks.org/archives/18212 
	        private boolean isCyclicUtil(int i, boolean[] visited, boolean[] recStack)  
	        { 
	            // Mark the current node as visited and 
	            // part of recursion stack 
	            if (recStack[i]) 
	                return true; 
	      
	            if (visited[i]) 
	                return false; 
	                  
	            visited[i] = true; 
	      
	            recStack[i] = true; 
	            List<Integer> children = adj.get(i); 
	              
	            for (Integer c: children) 
	                if (isCyclicUtil(c, visited, recStack)) 
	                    return true; 
	                      
	            recStack[i] = false; 
	      
	            return false; 
	        } 
	      
	        private void addEdge(int source, int dest) { 
	            adj.get(source).add(dest); 
	        } 
	      
	        // Returns true if the graph contains a  
	        // cycle, else false. 
	        // This function is a variation of DFS() in  
	        // https://www.geeksforgeeks.org/archives/18212 
	        private boolean isCyclic()  
	        { 
	              
	            // Mark all the vertices as not visited and 
	            // not part of recursion stack 
	            boolean[] visited = new boolean[V]; 
	            boolean[] recStack = new boolean[V]; 
	              
	              
	            // Call the recursive helper function to 
	            // detect cycle in different DFS trees 
	            for (int i = 0; i < V; i++) 
	                if (isCyclicUtil(i, visited, recStack)) 
	                    return true; 
	      
	            return false; 
	        } 
	    } 	    

	    
	    
  	    
		@Override
		public TreeNode getChildAt(int childIndex) {

			if (getChildren()==null || childIndex<0 || childIndex>=getChildren().size())
				return null;
			
			int n = 0;
		    Iterator<TreeFile> it = getChildren().iterator();
		    while (it.hasNext()) {
		    	if (n++==childIndex)
		    		return it.next();
		    }
		    return null;
		}


		@Override
		public int getChildCount() {
			if (getChildren()==null)
				return 0;
			return getChildren().size();
		}


		@Override
		public int getIndex(TreeNode node) {

			if (node==null)
				throw new IllegalArgumentException("node is null");
			
			if (getChildCount()==0)
				return -1;
			
		    Iterator<TreeFile> it = getChildren().iterator();
		    int n=0;
		    while (it.hasNext()) {
		    	if (node.equals(it.next()))
		    		return n;
		    	n++;
		    }
		 	
	        throw new NoSuchElementException();
	         
		}


		@Override
		public boolean getAllowsChildren() {
			return false;
		}


		@Override
		public boolean isLeaf() {
			return getChildCount()==0;
		}

		
		@Override
		public Enumeration<? extends TreeNode> children() {
			if (getChildren()==null)
				return null;
			return new IteratorEnumeration<TreeFile>(getChildren().iterator());
 		}

		@Override
		public boolean equals(Object o) {
			if (o.getClass().equals(KbeeTreeFile.class))
				return ( (Long) ((KbeeTreeFile) o).getId()).equals( (Long) getId());
			return false;	
		}
		
		@Override
		public AuditSet getAuditSet() {
			return AuditSet.RESOURCE;
		}
		
		@Override
		public void setDescription(String des) {
			throw new KbeeRuntimeException("to do ");
			
		}

}
