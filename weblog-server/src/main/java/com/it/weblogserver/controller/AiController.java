package com.it.weblogserver.controller;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Autowired
    ChatClient chatClient;

    @RequestMapping("/chat/{chatId}/{prompt}")
    public Flux<String> chat(@PathVariable String prompt,@PathVariable int chatId){
        return chatClient.prompt()
                .user(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
    }

    @RequestMapping("/chat/hi")
    public String sayHello(){
        return "hello!";
    }
}
