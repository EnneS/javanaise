package irc;

import java.io.Serializable;

public class Counter implements CounterItf {
    int counter;

    public Counter() {
        counter = 0;
    }

    public void plus() {
        counter++;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

}
