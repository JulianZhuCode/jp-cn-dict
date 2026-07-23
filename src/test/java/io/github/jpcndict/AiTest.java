package io.github.jpcndict;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class AiTest {

    @Autowired
    private ChatModel chatModel;

    @Autowired
    private ChatClient chatClient;

    @Test
    void testChatModelInjection() {
        assertNotNull(chatModel, "ChatModel should be injected");
        log.info("ChatModel type: {}", chatModel.getClass().getName());
    }

    @Test
    void testChatClientInjection() {
        assertNotNull(chatClient, "ChatClient should be injected");
        log.info("ChatClient type: {}", chatClient.getClass().getName());
    }

    @Test
    void testAiResponse() {
        String prompt = "Hello, please respond with a short greeting in Japanese and Chinese.";
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        assertNotNull(response, "AI response should not be null");
        assertFalse(response.isEmpty(), "AI response should not be empty");
        log.info("Prompt: {}", prompt);
        log.info("AI Response: {}", response);
    }

    @Test
    void testJapaneseTranslation() {
        String prompt = "Translate '你好' to Japanese.";
        String response = chatClient.prompt()
                .user(prompt)
                .call()
                .content();

        assertNotNull(response, "AI response should not be null");
        assertTrue(response.contains("こんにちは") || response.contains("ハロー"), 
                "Response should contain Japanese greeting");
        log.info("Prompt: {}", prompt);
        log.info("AI Response: {}", response);
    }
}
