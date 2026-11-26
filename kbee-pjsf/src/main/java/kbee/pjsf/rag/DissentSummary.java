package kbee.pjsf.rag;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DissentSummary {

    private String title;
    private String text;
    private List<String> judges;
    private String evidence;
}