package antyczne.imperium;

import java.awt.Color;
import java.awt.Graphics;
import java.io.*;
import java.util.*;

/**
 * Skrzyżowania na mapie
 */
public class Crossing extends ConnectableLoc {

    private boolean inUse_;

    /**
     * Konstruktor skrzyżowania
     *
     * @param idGotten ID skrzyżowania
     * @throws BadMapDesignException
     */
    public Crossing(int idGotten) throws BadMapDesignException {
        this.setMonitor(new Object());
        int ticks = 0;
        int distance;
        boolean ploppable = false;
        this.setId(idGotten);
        while (!ploppable) {
            ploppable = true;
            this.setX(Math.random() * 140 + 30);
            this.setxEnd(this.getX() + 5);
            this.setY(Math.round(Math.random() * 140) + 30);
            this.setyEnd(this.getY() + 5);
            for (ConnectableLoc c : Database.getLocationList()) {
                distance = 0;
                distance += Math.pow(
                        (this.getxEnd() - 3) - (c.getxEnd() - 3),
                        2);
                distance += Math.pow(
                        (this.getyEnd() - 3) - (c.getyEnd() - 3),
                        2);
                if (Math.sqrt(distance) < 60) {
                    ploppable = false;

                }
            }
            ticks++;
            if (ticks > 5000) {
                throw new BadMapDesignException();
            }
        }
        this.setName(this.createRandomName((int) Math.round(Math.random() * 5) + 5).concat(" Crossing"));
        this.inUse_ = false;
        this.setListFrom(new LinkedList<ConnectableLoc>());
        this.setListTo(new LinkedList<ConnectableLoc>());
        this.setResidentList_(new LinkedList<Unit>());

    }

    /**
     * Ustawianie wartości semaforowej dla skrzyżowania
     *
     * @return True - udało się ustawić, false - ktoś już jest na skrzyżowaniu.
     */
    public boolean setInUse() {
        synchronized (getMonitor()) {
            if (!this.inUse_) {
                this.inUse_ = true;
                return true;
            }
            return false;
        }
    }

    /**
     * Metoda oddająca skrzyżowanie do publicznego użytku
     */
    public void releaseInUse() {
        synchronized (getMonitor()) {
            this.inUse_ = false;
        }
    }

    /**
     * Metoda rysująca obiekt
     *
     * @param g grafika
     * @param scale skala mapy
     */
    @Override
    public void draw(Graphics g, int scale) {
        g.setColor(Color.GRAY);
        g.fillRect((int) Math.round(this.getX() * scale),
                (int) Math.round(this.getY() * scale),
                (int) Math.round((this.getxEnd() - this.getX()) * scale),
                (int) Math.round((this.getyEnd() - this.getY()) * scale));
        g.setColor(Color.RED);
        g.drawString(this.getName(),
                (int) Math.round(this.getX() * scale - 30),
                (int) Math.round(this.getY() * scale - 20));
    }

    /**
     * Wymagana aby nie być klasą abstrakcyjną, aczkolwiek skrzyżowanie nic nie robi.
     */
    @Override
    public void update() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
