package org.example.financemanagement.voice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class VoiceService {

    @Value("${openai.api.key}")
    private String apiKey;

    public String convertSpeechToText(MultipartFile file) throws IOException {

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(apiKey);
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body =
                new LinkedMultiValueMap<>();

        body.add("model", "gpt-4o-mini-transcribe");

        body.add(
                "file",
                new MultipartInputStreamFileResource(
                        file.getInputStream(),
                        file.getOriginalFilename()
                )
        );

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<TranscriptionResponse> response =
                restTemplate.postForEntity(
                        "https://api.openai.com/v1/audio/transcriptions",
                        request,
                        TranscriptionResponse.class
                );

        return response.getBody().getText();
    }
}