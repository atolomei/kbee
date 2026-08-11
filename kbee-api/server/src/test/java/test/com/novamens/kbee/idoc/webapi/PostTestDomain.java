package test.com.novamens.kbee.idoc.webapi;

import com.google.gson.Gson;
import com.novamens.kbee.domain.KbeeDomain;
import com.novamens.kbee.security.KbeeUser;
import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.LogManager;

import org.junit.Test;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class PostTestDomain {

    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(PostTestDomain.class.getName());

    String createDomainUrl = "http://booksapi-stg.realpage.com/companyinstance";
    String getUrl = "http://booksapi-stg.realpage.com/companyinstance/%d";

    @Test
    public void NewDomainCreation() {
        try {


            KbeeDomain domain = createDummyDomain();
            //domain.setId(5l);
            ResponseEntity<DomainData> response;
            try {
                response = sendDomain(createDomainUrl, domain, HttpMethod.POST);
            } catch (HttpClientErrorException e) {
                // System.out.println("Domain already exists, trying to update information.");
                response = sendDomain(createDomainUrl, domain, HttpMethod.PUT);
            }

            // System.out.println(response.getBody());
            // System.out.println(response.getStatusCode().value());
        } catch (HttpClientErrorException e) {
            // System.out.println(e.getResponseBodyAsString());
            // System.out.println(e.getMessage());
        } catch (HttpServerErrorException e) {
            // System.out.println(e.getResponseBodyAsString());
            // System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            // System.out.println(e.getMessage());
        }
    }


    protected KbeeDomain createDummyDomain() {
        KbeeDomain domain = new KbeeDomain();

        domain.setName("Prueba " + new Date().getTime());
        domain.setEncryptFiles(true);
        domain.setId(new Date().getTime());
        domain.setExternalId(domain.getId()+"99");
        domain.setAddress("Dallas");
        domain.setLastModifiedUser(new KbeeUser() {{
            setFirstName("German");
            setLastName("Rodriguez");
        }});
        return domain;
    }


    protected ResponseEntity<DomainData> sendDomain(String uri, KbeeDomain domain, HttpMethod method) {
        DomainData request = mapDomainToRequest(domain);
        RestTemplate restTemplate = new RestTemplate();

        Gson gson = new Gson();

        logger.debug("Sending domain synchronize message: " + gson.toJson(request));

        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
        converters.add(new GsonHttpMessageConverter());
        restTemplate.setMessageConverters(converters);

        ResponseEntity<DomainData> response = restTemplate.exchange(uri, method, new HttpEntity<>(request, getHeaders()), DomainData.class);
        logger.debug("Received synchronize response: " + gson.toJson(response.getBody()));
        return response;
    }

/*

 */
    private DomainData mapDomainToRequest(KbeeDomain domain) {
        DomainData.Data.Attributes attributes = new DomainData.Data.Attributes();

        attributes.setSource("DOC");
        //id
        attributes.setActive(domain.isEnabled());
        //address
        //attributes.setPhoneNumber(domain.getPhoneNumber);
        //attributes.setWebsite
        //website
        //name
        //organization
        //externalid
        attributes.setCompanyName(domain.getName());

        attributes.setCompanyInstanceSourceId(domain.getId().toString());
        DomainData.Data.Attributes.CompanyInstancePartner companyInstancePartner = new DomainData.Data.Attributes.CompanyInstancePartner();
        companyInstancePartner.setTargetCompanyInstanceSourceId(domain.getExternalId());
        companyInstancePartner.setTargetSource("OS");

        attributes.setCompanyInstancePartners(Arrays.asList(companyInstancePartner));


        attributes.setCreatedBy(domain.getLastModifiedUser().getFirstLastName()); //GR ver que nombre enviamos
        DomainData.Data data = new DomainData.Data();
        data.setAttributes(attributes);

        data.setId(domain.getId());
        DomainData newDomain = new DomainData();
        newDomain.setData(data);
        return newDomain;
    }


    private HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + getCredentials());
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private String getCredentials() {
        //	String plainCredentials="root@demoapi:1Aqqqqqq";
        //	String plainCredentials="root@windsor:w1nds0rR00t";
        String plainCredentials = "root@kbee:id0cB4sic";
        //	String plainCredentials="root@kbee:kbeeR00t";
        String base64Credentials = new String(Base64.encodeBase64(plainCredentials.getBytes()));
        return base64Credentials;
    }


    public static class DomainData {
        private Data data;

        public DomainData() {
        }


        public Data getData() {
            return data;
        }

        public void setData(Data data) {
            this.data = data;
        }

        public static class Data {
            private String type;
            private Attributes attributes;
            private Long id;

            public Data() {
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public Attributes getAttributes() {
                return attributes;
            }

            public void setAttributes(Attributes attributes) {
                this.attributes = attributes;
            }

            public Long getId() {
                return id;
            }

            public void setId(Long id) {
                this.id = id;
            }

            public static class Attributes {
                private String companyInstanceSourceId;
                private String source;
                private String companyName;
                private String phoneNumber;
                private boolean isActive;
                private String createdBy;
                private String modifiedBy;

                private List<CompanyInstancePartner> companyInstancePartners;

                public Attributes() {
                }

                public String getCompanyInstanceSourceId() {
                    return companyInstanceSourceId;
                }

                public void setCompanyInstanceSourceId(String companyInstanceSourceId) {
                    this.companyInstanceSourceId = companyInstanceSourceId;
                }

                public String getSource() {
                    return source;
                }

                public void setSource(String source) {
                    this.source = source;
                }

                public String getCompanyName() {
                    return companyName;
                }

                public void setCompanyName(String companyName) {
                    this.companyName = companyName;
                }

                public String getPhoneNumber() {
                    return phoneNumber;
                }

                public void setPhoneNumber(String phoneNumber) {
                    this.phoneNumber = phoneNumber;
                }

                public boolean isActive() {
                    return isActive;
                }

                public void setActive(boolean active) {
                    isActive = active;
                }

                public String getCreatedBy() {
                    return createdBy;
                }

                public void setCreatedBy(String createdBy) {
                    this.createdBy = createdBy;
                }

                public String getModifiedBy() {
                    return modifiedBy;
                }

                public void setModifiedBy(String modifiedBy) {
                    this.modifiedBy = modifiedBy;
                }

                public List<CompanyInstancePartner> getCompanyInstancePartners() {
                    return companyInstancePartners;
                }

                public void setCompanyInstancePartners(List<CompanyInstancePartner> companyInstancePartners) {
                    this.companyInstancePartners = companyInstancePartners;
                }

                public static class CompanyInstancePartner {
                    private String targetCompanyInstanceSourceId;
                    private String targetSource;

                    public CompanyInstancePartner() {
                    }

                    public String getTargetCompanyInstanceSourceId() {
                        return targetCompanyInstanceSourceId;
                    }

                    public void setTargetCompanyInstanceSourceId(String targetCompanyInstanceSourceId) {
                        this.targetCompanyInstanceSourceId = targetCompanyInstanceSourceId;
                    }

                    public String getTargetSource() {
                        return targetSource;
                    }

                    public void setTargetSource(String targetSource) {
                        this.targetSource = targetSource;
                    }
                }
            }


        }

    }


}
