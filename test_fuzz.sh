# ctrl-c will kill all process in there
(trap 'kill 0' SIGINT;
# Get java process id corresponding to javanaise programs and kill them
 jps -v | grep javanaise | grep -o '[0-9]\+ ' | xargs kill
 mvn clean
 mvn compile
 echo "Compilation finie"
 mvn exec:java@coord &
 P0=$!
 mvn exec:java@ircFuzz &
 P1=$!
 mvn exec:java@ircFuzz &
 P2=$!
 mvn exec:java@ircFuzz &
 P3=$!
 mvn exec:java@ircFuzz &
 P4=$!
 wait $P0 $P1 $P2 $P3 $P4
)
