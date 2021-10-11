(trap 'kill 0' SIGINT;
 mvn exec:java@coord &
 mvn exec:java@irc &
 mvn exec:java@irc
)
