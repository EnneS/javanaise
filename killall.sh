# Kill all javanaise instances
jps -v | grep javanaise | grep -o '[0-9]\+ ' | xargs kill
