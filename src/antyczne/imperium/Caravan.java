package antyczne.imperium;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa kupca w symulacji.
 */
public class Caravan extends Unit {

    private String surname_;
    private int maxCapacity_;
    private int curCapacity_;
    private int speedPenalty_; //Kara do szybkosci, od 0 do 3, im wieksza tym wolniejszy pojazd
    private LinkedList<Res> load_;
    private int wallet_;
    private boolean broken_;
    private boolean sleeping_;
    private int restrictedRes_;

    /**
     * Konstruktor karawany kupieckiej. Założenie jest, iż karawana wytworzy się
     * w mieście/wiosce
     *
     * @param idGotten ID obiektu
     * @param putX pozycja X
     * @param putY pozycja Y
     * @param whereAmI Lokacja, w której jest karawana
     */
    public Caravan(int idGotten, double putX, double putY, ConnectableLoc whereAmI) {
        super(idGotten, putX, putY, whereAmI);
        this.broken_ = false;
        this.curCapacity_ = 0;
        this.load_ = new LinkedList<>();
        this.maxCapacity_ = (int) (Math.round(Math.random() * 50) + 40);
        this.sleeping_ = true;
        this.speedPenalty_ = 0;
        this.surname_ = this.createRandomName((int) Math.round(Math.random() * 5) + 2);
        this.wallet_ = 100000;
        this.setTargetDest(whereAmI);
        this.changeLocation(whereAmI, 1);
        this.restrictedRes_ = 0;

    }

    /**
     * Metoda ta wywoływana jest by zatrzymać karawanę w wiosce do odwołania,
     * np. gdy karawana nie mogła sprzedać swoich towarów, lub kupić
     * czegokolwiek.
     *
     * @param location Wioska w ktorej jest karawana
     */
    public void stopAtVillage(Village location) {
        synchronized(getMonitor()) {
            this.sleeping_ = true;
            location.changeSleepingCaravans(this, true);
        }
    }

    /**
     * Metoda oblicza karę do szybkości karawany zależnie od obciążenia
     */
    public void calculateSpeedPenalty() {
        synchronized(getMonitor()) {
            int mass = 0;
            for (Res res : load_) {
                mass += res.getWeight();
            }
            if (mass <= 50) {
                this.speedPenalty_ = 0;
            }
            if (mass > 50 && mass <= 150) {
                this.speedPenalty_ = 1;
            }
            if (mass > 150 && mass <= 300) {
                this.speedPenalty_ = 2;
            }
            if (mass > 300) {
                this.speedPenalty_ = 3;
            }
        }
    }

    /**
     * Metoda wywoływana, gdy wioska powoła karawanę do pracy, lub gdy karawana
     * dotrze do celu. Karawaniarz sprzedaje możliwe towary, zaopatruje się w
     * nowe i wybiera następny cel.
     *
     * @param location Wioska w ktorej jest karawana
     */
    public void leaveVillage(Village location) {
        synchronized(getMonitor()) {
            this.sleeping_ = false;
            this.sellAllSellable(location);
            if (!this.sleeping_) {
                this.buyAllBuyable(location);
                this.calculateSpeedPenalty();
                if (this.getTargetDest().equals(this.getLocation())) {
                    this.pickNewTarget();
                }
            }
        }
    }

    /**
     * Metoda sprawdzająca które towary z listy karawany są pożądane przez
     * wioske, po czym są one sprzedawane.
     *
     * @param location Wioska w ktorej jest karawana
     */
    public void sellAllSellable(Village location) {
        synchronized(getMonitor()) {
            boolean soldAll = true;
            LinkedList<Res> removalList = new LinkedList<>();
            if (!this.load_.isEmpty()) {
                for (Res res : this.load_) {
                    if ((res.getResType() & location.getResUsed()) == res.getResType()) {
                        if (location.getTreasury() >= (res.calculateVal()) && (location.getCurStockBuy() + res.getVolume()) <= location.getMaxStockBuy()) {
                            location.setTreasury(location.getTreasury() - res.calculateVal());
                            this.wallet_ += res.calculateVal();
                            this.curCapacity_ -= res.getVolume();
                            location.addToStock(res, false);
                            removalList.add(res);
                            System.out.println("Towar sprzedany " + res.getResType());
                        } else {
                            soldAll = false;
                            System.out.println("Nie udalo sie sprzedac wszystkiego");
                        }
                    }
                }
                this.load_.removeAll(removalList);
                if (!soldAll) {
                    if (this.load_.size() < 3) {
                        this.stopAtVillage(location);
                    }
                }
            }
        }
    }

    /**
     * Metoda sprawdzająca czy towary spożywcze kupca są przeterminowane.
     * Przeterminowane towary są usuwane.
     */
    private void checkForRotten() {
        synchronized(getMonitor()) {
            LinkedList<Res> removalList = new LinkedList<>();
            for (Res res : this.load_) {
                if (res.getResType() == 1 || res.getResType() == 16) {
                    if (res.getTimeDue() < System.currentTimeMillis()) {
                        removalList.add(res);
                        this.setCurCapacity(curCapacity_ - res.getVolume());
                    }
                }
            }
            if (!removalList.isEmpty()) {
                this.load_.removeAll(removalList);
                this.calculateSpeedPenalty();
            }
        }
    }

    /**
     * Metoda sprawdzająca które towary wioska sprzedaje, po czym są one
     * kupowane (o ile to możliwe). W przypadku niemożności zakupienia towarów z
     * braku pieniędzy karawany zostaje ona rozwiązana. Jeżeli natomiast wioska
     * nie ma produktów to zostaje jedynie zatrzymana.
     *
     * @param location Wioska w ktorej jest karawana
     */
    public void buyAllBuyable(Village location) {
        synchronized(getMonitor()) {
            Res r;
            boolean giveMeMore;
            for (int i = 9; i >= 0; i--) {
                giveMeMore = true;
                if ((1 << i & location.getResCreated()) == 1 << i) {
                    while (giveMeMore) {
                        r = location.checkStockForGoods(1 << i);
                        if (r != null) {
                            if (this.getWallet() >= ((r.calculateVal() / 5) * 3) && (this.getCurCapacity() + r.getVolume() <= this.getMaxCapacity())) {
                                location.removeFromStock(r);
                                this.setWallet(wallet_ - ((r.calculateVal() / 5) * 3));
                                location.setTreasury(location.getTreasury() + (r.calculateVal() / 5) * 3);
                                this.load_.add(r);
                                this.curCapacity_ += r.getVolume();
                                System.out.println("Towar kupiony " + r.getResType());
                            } else {
                                System.out.println("Brak kasy lub miejsca na" + (1 << i));
                                giveMeMore = false;
                            }
                        } else {
                            System.out.println("Brak surowcow do kupna typu " + (1 << i));
                            giveMeMore = false;
                        }
                    }
                } else {
                    System.out.println("Wioska nie produkuje" + (1 << i));
                }
            }
            if (this.load_.isEmpty()) {
                for (int i = 0; i < 10; i++) {
                    if ((location.getResCreated() & (1 << i)) == (1 << i)) {
                        Res tempRes = new Res(Database.getNextID(), (1 << i));
                        if (this.wallet_ < (tempRes.calculateVal() / 5) * 4) {
                            this.retireAtVillage(location);
                            break;
                        } else {
                            this.stopAtVillage(location);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Metoda przekazujaca czesc majatku kupca na rzecz miasta. Kupuje sobie
     * domostwo.
     *
     * @param location Wioska w ktorej sie osadza
     */
    public void retireAtVillage(Village location) {
        synchronized(getMonitor()) {
            location.setTreasury(location.getTreasury() + (int) this.wallet_ / 2);
            this.removeUnit();
        }
    }

    /**
     * Usuwanie kupca z symulacji
     */
    @Override
    public synchronized void removeUnit() {
        synchronized(getMonitor()) {
            Crossing c = null;
            if (this.getLocation() instanceof Crossing) {
                c = (Crossing) this.getLocation();
            }
            this.load_.clear();
            for (ConnectableLoc loc : Database.getLocationList()) {
                if (loc.getResidentList_().contains(this)) {
                    loc.removeResident(this);
                }
            }
            Database.removeCaravanFromList(this);
            if (c != null) {
                c.releaseInUse();
            }
            this.setRunning(false);
            this.setLocation(null);
        }
    }

    /**
     * Metoda pętli kupca
     */
    @Override
    public void run() {
        while (this.isRunning()) {
            while (!this.broken_ && this.isRunning()) {
                update();
                try {
                    this.getThread_().sleep(1000 / (60 / (this.getSpeed() + this.speedPenalty_)));
                } catch (InterruptedException ex) {
                    Logger.getLogger(Caravan.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            try {
                this.getThread_().sleep(1000 / (60 / (this.getSpeed() + this.speedPenalty_)));
            } catch (InterruptedException ex) {
                Logger.getLogger(Caravan.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Metoda zajmująca się obsługą stanu kupca, zmiany lokacji,
     * przemieszczania, sprawdzania korków
     */
    @Override
    public void update() {
        synchronized(getMonitor()) {
            this.checkForRotten();
            if (!this.sleeping_) {
                if (this.getLocation().equals(this.getTargetDest())) {
                    Village v = null;
                    for (Village village : Database.getVillageList()) {
                        if (village.getResidentList_().contains(this)) {
                            v = village;
                        }
                    }
                    if (v != null) {
                        this.leaveVillage(v);
                    } else {
                        this.pickNewTarget();
                    }
                } else {
                    if (this.getLocationType() == 2 || this.getLocationType() == 1) {
                        if (this.getLocationType() == 1) {
                            this.leaveVillage((Village) this.getLocation());
                        }
                        this.pickNextStop();
                        if (this.getNextStop() != null) {
                            System.out.println("Zmiana nast. celu na" + this.getNextStop().getName());
                            for (Road road : Database.getRoadList()) {
                                if (road.getFirstFrom().equals(this.getLocation()) && road.getFirstTo().equals(this.getNextStop())) {
                                    boolean goodToGo = true;
                                    if (!road.checkAtStart(this).isEmpty()) {
                                        goodToGo = false;
                                        break;
                                    }
                                    if (goodToGo) {
                                        if (this.getLocation() instanceof Crossing) {
                                            Crossing crossing = (Crossing) this.getLocation();
                                            this.changeLocation(road, 3);
                                            crossing.releaseInUse();

                                        } else {
                                            this.changeLocation(road, 3);
                                        }
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        if (this.getLocation().findCollisionsOnLocation(this).isEmpty()) {
                            if ((Math.round(this.getX() + 1) > Math.round(this.getLocation().getxEnd() - 2)
                                    && Math.round(this.getX() + 1) < Math.round(this.getLocation().getxEnd()) + 2)
                                    && (Math.round(this.getY() + 1) > Math.round(this.getLocation().getyEnd()) - 2
                                    && Math.round(this.getY() + 1) < Math.round(this.getLocation().getyEnd()) + 2)) {
                                if (this.getNextStop() instanceof Crossing) {
                                    Crossing c = (Crossing) this.getNextStop();
                                    if (c.setInUse()) {
                                        this.changeLocation(c, 2);
                                    }
                                } else {
                                    if (this.getNextStop().equals(this.getTargetDest())) {
                                        this.changeLocation(this.getTargetDest(), 1);
                                    } else {
                                        this.changeLocation(this.getNextStop(), 1);
                                    }
                                }

                            } else {
                                this.move();
                                if(Math.random() * 1000 > 997)
                                    this.setBroken(true);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Metoda wybierająca następny cel podróży kupca w zależności od posiadanych
     * przez niego towaró. Kupcy najchętniej jadą sprzedać najwartościowsze
     * towary.
     */
    @Override
    public void pickNewTarget() {
        synchronized(getMonitor()) {
            int resTypeBest = 0;
            if (!this.load_.isEmpty()) {
                for (int i = 9; i > 0; i--) {
                    for (Res res : load_) {
                        if (res.getResType() == (1 << i) && (res.getResType() & this.restrictedRes_) == 0) {
                            resTypeBest = (1 << i);
                            break;
                        }
                    }
                    if (resTypeBest == (1 << i)) {
                        break;
                    }
                }
                LinkedList<Village> choicesAvailable = new LinkedList<>();
                for (Village v : Database.getVillageList()) {
                    if (!v.isDestroyed() && (v.getResUsed() & resTypeBest) == resTypeBest) {
                        choicesAvailable.add(v);
                    }
                }
                if (choicesAvailable.isEmpty()) {
                    this.restrictedRes_ = this.restrictedRes_ | resTypeBest;
                } else {
                    this.setTargetDest(choicesAvailable.get((int) Math.round(Math.random() * (choicesAvailable.size() - 1))));
                    System.out.println("Nowy cel to " + this.getTargetDest().getName());
                    if (this.getTargetDest() != this.getLocation()) {
                        this.pickNextStop();
                    }
                }
            } else {
                this.setTargetDest(Database.getVillageList().get((int) Math.round(Math.random() * (Database.getVillageList().size() - 1))));
            }
        }
    }

    /**
     * Metoda rysująca obiekt na mapie
     *
     * @param g grafika
     * @param scale skala mapy
     */
    @Override
    public void draw(Graphics g, int scale) {
        g.setColor(Color.red);
        g.fillRect((int) Math.round(this.getX() * scale),
                (int) Math.round(this.getY() * scale),
                2 * scale,
                2 * scale);
        g.setColor(Color.PINK);
        g.drawString(surname_ + " " + this.getName(),
                (int) Math.round(this.getX() * scale - 30),
                (int) Math.round(this.getY() * scale - 20));
    }

    /**
     * Metoda zatrzymująca karawanę.
     */
    public void breakWheel() {
        this.broken_ = true;
    }

    /**
     * Metoda wznawiająca ruch karawany.
     */
    public void fixWheel() {
        this.broken_ = false;
    }

    /**
     * @return Zwraca nazwisko
     */
    public String getSurname() {
        return surname_;
    }

    /**
     * @param surname Ustawia nazwisko
     */
    public void setSurname(String surname) {
        this.surname_ = surname;
    }

    /**
     * @return Zwraca maksymalny ladunek karawany
     */
    public int getMaxCapacity() {
        return maxCapacity_;
    }

    /**
     * @param maxCapacity Ustawia maksymalny ladunek karawany
     */
    public void setMaxCapacity(int maxCapacity) {
        this.maxCapacity_ = maxCapacity;
    }

    /**
     * @return Zwraca obladowanie karawany
     */
    public int getCurCapacity() {
        return curCapacity_;
    }

    /**
     * @param curCapacity Ustawia aktualne obladowanie
     */
    public void setCurCapacity(int curCapacity) {
        this.curCapacity_ = curCapacity;
    }

    /**
     * @return Zwraca kare do szybkosci spowodowana obciazeniem
     */
    public int getSpeedPenalty() {
        return speedPenalty_;
    }

    /**
     * @param speedPenalty Ustawia kare do szybkosci
     */
    public void setSpeedPenalty(int speedPenalty) {
        this.speedPenalty_ = speedPenalty;
    }

    /**
     * @return Zwraca zawartosc portfela
     */
    public int getWallet() {
        return wallet_;
    }

    /**
     * @param wallet Ustawia zawartosc portfela
     */
    public void setWallet(int wallet) {
        this.wallet_ = wallet;
    }

    /**
     * @return Zwraca stan karawany - zepsuta czy nie
     */
    public boolean isBroken() {
        return broken_;
    }

    /**
     * @param broken Ustawia stan karawany
     */
    public void setBroken(boolean broken) {
        this.broken_ = broken;
    }

    /**
     * @return Zwraca wartosc logiczna - czy karawana jest uspiona (w miescie)
     * czy nie
     */
    public boolean isSleeping() {
        return sleeping_;
    }

    /**
     * @param sleeping Ustawia wartosc uspienia
     */
    public void setSleeping(boolean sleeping) {
        this.sleeping_ = sleeping;
    }

    /**
     * @return Zwraca ladunek
     */
    public synchronized LinkedList<Res> getLoad() {
        return load_;
    }

    /**
     * @return the restrictedRes
     */
    public int getRestrictedRes() {
        return restrictedRes_;
    }

    /**
     * @param restrictedRes the restrictedRes to set
     */
    public void setRestrictedRes(int restrictedRes) {
        this.restrictedRes_ = restrictedRes;
    }

}
