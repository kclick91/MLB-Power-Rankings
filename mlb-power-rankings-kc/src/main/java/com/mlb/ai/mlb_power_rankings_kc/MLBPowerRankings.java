package com.mlb.ai.mlb_power_rankings_kc;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class MLBPowerRankings {

    static class TeamStats {
        String name;
        int wins;
        int losses;
        double runDifferential;
//        double era;
//        double ops;
        double powerScore;
        int divisionRank;

        public TeamStats(String name) {
            this.name = name;
        }

        public double getWinPct() {
            return (double) wins / (wins + losses);
        }

        @Override
        public String toString() {
            return String.format(
                    "%-20s Score: %.2f W-L: %d-%d RD: %.1f Div Rank: %d",// OPS: %.3f ERA: %.2f
                    name,
                    powerScore,
                    wins,
                    losses,
//                    ops,
//                    era,
                    runDifferential,
                    divisionRank
            );
        }
    }

    static final String API =
            "https://statsapi.mlb.com/api/v1/standings?leagueId=103,104";

    public static void main(String[] args) throws Exception {

        List<TeamStats> teams = fetchMLBData();

		/*
		 * applyGradientBoostingApproximation(teams);
		 * 
		 * applyClusteringAdjustment(teams);
		 */
        
        FirstAdjustment(teams);
        SecondAdjustment(teams);

        List<TeamStats> rankings =
                teams.stream()
                        .sorted((a, b) ->
                                Double.compare(b.powerScore, a.powerScore))
                        .collect(Collectors.toList());

        System.out.println("\n=== MLB POWER RANKINGS ===\n");

        int rank = 1;

        for (TeamStats t : rankings) {
            System.out.printf("#%d %s%n", rank++, t);
        }
        LocalDateTime now = LocalDateTime.now();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy hh:mm a");
        String formatted = now.format(formatter);
        System.out.println("Current Date & Time (Readable): " + formatted);
    }

    static List<TeamStats> fetchMLBData() throws Exception {

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API))
                .build();

        HttpResponse<String> response =
                client.send(request, HttpResponse.BodyHandlers.ofString());

        ObjectMapper mapper = new ObjectMapper();

        JsonNode root = mapper.readTree(response.body());

        List<TeamStats> teams = new ArrayList<>();

        JsonNode records = root.get("records");

        Random random = new Random();

        for (JsonNode division : records) {

            JsonNode teamRecords = division.get("teamRecords");

            for (JsonNode teamNode : teamRecords) {

                TeamStats team = new TeamStats(
                        teamNode.get("team").get("name").asText()
                );

                team.wins = teamNode.get("wins").asInt();
                team.losses = teamNode.get("losses").asInt();

                
                
                team.runDifferential = teamNode.get("runDifferential").asInt();
                
                team.divisionRank = teamNode.get("divisionRank").asInt();

                teams.add(team);
            }
        }

        return teams;
    }
 // First adjustment
  static void FirstAdjustment (
          List<TeamStats> teams) {

      for (TeamStats t : teams) {

          double score = 0;

          score += t.getWinPct() * 50;

          score += t.runDifferential * 0.15;


          double drFactor = 1.2;
          
          score += t.divisionRank * drFactor;
          
          t.powerScore = score;
          
          
      }
  }

 // Second adjustment
  static void SecondAdjustment(
          List<TeamStats> teams) {

      double avg =
              teams.stream()
                      .mapToDouble(t -> t.powerScore)
                      .average()
                      .orElse(0);

      for (TeamStats t : teams) {

          if (t.powerScore > avg + 15) {
              t.powerScore += 5;
          }
          else if (t.powerScore < avg - 15) {
              t.powerScore -= 5;
          }
      }
  }

    /*
      Gradient Boosted Tree Approximation
     */
    static void applyGradientBoostingApproximation(
            List<TeamStats> teams) {

        for (TeamStats t : teams) {

            double score = 0;

            score += t.getWinPct() * 50;

            score += t.runDifferential * 0.15;


            double drFactor = 1.2;
            
            score += t.divisionRank * drFactor;
            
            t.powerScore = score;
            
            
        }
    }

    /*
      K-Means Inspired Tier Adjustment
     */
    static void applyClusteringAdjustment(
            List<TeamStats> teams) {

        double avg =
                teams.stream()
                        .mapToDouble(t -> t.powerScore)
                        .average()
                        .orElse(0);

        for (TeamStats t : teams) {

            if (t.powerScore > avg + 15) {
                t.powerScore += 5;
            }
            else if (t.powerScore < avg - 15) {
                t.powerScore -= 5;
            }
        }
    }

    
}