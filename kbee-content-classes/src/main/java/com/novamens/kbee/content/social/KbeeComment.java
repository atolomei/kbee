package com.novamens.kbee.content.social;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.novamens.content.base.Content;
import com.novamens.content.model.ContentTemplate;
import com.novamens.content.social.Comment;

import com.novamens.kbee.content.base.KbeeContent;
import com.novamens.kbee.security.KbeeUser;
import com.novamens.security.User;

@Entity
@PrimaryKeyJoinColumn(name="content_id")
@Table(name = "kb_comment")
public class KbeeComment extends KbeeContent implements Comment {

	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeComment.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "parent_comment", updatable=true, insertable=true, nullable=true)
	private Comment parent_comment;
 
	@Column(name = "isfirstlevel")
	private boolean isfirstlevel=true;
	
	/** User that edited the comment for the last time 
	*/
	@ManyToOne(fetch = FetchType.LAZY, targetEntity = KbeeUser.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "user_id", updatable=false)
	private User user;
 	
	
	/** This is the Content Id (not the OId)
	    The DAO is responsible for retrieving all comments from all versions of this content whenever necessary.
	*/
	@ManyToOne(fetch = FetchType.EAGER, targetEntity = KbeeContent.class)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(name = "referenced_content_id", updatable=false)
	private Content content;

	@Column(name = "date_submitted")
	private OffsetDateTime date_submitted;

	@Column(name = "text")
	private String text;
	
	@Column(name = "site_id")
	private Long site_oid;
	
	public KbeeComment(Content content, String text) {
		super();
		this.content=content;
		this.text=text;
	}
	
	public KbeeComment(ContentTemplate ct) {
		super(ct);
	}
	
	@Override
	public boolean isFirstLevel() {
		return isfirstlevel;
 	}
	
	@Override
	public void setParent(Comment comment) {
 		parent_comment=comment;
 		isfirstlevel=(comment==null);
	}

	@Override
	public Comment getParent() {
 		return parent_comment;
	}

 	public KbeeComment() {
		super();
	}
	
	public boolean isEditable() {
		return content.isCommentsEnabled();
	}
	
	@Override
	public void setReferencedContent(Content content) {
		this.content=content;
	}

	@Override
	public Content getReferencedContent() {
		return content;
	}

	@Override
	public void setText(String text) {
		this.text=text;
	}

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void setUser(User user) {
		this.user = user;
	}

	@Override
	public User getUser() {
		return user;
	}

	public String toString() {
		return text;
	}
	
	@Override
	public OffsetDateTime getDateSubmitted() {
		return date_submitted;
	}

	@Override
	public void setDateSubmitted(OffsetDateTime date) {
		this.date_submitted=date;
	}
	
	@Override
	public void  setSiteOId(Long site_oid) {
		this.site_oid=site_oid;
	}

	@Override
	public Long getSiteOId() {
		return this.site_oid;
	}
	
	@Override
	public int getLevel() {
		if (isFirstLevel())
			return 0;
		else return 1;
	}
	

}
