package jhorlamide;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;

import javax.security.auth.login.LoginException;
import java.util.EnumSet;

public class Bot {
   private final String TOKEN;

   public Bot(String token) {
      this.TOKEN = token;
   }

   public void start() throws LoginException {
      EnumSet<GatewayIntent> intents = EnumSet.of(
         // Enables MessageReceivedEvent for guild (also known as servers)
         GatewayIntent.GUILD_MESSAGES,
         // Enables the event for private channels (also known as direct messages)
         GatewayIntent.DIRECT_MESSAGES,
         // Enables access to message.getContentRaw()
         GatewayIntent.MESSAGE_CONTENT,
         // Enables MessageReactionAddEvent for guild
         GatewayIntent.GUILD_MESSAGE_REACTIONS,
         // Enables MessageReactionAddEvent for private channels
         GatewayIntent.DIRECT_MESSAGE_REACTIONS
      );

      JDABuilder builder = JDABuilder.createDefault(TOKEN);
      builder.enableIntents(intents)
         .addEventListeners(new MessageReceiveListener())
         .build();
   }
}
