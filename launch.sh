(trap 'kill 0' SIGINT;
 mvn clean
 mvn compile
 echo "Compilation finie"
 mvn exec:java@coord &
 sleep 2
 mvn exec:java@irc &
 sleep 2
 mvn exec:java@irc
)
