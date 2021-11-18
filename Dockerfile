FROM arm32v7/openjdk:11
WORKDIR /app
COPY ./target/JMusicBot-Snapshot-All.jar JMusicBot.jar
COPY serversettings.json serversettings.json
COPY config.txt config.txt
EXPOSE 80 443 50000-59999
CMD java -Dnogui=true -jar JMusicBot.jar
