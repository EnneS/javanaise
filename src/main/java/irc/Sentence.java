/***
 * Sentence class : used for keeping the text exchanged between users
 * during a chat application
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.SentenceItf;

public class Sentence implements SentenceItf {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String data;

    public Sentence() {
        data = new String("");
    }

    public void write(String text) {
        data = text;
    }

    public String read() {
        return data;
    }

}