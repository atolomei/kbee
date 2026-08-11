package com.novamens.portal.model.diagrammablesite;

import java.io.Serializable;

import com.novamens.thumbnail.ThumbnailSize;

public class HitPanelConf  implements Serializable {
	
	private static final long serialVersionUID = -7868005868324367569L;
	
	public boolean description_enabled = false;
	public boolean thumbnail_enabled = false;
	public boolean title_visible = false;
	public boolean expanded = false;
	public boolean target_blank = false;
	public boolean useactual_image = false;
	public boolean menu_enabled = true;
	public boolean linkfromresource = false;
	public boolean title_datepublished = false;
	public boolean player=false;
	
	public boolean iconcss=false;
	
	public ThumbnailSize thumbnail_dimensions = ThumbnailSize.LARGE;

	public int description_length;
	public int thumbnail_size;
	public int thumbnail_pos;
	public String result_css;
	public String thumbnail_css;
	
	public int subtitle_mode;
	public boolean writeSessionUser = true;
	

	public boolean 		iswriteSessionUser() 		{return writeSessionUser;}
	public boolean 		isLinkFromResource() 		{return linkfromresource;}
	public boolean 		isDescriptionEnabled() 		{return description_enabled;}
	public boolean	  	isThumbnailEnabled()		{return thumbnail_enabled;}	
	public boolean 		isTitleVisible() 			{return title_visible;}
	public boolean 		isTitleDatePublished() 		{return title_datepublished;}
	public boolean 		isExpanded() 				{return expanded;}
	public boolean 		isTargetBlank() 			{return target_blank;}
	public boolean 		isUseActualImageEnabled()  	{return useactual_image;}
	
	public int 			getMaxDescriptionLength() 	{return description_length;}
	public int 			getThumbnailSize() 			{return thumbnail_size;}	
	public String 		getResultCss() 				{return result_css;}			  
	public int   		getThumbnailPos()			{return thumbnail_pos;}
	public int 			getSubtitleMode()  			{return subtitle_mode;}
	
	public ThumbnailSize getThumbnailDimensions()	{return thumbnail_dimensions;}

	public boolean 		 isMenuEnabled() 			{return menu_enabled;}
	public boolean		isPlayerVisible() 			{return player;}
	
	public boolean		isIconCss() 				{return iconcss;}
	
	public String getThumbnailCss() 				{return this.thumbnail_css;}

}
