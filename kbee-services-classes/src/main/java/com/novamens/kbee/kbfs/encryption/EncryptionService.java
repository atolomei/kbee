package com.novamens.kbee.kbfs.encryption;


import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novamens.beans.BeansService;
import com.novamens.kbee.kbfs.encryption.interfaces.StreamEncryptor;
import com.novamens.kbee.kbfs.encryption.interfaces.StreamEncryptorInfo;
import com.novamens.service.ServiceLocator;
import com.novamens.service.SystemService;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.PropertiesFactory;

import java.io.*;

public class EncryptionService implements SystemService {
			
   static private final String	fileEncryptorClass= PropertiesFactory.getInstance("kbee").getProperties().getProperty("com.novamens.kbee.fileEncryptorBean", "KbeeDefaultStreamEncryption");

   static private ObjectMapper mapper = new ObjectMapper();
   
   
   static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(EncryptionService.class.getName());

    public InputStream encryptStream(InputStream inputStream){
        try {
            StreamEncryptor streamEncryption = (StreamEncryptor) ServiceLocator.getService(BeansService.class).getBean(fileEncryptorClass);
            String key = streamEncryption.genNewKey();

            KbeeEncryptedInputStream kbeeEncryptedInputStream = streamEncryption.encrypt(inputStream, key);

            String jsonStreamEncryptionInfo = mapper.writeValueAsString(kbeeEncryptedInputStream.getStreamEncryptorInfo());
            InputStream jsonStreamEncryptionInfoStream = new ByteArrayInputStream(jsonStreamEncryptionInfo.getBytes());

            return new SequenceInputStream(jsonStreamEncryptionInfoStream, kbeeEncryptedInputStream);
        } 
        catch ( JsonProcessingException e) {
        	logger.error(e);
            throw new KbeeRuntimeException(e);
        }
    }


    
    public InputStream decryptStream(InputStream inputStream){
        try {
            JsonFactory f = new MappingJsonFactory();
            f.disable(JsonParser.Feature.AUTO_CLOSE_SOURCE);
            JsonParser parser = f.createParser(inputStream);
            String json = parser.readValueAsTree().toString();
            									
            StreamEncryptorInfo streamEncryptionInfo  = mapper.readValue(json, StreamEncryptorInfo.class);
            String key = streamEncryptionInfo.getEncryptedKey();
            StreamEncryptor streamEncryption = streamEncryptionInfo.getStreamEncryption();

            ByteArrayOutputStream remainderOutputStream = new ByteArrayOutputStream();
            parser.releaseBuffered(remainderOutputStream);
            ByteArrayInputStream  remainderInputStream = new ByteArrayInputStream(remainderOutputStream.toByteArray());

            InputStream encryptedStream = new SequenceInputStream(remainderInputStream, inputStream);

            return streamEncryption.decrypt(encryptedStream, key);
        } catch (IOException  e) {
        	logger.error(e);
            throw new KbeeRuntimeException(e);
        }
    }

}
