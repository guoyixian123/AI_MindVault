package org.example.major_ai.controller;

import dev.langchain4j.model.openai.OpenAiChatModel;
import org.example.major_ai.aiservice.ConsoultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
public class ChatController {
    @Autowired
    private ConsoultantService consoultantService;

    @RequestMapping(value = "/chat",produces = "text/html;charset=utf-8")
    public Flux<String> message(String memoryId,String message){
        Flux<String> chat = consoultantService.chat(memoryId,message);
        return chat;
    }
}
