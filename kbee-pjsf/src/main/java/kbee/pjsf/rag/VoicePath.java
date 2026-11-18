package kbee.pjsf.rag;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoicePath {

    private String id;
    private String preferredLabel;
    private List<VoiceNode> path;
}