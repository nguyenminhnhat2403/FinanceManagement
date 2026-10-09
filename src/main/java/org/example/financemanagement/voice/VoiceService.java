package org.example.financemanagement.voice;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class VoiceService {

    public String convertSpeechToText(MultipartFile audioFile) {

        // Sau này gọi Speech-to-Text API ở đây

        return "mua cafe 30k";
    }
}