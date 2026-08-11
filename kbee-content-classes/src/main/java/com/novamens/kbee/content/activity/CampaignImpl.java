package com.novamens.kbee.content.activity;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import com.novamens.content.activity.Activity;
import com.novamens.content.activity.Campaign;

@Entity
@DiscriminatorValue(Activity.CAMPAIGN)
public class CampaignImpl extends KbeeActivity implements Campaign {

	private static final long serialVersionUID = 1902529347265522165L;

}
