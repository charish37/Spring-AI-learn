package com.program.spring.ai.Controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.MimeType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ImageController {
    private final ChatModel chatModel;

    public ImageController(ChatModel chatModel){
        this.chatModel = chatModel;
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
}
