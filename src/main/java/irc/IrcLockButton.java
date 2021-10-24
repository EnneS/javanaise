/***
 * Irc class : simple implementation of a chat using JAVANAISE 
 * Contact: 
 *
 * Authors: 
 */

package irc;

import jvn.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IrcLockButton {
    public TextArea text;
    public TextField data;
    Frame frame;
    JvnObject sentence;




    /**
     * main method
     * create a JVN object nammed IRC for representing the Chat application
     **/
    public static void main(String argv[]) {
        for (String arg : argv) {
            if(arg.equals("-v")){
                JvnGlobals.debug = true;
            }
        }

        try {
            // look up the IRC object in the JVN server
            // if not found, create it, and register it in the JVN server
            // initialize JVN
            JvnServerImpl js = JvnServerImpl.jvnGetServer("localhost");

            // look up the IRC object in the JVN server
            // if not found, create it, and register it in the JVN server
            JvnObject jo = js.jvnLookupObject("IRC");

            if (jo == null) {
                jo = js.jvnCreateObject(new Sentence());
                // after creation, I have a write lock on the object
                jo.jvnUnLock();

                js.jvnRegisterObject("IRC", jo);
            }
            new IrcLockButton(jo);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    /**
     * IRC Constructor
     *
     * @param jvn the JVN object representing the Chat
     **/
    public IrcLockButton(JvnObject jvn) {
        sentence = jvn;
        frame = new Frame();
        frame.setLayout(new GridLayout(1, 1));
        text = new TextArea(10, 60);
        text.setEditable(false);
        text.setForeground(Color.red);
        frame.add(text);
        data = new TextField(40);
        frame.add(data);
        Button read_button = new Button("read");
        read_button.addActionListener(new readListenerLockButton(this, jvn));
        frame.add(read_button);
        Button write_button = new Button("write");
        write_button.addActionListener(new writeListenerLockButton(this, jvn));
        frame.add(write_button);

        JToggleButton toggle_write_button = new JToggleButton("Lock/unlock write");
        toggle_write_button.addActionListener(new writeListenerToggleButton(this, jvn));
        frame.add(toggle_write_button);

        JToggleButton toggle_read_button = new JToggleButton("Lock/unlock read");
        toggle_read_button.addActionListener(new readListenerToggleButton(this, jvn));
        frame.add(toggle_read_button);

        frame.setSize(545, 201);
        text.setBackground(Color.black);
        frame.setVisible(true);
    }
}


/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class readListenerLockButton implements ActionListener {
    IrcLockButton irc;
    JvnObject o;

    public readListenerLockButton(IrcLockButton i, JvnObject o) {
        irc = i;
        this.o = o;
    }

    /**
     * Management of user events
     **/
    public void actionPerformed(ActionEvent e) {
            String s = "";

            try {
                s = ((SentenceItf) this.o.jvnGetSharedObject()).read();
            } catch (JvnException ex) {
                ex.printStackTrace();
            }

            // display the read value
            irc.data.setText(s);
            irc.text.append(s + "\n");
    }
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class writeListenerLockButton implements ActionListener {
    IrcLockButton irc;
    JvnObject o;

    public writeListenerLockButton(IrcLockButton i, JvnObject o) {
        irc = i;
        this.o = o;
    }

    /**
     * Management of user events
     **/
    public void actionPerformed(ActionEvent e) {
        // get the value to be written from the buffer
        String s = irc.data.getText();

        // invoke the method
        try {
            ((SentenceItf) this.o.jvnGetSharedObject()).write(s);
        } catch (JvnException ex) {
            ex.printStackTrace();
        }
    }
}

/**
 * Internal class to manage user events (read) on the CHAT application
 **/
class readListenerToggleButton implements ActionListener {
    IrcLockButton irc;
    JvnObject o;

    public readListenerToggleButton(IrcLockButton i, JvnObject o) {
        irc = i;
        this.o = o;
    }

    /**
     * Management of user events
     **/
    public void actionPerformed(ActionEvent e) {

        if(((JToggleButton) e.getSource()).isSelected()) {
            try {
                this.o.jvnLockRead();
            } catch (JvnException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                this.o.jvnUnLock();
            } catch (JvnException ex) {
                ex.printStackTrace();
            }
        }
    }
}

/**
 * Internal class to manage user events (write) on the CHAT application
 **/
class writeListenerToggleButton implements ActionListener {
    IrcLockButton irc;
    JvnObject o;

    public writeListenerToggleButton(IrcLockButton i, JvnObject o) {
        irc = i;
        this.o = o;
    }

    /**
     * Management of user events
     **/
    public void actionPerformed(ActionEvent e) {

        if(((JToggleButton) e.getSource()).isSelected()) {
            try {
                this.o.jvnLockWrite();
            } catch (JvnException ex) {
                ex.printStackTrace();
            }
        } else {
            try {
                this.o.jvnUnLock();
            } catch (JvnException ex) {
                ex.printStackTrace();
            }
        }

    }
}



