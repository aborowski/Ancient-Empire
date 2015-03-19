package antyczne.imperium;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

/**
 * Klasa wioski w symulacji.
 */
public class Village extends ConnectableLoc {

    private long counter_;
    private int population_;
    private int treasury_;
    private int resCreated_;
    private int resUsed_;
    private int maxStockSell_;
    private int maxStockBuy_;
    private int curStockSell_;
    private int curStockBuy_;
    private LinkedList<Res> resourcesKeptSell_;
    private LinkedList<Res> resourcesKeptConsumed_;
    private LinkedList<Caravan> sleepingCaravans_;
    private boolean besieged_;
    private boolean destroyed_;

    /**
     * Konstruktor wioski
     *
     * @param idGotten ID wioski
     * @throws BadMapDesignException
     */
    public Village(int idGotten) throws BadMapDesignException {
        this.setMonitor(new Object());
        int ticks = 0;
        boolean ploppable = false;
        int distance;
        //Konieczne
        this.setId(idGotten);
        while (!ploppable) {
            ploppable = true;
            this.setX(Math.round(Math.random() * 160) + 20);
            this.setxEnd(this.getX() + 5);
            this.setY(Math.round(Math.random() * 160) + 20);
            this.setyEnd(this.getY() + 5);
            for (ConnectableLoc c : Database.getLocationList()) {
                distance = 0;
                distance += Math.pow(
                        (this.getxEnd() - 3) - (c.getxEnd() - 3),
                        2);
                distance += Math.pow(
                        (this.getyEnd() - 3) - (c.getyEnd() - 3),
                        2);
                if (Math.sqrt(distance) < 40) {
                    ploppable = false;

                }
            }
            ticks++;
            if (ticks > 5000) {
                throw new BadMapDesignException();
            }
        }
        this.setName(this.createRandomName((int) Math.round(Math.random() * 5) + 5).concat(" Village"));
        this.counter_ = 1;
        this.besieged_ = false;
        //Parametry miasta poczatkowego
        this.population_ = (int) Math.round(Math.random() * 1000) + 2000;
        this.maxStockSell_ = (int) Math.round(Math.random() * 50) + 150;
        this.curStockSell_ = 0;
        this.maxStockBuy_ = (int) Math.round(Math.random() * 50) + 150;
        this.curStockBuy_ = 0;
        //Zainicjalizowanie list
        this.resourcesKeptSell_ = new LinkedList<>();
        this.resourcesKeptConsumed_ = new LinkedList<>();
        this.sleepingCaravans_ = new LinkedList<>();
        this.setListFrom(new LinkedList<ConnectableLoc>());
        this.setListTo(new LinkedList<ConnectableLoc>());
        //Surowce
        this.resCreated_ = (int) (Math.round(Math.random() * 1022) + 1);
        System.out.println("--------------------------------");
        System.out.println("PROD: " + (this.resCreated_ & (1 << 9)) + " "
                + (this.resCreated_ & (1 << 8)) + " "
                + (this.resCreated_ & (1 << 7)) + " "
                + (this.resCreated_ & (1 << 6)) + " "
                + (this.resCreated_ & (1 << 5)) + " "
                + (this.resCreated_ & (1 << 4)) + " "
                + (this.resCreated_ & (1 << 3)) + " "
                + (this.resCreated_ & (1 << 2)) + " "
                + (this.resCreated_ & (1 << 1)) + " "
                + (this.resCreated_ & 1));
        this.resUsed_ = (int) Math.round(Math.random() * 1022) + 1 & ~this.resCreated_;
        System.out.println("USED: " + (this.resUsed_ & (1 << 9)) + " "
                + (this.resUsed_ & (1 << 8)) + " "
                + (this.resUsed_ & (1 << 7)) + " "
                + (this.resUsed_ & (1 << 6)) + " "
                + (this.resUsed_ & (1 << 5)) + " "
                + (this.resUsed_ & (1 << 4)) + " "
                + (this.resUsed_ & (1 << 3)) + " "
                + (this.resUsed_ & (1 << 2)) + " "
                + (this.resUsed_ & (1 << 1)) + " "
                + (this.resUsed_ & 1));
        System.out.println("--------------------------------");
        if (this.resUsed_ == 0) {
            this.resUsed_ = (1 << ((int) Math.round(Math.random() * 9))) | 1 | ((int) Math.round(Math.random() * 1022) + 1 & ~this.resCreated_);
            this.resCreated_ = this.resCreated_ & ~this.resUsed_;
            System.out.println("POPRAWKA PROD: " + (this.resCreated_ & (1 << 9)) + " "
                    + (this.resCreated_ & (1 << 8)) + " "
                    + (this.resCreated_ & (1 << 7)) + " "
                    + (this.resCreated_ & (1 << 6)) + " "
                    + (this.resCreated_ & (1 << 5)) + " "
                    + (this.resCreated_ & (1 << 4)) + " "
                    + (this.resCreated_ & (1 << 3)) + " "
                    + (this.resCreated_ & (1 << 2)) + " "
                    + (this.resCreated_ & (1 << 1)) + " "
                    + (this.resCreated_ & 1));
            System.out.println("POPRAWKA USED: " + (this.resUsed_ & (1 << 9)) + " "
                    + (this.resUsed_ & (1 << 8)) + " "
                    + (this.resUsed_ & (1 << 7)) + " "
                    + (this.resUsed_ & (1 << 6)) + " "
                    + (this.resUsed_ & (1 << 5)) + " "
                    + (this.resUsed_ & (1 << 4)) + " "
                    + (this.resUsed_ & (1 << 3)) + " "
                    + (this.resUsed_ & (1 << 2)) + " "
                    + (this.resUsed_ & (1 << 1)) + " "
                    + (this.resUsed_ & 1));
            System.out.println("--------------------------------");
        }
        for (int i = 0; i < 10; i++) {
            if ((this.getResCreated() & 1 << i) == 1 << i) {
                this.treasury_ += (1 << i) * 10;
            }
        }
        this.setResidentList_(new LinkedList<Unit>());
    }

    /**
     * Metoda sprawdzająca, czy produkty z datą przydatności się zepsuły.
     * Sprawdza w obu listach, surowcow sprzedawanych oraz kupowanych. Surowce
     * przeterminowane zostaja usuniete bez przerobienia ich na zloto.
     */
    public void checkStockRotten() {
        synchronized (getMonitor()) {
            LinkedList<Res> removalList = new LinkedList<>();
            if (!this.resourcesKeptSell_.isEmpty()) {
                for (Res res : this.resourcesKeptSell_) {
                    if (res.getResType() == 1 || res.getResType() == 16) {
                        if (res.getTimeDue() < System.currentTimeMillis()) {
                            removalList.add(res);
                        }
                    }
                }
                for (Res r : removalList) {
                    this.resourcesKeptSell_.remove(r);
                    this.setCurStockSell(this.getCurStockSell() - r.getVolume());
                }
                removalList.clear();
            }
            if (!this.resourcesKeptConsumed_.isEmpty()) {
                for (Res res : this.resourcesKeptConsumed_) {
                    if (res.getResType() == 1 || res.getResType() == 16) {
                        if (res.getTimeDue() < System.currentTimeMillis()) {
                            removalList.add(res);
                        }
                    }
                }
                for (Res r : removalList) {
                    this.resourcesKeptConsumed_.remove(r);
                    this.setCurStockBuy(this.getCurStockBuy() - r.getVolume());
                }
                removalList.clear();
            }
        }
    }

    /**
     * Metoda wywoływana w trakcie plądrowania barbarzyńców. Pomniejsza
     * populację i usuwa towary/skarbiec.
     *
     * @param byWho Barbarzyńca plądrujący
     */
    public void bePillaged(Unit byWho) {
        synchronized (getMonitor()) {
            LinkedList<Unit> removalList = this.findCollisionsOnLocation(byWho);
            for (Unit u : removalList) {
                u.removeUnit();
            }
            this.clearResKept();
            this.clearResKeptBuy();
            this.setCurStockBuy(0);
            this.setCurStockSell(0);
            this.setBesieged(true);
            this.setTreasury(0);
            if (this.getPopulation() > 1) {
                this.setPopulation(population_ - 1);
            } else {
                this.setPopulation(0);
                this.setDestroyed(true);
                Database.decrementVillages();
            }
        }
    }

    /**
     * Metoda szukająca danego typu towaru w składzie
     *
     * @param type Typ surowca, ktory jest poszukiwany
     * @return Zwraca referencję na towar danego typu na skłądzie lub null gdy
     * takowego nie ma
     */
    public Res checkStockForGoods(int type) {
        synchronized (getMonitor()) {
            Res r = null;
            for (Res res : resourcesKeptSell_) {
                if (res.getResType() == type) {
                    r = res;
                    break;
                }
            }
            return r;
        }
    }

    /**
     * Metoda dodająca towar do składu. Sprawdzanie czy towar przekroczy
     * zawartosc magazynu jest zewnętrzna!
     *
     * @param goods Referencja na towar.
     * @param sellable Zmienna decydujaca do ktorej listy dodajemy (true = lista
     * produktow)
     */
    public void addToStock(Res goods, boolean sellable) {
        synchronized (getMonitor()) {
            if (sellable) {
                this.resourcesKeptSell_.add(goods);
                this.curStockSell_ += goods.getVolume();
            } else {
                this.resourcesKeptConsumed_.add(goods);
                this.curStockBuy_ += goods.getVolume();
            }
        }
    }

    /**
     * Metoda usuwająca towar ze składu
     *
     * @param goods Referencja na towar.
     */
    public void removeFromStock(Res goods) {
        synchronized (getMonitor()) {
            if (this.resourcesKeptSell_.remove(goods)) {
                this.curStockSell_ -= goods.getVolume();
            }
        }
    }

    /**
     * Metoda zużywająca część towarów na składzie (Konsumpcja).
     */
    public void useSomeStock() {
        synchronized (getMonitor()) {
            Res r;
            if (!this.resourcesKeptConsumed_.isEmpty()) {
                r = this.resourcesKeptConsumed_.removeFirst();
                this.curStockBuy_ -= r.getVolume();
                this.population_ += (r.calculateVal() / 10);
            }
        }
    }

    /**
     * Metoda rysująca obiekt
     *
     * @param g grafika
     * @param scale skala na mapie
     */
    @Override
    public void draw(Graphics g, int scale) {
        g.setColor(Color.CYAN);
        g.fillRect((int) Math.round(this.getX() * scale),
                (int) Math.round(this.getY() * scale),
                (int) Math.round((this.getxEnd() - this.getX()) * scale),
                (int) Math.round((this.getyEnd() - this.getY()) * scale));
        g.drawString(this.getName(),
                (int) Math.round(this.getX() * scale - 30),
                (int) Math.round(this.getY() * scale - 20));
    }

    /**
     * Metoda produkująca surowiec
     *
     * @param type Typ surowca do wyprodukowania
     * @return Czy wymagana jest karawana (czy magazyn jest pełen)
     */
    public synchronized boolean produceStuff(int type) {
        synchronized (getMonitor()) {
            Res resTemp = new Res(Database.getIdCount(), type);
            if (this.getCurStockSell() + resTemp.getVolume() <= this.getMaxStock()) {
                if (this.getTreasury() >= (resTemp.calculateVal() / 5) * 2) {
                    this.addToStock(resTemp, true);
                    this.setTreasury(this.getTreasury() - ((resTemp.calculateVal() / 5) * 2));
                    System.out.println(this.getName() + " produced " + String.valueOf(type));
                } else {
                    //System.out.println(this.getName() + " is too poor to produce " + String.valueOf(1<<i));
                }
            } else {
                //System.out.println(this.getName() + " has no place in stock: " + String.valueOf(this.getCurStockSell()) + " " + String.valueOf(resTemp.getVolume()));
                return true;
            }
            return false;
        }
    }

    /**
     * Metoda obsługująca stan miasta. Kontroluje produkcje, konsumpcje,
     * wysyłanie karawany, oraz zbiory podatków zgodnie z odstępami czasowymi.
     */
    @Override
    public void update() {
        if (!this.isDestroyed()) {
            if (!this.isBesieged()) {
                boolean needToSend = false;
                for (int i = 9; i >= 0; i--) {
                    if (((this.getResCreated() & 1 << i) == 1 << i) && (this.getCounter_() % ((1 << i) * 100) == 0)) {
                        needToSend = this.produceStuff(1 << i);
                    }
                }
                if (needToSend) {
                    this.sendCaravan();
                }
                this.checkStockRotten();
                if ((this.getCounter_() % 100) == 0) {
                    if (this.getResourcesKeptConsumed().isEmpty()) {
                        //System.out.println(this.getName() + " has nothing to consume");
                    } else {
                        this.useSomeStock();
                        System.out.println("Konsumuje");
                        this.changeSleepingCaravans(null, false);
                    }
                    this.setTreasury(this.getTreasury() + (int) (this.getPopulation() / 20));
                    //System.out.println("It is payday in " + this.getName());
                    //System.out.println("Treasury at " + this.treasury_);
                }
                this.setCounter_(this.getCounter_() + 1);
            } else {
                boolean stillUnderSiege = false;
                for (Unit u : this.getResidentList_()) {
                    if (u instanceof Barbarians) {
                        stillUnderSiege = true;
                    }
                }
                if (!stillUnderSiege) {
                    this.setBesieged(false);
                }
            }
        }
    }

    /**
     * Metoda synchroniczna uzywana do mieszania w śpiacych karawanach. Gdy
     * zdejmujemy karawane automatycznie ustawiamy jej uśpienie na false
     *
     * @param c Referencja na karawanę - gdy add == false może być null
     * @param add Czy dodajemy na liste czy nie
     * @return Metoda zwraca true gdy dodajemy, a zależnie od tego czy udało się
     * coś zdjąć z listy true lub false.
     */
    public boolean changeSleepingCaravans(Caravan c, boolean add) {
        synchronized (getMonitor()) {
            if (add) {
                this.sleepingCaravans_.add(c);
                return true;
            } else {
                if (!this.sleepingCaravans_.isEmpty()) {
                    Caravan caravan = this.sleepingCaravans_.removeFirst();
                    caravan.setSleeping(false);
                    return true;
                } else {
                    return false;
                }
            }
        }
    }

    /**
     * Metoda która zależnie od pola sleepingCaravans albo budzi śpiącą
     * karawanę, albo tworzy nową. Tworzyć karawany można tylko poniżej limitu
     * (Aktualnie 50).
     */
    public void sendCaravan() {
        synchronized (getMonitor()) {
            if (this.changeSleepingCaravans(null, false) == false) {
                int minimalWage = 0;
                for (int i = 0; i < 10; i++) {
                    if ((this.getResCreated() & 1 << i) == 1 << i) {
                        minimalWage = (1 << i) * 200;
                        break;
                    }
                }
                if (this.getTreasury() > minimalWage && Database.getCaravanList().size() < 50) {
                    Caravan c = new Caravan(Database.getNextID(), this.getX() + 3, this.getY() + 3, this);
                    Database.appendToCaravanList(c);
                    c.setWallet(minimalWage);
                    this.setTreasury(this.getTreasury() - minimalWage);
                    c.setSleeping(false);
                }
            }
        }
    }

    /**
     * @return the counter_
     */
    public long getCounter_() {
        return counter_;
    }

    /**
     * @param counter_ the counter_ to set
     */
    public void setCounter_(long counter_) {
        this.counter_ = counter_;
    }

    /**
     * @return the destroyed
     */
    public boolean isDestroyed() {
        return destroyed_;
    }

    /**
     * @param destroyed the destroyed to set
     */
    public void setDestroyed(boolean destroyed) {
        synchronized (getMonitor()) {
            this.destroyed_ = destroyed;
        }
    }

    /**
     * @return the population
     */
    public int getPopulation() {
        return population_;
    }

    /**
     * @param population the population to set
     */
    public void setPopulation(int population) {
        synchronized (getMonitor()) {
            this.population_ = population;
        }
    }

    /**
     * @return the treasury
     */
    public int getTreasury() {
        return treasury_;
    }

    /**
     * @param treasury the treasury to set
     */
    public void setTreasury(int treasury) {
        synchronized (getMonitor()) {
            this.treasury_ = treasury;
        }
    }

    /**
     * @return the resCreated
     */
    public int getResCreated() {
        return resCreated_;
    }

    /**
     * @param resCreated the resCreated to set
     */
    public void setResCreated(int resCreated) {
        this.resCreated_ = resCreated;
    }

    /**
     * @return the resUsed
     */
    public int getResUsed() {
        return resUsed_;
    }

    /**
     * @param resUsed the resUsed to set
     */
    public void setResUsed(int resUsed) {
        this.resUsed_ = resUsed;
    }

    /**
     * @return the maxStock
     */
    public int getMaxStock() {
        return maxStockSell_;
    }

    /**
     * @param maxStock the maxStock to set
     */
    public void setMaxStock(int maxStock) {
        this.maxStockSell_ = maxStock;
    }

    /**
     * @return the curStock
     */
    public int getCurStockSell() {
        return curStockSell_;
    }

    /**
     * @param curStock the curStock to set
     */
    public void setCurStockSell(int curStock) {
        this.curStockSell_ = curStock;
    }

    /**
     * @return the resourcesKept
     */
    public LinkedList<Res> getResourcesKept() {
        return resourcesKeptSell_;
    }

    /**
     * @param resourcesKept the resourcesKept to set
     */
    public void setResourcesKept(LinkedList<Res> resourcesKept) {
        this.resourcesKeptSell_ = resourcesKept;
    }

    /**
     * Czyszczenie listy surowcow na sprzedarz
     */
    public void clearResKept() {
        synchronized (getMonitor()) {
            this.resourcesKeptSell_.clear();
        }
    }

    /**
     * @return the sleepingCaravans
     */
    public LinkedList<Caravan> getSleepingCaravans() {
        return sleepingCaravans_;
    }

    /**
     * @param sleepingCaravans the sleepingCaravans to set
     */
    public void setSleepingCaravans(LinkedList<Caravan> sleepingCaravans) {
        this.sleepingCaravans_ = sleepingCaravans;
    }

    /**
     * @return the besieged
     */
    public boolean isBesieged() {
        return besieged_;
    }

    /**
     * @param besieged the besieged to set
     */
    public void setBesieged(boolean besieged) {
        synchronized (getMonitor()) {
            this.besieged_ = besieged;
        }
    }

    /**
     * @return the maxStockBuy_
     */
    public int getMaxStockBuy() {
        return maxStockBuy_;
    }

    /**
     * @param maxStockBuy_ the maxStockBuy_ to set
     */
    public void setMaxStockBuy(int maxStockBuy_) {
        this.maxStockBuy_ = maxStockBuy_;
    }

    /**
     * @return the curStockBuy_
     */
    public int getCurStockBuy() {
        return curStockBuy_;
    }

    /**
     * @param curStockBuy_ the curStockBuy_ to set
     */
    public void setCurStockBuy(int curStockBuy_) {
        this.curStockBuy_ = curStockBuy_;
    }

    /**
     * @return the resourcesKeptConsumed_
     */
    public LinkedList<Res> getResourcesKeptConsumed() {
        return resourcesKeptConsumed_;
    }

    /**
     * @param resourcesKeptConsumed_ the resourcesKeptConsumed_ to set
     */
    public void setResourcesKeptConsumed(LinkedList<Res> resourcesKeptConsumed_) {
        this.resourcesKeptConsumed_ = resourcesKeptConsumed_;
    }

    /**
     * Metoda czyszczaca magazyn surowcow skupowanych
     */
    public void clearResKeptBuy() {
        synchronized (getMonitor()) {
            this.resourcesKeptSell_.clear();
        }
    }
}
