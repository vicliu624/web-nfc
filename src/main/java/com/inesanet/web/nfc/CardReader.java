package com.inesanet.web.nfc;

import javax.smartcardio.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @Auther: liuweikai
 * @Date: 2019-12-31 22:26
 * @Description:
 */
public class CardReader {
    private TerminalFactory factory;
    private static CardReader cardReader;
    private List<CardTerminal> cardTerminals;
    private CardTerminal selectedCardTerminal;
    private Card card;
    private CardChannel cardChannel;

    private CardReader(){

    }

    public List<CardTerminal> allCardTerminals(){
        if (factory == null){
            factory = TerminalFactory.getDefault();
        }
        try {
            cardTerminals = factory.terminals().list();
            if(cardTerminals == null){
                return new ArrayList<>();
            }
            return cardTerminals;
        } catch (CardException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public void selectedCardTerminal(int index){
        this.selectedCardTerminal = this.cardTerminals.get(index);
    }

    public void connect(String channel){
        try {
            this.card = selectedCardTerminal.connect(channel);
        } catch (CardException e) {
            e.printStackTrace();
        }
    }

    public void getBasicChannel(){
        this.cardChannel = this.card.getBasicChannel();
    }

    public boolean isCardPresent() throws Exception{
        try {
            return this.selectedCardTerminal.isCardPresent();
        } catch (CardException e) {
            throw new Exception(e.getMessage());
        }
    }

    public ResponseAPDU transmit(CommandAPDU apdu){
        try {
            System.out.println("card channel object:" + this.cardChannel.hashCode());
            return this.cardChannel.transmit(apdu);
        } catch (CardException e) {
            return null;
        }
    }

    public static CardReader getInstance(){
        if(cardReader == null){
            cardReader = new CardReader();
        }
        return cardReader;
    }

}
