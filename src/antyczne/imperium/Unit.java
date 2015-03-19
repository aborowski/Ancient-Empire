package antyczne.imperium;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Klasa jednostki na potrzeby polimorfizmu.
 */
public abstract class Unit extends ObjUpdatable implements Runnable {

    private Object monitor;
    private boolean running_;
    private Thread thread_;
    private ConnectableLoc targetDest_;
    private ConnectableLoc nextStop_;
    private int speed_; // Predkosc, im mniej tym lepiej (od 1 do 3)
    private ConnectableLoc location_;
    private int locationType_;
    private LinkedList<ConnectableLoc> route_;

    /**
     * Konstruktor jednostki
     *
     * @param idGotten ID jednostki
     * @param putX Pozycja X
     * @param putY Pozycja Y
     * @param whereAmI Lokacja początkowa
     */
    public Unit(int idGotten, double putX, double putY, ConnectableLoc whereAmI) {
        monitor = new Object();
        this.setId(idGotten);
        this.setX(putX);
        this.setxEnd(this.getX() + 1);
        this.setY(putY);
        this.setyEnd(this.getY() + 1);
        this.setName(this.createRandomName((int) Math.round(Math.random() * 5) + 2));
        this.speed_ = (int) (Math.round(Math.random() * 2) + 1);
        this.location_ = whereAmI;
        this.route_ = new LinkedList<>();

        this.running_ = true;
        this.thread_ = new Thread(this);
        this.thread_.setDaemon(true);
        this.thread_.start();
    }

    /**
     * Metoda przypisująca obiektowi nowy cel podróży. Cel będzie różny dla
     * różnych obiektów
     */
    public abstract void pickNewTarget();

    /**
     * Metoda wybierająca następny cel pośredni pomiędzy jednostką a celem, lub
     * tworząca drogę pośrednich celów jeśli taka jeszcze nie istnieje.
     */
    public void pickNextStop() {
        synchronized(monitor) {
            int roadsTravelledLeast;
            if (this.route_.isEmpty()) {
                LinkedList<ConnectableLoc> visitedList = new LinkedList<>();
                if (this.getLocationType() != 3) {
                    roadsTravelledLeast = this.recurringDepthSearch(0, 10, this.location_, this.route_, visitedList);
                } else {
                    roadsTravelledLeast = this.recurringDepthSearch(0, 10, this.nextStop_, this.route_, visitedList);
                }
                if (roadsTravelledLeast > 0 && roadsTravelledLeast < 10) {
                    System.out.println("Znaleziono droge");
                } else {
                    System.out.println("Nie znaleziono drogi");
                }

            } else {
                this.nextStop_ = this.route_.removeLast();
            }
        }
    }

    /**
     * Metoda znajdująca najkrótszą drogę do celu i zapisująca ją jako listę.
     * Rekurencyjna.
     *
     * @param depth Glebokosc wejsciowa danego wierzcholka w grafie (w aktualnym
     * przeszukiwaniu)
     * @param vertex Wierzcholek w ktorym odbywa sie przeszukiwanie
     * @param visitedList Lista wierzcholkow poprzednio przeszukanych
     * @param route Droga do celu
     * @return Zwraca dlugosc drogi do celu (od 1 do 9) lub 0 w przypadku gdy
     * maksymalna dlugosc drogi jest co najmniej 10 Drogi wtedy nie ma.
     */
    private int recurringDepthSearch(int depth, int curMaxDepth, ConnectableLoc vertex, LinkedList<ConnectableLoc> route, LinkedList<ConnectableLoc> visitedList) {
        synchronized(monitor) {
            int tempVal;
            int tempMaxDepth = curMaxDepth;
            if (visitedList.containsAll(vertex.getListTo())) {
                System.out.println("Brak dalszej drogi od " + vertex.getName());
                return 10;
            }
            if (depth + 1 < tempMaxDepth) {
                for (ConnectableLoc c : vertex.getListTo()) {
                    if (!visitedList.contains(c)) {
                        if (c.equals(this.targetDest_)) {
                            if (depth + 1 < tempMaxDepth) {
                                route.clear();
                                route.add(c);
                                if (depth != 0) {
                                    route.add(vertex);
                                }
                                return depth + 1;
                            }
                        } else {
                            if (c instanceof Crossing) {
                                visitedList.add(vertex);
                                tempVal = recurringDepthSearch(depth + 1, tempMaxDepth, c, route, visitedList);
                                visitedList.remove(vertex);
                                if (tempVal < tempMaxDepth) {
                                    tempMaxDepth = tempVal;
                                }
                            }
                        }
                    }
                }
                if (depth == 0 && tempMaxDepth >= 10) {
                    route.clear();
                    return 0;
                } else {
                    if (tempMaxDepth < curMaxDepth) {
                        if (depth != 0) {
                            route.add(vertex);
                        }
                        return tempMaxDepth;
                    } else {
                        return 10;
                    }
                }
            } else {
                return 10;
            }
        }
    }

    /**
     * Metoda przemieszczająca jednostkę. Uzyteczna tylko w przypadku gdy
     * aktualna lokacja jednostki to droga
     */
    public void move() {
        synchronized(monitor) {
            double hyp = Math.sqrt(Math.pow(this.getLocation().getxEnd() - this.getLocation().getX(), 2)
                    + Math.pow(this.getLocation().getyEnd() - this.getLocation().getY(), 2));
            this.setX(this.getX() + ((this.getLocation().getxEnd() - this.getLocation().getX()) / hyp));
            this.setY(this.getY() + ((this.getLocation().getyEnd() - this.getLocation().getY()) / hyp));
            this.updateSize();
        }
    }

    /**
     * Metoda zmieniająca lokację w której ta jednostka się znajduje, na
     * potrzeby poruszania sie po "grafie"
     *
     * @param loc nowa lokacja
     * @param type typ nowej lokacji (3 - droga, 2 - skrzyzowanie, 1 - miasto)
     */
    public void changeLocation(ConnectableLoc loc, int type) {
        synchronized(monitor) {
            if (type != 3) {
                this.setX(loc.getX() + 1);
                this.setY(loc.getY() + 1);
            } else {
                this.setX(loc.getX() - 1);
                this.setY(loc.getY() - 1);
            }
            this.updateSize();
            if (this.location_ != null) {
                if (!this.location_.getResidentList_().isEmpty()) {
                    this.location_.removeResident(this);
                }
            }
            loc.addResident(this);
            this.setLocation(loc);
            this.setLocationType(type);
        }
    }

    /**
     * Usuwanie jednostki
     */
    public void removeUnit() {
        synchronized(monitor) {
            if (this instanceof Barbarians) {
                Database.removeBarbFromList((Barbarians) this);
            } else {
                Database.removeLegionFromList((Legion) this);
            }
            this.setRunning(false);
            if (this.getLocation() instanceof Crossing) {
                Crossing c = (Crossing) this.getLocation();
                c.releaseInUse();
            }
            this.getLocation().removeResident(this);
        }
    }

    /**
     * Metoda pętli głównej danego wątku. Usypiana na czas zależny od szybkości
     * jednostki.
     */
    @Override
    public void run() {
        while (this.isRunning()) {
            update();
            try {
                this.getThread_().sleep(1000 / (60 / (this.getSpeed())));
            } catch (InterruptedException ex) {
                Logger.getLogger(Caravan.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Metoda przypisująca odpowiednie wartości dla "końcowych" wartości X i Y
     * jednostki.
     */
    public void updateSize() {
        this.setxEnd(this.getX() + 1);
        this.setyEnd(this.getY() + 1);
    }

    /**
     * @return the targetDest
     */
    public ConnectableLoc getTargetDest() {
        return targetDest_;
    }

    /**
     * @param targetDest the targetDest to set
     */
    public void setTargetDest(ConnectableLoc targetDest) {
        this.targetDest_ = targetDest;
    }

    /**
     * @return the nextStop
     */
    public ConnectableLoc getNextStop() {
        return nextStop_;
    }

    /**
     * @param nextStop the nextStop to set
     */
    public void setNextStop(ConnectableLoc nextStop) {
        this.nextStop_ = nextStop;
    }

    /**
     * @return the speed
     */
    public int getSpeed() {
        return speed_;
    }

    /**
     * @param speed the speed to set
     */
    public void setSpeed(int speed) {
        this.speed_ = speed;
    }

    /**
     * @return the location
     */
    public ConnectableLoc getLocation() {
        return location_;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(ConnectableLoc location) {
        this.location_ = location;
    }

    /**
     * @return the locationType
     */
    public int getLocationType() {
        return locationType_;
    }

    /**
     * @param locationType the locationType to set
     */
    public void setLocationType(int locationType) {
        this.locationType_ = locationType;
    }

    /**
     * @return the running_
     */
    public boolean isRunning() {
        return running_;
    }

    /**
     * @param running the running_ to set
     */
    public void setRunning(boolean running) {
        this.running_ = running;
    }

    /**
     * @return the thread_
     */
    public Thread getThread_() {
        return thread_;
    }

    /**
     * @param thread the thread_ to set
     */
    public void setThread_(Thread thread) {
        this.thread_ = thread;
    }

    /**
     * @return the route_
     */
    public LinkedList<ConnectableLoc> getRoute() {
        return route_;
    }

    /**
     * @param route the route_ to set
     */
    public void setRoute(LinkedList<ConnectableLoc> route) {
        this.route_ = route;
    }

    /**
     * Czyszczenie listy celów pośrednich
     */
    public void clearRoute() {
        this.route_.clear();
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
