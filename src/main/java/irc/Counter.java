package irc;

import java.io.Serializable;

public class Counter implements Serializable {
    int counter;

    public Counter(){
        counter = 0;
    }

    public void plus(){
        counter++;
    }
}
