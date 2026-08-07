package com.novamens.logging;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("EmailTemplateCreateEvent")
public class EmailTemplateCreateEvent extends EmailTemplateEvent {

}
