/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package antyczne.imperium;

import java.io.Serializable;

/**
 * Klasa wyniku, zapisywana za pomocą serializacji do XML.
 */
public class Result implements Serializable, Comparable<Result> {
    private String name;
    private Integer points;

    /**
     * Konstruktor
     */
    public Result() {
        this.name = new String("Empty");
        this.points = new Integer(0);
    }
    
    /**
     * Sposób porównywania obiektu
     *
     * @param o Inny wynik
     * @return Zwraca odpowiednie wartości zależnie od tego czy obiekt ma
     * większa/mniejszą/tą samą wartość.
     */
    @Override
    public int compareTo(Result o) {
        if (this.getPoints() < o.getPoints()) {
            return -1;
        } else {
            if (this.getPoints() > o.getPoints()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the points
     */
    public Integer getPoints() {
        return points;
    }

    /**
     * @param points the points to set
     */
    public void setPoints(Integer points) {
        this.points = points;
    }

}
