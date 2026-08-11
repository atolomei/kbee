package com.novamens.kbee.portal.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.novamens.portal6.model.ViewBK;
import com.novamens.portal6.model.block.ListViewBlock;

@Entity
@PrimaryKeyJoinColumn(name = "po_id")
@Table(name = "PO_BLOCK_LISTVIEW")
public class KbeeBlockListView extends KbeeBlock implements ListViewBlock {

	/** Views are Deleted with the Block (CascadeType.ALL) **/
	@OneToMany(orphanRemoval=true, fetch = FetchType.LAZY, cascade=CascadeType.ALL, targetEntity = KbeeViewBK.class)
	@JoinColumn(name = "block_id", nullable=false) 
	@OrderColumn(name="list_position")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="content")
	List<ViewBK> views = new ArrayList<ViewBK>();

	
	public KbeeBlockListView() {
	}
	
	public KbeeBlockListView(String title) {
		super(title);
	}
	
	public void setDefaults() {
		super.setDefaults();
		
		if (getItems()!=null) {
			for (ViewBK view: getItems()) {
				if (view instanceof KbeeViewBK) {
					((KbeeViewBK) view).setDefaults();
				}
				else {
					if (view.getLastModifiedOffsetDateTime()==null)
						view.setLastModifiedOffsetDateTime(OffsetDateTime.now());
					
					if (view.getLastModifiedUser()==null)
						view.setLastModifiedUser(getSessionUser());
				}
			}
		}
	}
	
	@Override
	public void add(ViewBK view) {
		//view.setPosition(this.views.size());
		this.views.add(view);
	}
	
	@Override
	public void remove(ViewBK v) {
		this.views.remove(v);
		//int n=0;
		//for (ViewBK b: views)
		//	b.setPosition(n++);
	}
	
	@Override
	public List<ViewBK> getItems() {
		return this.views;
	}
	
	@Override
	public void setViews(List<ViewBK> li) {
		this.views=li;
	}
	
	
}
