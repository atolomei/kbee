package com.novamens.kbee.content.repository;

import org.springframework.stereotype.Component;


import com.novamens.content.library.Library;
import com.novamens.kbee.content.library.KbeeLibrary;
import com.novamens.kbee.repository.AbstractDomRepository;

@Component 
public class LibraryRepository extends AbstractDomRepository<KbeeLibrary, Library> {

}
