 package com.novamens.kbee.content.repository;

import org.springframework.stereotype.Component;

import com.novamens.kbee.content.workflow.KbeeActivityProgressNote;
import com.novamens.kbee.repository.AbstractDomRepository;
import com.novamens.workflow.ActivityProgressNote;

@Component 
public class SourceRepository extends AbstractDomRepository<KbeeActivityProgressNote, ActivityProgressNote> {

}
