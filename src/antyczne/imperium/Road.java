package antyczne.imperium;

import java.awt.Color;
import java.awt.Graphics;
import java.io.*;
import java.util.*;

/**
 * Klasa drogi w symulacji.
 */
public class Road extends ConnectableLoc {

    private boolean reallyExists = false;

    /**
     * Konstruktor drogi. Ten konstruktor powinien być wywoływany jako jedyny.
     * Drugi konstruktor wymagany jest na potrzeby tego. Konstruktor sam
     * decyduje do czego się podczepi. Jedyne co możemy podać to:
     *
     * @param beginsAt Lokacja początkowa
     * @param toCrossroad Czy ma się podczepić do skrzyżowania czy wioski
     * @param returns Czy ma stworzyć drogę powrotną
     */
    public Road(ConnectableLoc beginsAt, boolean toCrossroad, boolean returns) {
        this.setMonitor(new Object());
        int distanceToLastChecked = 10000;
        int curDist;
        int Vertical = 0, Horizontal = 0;
        boolean generatable = false;
        this.setListFrom(new LinkedList<ConnectableLoc>());
        this.setListTo(new LinkedList<ConnectableLoc>());
        this.addElementFrom(beginsAt);
        if (toCrossroad) {
            for (Crossing c : Database.getCrossingList()) {
                curDist = 0;
                Horizontal = testProxHorizontal(beginsAt, c);
                if (Horizontal < 0) {
                    curDist += Math.pow(
                            beginsAt.getX() - c.getxEnd(),
                            2);
                } else {
                    if (Horizontal > 0) {
                        curDist += Math.pow(
                                c.getX() - beginsAt.getxEnd(),
                                2);
                    }
                }
                Vertical = testProxVertical(beginsAt, c);
                if (Vertical < 0) {
                    curDist += Math.pow(
                            beginsAt.getY() - c.getyEnd(),
                            2);
                } else {
                    if (Vertical > 0) {
                        curDist += Math.pow(
                                beginsAt.getyEnd() - c.getY(),
                                2);
                    }
                }

                if (Math.sqrt(curDist) < distanceToLastChecked) {
                    if (!c.equals(beginsAt) && !beginsAt.getListFrom().contains(c) && !beginsAt.getListTo().contains(c)) {
                        distanceToLastChecked = (int) Math.sqrt(curDist);
                        this.clearListTo();
                        this.addElementTo(c);
                        generatable = true;
                    }
                }
            }
        } else {
            for (Village v : Database.getVillageList()) {
                curDist = 0;
                Horizontal = testProxHorizontal(beginsAt, v);
                if (Horizontal < 0) {
                    curDist += Math.pow(
                            beginsAt.getX() - v.getxEnd(), 2);
                } else {
                    if (Horizontal > 0) {
                        curDist += Math.pow(
                                beginsAt.getxEnd() - v.getX(), 2);
                    }
                }
                Vertical = testProxVertical(beginsAt, v);
                if (Vertical < 0) {
                    curDist += Math.pow(
                            beginsAt.getY() - v.getyEnd(), 2);
                } else {
                    if (Vertical > 0) {
                        curDist += Math.pow(
                                beginsAt.getyEnd() - v.getY(), 2);
                    }
                }

                if (Math.sqrt(curDist) < distanceToLastChecked) {
                    if (!v.equals(beginsAt) && !beginsAt.getListFrom().contains(v) && !beginsAt.getListTo().contains(v)) {
                        distanceToLastChecked = (int) Math.sqrt(curDist);
                        this.clearListTo();
                        this.addElementTo(v);
                        generatable = true;
                    }
                }
            }
        }
        if (generatable) {
            // Co sie dzieje jak znajdziemy potencjalne miejsce do stworzenia drogi?
            //Sprawdzamy w ktorej cwiartce jest
            System.out.println("Wygenerowano droge z " + this.getFirstFrom().getName() + " do " + this.getFirstTo().getName());
            Horizontal = testProxHorizontal(this.getFirstFrom(), this.getFirstTo());
            Vertical = testProxVertical(this.getFirstFrom(), this.getFirstTo());
            this.checkAndAttach(Horizontal, Vertical);

            this.getFirstFrom().addElementTo(this.getFirstTo());
            this.getFirstTo().addElementFrom(this.getFirstFrom());
            this.setResidentList_(new LinkedList<Unit>());
            if (returns) {
                Database.appendToRoadList(new Road(this.getFirstTo(), this.getFirstFrom(), Horizontal, Vertical));
            }
            this.reallyExists = true;
            System.out.println("---------------------------------------");
        }

    }

    /**
     * Konstruktor drogi powrotnej. Nieużywać manualnie!
     *
     * @param beginsAt Lokacja początkowa
     * @param endsAt Lokacja końcowa
     * @param wasHorizontal Odległość horyzontalna, z której bliżej był aktualny
     * punkt początkowy względem aktualnego punktu końcowego. -1 - lewo, 0 -
     * równo, 1 - prawo
     * @param wasVertical Odległość wertykalna - podobnie jak horyzontalna -1 -
     * góra, 0 - równo, 1 - dół
     */
    public Road(ConnectableLoc beginsAt, ConnectableLoc endsAt, int wasHorizontal, int wasVertical) {
        this.setMonitor(new Object());
        int Horizontal = 0, Vertical = 0;
        if (wasHorizontal > 0) {
            Horizontal = -1;
        }
        if (wasHorizontal < 0) {
            Horizontal = 1;
        }
        if (wasVertical > 0) {
            Vertical = -1;
        }
        if (wasVertical < 0) {
            Vertical = 1;
        }
        this.setListFrom(new LinkedList<ConnectableLoc>());
        this.setListTo(new LinkedList<ConnectableLoc>());
        this.addElementFrom(beginsAt);
        this.addElementTo(endsAt);
        System.out.println("Wygenerowano droge powrotna");
        this.checkAndAttach(Horizontal, Vertical);
        beginsAt.addElementTo(endsAt);
        endsAt.addElementFrom(beginsAt);
        this.reallyExists = true;
        this.setResidentList_(new LinkedList<Unit>());
    }

    /**
     * Metoda sprawdzająca przy której krawędzi ends jest bliżej do begins
     *
     * @param begins Lokacja początkowa
     * @param ends Lokacja końcowa
     * @return Wartość zależną od wyniku; -1 - lewo, 0 - równo, 1 - prawo.
     */
    private int testProxHorizontal(ConnectableLoc begins, ConnectableLoc ends) {
        if (begins.getX() > ends.getX()) {
            return -1;
        } else {
            if (begins.getX() == ends.getX()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    /**
     * Metoda sprawdzająca przy której krawędzi ends jest bliżej do begins
     *
     * @param begins Lokacja początkowa
     * @param ends Lokacja końcowa
     * @return Wartość zależną od wyniku; -1 - góra, 0 - równo, 1 - dół.
     */
    private int testProxVertical(ConnectableLoc begins, ConnectableLoc ends) {
        if (begins.getY() > ends.getY()) {
            return -1;
        } else {
            if (begins.getY() == ends.getY()) {
                return 0;
            } else {
                return 1;
            }
        }
    }

    /**
     * Metoda sprawdzająca do której krawędzi lokacji ma być podczepiona droga,
     * po czym jest przyczepiana.
     *
     * @param Horizontal Wartość, po której stronie lokacji początkowej leży
     * końcowa (Horyzontalnie)
     * @param Vertical Wartość, po której stronie lokacji początkowej leży
     * końcowa (Wertykalnie)
     */
    private void checkAndAttach(int Horizontal, int Vertical) {
        if (Horizontal == 0 && Vertical < 0) {
            this.attachTop();
            System.out.println("TOP: Hor = 0, Ver < 0");
        }
        if (Horizontal == 0 && Vertical > 0) {
            this.attachBottom();
            System.out.println("BOTTOM: Hor = 0, Ver > 0");
        }
        if (Horizontal < 0 && Vertical == 0) {
            this.attachLeft();
            System.out.println("LEFT: Hor < 0, Ver = 0");
        }
        if (Horizontal > 0 && Vertical == 0) {
            this.attachRight();
            System.out.println("RIGHT: Hor > 0, Ver = 0");
        }
        if (Horizontal < 0 && Vertical < 0) {
            System.out.println("Hor < 0, Ver < 0");
            //Sprawdzamy ktore koordynaty sa od siebie bardziej odlegle
            // miedzy nimi bedzie droga
            if (this.getFirstFrom().getX() - this.getFirstTo().getxEnd() < 0) {
                this.attachTop();
                System.out.println("TOP: blisko siebie os X");
            } else {
                if (this.getFirstFrom().getY() - this.getFirstTo().getyEnd() < 0) {
                    this.attachLeft();
                    System.out.println("LEFT: blisko siebie os Y");
                } else {
                    if (this.getFirstFrom().getX() - this.getFirstTo().getxEnd()
                            > this.getFirstFrom().getY() - this.getFirstTo().getyEnd()) {
                        this.attachLeft();
                        System.out.println("LEFT");
                    } else {
                        this.attachTop();
                        System.out.println("TOP");
                    }
                }

            }
        }
        if (Horizontal < 0 && Vertical > 0) {
            System.out.println("Hor < 0, Ver > 0");
            if (this.getFirstFrom().getX() - this.getFirstTo().getxEnd() < 0) {
                this.attachBottom();
                System.out.println("BOTTOM: blisko siebie os X");
            } else {
                if (this.getFirstTo().getY() - this.getFirstFrom().getyEnd() < 0) {
                    this.attachLeft();
                    System.out.println("LEFT: blisko siebie os Y");
                } else {
                    if (this.getFirstFrom().getX() - this.getFirstTo().getxEnd()
                            > this.getFirstTo().getY() - this.getFirstFrom().getyEnd()) {
                        this.attachLeft();
                        System.out.println("LEFT");
                    } else {
                        this.attachBottom();
                        System.out.println("BOTTOM");
                    }
                }
            }
        }
        if (Horizontal > 0 && Vertical < 0) {
            System.out.println("Hor > 0, Ver < 0");
            if (this.getFirstTo().getX() - this.getFirstFrom().getxEnd() < 0) {
                this.attachTop();
                System.out.println("TOP: blisko siebie os X");
            } else {
                if (this.getFirstFrom().getY() - this.getFirstTo().getyEnd() < 0) {
                    this.attachRight();
                    System.out.println("RIGHT: blisko siebie os Y");
                } else {
                    if (this.getFirstTo().getX() - this.getFirstFrom().getxEnd()
                            > this.getFirstFrom().getY() - this.getFirstTo().getyEnd()) {
                        this.attachRight();
                        System.out.println("RIGHT");
                    } else {
                        this.attachTop();
                        System.out.println("TOP");
                    }
                }
            }
        }
        if (Horizontal > 0 && Vertical > 0) {
            System.out.println("Hor > 0, Ver > 0");
            if (this.getFirstTo().getX() - this.getFirstFrom().getxEnd() < 0) {
                this.attachBottom();
                System.out.println("BOTTOM: blisko siebie os X");
            } else {
                if (this.getFirstTo().getY() - this.getFirstFrom().getyEnd() < 0) {
                    this.attachRight();
                    System.out.println("RIGHT: blisko siebie os Y");
                } else {
                    if (this.getFirstTo().getX() - this.getFirstFrom().getxEnd()
                            > this.getFirstTo().getY() - this.getFirstFrom().getyEnd()) {
                        this.attachRight();
                        System.out.println("RIGHT");
                    } else {
                        this.attachBottom();
                        System.out.println("BOTTOM");
                    }
                }
            }
        }
    }

    /**
     * Podczepienie drogi po lewej
     */
    private void attachLeft() {
        this.setX(this.getFirstFrom().getX());
        this.setY(this.getFirstFrom().getY());
        this.setxEnd(this.getFirstTo().getxEnd());
        this.setyEnd(this.getFirstTo().getY());
    }

    /**
     * Podczepienie drogi na górze
     */
    private void attachTop() {
        this.setX(this.getFirstFrom().getxEnd());
        this.setY(this.getFirstFrom().getY());
        this.setxEnd(this.getFirstTo().getxEnd());
        this.setyEnd(this.getFirstTo().getyEnd());
    }

    /**
     * Podczepienie drogi na dole
     */
    private void attachBottom() {
        this.setX(this.getFirstFrom().getX());
        this.setY(this.getFirstFrom().getyEnd());
        this.setxEnd(this.getFirstTo().getX());
        this.setyEnd(this.getFirstTo().getY());
    }

    /**
     * Podczepienie drogi po prawej
     */
    private void attachRight() {
        this.setX(this.getFirstFrom().getxEnd());
        this.setY(this.getFirstFrom().getyEnd());
        this.setxEnd(this.getFirstTo().getX());
        this.setyEnd(this.getFirstTo().getyEnd());
    }

    /**
     * Metoda rysująca obiekt
     *
     * @param g grafika
     * @param scale skala na mapie
     */
    @Override
    public void draw(Graphics g, int scale) {
        Color roadColor = new Color(120, 20, 0);
        g.setColor(roadColor);
        g.drawLine((int) Math.round(this.getX() * scale),
                (int) Math.round(this.getY() * scale),
                (int) Math.round(this.getxEnd() * scale),
                (int) Math.round(this.getyEnd() * scale));
        roadColor = null;
    }

    /**
     * @return the reallyExists
     */
    public boolean isReallyExists() {
        return reallyExists;
    }

    /**
     * @param reallyExists the reallyExists to set
     */
    public void setReallyExists(boolean reallyExists) {
        this.reallyExists = reallyExists;
    }

    /**
     * Wymagana aby nie być klasą abstrakcyjną, aczkolwiek skrzyżowanie nic nie
     * robi.
     */
    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
