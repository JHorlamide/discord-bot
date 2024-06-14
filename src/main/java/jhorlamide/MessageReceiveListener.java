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
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;

public class MessageReceiveListener extends ListenerAdapter {
   private final String CHALLENGE_FILE_PATH = "./challenge.json";

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

      if (messageContent.startsWith("!dd")) {
         handleAddChallengeCommand(message, messageContent);
      }
   }

   private void handleMessage(Message message, String authorName) {
      String responseMessage = "Hello, " + authorName;
      message.getChannel().sendMessage(responseMessage).queue();
   }

   private void handleQuoteCommand(Message message) {
      QuoteFetcher.getRandomQuite().thenApply(quote -> {
         message.getChannel().sendMessage(quote).queue();
         return null;
      });
   }

   private void handleChallengeCommand(Message message) {
      try {
         String jsonContent = readJsonFile();
         String messageResponse = getString(jsonContent);
         message.getChannel().sendMessage(messageResponse).queue();
      } catch (IOException e) {
         responseWithError(message, "❌ Error reading challenges ❌. Please again later");
         e.printStackTrace();
      }
   }

   private void handleListChallengeCommand(Message message) {
      try {
         String jsonContent = readJsonFile();
         JSONObject jsonObject = new JSONObject(jsonContent);
         JSONArray challenges = jsonObject.getJSONArray("challenges");
         StringBuilder messageResponse = new StringBuilder();

         for (int i = 0; i < challenges.length(); i++) {
            JSONObject challenge = challenges.getJSONObject(i);
            var challengeName = challenge.getString("name");
            var challengeUrl = challenge.getString("url");
            messageResponse.append(challengeName).append(": ").append(challengeUrl).append("\n");
         }

         messageResponse.toString();
         message.getChannel().sendMessage(messageResponse).queue();
      } catch (IOException e) {
         responseWithError(message, "❌ Error reading challenges ❌. Please again later");
         e.printStackTrace();
      }
   }


   private void handleAddChallengeCommand(Message message, String messageContent) {
      String[] parts = messageContent.split("\\s+", 2); // Split into !add and the URL

      if (parts.length != 2)
         return;

      String url = parts[1].trim();
      if (!isValidUrl(url)) {
         String errorMessage = "Unable to add " + url + " Please check if it's a valid coding challenge";
         responseWithError(message, errorMessage);
      }

      try {
         String challengeTitle = getChallengeTitle(url);
         if (challengeTitle != null) {
            addChallenge(challengeTitle, url);

            String responseMessage = "Added: " + challengeTitle + ": " + url;
            message.getChannel().sendMessage(responseMessage).queue();
         }
      } catch (IOException e) {
         responseWithError(message, "Unable to get challenge. Please it is a valid coding challenge.");
      }
   }


   private void addChallenge(String url, String name) throws IOException {
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

   private String getString(String jsonContent) {
      JSONObject jsonObject = new JSONObject(jsonContent);
      JSONArray challenges = jsonObject.getJSONArray("challenges");

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
      try {
         URI uri = new URI(url);
         return uri.getHost() != null && !uri.getHost().isEmpty();
      } catch (URISyntaxException e) {
         return false;
      }
   }

   private String getChallengeTitle(String url) throws IOException {
      Document document = Jsoup.connect(url).get();
      Element titleElement = document.selectFirst("title");
      return titleElement != null ? titleElement.text() : null;
   }

   private void responseWithError(Message message, String errorMessage) {
      message.getChannel().sendMessage(errorMessage).queue();
   }
}
