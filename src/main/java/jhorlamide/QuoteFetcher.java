package jhorlamide;

import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class QuoteFetcher {
   private static final String API_URL = System.getenv("DUMMY_QUOTE_API");

   public static CompletableFuture<String> getRandomQuote() {
      HttpClient client = HttpClient.newHttpClient();
      HttpRequest request = HttpRequest.newBuilder()
         .uri(URI.create(API_URL))
         .build();

      return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
         .thenApply(HttpResponse::body)
         .thenApply(responseBody -> {
            JSONObject jsonResponse = new JSONObject(responseBody);
            return jsonResponse.getString("quote");
         });
   }
}
