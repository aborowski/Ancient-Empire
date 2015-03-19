package antyczne.imperium;

import java.awt.Color;
import java.awt.Graphics;
import java.io.*;
import java.util.*;

/**
 * Klasa legionu w symulacji.
 */
public class Legion extends Unit {

    private Unit barbRef_;

    /**
     * Konstruktor legionu
     *
     * @param idGotten ID legionu
     * @param putX Pozycja X
     * @param putY Pozycja Y
     * @param whereAmI Lokacja, w której ma się legion pojawić
     */
    public Legion(int idGotten, double putX, double putY, ConnectableLoc whereAmI) {
        super(idGotten, putX, putY, whereAmI);
        this.setSpeed(2);
        this.setName(this.getName().concat(" Legion"));
        this.barbRef_ = null;
        this.setTargetDest(whereAmI);
        this.changeLocation(whereAmI, 1);
    }

    /**
     * Metoda usuwająca legion oraz barbarzyńców (oraz część populacji wioski
     * jeśli w takiej się znajdują).
     */
    public void doBattle(Barbarians b) {
        if (!(this.getLocation() instanceof Crossing) && !(this.getLocation() instanceof Road)) {
            Village v = (Village) this.getLocation();
            v.setPopulation(v.getPopulation() - (int) (b.getPopulation() / 10));
            b.removeUnit();
            this.removeUnit();
        } else {
            b.removeUnit();
            this.removeUnit();
        }
    }

    /**
     * Metoda sprawdzająca, czy legion dogonił barbarzyńców na drodze.
     */
    public void patrolOnRoad() {
        LinkedList<Unit> removalList = this.getLocation().findCollisionsOnLocation(this);
        if (!removalList.isEmpty()) {
            this.doBattle((Barbarians) removalList.getFirst());
        }
    }

    /**
     * Metoda wybierająca dla legionu nowe plemię-cel.
     */
    public void pickNewBarb() {
        if (!Database.getBarbarianList().isEmpty()) {
            this.barbRef_ = Database.getBarbarianList().get((int) Math.round(Math.random() * (Database.getBarbarianList().size() - 1)));
            this.pickNewTarget();
            this.pickNextStop();
        } else {
            this.barbRef_ = null;
        }
    }
    
    /**
     * Przeklejenie celu barbarzyńcy do legionu
     */
    @Override
    public void pickNewTarget() {
       this.setTargetDest(barbRef_.getTargetDest());
    }

    /**
     * Metoda obsługująca zachowanie się legionu w odpowiednich sytuacjach na
     * mapie, jak np. zmiana lokacji, przemieszczanie, sprawdzanie "kolizji" z
     * barbarzyńcami itp.
     */
    @Override
    public void update() {
        if (this.barbRef_ != null) {
            boolean myTargetIsAlive = false;
            for (Barbarians b : Database.getBarbarianList()) {
                if (this.barbRef_.equals(b)) {
                    myTargetIsAlive = true;
                }
            }
            if (!myTargetIsAlive) {
                this.pickNewBarb();
            }
        }
        if ((this.getLocation().equals(this.getTargetDest()) && (this.barbRef_ == null))) {
            if (Database.getBarbarianList().isEmpty()) {
                this.setTargetDest(this.getLocation());
            } else {
                this.pickNewBarb();
            }
        } else {
            if ((this.getLocation().equals(this.getTargetDest())) && (this.barbRef_ != null)) {
                if (this.barbRef_.getTargetDest().equals(this.getLocation())) {
                    LinkedList<Unit> removalList = this.getLocation().findCollisionsOnLocation(this);
                    if (!removalList.isEmpty()) {
                        this.doBattle((Barbarians) removalList.getFirst());
                    }
                } else {
                    this.setTargetDest(this.barbRef_.getTargetDest());
                }
            } else {
                if (this.getLocationType() == 2 || this.getLocationType() == 1) {
                    this.pickNextStop();
                    for (Road road : Database.getRoadList()) {
                        if (road.getFirstFrom().equals(this.getLocation()) && road.getFirstTo().equals(this.getNextStop())) {
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
                        this.patrolOnRoad();
                    }
                }
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
        g.setColor(Color.WHITE);
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
     * @return the barbRef
     */
    public Unit getBarbRef() {
        return barbRef_;
    }

    /**
     * @param barbRef the barbRef to set
     */
    public void setBarbRef(Unit barbRef) {
        this.barbRef_ = barbRef;
    }

}
