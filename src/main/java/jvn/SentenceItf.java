package jvn;

import annotations.Read;
import annotations.Write;

public interface SentenceItf extends java.io.Serializable {

    @Write
    public void write(String text);

    @Read
    public String read();

}
