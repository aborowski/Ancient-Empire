package antyczne.imperium;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;

/**
 * Klasa barbarzyńcy w symulatorze.
 */
public class Barbarians extends Unit {

    private int population_;
    private String weaponry_;
    private boolean isPillaging_;
    private boolean outOfMap;

    /**
     * Konstruktor barbarzyńcy. Parametry wymagane dla konstruktora nadrzędnego
     * (głównie)
     *
     * @param idGotten ID obiektu
     * @param putX Pozycja X obiektu
     * @param putY Pozycja Y obiektu
     * @param whereAmI Lokacja obiektu
     */
    public Barbarians(int idGotten, double putX, double putY, ConnectableLoc whereAmI) {
        super(idGotten, putX, putY, whereAmI);
        this.setName(this.getName().concat(" Barbs"));
        this.population_ = (int) Math.round(Math.random() * 1000);
        this.pickWeapon();
        this.isPillaging_ = false;
        this.outOfMap = true;
        this.setSpeed(3);
        this.setTargetDest(whereAmI);
    }

    /**
     * Wybieranie broni z listy.
     */
    private void pickWeapon() {
        int num = (int) Math.round(Math.random() * 5);
        switch (num) {
            case 0:
                this.weaponry_ = "Sharp sticks";
                break;
            case 1:
                this.weaponry_ = "Spears";
                break;
            case 2:
                this.weaponry_ = "Swords";
                break;
            case 3:
                this.weaponry_ = "Bows";
                break;
            case 4:
                this.weaponry_ = "Tridents";
                break;
            case 5:
                this.weaponry_ = "Some odd potions";
                break;
        }
    }

    /**
     * Metoda update zajmuje się obsługą aktualnego stanu barbarzyńcy oraz tego
     * w jaki sposób się zachowuje będąc w danym miejscu. Zajmuje się m. in.
     * zmianą celu gdy miasto upadnie, lub zmianą lokacji na drogi i
     * skrzyżowania/wioski
     */
    @Override
    public void update() {
        synchronized(getMonitor()) {
            if (this.outOfMap && this.getTargetDest() == null) {
                int minDist = 10000;
                for (Crossing c : Database.getCrossingList()) {
                    int curDist = 0;
                    curDist += Math.pow(this.getX() - c.getX(), 2);
                    curDist += Math.pow(this.getY() - c.getY(), 2);
                    if (Math.sqrt(curDist) < minDist) {
                        minDist = (int) Math.sqrt(curDist);
                        this.setTargetDest(c);
                    }
                }
            } else {
                if (this.outOfMap) {
                    if (this.getX() < this.getTargetDest().getxEnd() + 1
                            && this.getX() > this.getTargetDest().getX() - 1
                            && this.getY() < this.getTargetDest().getyEnd() + 1
                            && this.getY() > this.getTargetDest().getY() - 1) {
                        this.changeLocation(this.getTargetDest(), 2);
                        this.outOfMap = false;
                    } else {
                        this.outOfMapMove();
                    }
                } else {
                    if (this.getLocation().equals(this.getTargetDest())) {
                        if (this.getLocation() instanceof Crossing) {
                            this.pickNewTarget();
                        } else {
                            Village v = (Village) this.getLocation();
                            if (v.isDestroyed()) {
                                this.setIsPillaging(false);
                                this.pickNewTarget();
                            } else {
                                this.pillage();
                            }
                        }
                    } else {
                        if (this.getLocationType() == 2 || this.getLocationType() == 1) {
                            this.pickNextStop();
                            for (Road road : Database.getRoadList()) {
                                if (road.getFirstFrom().equals(this.getLocation()) && road.getFirstTo().equals(this.getNextStop())) {
                                    LinkedList<Unit> removalList = road.checkAtStart(this);
                                    if (!removalList.isEmpty()) {
                                        for (Unit u : removalList) {
                                            u.removeUnit();
                                        }
                                    }
                                    if (this.getLocation() instanceof Crossing) {
                                        Crossing c = (Crossing) this.getLocation();
                                        this.changeLocation(road, 3);
                                        c.releaseInUse();

                                    } else {
                                        this.changeLocation(road, 3);
                                    }

                                }
                            }
                        } else {
                            if ((Math.round(this.getX() + 1) > Math.round(this.getLocation().getxEnd() - 2)
                                    && Math.round(this.getX() + 1) < Math.round(this.getLocation().getxEnd()) + 2)
                                    && (Math.round(this.getY() + 1) > Math.round(this.getLocation().getyEnd()) - 2
                                    && Math.round(this.getY() + 1) < Math.round(this.getLocation().getyEnd()) + 2)) {
                                if (this.getNextStop() instanceof Crossing) {
                                    Crossing c = (Crossing) this.getNextStop();
                                    if (c.setInUse()) {
                                        this.changeLocation(c, 2);
                                    }
                                } else
                                    this.changeLocation(this.getNextStop(), 1);
                            } else {
                                this.move();
                                LinkedList<Unit> foundOnTrack = this.getLocation().findCollisionsOnLocation(this);
                                if (!foundOnTrack.isEmpty()) {
                                    for (Unit u : foundOnTrack) {
                                        u.removeUnit();
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Barbarzyńco, plądruj!
     */
    private void pillage() {
        this.setIsPillaging(true);
        Village v = (Village) this.getLocation();
        v.bePillaged(this);
    }

    /**
     * Przemieszczanie się, gdy barbarzyńca jest poza mapą.
     */
    private void outOfMapMove() {
        double hyp = Math.sqrt(Math.pow(this.getTargetDest().getX() - this.getX(), 2)
                + Math.pow(this.getTargetDest().getY() - this.getY(), 2));
        this.setX(this.getX() + ((this.getTargetDest().getxEnd() - this.getX()) / hyp));
        this.setY(this.getY() + ((this.getTargetDest().getyEnd() - this.getY()) / hyp));
        this.updateSize();
    }

    /**
     * Wybieranie nowego celu dla barbarzyńcy. Najbliższa wioska -
     * geometrycznie, nie liczbowo ile dróg do pokonania.
     */
    @Override
    public void pickNewTarget() {
        synchronized(getMonitor()) {
            int minDist = 10000;
            for (Village v : Database.getVillageList()) {
                int curDist = 0;
                if (!v.isDestroyed()) {
                    curDist += Math.pow(this.getX() - v.getX(), 2);
                    curDist += Math.pow(this.getY() - v.getY(), 2);
                    if (Math.sqrt(curDist) < minDist) {
                        minDist = (int) Math.sqrt(curDist);
                        this.setTargetDest(v);
                        this.pickNextStop();
                    }
                }
            }
        }
    }

    /**
     * Rysowanie barbarzyńcy
     *
     * @param g grafika
     * @param scale skala mapy
     */
    @Override
    public void draw(Graphics g, int scale) {
        g.setColor(Color.YELLOW);
        g.fillRect((int) Math.round(this.getX() * scale),
                (int) Math.round(this.getY() * scale),
                2 * scale,
                2 * scale);
        g.setColor(Color.PINK);
        g.drawString(this.getName(),
                (int) Math.round(this.getX() * scale - 30),
                (int) Math.round(this.getY() * scale - 20));
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
        this.population_ = population;
    }

    /**
     * @return the weaponry
     */
    public String getWeaponry() {
        return weaponry_;
    }

    /**
     * @param weaponry the weaponry to set
     */
    public void setWeaponry(String weaponry) {
        this.weaponry_ = weaponry;
    }

    /**
     * @return the isPillaging
     */
    public boolean isIsPillaging() {
        return isPillaging_;
    }

    /**
     * @param isPillaging the isPillaging to set
     */
    public void setIsPillaging(boolean isPillaging) {
        this.isPillaging_ = isPillaging;
    }

}
