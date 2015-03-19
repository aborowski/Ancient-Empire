package antyczne.imperium;

import java.io.*;
import java.util.*;

public class Capital extends Village {

    public Capital(int idGotten) throws BadMapDesignException {
        super(idGotten);
        this.setResCreated(16);
        this.setResUsed(1023-16);
        System.out.println("ZMIANA DLA STOLICY");
        this.setPopulation(this.getPopulation() + 1000);
        this.setName(this.createRandomName((int) Math.ceil(Math.random() * 3) + 3) + " City");
    }

    /**
     * Metoda tworząca legion w stolicy po naciśnięciu przycisku przez
     * użytkownika
     */
    public void dispatchLegion() {
        synchronized(this.getMonitor()) {
            if(this.getTreasury()>=1000) {
                Legion l = new Legion(Database.getNextID(), this.getX(), this.getY(), this);
                Database.appendToLegionList(l);
                this.setTreasury(this.getTreasury() - 1000);
            }
        }
    }
    
    @Override
    public void update() {
        boolean needToSend = false;
        for (int i = 0; i < 10; i++) {
            if(((this.getResCreated() & 1<<i) == 1<<i) && (this.getCounter_() % ((1<<i) * 20) == 0)) {
                needToSend = this.produceStuff(1<<i);
            }
        }
        if(needToSend)
            this.sendCaravan();
        this.checkStockRotten();
        if((this.getCounter_() % 50) == 0) {
            if(this.getResourcesKeptConsumed().isEmpty()) {
                //System.out.println(this.getName() + " has nothing to consume");
            }
            else {
                this.useSomeStock();
                System.out.println("Konsumuje");
                this.changeSleepingCaravans(null, false);
            }
            this.setTreasury(this.getTreasury() + (int) (this.getPopulation() / 20));
            //System.out.println("It is payday in " + this.getName());
            //System.out.println("Treasury at " + this.treasury_);
        }
        this.setCounter_(this.getCounter_() + 1);
    }
    

}
