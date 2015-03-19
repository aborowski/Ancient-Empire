/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antyczne.imperium;

import java.awt.Graphics;
import java.util.*;

/**
 * Metoda klasy wymaganej do polimorfizmu
 */
public abstract class ConnectableLoc extends ObjUpdatable {

    private Object monitor;
    private LinkedList<ConnectableLoc> listFrom_;
    private LinkedList<ConnectableLoc> listTo_;
    private LinkedList<Unit> residentList_;

    /**
     * Metoda znajdująca odpowiednie "kolizje" dla odpowiednich rodzajów
     * jednostek, tj. karawany szukają kolizji ze wszystkimi jednostkami,
     * barbarzyńcy szukają karawan, a legioniści barbarzyńców Metoda działa dla
     * dróg jak i dla skrzyżowań/wiosek
     *
     * @param testWith Jednostka testująca (dla niej są szukane wyniki)
     * @return Lista jednostek znalezionych jako "kolizje". Pusta oznacza brak
     * jednostek w danym miejscu.
     */
    public LinkedList<Unit> findCollisionsOnLocation(Unit testWith) {
        synchronized (monitor) {
            LinkedList<Unit> returnList = new LinkedList<>();
            for (Unit u : this.getResidentList_()) {
                if (this instanceof Road) {
                    double hyp = Math.sqrt(Math.pow(this.getxEnd() - this.getX(), 2)
                            + Math.pow(this.getyEnd() - this.getY(), 2));
                    if ((testWith.getX() + ((this.getxEnd() - this.getX()) / hyp) * 2 > u.getX() - 0.5
                            && testWith.getX() + ((this.getxEnd() - this.getX()) / hyp) * 2 < u.getX() + 0.5
                            && testWith.getY() + ((this.getyEnd() - this.getY()) / hyp) * 2 > u.getY() - 0.5
                            && testWith.getY() + ((this.getyEnd() - this.getY()) / hyp) * 2 < u.getY() + 0.5)
                            || (testWith.getX() + ((this.getxEnd() - this.getX()) / hyp) * 2 > u.getX() - 1.5
                            && testWith.getX() + ((this.getxEnd() - this.getX()) / hyp) * 2 < u.getX() + 0.5
                            && testWith.getY() + ((this.getyEnd() - this.getY()) / hyp) * 2 > u.getY() - 1.5
                            && testWith.getY() + ((this.getyEnd() - this.getY()) / hyp) * 2 < u.getY() + 0.5)) {
                        if (testWith instanceof Caravan) {
                            if (!u.equals(testWith)) {
                                returnList.add(u);
                                break;
                            }
                        } else {
                            if (testWith instanceof Legion) {
                                if (u instanceof Barbarians) {
                                    returnList.add(u);
                                    break;
                                }
                            } else {
                                if (u instanceof Caravan) {
                                    returnList.add(u);
                                }
                            }
                        }
                    }
                } else {
                    if (testWith instanceof Legion) {
                        if (u instanceof Barbarians) {
                            returnList.add(u);
                            break;
                        }
                    } else {
                        if (u instanceof Caravan) {
                            returnList.add(u);
                        }
                    }
                }
            }
            return returnList;
        }
    }

    /**
     * Metoda szukająca "kolizji" na początku drogi. Szuka różnych rodzajów
     * jednostek zależnie od tego co jest parametrem testWith.
     *
     * @param testWith Jednostka dla której szukamy "kolizji"
     * @return Lista jednostek znalezionych. Pusta oznacza brak jednostek na
     * początku drogi.
     */
    public LinkedList<Unit> checkAtStart(Unit testWith) {
        synchronized (this.getMonitor()) {
            LinkedList<Unit> returnList = new LinkedList<>();
            for (Unit u : this.getResidentList_()) {
                if (u.getX() > this.getX() - 2
                        && u.getX() < this.getX() + 2
                        && u.getY() > this.getY() - 2
                        && u.getY() < this.getY() + 2) {
                    if (testWith instanceof Barbarians) {
                        if (u instanceof Caravan) {
                            returnList.add(u);
                        }
                    } else {
                        if (testWith instanceof Legion) {
                            if (u instanceof Barbarians) {
                                returnList.add(u);
                                break;
                            }
                        } else {
                            if (!u.equals(testWith)) {
                                returnList.add(u);
                                break;
                            }
                        }
                    }
                }
            }
            return returnList;
        }
    }

    /**
     * Czyszczenie listy listFrom.
     */
    public void clearListFrom() {
        listFrom_.clear();
    }

    /**
     * @return PPierwsza lokacja na liście listFrom.
     */
    public ConnectableLoc getFirstFrom() {
        return this.getListFrom().getFirst();
    }

    /**
     * @return the listFrom
     */
    public LinkedList<ConnectableLoc> getListFrom() {
        return listFrom_;
    }

    /**
     * @param listFrom the listFrom to set
     */
    public void setListFrom(LinkedList<ConnectableLoc> listFrom) {
        this.listFrom_ = listFrom;
    }

    /**
     * @param element element to be added to ListFrom
     */
    public void addElementFrom(ConnectableLoc element) {
        this.listFrom_.addLast(element);
    }

    /**
     * @param element element to be removed from ListFrom
     */
    public void removeElementFrom(ConnectableLoc element) {
        this.listFrom_.remove(element);
    }

    /**
     * @return the listTo
     */
    public LinkedList<ConnectableLoc> getListTo() {
        return listTo_;
    }

    /**
     * Czyszczenie listy listTo.
     */
    public void clearListTo() {
        listTo_.clear();
    }

    /**
     * @return Pierwsza lokacja na liście listTo.
     */
    public ConnectableLoc getFirstTo() {
        return this.getListTo().getFirst();
    }

    /**
     * @param listTo the listTo to set
     */
    public void setListTo(LinkedList<ConnectableLoc> listTo) {
        this.listTo_ = listTo;
    }

    /**
     * @param element element to be added to ListTo
     */
    public void addElementTo(ConnectableLoc element) {
        this.listTo_.addLast(element);
    }

    /**
     * @param element element to be removed from ListTo
     */
    public void removeElementTo(ConnectableLoc element) {
        this.listTo_.remove(element);
    }

    /**
     * @return the residentList_
     */
    public LinkedList<Unit> getResidentList_() {
        synchronized (monitor) {
            return residentList_;
        }
    }

    /**
     * @param residentList_ the residentList_ to set
     */
    public void setResidentList_(LinkedList<Unit> residentList_) {
        synchronized (monitor) {
            this.residentList_ = residentList_;
        }
    }

    /**
     * @param element element to be added to residentList
     */
    public void addResident(Unit element) {
        synchronized (monitor) {
            this.residentList_.addLast(element);
        }
    }

    /**
     * @param element element to be removed from residentList
     */
    public void removeResident(Unit element) {
        synchronized (monitor) {
            this.residentList_.remove(element);
        }
    }

    /**
     * @return the monitor
     */
    public Object getMonitor() {
        return monitor;
    }

    /**
     * @param monitor the monitor to set
     */
    public void setMonitor(Object monitor) {
        this.monitor = monitor;
    }
}
