package kbee.pjsf.rag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegalReference {

    private String citation;
    private String evidence;

    public LegalReference() {
    }
}