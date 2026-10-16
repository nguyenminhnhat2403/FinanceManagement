//package org.example.financemanagement.voice;
//
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//
//@RestController
//@RequestMapping("/api/users")
//public class VoiceController {
//
//    private final VoiceService voiceService;
//
//    public VoiceController(VoiceService voiceService) {
//        this.voiceService = voiceService;
//    }
//
//    @PostMapping("/voice/transcribe")
//    public String transcribe(
//            @RequestParam("file") MultipartFile file
//    ) throws IOException {
//
//        return voiceService.convertSpeechToText(file);
//    }
//}
