package com.program.spring.ai.Controller;

import com.program.spring.ai.Model.Author;
import com.program.spring.ai.Model.Sport;
import org.apache.catalina.User;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.ai.converter.ListOutputConverter;
import org.springframework.ai.converter.MapOutputConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    private final ChatClient chatClient;

    @Value("classpath:prompts/userPrompt.st")
    public Resource userPrompt;

    @Value("classpath:prompts/olympic_list.st")
    public Resource olympicList;

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

    @GetMapping("/ai/songs")
    public List<String> getSongsInfo(@RequestParam String artist){
        String userMessage = """
                Get a list of 5 top songs by the artist {artist} in a given format {format}.
                """;

        ListOutputConverter outputConverter = new ListOutputConverter(new DefaultConversionService());

        PromptTemplate template = PromptTemplate.builder()
                .template(userMessage)
                .variables(Map.of("format", outputConverter.getFormat(),"artist",artist))
                .build();

        Prompt prompt = template.create();
        ChatResponse res =  chatClient
                .prompt(prompt)
                .call()
                .chatResponse();

        return outputConverter.convert(res.getResult().getOutput().getText());
    }

    @GetMapping("/ai/books")
    public Map<String,Object> getBookInfo(@RequestParam String author){
        String userMsg = """
                Get me a top 10 books written by the author {author} returning in a given format {format} 
                with book name as key the details like published year, genre and short description as value.
                """;

        MapOutputConverter outputConverter = new MapOutputConverter();

        PromptTemplate template = PromptTemplate.builder()
                .template(userMsg)
                .variables(Map.of("format", outputConverter.getFormat(), "author", author))
                .build();

        Prompt prompt = template.create();
        Generation result = chatClient.prompt(prompt).call().chatResponse().getResult();
        return outputConverter.convert(result.getOutput().getText());
    }

    @GetMapping("/ai/books/by-topic")
    public List<Author> getAuthorByTopic(@RequestParam String topic){
        String userMsg = """
                Give me top 10 details about famous author who wrote books on the given topic {topic}
                along with their list of other top 5 book titles that author wrote in a given format {format}. 
                """;

        BeanOutputConverter<List<Author>> converter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<Author>>(){});
        PromptTemplate template = PromptTemplate.builder()
                .template(userMsg)
                .variables(Map.of("format", converter.getFormat(),"topic", topic))
                .build();

        Prompt prompt = template.create();
        Generation result = chatClient.prompt(prompt).call().chatResponse().getResult();
        return converter.convert(result.getOutput().getText());
    }

    @GetMapping("/olympics/2026")
    public String getOlympicsDetails(
            @RequestParam(value = "query", defaultValue = "What are the sports included in Olympics 2026?") String query,
            @RequestParam(value = "stuf", defaultValue = "false") String stufRaw){

        // normalize inputs: strip surrounding quotes that clients mistakenly add
        String normalizedQuery = query == null ? "" : query.replace("\"", "").trim();
        String normalizedStuf = stufRaw == null ? "false" : stufRaw.replace("\"", "").trim();
        boolean stuf = Boolean.parseBoolean(normalizedStuf);

        PromptTemplate template = new PromptTemplate(userPrompt);
        Map<String,Object> map = new HashMap<>();
        map.put("question", normalizedQuery);
        if(stuf) {
            map.put("context", olympicList);
        } else {
            map.put("context", " ");
        }
        Prompt prompt = template.create(map);
        Generation result = chatClient.prompt(prompt).call().chatResponse().getResult();
        return result.getOutput().getText();
    }
}
