package pl.piomin.services.controller;

import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.spec.McpSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/persons")
@CrossOrigin(origins = "http://localhost:3000") 
public class PersonController {

    private final static Logger LOG = LoggerFactory.getLogger(PersonController.class);
    private final ChatClient chatClient;
    private final List<McpSyncClient> mcpSyncClients;
    private final ChatMemory chatMemory;
    
    private final List<Message> chatHistory = new ArrayList<>();

    public PersonController(ChatClient.Builder chatClientBuilder,
                            ToolCallbackProvider tools,
                            List<McpSyncClient> mcpSyncClients,ChatMemory chatMemory) {
        this.chatClient = chatClientBuilder
                .defaultToolCallbacks(tools)
                //.defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .build();
        this.mcpSyncClients = mcpSyncClients;
        this.chatMemory = chatMemory;
    }

    @GetMapping("/nationality/{nationality}")
    String findByNationality(@PathVariable String nationality) {

        PromptTemplate pt = new PromptTemplate("""
                Find persons with {nationality} nationality.
                """);
        Prompt p = pt.create(Map.of("nationality", nationality));
        return this.chatClient.prompt(p)
                .call()
                .content();
    }
    
    @GetMapping("/freeText/{text}")
    String freeText(@PathVariable String text) {

    	UserMessage userMessage = new UserMessage(text);
    	chatHistory.add(userMessage);
    	
        PromptTemplate pt = new PromptTemplate(text);
        Prompt p = pt.create();
        String llmResponseContent = this.chatClient.prompt(p)
        		.messages(chatHistory)
                .call()
                .content();
        
        AssistantMessage assistantMessage = new AssistantMessage(llmResponseContent);
        chatHistory.add(assistantMessage);
        
        return llmResponseContent;
    }

    @GetMapping("/count-by-nationality/{nationality}")
    String countByNationality(@PathVariable String nationality) {
        PromptTemplate pt = new PromptTemplate("""
                How many persons come from {nationality} ?
                """);
        Prompt p = pt.create(Map.of("nationality", nationality));
        return this.chatClient.prompt(p)
                .call()
                .content();
    }

    @GetMapping("/count-by-nationality-from-client/{nationality}")
    String countByNationalityFromClient(@PathVariable String nationality) {
        return this.chatClient.prompt(loadPromptByName("persons-by-nationality", nationality))
                .call()
                .content();
    }

    Prompt loadPromptByName(String name, String nationality) {
        McpSchema.GetPromptRequest r = new McpSchema.GetPromptRequest(name, Map.of("nationality", nationality));
        var client = mcpSyncClients.stream()
                .filter(c -> c.getServerInfo().name().equals("person-mcp-server"))
                .findFirst();
        if (client.isPresent()) {
            var content = (McpSchema.TextContent) client.get().getPrompt(r).messages().get(0).content();
            PromptTemplate pt = new PromptTemplate(content.text());
            Prompt p = pt.create(Map.of("nationality", nationality));
            LOG.info("Prompt: {}", p);
            return p;
        } else return null;
    }
}
