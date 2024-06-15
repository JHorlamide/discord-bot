package jhorlamide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.security.auth.login.LoginException;

public class Main {
   private static final Logger logger = LoggerFactory.getLogger(MessageReceiveListener.class);

   public static void main(String[] args) {
      var token = System.getenv("DISCORD_BOT_TOKEN");

      if (token == null) {
         System.err.println("Error: DISCORD_BOT_TOKEN is not set in .env file.");
         return;
      }

      try {
         new Bot(token).start();
      } catch (LoginException exception) {
         logger.error("An unexpected error occurred", exception);
      }
   }
}