package com.novamens.kbee.portal.model;


import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;


import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.portal6.model.Block;
import com.novamens.portal6.model.ViewBKBlock;

@Entity
@Table(name = "PO_VIEWBKBLOCK")
@PrimaryKeyJoinColumn(name = "view_id")
@DynamicInsert
public class KbeeViewBKBlock extends KbeeViewBK implements ViewBKBlock {

	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeeBlock.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "block_id", nullable = true) // si borran el Block la View no se borra, queda apuntando a null.
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "entity")
	private Block block;

	@Override
	public KbeeViewBKBlock clone() {
		KbeeViewBKBlock clone = new KbeeViewBKBlock();
		onClone(clone);
		return clone;
	}

	public void onClone(KbeeViewBKBlock clone) {
		super.onClone((KbeeViewBK) clone);
		clone.setReferencedBlock(this.getReferencedBlock());
	}

	public KbeeViewBKBlock() {
	}

	

	public KbeeViewBKBlock(Block block) {
		setReferencedBlock(block);
	}

	public KbeeViewBKBlock(String title, Block block) {
		setTitle(title);
		setReferencedBlock(block);
	}

	@Override
	public Block getReferencedBlock() {
		return this.block;
	}

	@Override
	public void setReferencedBlock(Block block) {
		this.block = block;
	}

	@Override
	public String getViewType() {
		return KbeeViewBK.BLOCK_TYPE;
	}

	 

	@Override
	public String getTitle() {
		if (super.getTitle() != null)
			return super.getTitle();
		if (block == null)
			return "[block deleted]";
		return block.getTitle();
	}

	@Override
	public Object getObject() {
		return block;
	}

	

	@Override
	public String getMetadataAsString() {

		StringBuilder str = new StringBuilder();
		
		//if (this.getReferencedBlock() != null) {
		//	str.append(this.getReferencedBlock().getBlockTypeDisplayName() + ". ");
		//}
		
		str.append(getLastModifiedUser() != null ? getLastModifiedUser().getFirstLastName() + ". " : "");
		str.append(getLastModifiedOffsetDateTimeColloquial());
		return str.toString();

	}

	

	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(super.toString());
		str.append("\n" + getViewType());
		str.append("\nReferenced Block: " + (getReferencedBlock() != null ? getReferencedBlock().getTitle() : ""));
		return str.toString();
	}

	@Override
	public boolean isSearchable() {
		return false;
	}

	

}
