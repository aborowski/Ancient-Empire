package antyczne.imperium;

import java.awt.Graphics;
import java.io.*;
import java.util.*;

/**
 * Klasa posiadająca podstawowe pola obiektów na mapie.
 * @author Borowik
 */
public abstract class ObjUpdatable {

    private int id_;
    private double x_;
    private double xEnd_;
    private double y_;
    private double yEnd_;
    private String name_;

    /**
     * Metoda wywoływana w pętli w celu wykonania innych czynności, np. zmiany
     * pola lub wywołania innej metody.
     */
    public abstract void update();

    /**
     * Metoda wywoływana w pętli renderującej, rysuje obiekt na mapie.
     *
     * @param g grafika
     * @param scale skala na mapie
     */
    public abstract void draw(Graphics g, int scale);

    /**
     * Metoda generująca nazwy
     *
     * @param length długość nazwy
     * @return String z nazwą
     */
    public String createRandomName(int length) {
        ArrayList<Character> nameArray;
        StringBuilder builder = new StringBuilder(length);
        Character[] vowelList = {'a', 'e', 'i', 'o', 'u', 'y'};

        nameArray = new ArrayList(length);
        nameArray.add(0, (char) ('A' + Math.round(Math.random() * 23)));
        for (int i = 1; i < length; i++) {
            if ((i + 1) % 3 == 0) {
                nameArray.add(i, vowelList[(int) Math.round(Math.random() * 5)]);
            } else {
                nameArray.add(i, (char) ('a' + Math.round(Math.random() * 23)));
            }
        }
        for (Character c : nameArray) {
            builder.append(c);
        }
        return builder.toString();
    }

    /**
     * @return the id
     */
    public int getId() {
        return id_;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id_ = id;
    }

    /**
     * @return the x
     */
    public double getX() {
        return x_;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
        this.x_ = x;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y_;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
        this.y_ = y;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name_;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name_ = name;
    }

    /**
     * @return the xEnd
     */
    public double getxEnd() {
        return xEnd_;
    }

    /**
     * @param xEnd the xEnd to set
     */
    public void setxEnd(double xEnd) {
        this.xEnd_ = xEnd;
    }

    /**
     * @return the yEnd
     */
    public double getyEnd() {
        return yEnd_;
    }

    /**
     * @param yEnd the yEnd to set
     */
    public void setyEnd(double yEnd) {
        this.yEnd_ = yEnd;
    }

}
