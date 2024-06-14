package jhorlamide;

import io.github.cdimascio.dotenv.Dotenv;

import javax.security.auth.login.LoginException;

public class Main {
   public static void main(String[] args) {
      var dotenv = Dotenv.load();
      var token = dotenv.get("DISCORD_BOT_TOKEN");

      if (token == null) {
         System.err.println("Error: DISCORD_BOT_TOKEN is not set in .env file.");
         return;
      }

      try {
         new Bot(token).start();
      } catch (LoginException exception) {
         exception.printStackTrace(); // replaced with more robust logging
      }
   }
}