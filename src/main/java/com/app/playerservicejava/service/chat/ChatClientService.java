package com.app.playerservicejava.service.chat;

import com.app.playerservicejava.model.Player;
import com.app.playerservicejava.model.PlayerUtils;
import com.app.playerservicejava.service.PlayerService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.Model;
import io.github.ollama4j.models.OllamaResult;
import io.github.ollama4j.types.OllamaModelType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import io.github.ollama4j.utils.OptionsBuilder;
import io.github.ollama4j.utils.PromptBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatClientService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChatClientService.class);

    @Autowired
    private OllamaAPI ollamaAPI;
    @Autowired
    private PlayerService playerService;
    public List<Model> listModels() throws OllamaBaseException, IOException, URISyntaxException, InterruptedException {
        List<Model> models = ollamaAPI.listModels();
        return models;
    }
    public String generateSelectedPlayersForTournaments() throws Exception {
        // Fetch and limit players
        List<Player> players = playerService.getPlayers().getPlayers().stream()
                .limit(50)
                .collect(Collectors.toList());

        // Validate players list
        if (players == null || players.isEmpty()) {
            throw new IllegalStateException("No players available for recommendation.");
        }

        // Convert players to messages list
        String playerDetails = players.stream()
                .map(player -> {
                    try {
                        return PlayerUtils.createRecommendationRequest(player);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException("Error processing player: " + player, e);
                    }
                })
                .collect(Collectors.joining(","));

        // Validate messages list
        if (playerDetails.isEmpty()) {
            throw new IllegalStateException("No valid player data available for prompt generation.");
        }
        String systemPrompt = "You are an expert in selecting the most qualified players for upcoming tournaments. " +
                "Consider player demographics and match statistics to provide the best recommendations. " +
                "Players: " + playerDetails + "\n" +
                "Please return a list of qualified players in the following JSON format:" +
                "[ { 'name': 'Player Name', 'qualification': 'Qualification Details', 'reason': 'Reason for Qualification' }, ... ]" +
                "Do not include any additional text, just return the JSON array.";



        PromptBuilder promptBuilder = new PromptBuilder()
                .addLine(systemPrompt);  // System prompt alone is enough to guide the filtering


        // Call AI service with SYSTEM and USER prompts
        OllamaResult response = ollamaAPI.generate(
                OllamaModelType.TINYLLAMA,
                promptBuilder.build(),false,
                new OptionsBuilder().build()
        );

        // Validate AI Response
        if (response == null || response.getResponse() == null || response.getResponse().isEmpty()) {
            throw new IllegalStateException("AI service returned an invalid or empty response.");
        }

        return response.getResponse();
    }

    public String chat() throws OllamaBaseException, IOException, InterruptedException {
        String model = OllamaModelType.TINYLLAMA;

        // https://ollama4j.github.io/ollama4j/intro
        PromptBuilder promptBuilder =
                new PromptBuilder()
                        .addLine("Recite a haiku about recursion.");

        boolean raw = false;
        OllamaResult response = ollamaAPI.generate(model, promptBuilder.build(), raw, new OptionsBuilder().build());
        return response.getResponse();
    }

}
