package kbee.pjsf.rag;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVoicesResponse {

    private String documentId;
    private List<VoicePath> voices;
    private List<String> unresolved;
}