package dev.danvega.hub;

import java.io.IOException;
import java.util.List;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.content.Media;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class ImageAnalysisController {

    private final OpenAiChatModel chatModel;

    public ImageAnalysisController(OpenAiChatModel chatModel) {
        this.chatModel = chatModel;
    }
    /***
     * curl -X POST "http://localhost:8063/analyze" -H "Accept: text/plain" -F "file=@C:\path\to\image.png;type=image/png"
     * 
     * **/
    @PostMapping("/analyze")
    public String analyzeImage(@RequestParam("file") MultipartFile file) throws IOException {
        // 1. Wrap the image data into a Media object
        Media imageData = new Media(MimeTypeUtils.IMAGE_PNG, file.getResource());

        // 2. Create the user message with text and image
        UserMessage userMessage = UserMessage.builder()
                          .text("OCR this image in Traditional Chinese.")
                          .media(imageData).build();

        // 3. Create prompt with the message
        Prompt prompt = new Prompt(List.of(userMessage));

        // 4. Call the model
        ChatResponse response = chatModel.call(prompt);
        return response.getResult().getOutput().getText();
    }
}
