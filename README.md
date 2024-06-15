# Overview
Implementing the Build Your Own Discort bot from the coding challenge by https://codingchallenges.fyi/challenges/challenge-discord by John Cricket

# Language used
I use Java as I am new to the language and trying to get better at it.

# Prepare

prepare your bot as described in the challenge and follow the instructions. https://codingchallenges.fyi/challenges/challenge-discord#step-zero

# Run

- Locally: Set your DISCORD_BOT_TOKEN and DUMMY_QUOTE_API=https://dummyjson.com/quotes/random as part of the program starter.
- Docker: ```bash docker build -t discord-bot <DOCKER_FILE_PATH>``` and the run with your env vars ```docker run -d --name discord.bot \
  -e DISCORD_BOT_TOKEN=<yout-discord-bot-token> \
  -e DUMMY_QUOTE_API=https://dummyjson.com/quotes/random \
  discord-bot ```