package com.novamens.kbee.vault;

import com.novamens.event.Event;
import com.novamens.event.EventListener;
import com.novamens.kbee.event.EvictCacheServiceEvent;
import com.novamens.metrics.SystemMetricsService;
import com.novamens.service.ServiceLocator;
import com.novamens.service.SystemService;
import com.novamens.util.KbeeRuntimeException;

import kbee.util.PropertiesFactory;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.vault.authentication.AppRoleAuthentication;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.RoleId;
import org.springframework.vault.authentication.AppRoleAuthenticationOptions.SecretId;
import org.springframework.vault.client.VaultClients;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.web.client.RestOperations;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

public class VaultService implements SystemService, EventListener  {
			
	static private kbee.util.logging.Logger logger = kbee.util.logging.Logger.getLogger(VaultService.class.getName());
	
    private VaultTemplate vaultTemplate=null;
    private String url;
    private String token;

    private boolean parametersChanged = false;

    public VaultService() {
    }

    
    public String encrypt(String keyID, String key) {
    	return encrypt(keyID, key, true);
    }
    
    public String encrypt(String keyID, String key, boolean countMetric) {
    	
    	if (keyID==null || key==null)
    		throw new IllegalArgumentException("KeyId and key must be non null");
    	
        if (keyID.startsWith("/"))
            keyID = keyID.substring(keyID.indexOf("/") + 1);

        String[] keySplit = keyID.split("/", 2);
        String path = keySplit[0];
        String keyName = keySplit[1];
        String result = getVaultTemplate().opsForTransit(path).encrypt(keyName, key);
        
    	//logger.debug("encrypt -> " + keyID + "	- " + key);
        
        if (countMetric) {
	        try {
	        	ServiceLocator.getService(SystemMetricsService.class).getMeterVaultEncrypt().mark();
	        } catch (Exception e) {
	        	logger.error(e);
	        }
        }
         return result;
         
    }

    public String decrypt(String keyID, String key) {
      
    	if (keyID.startsWith("/"))
            keyID = keyID.substring(keyID.indexOf("/") + 1);

        //logger.debug("decrypt -> " + keyID + "	- " + key);
        String[] keySplit = keyID.split("/", 2);
        String path = keySplit[0];
        String keyName = keySplit[1];
        
        String result;
        
        try {
            result = getVaultTemplate().opsForTransit(path).decrypt(keyName, key);
        	ServiceLocator.getService(SystemMetricsService.class).getMeterVaultDeEncrypt().mark();
        } 
        catch (Exception e) {
        	vaultTemplate = null;
        	logger.error(e);
        	throw e;
        }
        
        return result;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    
	@Override
	public boolean listen(Event event) {
		if (event instanceof EvictCacheServiceEvent)
			return true;
		return false;
	}
	
	@Override
	public void onEvent(Event event) {
		if (event instanceof EvictCacheServiceEvent) {
			vaultTemplate = null;
		}
	}
	
	
	
	
	/**

	<p>El uso normal del vault es como una hash table. secreto->valor
	cada secreto tiene un path done el primer termino del path se corresponde con un repositorio
	el transit es un repositorio especial que funciona para servicios
	en este caso el servicio pedido es de encriptacion
	donde especificas una clave que se configura en el proceso documentado de setup el vault
	entonces (transit/clave, string) retorna el string encriptado
	de hecho lo unico que hay en el repositorio del vault es esta clave kbee-kee
	de la que el vault podria manejar  rotaciones
	</p>
	
	kbee-key
	transit/kbee-key
	
	es el nombre asignado en vault a esa clave
	en el proceso de setup del vault

*/
	
	public String getRoleId() {
    	Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
		return properties.getProperty("vault.roleId");
	}
	
	
	
	/**
	 * 
	 

	el vault tiene varios metodos de autenticación
	uno de ellos esta pensado para aplicaciones
	este metodo de autenticacion para aplicaciones tiene dos parametros
	que son esos uno es el rol que tiene la aplicacion 
	rol que tiene asociados una serie de permisos 
	permisos que habilitan al kbee a consultar el backend transit
	y el secretid es una credencial para el kbee
	esos dos tokens se configuran en el proceso de setup

 */
	
	public String getSecretId() {
		Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
		return properties.getProperty("vault.secretId");
	}
	
	
	private VaultTemplate getVaultTemplate() {
        if (this.vaultTemplate == null || this.parametersChanged) {
            try {
        
            	Properties properties = PropertiesFactory.getInstance("kbee").getProperties();
            	String roleId = properties.getProperty("vault.roleId");
            	String secretId = properties.getProperty("vault.secretId");
                VaultEndpoint endpoint = VaultEndpoint.from(new URI(url));
                RestOperations restOperations = VaultClients.createRestTemplate(endpoint, new SimpleClientHttpRequestFactory());
                AppRoleAuthenticationOptions appRoleAuthenticationOptions = AppRoleAuthenticationOptions.builder()
                        .path(AppRoleAuthenticationOptions.DEFAULT_APPROLE_AUTHENTICATION_PATH)
                        .roleId(RoleId.provided(roleId))
                        .secretId(SecretId.provided(secretId))
                        .build();
                AppRoleAuthentication app =  new AppRoleAuthentication(appRoleAuthenticationOptions, restOperations);
                this.vaultTemplate = new VaultTemplate(endpoint, app);
            } 
            catch (URISyntaxException e) {
            	logger.error(e);
                throw new KbeeRuntimeException("VaultTemplate cannot be initialized", e);
            }
        }
        return vaultTemplate;
    }


	public String ping() {
		try {
			@SuppressWarnings("unused")
			String e=encrypt("transit/kbee-key", "kbee", false);
		return "ok";
		} catch (Exception e) {
			logger.error(e);
			return e.getClass().getName() + ". " + e.getMessage();
		}
	}

}