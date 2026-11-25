package kbee.pjsf.rag;
public class ReferencesRequest {

    private final String text;
    private final String llm;

    public ReferencesRequest(String text, String llm) {
        this.text = text;
        this.llm = llm;
    }

    public String getText() {
        return text;
    }

    public String getLlm() {
        return llm;
    }
}