package com.printapp.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.printapp.model.PrintJobDto;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;

public class ApiService {
    private static final String API_URL = "http://print-test-env-env.eba-9gvrcrjp.us-east-1.elasticbeanstalk.com/order/get-config";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public List<PrintJobDto> fetchPrintConfigs() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return objectMapper.readValue(response.body(), new TypeReference<List<PrintJobDto>>() {
                });
            } else {
                System.err.println("API Error: " + response.statusCode());
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("Exception while fetching API: " + e.getMessage());
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
