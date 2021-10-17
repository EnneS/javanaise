package irc;

import annotations.Read;
import annotations.Write;

import java.io.Serializable;

public interface CounterItf extends Serializable {

    @Write
    public void plus();

    @Read
    public int getCounter();

    @Write
    public void setCounter(int counter);

}
