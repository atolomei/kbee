package kbee.pjsf.io;

import java.util.List;

public interface ApiFileParser {

    List<TextFile> parse(
            ApiFile file
    );
}