package kbee.pjsf.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

public class RagClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String authorization;

    public RagClient(String baseUrl, String username, String password) {

        this.baseUrl = baseUrl.replaceAll("/+$", "");

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        this.objectMapper = new ObjectMapper();

        String credentials = username + ":" + password;

        this.authorization = "Basic " + Base64.getEncoder()
                .encodeToString(
                        credentials.getBytes(StandardCharsets.UTF_8)
                );
    }

    public DocumentVoicesResponse getDocumentVoices(String documentId)
            throws IOException, InterruptedException {

        String encodedId = URLEncoder.encode(
                documentId, StandardCharsets.UTF_8
        );

        String url = baseUrl
                + "/api/documents/"
                + encodedId
                + "/voices";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(60))
                .header("Accept", "application/json")
                .header("Authorization", authorization)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8)
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Error consultando RAG. HTTP "
                    + response.statusCode()
                    + ": " + response.body()
            );
        }

        return objectMapper.readValue(
                response.body(),
                DocumentVoicesResponse.class
        );
    }
    
    public List<VoicePath> equivalentVoices(String voice)
            throws IOException, InterruptedException {
    	
        String text = trimVoiceHierarchy(voice, 2);

        String url = baseUrl + "/api/thesaurus/equivalence";

        String json = objectMapper.writeValueAsString(
                new EquivalenteVoiceRequest(text, "ollama")
        );
        

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(120))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(
                        json, StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Error consultando equivalencia de voces. HTTP "
                    + response.statusCode()
                    + ": " + response.body()
            );
        }

        return objectMapper.readValue(
                response.body(),
                new TypeReference<List<VoicePath>>() {}
        );
    }
    
    public String summaryTitle(String summaryText)
            throws IOException, InterruptedException {
    	

        String url = baseUrl + "/api/judicial/title";

        String json = objectMapper.writeValueAsString(
                new TitleRequest(summaryText, "ollama")
        );
        

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(120))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(
                        json, StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Error consultando equivalencia de voces. HTTP "
                    + response.statusCode()
                    + ": " + response.body()
            );
        }
        
        String title = response.body();

        return title;
    }
    
    public String writeSummary(String text)
            throws IOException, InterruptedException {
    	

        String url = baseUrl + "/api/judicial/summary";

        String json = objectMapper.writeValueAsString(
                new TitleRequest(text, "ollama")
        );
        

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(120))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(
                        json, StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Error consultando equivalencia de voces. HTTP "
                    + response.statusCode()
                    + ": " + response.body()
            );
        }
        
        String summary = response.body();

        return summary;
    }
    
    public DocumentReferencesResponse getReferences(String documentText)
            throws IOException, InterruptedException {

        String url = baseUrl + "/api/judicial/references";

        String json = objectMapper.writeValueAsString(
                new ReferencesRequest(documentText, "ollama")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(180))
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", authorization)
                .POST(HttpRequest.BodyPublishers.ofString(
                        json, StandardCharsets.UTF_8
                ))
                .build();

        HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString(
                        StandardCharsets.UTF_8
                )
        );

        if (response.statusCode() != 200) {
            throw new IOException(
                    "Error extrayendo referencias jurídicas. HTTP "
                            + response.statusCode()
                            + ": " + response.body()
            );
        }

        return objectMapper.readValue(
                response.body(),
                DocumentReferencesResponse.class
        );
    }
    
    private String trimVoiceHierarchy(String voice, int maxLevels) {

        if (voice == null || voice.isBlank()) {
            throw new IllegalArgumentException("La voz no puede estar vacía");
        }

        if (maxLevels < 1) {
            throw new IllegalArgumentException("maxLevels debe ser mayor que cero");
        }

        String[] levels = voice.split(">");

        int start = Math.max(0, levels.length - maxLevels);

        return java.util.stream.IntStream
                .range(start, levels.length)
                .mapToObj(i -> levels[i].trim())
                .collect(java.util.stream.Collectors.joining(" > "));
    }

    public List<VoicePath> getVoices(String documentId)
            throws IOException, InterruptedException {

        return getDocumentVoices(documentId).getVoices();
    }
}