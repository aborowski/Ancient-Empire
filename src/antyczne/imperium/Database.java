package antyczne.imperium;

import java.util.*;

/**
 * Baza danych dla obiektów mapy
 */
public class Database {

    private static Object monitor = new Object();
    private volatile static LinkedList<Village> villageList_ = new LinkedList<>();
    private volatile static LinkedList<ConnectableLoc> locationList_ = new LinkedList<>();
    private volatile static LinkedList<Caravan> caravanList_ = new LinkedList<>();
    private volatile static LinkedList<Barbarians> barbarianList_ = new LinkedList<>();
    private volatile static LinkedList<Road> roadList_ = new LinkedList<>();
    private volatile static LinkedList<Crossing> crossingList_ = new LinkedList<>();
    private volatile static LinkedList<Legion> legionList_ = new LinkedList<>();
    private static int numOfVillagesLeft_ = 10;
    private static int idCount;

    /**
     * Metoda inkrementujaca oraz zwracajaca ID
     */
    public static int getNextID() {
        synchronized (monitor) {
            idCount++;
            return idCount - 1;
        }
    }

    /**
     * @return the villageList
     */
    public static LinkedList<Village> getVillageList() {
        synchronized (monitor) {
            return villageList_;
        }
    }

    /**
     * @param aVillageList the villageList to set
     */
    public static void setVillageList(LinkedList<Village> aVillageList) {
        synchronized (monitor) {
            villageList_ = aVillageList;
        }
    }

    /**
     * @param village the element to append to the list
     */
    public static void appendToVillageList(Village village) {
        synchronized (monitor) {
            villageList_.addLast(village);
        }
    }

    /**
     * @param village the element to be removed from the list
     */
    public static void removeVillageFromList(Village village) {
        synchronized (monitor) {
            villageList_.remove(village);
        }
    }

    /**
     * Czyszczenie listy wiosek.
     */
    public static void clearVillageList() {
        synchronized (monitor) {
            villageList_.clear();
        }
    }

    /**
     * @return the fullObjList
     */
    public static LinkedList<ConnectableLoc> getLocationList() {
        synchronized (monitor) {
            return locationList_;
        }
    }

    /**
     * @param aLocationList the fullObjList to set
     */
    public static void setLocationList(LinkedList<ConnectableLoc> aLocationList) {
        synchronized (monitor) {
            locationList_ = aLocationList;
        }
    }

    /**
     * @param obj element to be added
     */
    public static void appendToLocationList(ConnectableLoc obj) {
        synchronized (monitor) {
            locationList_.addLast(obj);
        }
    }

    /**
     * @param obj element to be removed from list
     */
    public static void removeObjFromList(ConnectableLoc obj) {
        synchronized (monitor) {
            locationList_.remove(obj);
        }
    }

    /**
     * Czyszczenie listy lokacji.
     */
    public static void clearLocationList() {
        synchronized (monitor) {
            locationList_.clear();
        }
    }

    /**
     * @return the caravanList
     */
    public static LinkedList<Caravan> getCaravanList() {
        synchronized (monitor) {
            return caravanList_;
        }
    }

    /**
     * @param aCaravanList the caravanList to set
     */
    public static void setCaravanList(LinkedList<Caravan> aCaravanList) {
        synchronized (monitor) {
            caravanList_ = aCaravanList;
        }
    }

    /**
     * @param caravan the element to append to the list
     */
    public static void appendToCaravanList(Caravan caravan) {
        synchronized (monitor) {
            caravanList_.addLast(caravan);
        }
    }

    /**
     * @param caravan the element to be removed from the list
     */
    public static void removeCaravanFromList(Caravan caravan) {
        synchronized (monitor) {
            caravanList_.remove(caravan);
        }
    }

    /**
     * @return the barbarianList
     */
    public static LinkedList<Barbarians> getBarbarianList() {
        synchronized (monitor) {
            return barbarianList_;
        }
    }

    /**
     * @param aBarbarianList the barbarianList to set
     */
    public static void setBarbarianList(LinkedList<Barbarians> aBarbarianList) {
        synchronized (monitor) {
            barbarianList_ = aBarbarianList;
        }
    }

    /**
     * @param barb the element to append to the list
     */
    public static void appendToBarbList(Barbarians barb) {
        synchronized (monitor) {
            barbarianList_.addLast(barb);
        }
    }

    /**
     * @param barb the element to be removed from the list
     */
    public static void removeBarbFromList(Barbarians barb) {
        synchronized (monitor) {
            barbarianList_.remove(barb);
        }
    }

    /**
     * @return the roadList
     */
    public static LinkedList<Road> getRoadList() {
        synchronized (monitor) {
            return roadList_;
        }
    }

    /**
     * @param aRoadList the roadList to set
     */
    public static void setRoadList(LinkedList<Road> aRoadList) {
        synchronized (monitor) {
            roadList_ = aRoadList;
        }
    }

    /**
     * @param road the element to append to the list
     */
    public static void appendToRoadList(Road road) {
        synchronized (monitor) {
            roadList_.addLast(road);
        }
    }

    /**
     * @param road the element to be removed from the list
     */
    public static void removeRoadFromList(Road road) {
        synchronized (monitor) {
            roadList_.remove(road);
        }
    }

    /**
     * @return the crossingList
     */
    public static LinkedList<Crossing> getCrossingList() {
        synchronized (monitor) {
            return crossingList_;
        }
    }

    /**
     * @param aCrossingList the crossingList to set
     */
    public static void setCrossingList(LinkedList<Crossing> aCrossingList) {
        synchronized (monitor) {
            crossingList_ = aCrossingList;
        }
    }

    /**
     * @param crossing the element to append to the list
     */
    public static void appendToCrossingList(Crossing crossing) {
        synchronized (monitor) {
            crossingList_.addLast(crossing);
        }
    }

    /**
     * @param crossing the element to be removed from the list
     */
    public static void removeCrossingFromList(Crossing crossing) {
        synchronized (monitor) {
            crossingList_.remove(crossing);
        }
    }

    /**
     * Czyszczenie listy skrzyżowań.
     */
    public static void clearCrossingList() {
        synchronized (monitor) {
            crossingList_.clear();
        }
    }

    /**
     * @return the numOfVillagesLeft
     */
    public static int getNumOfVillagesLeft() {
        synchronized (monitor) {
            return numOfVillagesLeft_;
        }
    }

    /**
     * @param aNumOfVillagesLeft the numOfVillagesLeft to set
     */
    public static void setNumOfVillagesLeft(int aNumOfVillagesLeft) {
        synchronized (monitor) {
            numOfVillagesLeft_ = aNumOfVillagesLeft;
        }
    }

    /**
     * Zmniejszanie liczby wiosek pozostałych
     */
    public static void decrementVillages() {
        synchronized (monitor) {
            numOfVillagesLeft_--;
        }
    }

    /**
     * @return the idCount
     */
    public static int getIdCount() {
        return idCount;
    }

    /**
     * @param aIdCount the idCount to set
     */
    public static void setIdCount(int aIdCount) {
        idCount = aIdCount;
    }

    /**
     * @return the legionList_
     */
    public static LinkedList<Legion> getLegionList_() {
        synchronized (monitor) {
            return legionList_;
        }
    }

    /**
     * @param aLegionList_ the legionList_ to set
     */
    public static void setLegionList_(LinkedList<Legion> aLegionList_) {
        synchronized (monitor) {
            legionList_ = aLegionList_;
        }
    }

    /**
     * Dodawanie legionu do listy
     *
     * @param legion Legion do dodania
     */
    public static void appendToLegionList(Legion legion) {
        synchronized (monitor) {
            legionList_.add(legion);
        }
    }

    /**
     * Usuwanie legionu z listy
     *
     * @param legion Legion do usunięcia
     */
    public static void removeLegionFromList(Legion legion) {
        synchronized (monitor) {
            legionList_.remove(legion);
        }
    }
}
