package jhorlamide;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileWriter;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class MessageReceiveListener extends ListenerAdapter {
   private final String CHALLENGE_FILE_PATH = "./challenges.json";

   @Override
   public void onMessageReceived(@NotNull MessageReceivedEvent event) {
      Message message = event.getMessage();
      String messageContent = message.getContentRaw();

      if (messageContent.equals("Hello")) {
         String authorName = event.getAuthor().getEffectiveName();
         handleMessage(message, authorName);
      }

      if (messageContent.equals("!quote"))
         handleQuoteCommand(message);

      if (messageContent.equals("!challenge")) {
         handleChallengeCommand(message);
      }

      if (messageContent.equals("!list")) {
         handleListChallengeCommand(message);
      }

      if (messageContent.startsWith("!add")) {
         handleAddChallengeCommand(message, messageContent);
      }
   }

   private void handleMessage(Message message, String authorName) {
      String responseMessage = "Hello, " + authorName;
      message.getChannel().sendMessage(responseMessage).queue();
   }

   private void handleQuoteCommand(Message message) {
      QuoteFetcher.getRandomQuote().thenApply(quote -> {
         message.getChannel().sendMessage(quote).queue();
         return null;
      });
   }

   private void handleChallengeCommand(Message message) {
      try {
         String messageResponse = getRandomChallenge();
         message.getChannel().sendMessage(messageResponse).queue();
      } catch (IOException e) {
         responseWithError(message, "❌ Error reading challenges ❌. Please again later");
         e.printStackTrace();
      }
   }

   private void handleListChallengeCommand(Message message) {
      try {
         JSONArray challenges = getChallenges();
         StringBuilder messageResponse = new StringBuilder();

         for (int i = 0; i < challenges.length(); i++) {
            JSONObject challenge = challenges.getJSONObject(i);
            var challengeName = challenge.getString("name");
            var challengeUrl = challenge.getString("url");
            messageResponse.append(challengeName).append(": ").append(challengeUrl).append("\n");
         }

         //messageResponse.toString();
         message.getChannel().sendMessage(messageResponse).queue();
      } catch (IOException e) {
         responseWithError(message, "❌ Error reading challenges ❌. Please again later");
         e.printStackTrace();
      }
   }

   private void handleAddChallengeCommand(Message message, String messageContent) {
      String[] parts = messageContent.split("\\s+", 2); // Split into !add and the URL
      String challengeUrl = parts[1].trim();

      if (!isValidUrl(challengeUrl)) {
         String errorMessage = "Unable to add: " + challengeUrl + " Please check if it is a valid Coding Challenge";
         responseWithError(message, errorMessage);
         return;
      }

      try {
         String challengeName = getChallengeName(challengeUrl);
         if (challengeName != null) {
            if (isChallengeInCatalog(challengeName, challengeUrl)) {
               responseWithError(message, "This Coding Challenge already exist");
               return;
            }

            addChallengeToCatalog(challengeName, challengeUrl);

            String responseMessage = "Added: " + challengeName + ": " + challengeUrl;
            message.getChannel().sendMessage(responseMessage).queue();
         }
      } catch (IOException e) {
         String errorMessage = "Unable to get challenge: " + e.getMessage();
         responseWithError(message, errorMessage);
      }
   }


   private void addChallengeToCatalog(String name, String url) throws IOException {
      String jsonContent = readJsonFile();
      JSONObject jsonObject = new JSONObject(jsonContent);
      JSONArray challenges = jsonObject.getJSONArray("challenges");

      JSONObject newChallenge = new JSONObject();
      newChallenge.put("name", name);
      newChallenge.put("url", url);
      challenges.put(newChallenge);

      try (FileWriter file = new FileWriter(CHALLENGE_FILE_PATH)) {
         file.write(jsonObject.toString(2));
      }
   }

   private String getRandomChallenge() throws IOException {
      JSONArray challenges = getChallenges();

      var random = new Random();
      int randomIndex = random.nextInt(challenges.length());

      JSONObject randomChallenge = challenges.getJSONObject(randomIndex);
      String challengeName = randomChallenge.getString("name");
      String challengeUrl = randomChallenge.getString("url");
      return challengeName + ": " + challengeUrl;
   }

   private String readJsonFile() throws IOException {
      return Files.readString(Paths.get(CHALLENGE_FILE_PATH));
   }

   private boolean isValidUrl(String url) {
      String VALID_DOMAIN = "codingchallenges.fyi";

      try {
         URL obj = new URL(url);
         obj.toURI();

         HttpURLConnection huc = (HttpURLConnection) obj.openConnection();
         huc.setRequestMethod("GET");
         huc.setConnectTimeout(5000);
         huc.setReadTimeout(5000);
         int responseCode = huc.getResponseCode();
         String host = obj.getHost();
         return responseCode == 200 && host.endsWith(VALID_DOMAIN);
      } catch (URISyntaxException | IOException e) {
         return false;
      }
   }

   private boolean isChallengeInCatalog(String name, String url) throws IOException {
      JSONArray challenges = getChallenges();
      JSONObject lastChallenge = challenges.getJSONObject(challenges.length() - 1);
      String challengeName = lastChallenge.getString("name");
      String challengeUrl = lastChallenge.getString("url");
      return challengeName.equals(name) && challengeUrl.equals(url);
   }

   private JSONArray getChallenges() throws IOException {
      String jsonContent = readJsonFile();
      JSONObject jsonObject = new JSONObject(jsonContent);
      return jsonObject.getJSONArray("challenges");
   }

   private String getChallengeName(String url) throws IOException {
      Document document = Jsoup.connect(url).get();
      Element titleElement = document.selectFirst("title");
      return titleElement != null ? titleElement.text() : null;
   }

   private void responseWithError(Message message, String errorMessage) {
      message.getChannel().sendMessage(errorMessage).queue();
   }
}
