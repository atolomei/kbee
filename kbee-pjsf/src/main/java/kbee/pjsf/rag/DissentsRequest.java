package kbee.pjsf.rag;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DissentsRequest {
    private final String text;
    private final String llm;
}