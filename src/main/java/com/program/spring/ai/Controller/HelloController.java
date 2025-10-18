package com.program.spring.ai.Controller;

import com.program.spring.ai.Model.Sport;
import org.apache.catalina.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    private final ChatClient chatClient;

    public  HelloController(ChatClient.Builder builder){
        this.chatClient = builder.build();
    }
    @GetMapping("/ai/hello")
    public String getSpringAiHelloMsg(@RequestParam String msg){
        return chatClient
                .prompt(msg)
                .call().content();
    }

    @GetMapping("/ai/sports")
    public List<Sport> getSportsInfo(@RequestParam String name){

        BeanOutputConverter<List<Sport>> converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<Sport>>(){});
        String userMessage = """
                Give me a detailed information about famous players in that sport {name} with their achievements.
                {format}
                """;

        String systemMessage = """
                You are a sports assistant. Give details of the sport that the user asked. 
                If you dont know, just reply it politely that you are not aware of it.
                """;

      PromptTemplate template = PromptTemplate.builder()
              .template(userMessage)
              .variables(Map.of("format", converter.getFormat(),"name", name))
                .build();
        UserMessage usermsg = new UserMessage(template.render());
        SystemMessage systemMsg = new SystemMessage(systemMessage);
        Prompt prompt = new Prompt(usermsg,systemMsg);

        Generation result = chatClient.prompt(prompt)
                .call()
                .chatResponse()
                .getResult();

        return converter.convert(result.getOutput().getText());
    }
}
