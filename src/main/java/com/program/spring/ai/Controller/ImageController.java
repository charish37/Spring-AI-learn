package com.program.spring.ai.Controller;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.image.*;
import org.springframework.ai.openai.OpenAiAudioSpeechModel;
import org.springframework.ai.openai.OpenAiAudioSpeechOptions;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.speech.SpeechPrompt;
import org.springframework.ai.openai.audio.speech.SpeechResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ImageController {
    private final ChatModel chatModel;
    private final ImageModel imageModel;
    private final OpenAiAudioTranscriptionModel audioModel;
    private final OpenAiAudioSpeechModel speechModel;

    public ImageController(ChatModel chatModel, ImageModel imageModel, OpenAiAudioTranscriptionModel audioModel, OpenAiAudioSpeechModel speechModel) {
        this.imageModel = imageModel;
        this.chatModel = chatModel;
        this.audioModel = audioModel;
        this.speechModel = speechModel;
    }

    @GetMapping("/ai/image-model")
    public String getImageInfo(){
        String response = ChatClient.create(chatModel)
                .prompt()
                .user(
                        userSpec -> userSpec
                                .text("Explain what you see in the image.")
                                .media(MimeTypeUtils.IMAGE_JPEG,new ClassPathResource("images/ai-image.jpg"))
                )
                .call()
                .content();
        return response;
    }

    @GetMapping("/ai/image/{prompt}")
    public String generateImage(@PathVariable String prompt){
        ImageResponse vivid = imageModel.call(new ImagePrompt(prompt, ImageOptionsBuilder.builder()
                .height(1024)
                .width(1024)
                .N(1)
                .style("vivid")
                .build()));

        return vivid.getResult().getOutput().getUrl();
    }

    @GetMapping("/ai/audio-to-text")
    public String generateSpeechFromAudio(){

        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions
                .builder()
                .language("telugu")
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .temperature(0.5f)
                .build();
        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(new ClassPathResource("audio/sample-ai-test.m4a"), options);

        return audioModel
                .call(prompt).getResult().getOutput();

    }

    @GetMapping("/ai/text-to-audio/{msg}")
    public ResponseEntity<Resource> generateAudioFromText(@PathVariable String msg ){
        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .model(OpenAiAudioApi.TtsModel.TTS_1.getValue())
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .voice(OpenAiAudioApi.SpeechRequest.Voice.NOVA.getValue())
                .speed(1.0F).build();

        SpeechPrompt prompt = new SpeechPrompt(msg, options);

        SpeechResponse response = speechModel.call(prompt);

        byte[] output = response.getResult().getOutput();

        ByteArrayResource res = new ByteArrayResource(output);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(res.contentLength())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename("output.mp3")
                        .build().toString())
                .body(res);
    }
}
