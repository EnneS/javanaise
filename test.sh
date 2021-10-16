# Get java process id corresponding to javanaise programs and kill them
 jps -v | grep javanaise | grep -o '[0-9]\+ ' | xargs kill
 mvn clean
 mvn compile
 echo "Compilation finie"
 mvn exec:java@coord &
 P0=$!
 sleep 2
 mvn exec:java@ircNw &
 P1=$!
 sleep 1
 mvn exec:java@ircNw &
 P2=$!
 mvn exec:java@ircNw &
 P3=$!
 wait $P0 $P1 $P2 $P3
