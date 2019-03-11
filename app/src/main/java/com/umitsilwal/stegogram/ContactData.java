package com.umitsilwal.stegogram;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.BareJid;

import java.io.Serializable;

public class ContactData implements Serializable {

    protected String name;
    protected BareJid jid;


    public ContactData(RosterEntry contact){
        jid = contact.getJid();
        name = contact.getName();
    }

    public String getName(){
        return name;
    }

    public BareJid getJid(){
        return jid;
    }

}
