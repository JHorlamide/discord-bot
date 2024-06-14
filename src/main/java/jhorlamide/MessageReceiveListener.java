package jhorlamide;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class MessageReceiveListener extends ListenerAdapter {
   @Override
   public void onMessageReceived(@NotNull MessageReceivedEvent event) {
      Message message = event.getMessage();
      String authorName = event.getAuthor().getEffectiveName();
      String messageContent = message.getContentRaw();

      if (messageContent.equals("Hello")) {
         handleMessage(message, authorName);
      }

      if (messageContent.equals("!quote"))
         handleQuoteCommand(message);
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
}
