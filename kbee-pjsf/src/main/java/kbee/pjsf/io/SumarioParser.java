package kbee.pjsf.io;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class SumarioParser {

    private static final Pattern SECTIONS = Pattern.compile(
        "(?:^|\\s+-\\s*|\\s+)" +
        "(CITAS|REFERENCIAS\\s+NORMATIVAS)\\s*:",
        Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE
    );

    private SumarioParser() {
    }

    public static ParsedSumario parse(String input) {

        if (input == null || input.isBlank()) {
            return new ParsedSumario(
                "",
                Collections.emptyList(),
                Collections.emptyList()
            );
        }

        Matcher matcher = SECTIONS.matcher(input);

        String texto = input.trim();

        List<String> citas = new ArrayList<>();
        List<String> referenciasNormativas = new ArrayList<>();

        String currentSection = null;
        int sectionStart = 0;
        boolean first = true;

        while (matcher.find()) {

            if (first) {
                texto = clean(
                    input.substring(0, matcher.start())
                );
                first = false;

            } else {
                String content = input.substring(
                    sectionStart,
                    matcher.start()
                );

                addSection(
                    currentSection,
                    content,
                    citas,
                    referenciasNormativas
                );
            }

            currentSection = matcher.group(1)
                .toUpperCase(Locale.ROOT)
                .replaceAll("\\s+", " ");

            sectionStart = matcher.end();
        }

        if (!first) {
            addSection(
                currentSection,
                input.substring(sectionStart),
                citas,
                referenciasNormativas
            );
        }

        return new ParsedSumario(
            texto,
            citas,
            referenciasNormativas
        );
    }

    private static void addSection(
            String section,
            String content,
            List<String> citas,
            List<String> referenciasNormativas) {

        if (section == null || content == null ||
                content.isBlank()) {
            return;
        }

        List<String> values = Arrays.stream(content.split(";"))
            .map(SumarioParser::clean)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());

        switch (section) {

            case "CITAS":
                citas.addAll(values);
                break;

            case "REFERENCIAS NORMATIVAS":
                referenciasNormativas.addAll(values);
                break;

            default:
                break;
        }
    }

    private static String clean(String value) {

        if (value == null) {
            return "";
        }

        return value.trim()
            .replaceAll("^[\\s;:.,-]+", "")
            .replaceAll("[\\s;]+$", "");
    }

    public static final class ParsedSumario {

        private final String texto;
        private final List<String> citas;
        private final List<String> referenciasNormativas;

        public ParsedSumario(
                String texto,
                List<String> citas,
                List<String> referenciasNormativas) {

            this.texto = texto;

            this.citas = Collections.unmodifiableList(
                new ArrayList<>(citas)
            );

            this.referenciasNormativas =
                Collections.unmodifiableList(
                    new ArrayList<>(referenciasNormativas)
                );
        }

        public String getTexto() {
            return texto;
        }

        public List<String> getCitas() {
            return citas;
        }

        public List<String> getReferenciasNormativas() {
            return referenciasNormativas;
        }
    }
}
