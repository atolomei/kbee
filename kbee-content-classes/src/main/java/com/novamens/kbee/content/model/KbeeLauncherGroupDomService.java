package com.novamens.kbee.content.model;

import com.novamens.content.model.LauncherGroup;
import com.novamens.kbee.content.service.KbeeDomService;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component @Scope("prototype")
public class KbeeLauncherGroupDomService extends KbeeDomService<KbeeLauncherGroup, LauncherGroup> {

}