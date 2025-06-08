package pl.piomin.services.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ChatConfig {
//
//    @Bean
//    public ChatMemory chatMemory(InMemoryChatMemoryRepository repository) {
//        // Hata veren kısım burasıydı: new MessageWindowChatMemory.Builder()
//        // DOĞRU KULLANIM: MessageWindowChatMemory.builder()
//        return MessageWindowChatMemory.builder() // Statik builder() metodunu kullan
//                .chatMemoryRepository(repository)
//                .maxMessages(20)
//                .build();
//    }
//
//    @Bean
//    public InMemoryChatMemoryRepository inMemoryChatMemoryRepository() {
//        return new InMemoryChatMemoryRepository();
//    }
}