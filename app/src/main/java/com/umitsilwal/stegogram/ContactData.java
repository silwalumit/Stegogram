package com.umitsilwal.stegogram;

import org.jivesoftware.smack.roster.RosterEntry;
import org.jxmpp.jid.BareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.io.Serializable;

public class ContactData implements Serializable {

    protected String name;
    protected BareJid jid;

    public ContactData(String jid, String name){
        this.name = name;
        try {
            this.jid = JidCreate.bareFrom(jid);
        } catch (XmppStringprepException e) {
            e.printStackTrace();
        }
    }

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
