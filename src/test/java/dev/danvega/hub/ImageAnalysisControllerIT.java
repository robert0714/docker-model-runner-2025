package dev.danvega.hub;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ImageAnalysisControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OpenAiChatModel chatModel;

    @Test
    void analyze_returns_model_text_and_sends_media() throws Exception {
        when(chatModel.call(any(Prompt.class)))
                .thenReturn(new ChatResponse(List.of(new Generation(new AssistantMessage("mock-result")))));

        var file = new MockMultipartFile(
                "file",
                "image.png",
                "image/png",
                new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47 } // minimal PNG signature
        );

        mockMvc.perform(multipart("/analyze").file(file))
                .andExpect(status().isOk())
                .andExpect(content().string("mock-result"));

        var promptCaptor = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, atLeastOnce()).call(promptCaptor.capture());

        var prompt = promptCaptor.getAllValues().get(promptCaptor.getAllValues().size() - 1);
        var userMessage = prompt.getUserMessage();

        assertThat(userMessage.getText()).contains("Describe this image");
        assertThat(userMessage.getMedia()).hasSize(1);
    }
}
