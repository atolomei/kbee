package kbee.pjsf.rag;
import java.util.List;

public class DocumentReferencesResponse {

    private String documentId;
    private List<LegalReference> jurisprudence;
    private List<LegalReference> regulations;

    public DocumentReferencesResponse() {
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public List<LegalReference> getJurisprudence() {
        return jurisprudence;
    }

    public void setJurisprudence(List<LegalReference> jurisprudence) {
        this.jurisprudence = jurisprudence;
    }

    public List<LegalReference> getRegulations() {
        return regulations;
    }

    public void setRegulations(List<LegalReference> regulations) {
        this.regulations = regulations;
    }
}