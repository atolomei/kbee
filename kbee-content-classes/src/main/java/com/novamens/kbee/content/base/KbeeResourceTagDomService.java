package com.novamens.kbee.content.base;

import com.novamens.content.base.ResourceTag;
import com.novamens.kbee.content.service.KbeeDomService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component @Scope("prototype")
public class KbeeResourceTagDomService extends KbeeDomService<KbeeResourceTag, ResourceTag> {

}