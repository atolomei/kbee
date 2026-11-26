package kbee.pjsf.rag;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DissentsResponse {

    private List<DissentSummary> summaries;
}