package antyczne.imperium;

/**
 * Klasa surowców
 */
public class Res {

    private int resourceId_;
    /**
     * Typy surowcow i ich wartosc liczbowa (w wektorze bitowym) 1: Zywnosc 2:
     * Drewno 4: Kamien 8: Bydlo 16: Alkohol 32: Zelazo 64: Sol 128: Plutno 256:
     * Ceramika 512: Bursztyn
     */
    private int resType_;
    private int volume_;
    private int weight_;
    private long timeDue_;

    /**
     * Konstruktor surowca
     *
     * @param id ID surowca
     * @param type typ surowca, musi być jedną z kolejnych potęg liczby 2, od
     * 2^0 do 2^9. Od tego zależne są wartości pozostałych pól.
     */
    public Res(int id, int type) {
        this.resType_ = type;
        this.resourceId_ = id;
        this.timeDue_ = 0;
        switch (type) {
            case 1: //Zywnosc   1*5*2=10
                this.timeDue_ = System.currentTimeMillis() + (1000 * 100);
                this.volume_ = 2;
                this.weight_ = 10;
                break;
            case 2: //Drewno    2*5*5=50
                this.volume_ = 5;
                this.weight_ = 10;
                break;
            case 4: //Kamien    4*5*10=200
                this.volume_ = 5;
                this.weight_ = 20;
                break;
            case 8: //Bydlo     8*15*10=1200
                this.volume_ = 15;
                this.weight_ = 20;
                break;
            case 16: //Alkohol  16*10*10=1600
                this.timeDue_ = System.currentTimeMillis() + (1000 * 200);
                this.volume_ = 10;
                this.weight_ = 20;
                break;
            case 32: //Zelazo   32*5*15=2400
                this.volume_ = 5;
                this.weight_ = 30;
                break;
            case 64: //Sol      64*5*10=3200
                this.volume_ = 5;
                this.weight_ = 20;
                break;
            case 128: //Plutno  128*10*5=6400
                this.volume_ = 10;
                this.weight_ = 10;
                break;
            case 256: //Ceramika    256*5*10=12800
                this.volume_ = 5;
                this.weight_ = 20;
                break;
            case 512: //Bursztyn    512*5*10=25600
                this.volume_ = 5;
                this.weight_ = 20;
                break;
        }
    }

    /**
     * Metoda obliczająca rynkową wartość surowca.
     *
     * @return Wartość obliczona
     */
    public int calculateVal() {
        return this.resType_ * (this.weight_ / 2) * this.volume_;
    }

    /**
     * @return the resourceId
     */
    public int getResourceId() {
        return resourceId_;
    }

    /**
     * @param resourceId the resourceId to set
     */
    public void setResourceId(int resourceId) {
        this.resourceId_ = resourceId;
    }

    /**
     * @return the resType
     */
    public int getResType() {
        return resType_;
    }

    /**
     * @param resType the resType to set
     */
    public void setResType(int resType) {
        this.resType_ = resType;
    }

    /**
     * @return the volume
     */
    public int getVolume() {
        return volume_;
    }

    /**
     * @param volume the volume to set
     */
    public void setVolume(int volume) {
        this.volume_ = volume;
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight_;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(int weight) {
        this.weight_ = weight;
    }

    /**
     * @return the timeLeft_
     */
    public long getTimeDue() {
        return timeDue_;
    }

    /**
     * @param timeDue the timeLeft_ to set
     */
    public void setTimeDue(long timeDue) {
        this.timeDue_ = timeDue;
    }

}
