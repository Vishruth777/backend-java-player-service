package com.app.playerservicejava.model;

import com.fasterxml.jackson.core.JsonProcessingException;

public class PlayerUtils {
    public static String createRecommendationRequest(Player player) throws JsonProcessingException {
        return  String.format("Name: %s %s, Height: %s, Weight: %s, Matches Played: %s",
                player.getFirstName(), player.getLastName(),
                player.getHeight(), player.getWeight(),
                player.getBirthCountry());


    }
}
