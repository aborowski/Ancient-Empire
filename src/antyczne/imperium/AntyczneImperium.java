/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antyczne.imperium;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;


/**
 * Metoda główna, tworzy cale GUI oraz poczatkowy stan rozgrywki, czyli wioski,
 * skrzyzowania, drogi. Tworzy EventListener dla GUI
 *
 * @author Andrzej Borowski
 */
public class AntyczneImperium extends Canvas implements Runnable {

    /**
     * @param args the command line arguments
     */
    private static final int size_ = 200;
    private static final int scaleMultiplier_ = 3;
    private static int idCount_ = 0;

    private volatile boolean running_;
    private Eventlistener el_;
    private static ArrayList<JComponent> villageRelated_;
    private static ArrayList<JComponent> unitRelated_;
    private JFrame window_;
    private Thread mainLoopThread_;
    private int ticks = 0;
    private volatile static boolean finishIt = false;

    /**
     * Konstruktor dla klasy AntyczneImperium. Wymagany z racji tego, iż jest
     * ona rozszerzeniem klasy Canvas (na której będziemy rysować obiekty.
     */
    public AntyczneImperium() {
        Dimension windowSize = new Dimension(size_ * getScaleMultiplier_(), size_ * getScaleMultiplier_());
        setPreferredSize(windowSize);
        window_ = new JFrame("Antyczne Imperium");
        villageRelated_ = new ArrayList<>();
        unitRelated_ = new ArrayList<>();
        el_ = new Eventlistener();
        addMouseListener(el_);
    }

    /**
     * Metoda generujaca wioski
     *
     * @throws BadMapDesignException
     */
    private static void generateVillages() throws BadMapDesignException {
        Village village;
        for (int i = 0; i < 10; i++) {
            try {
                if (i == 0) {
                    village = new Capital(idCount_++);
                    Database.appendToVillageList(village);
                    Database.appendToLocationList(village);
                } else {
                    village = new Village(idCount_++);
                    Database.appendToVillageList(village);
                    Database.appendToLocationList(village);
                }
            } catch (BadMapDesignException e) {
                throw e;
            }
        }
        village = Database.getVillageList().getLast();
        village.setResUsed(village.getResUsed() | 16);
        village.setResCreated(village.getResCreated() & (1023 - 16));

    }

    /**
     * Metoda generujaca skrzyzowania.
     *
     * @throws BadMapDesignException
     */
    private static void generateCrossings() throws BadMapDesignException {
        Crossing crossing;
        for (int i = 0; i < 5; i++) {
            try {
                crossing = new Crossing(idCount_++);
                Database.appendToCrossingList(crossing);
                Database.appendToLocationList(crossing);
            } catch (BadMapDesignException e) {
                throw e;
            }
        }
    }

    /**
     * Metoda generujaca drogi miedzy innymi obiektami.
     */
    private static void generateRoads() {
        LinkedList<Road> removalList = new LinkedList<>();

        for (ConnectableLoc c : Database.getCrossingList()) {
            for (int i = 0; i < 1; i++) {
                Database.appendToRoadList(new Road(c, true, true));
            }
        }
        for (ConnectableLoc c : Database.getVillageList()) {
            Database.appendToRoadList(new Road(c, true, true));
            if (Math.random() * 1000 < 650) {
                Database.appendToRoadList(new Road(c, false, true));
            } else {
                Database.appendToRoadList(new Road(c, false, false));
            }
        }
        for (Road r : Database.getRoadList()) {
            if (!r.isReallyExists()) {
                removalList.addLast(r);
            }
        }
        for (Road r : removalList) {
            Database.removeRoadFromList(r);
        }
        removalList.clear();

    }

    /**
     * Metoda tworzaca barbarzyncow przy losowej krawedzi mapy
     */
    private static void spawnBarbs() {
        int direction = (int) Math.floor(Math.random() * 4);
        Barbarians b;
        switch (direction) {
            case 0:
                b = new Barbarians(Database.getNextID(), 0, Math.random() * 200, null);
                break;
            case 1:
                b = new Barbarians(Database.getNextID(), Math.random() * 200, size_, null);
                break;
            case 2:
                b = new Barbarians(Database.getNextID(), size_, Math.random() * 200, null);
                break;
            default:
                b = new Barbarians(Database.getNextID(), Math.random() * 200, 0, null);
                break;
        }
        Database.appendToBarbList(b);
    }

    /**
     * Metoda przygotowujaca faktyczne GUI symulatora.
     */
    private void setupJFrame() {
        JPanel sidePanel = new JPanel();
        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.PAGE_AXIS));
        JLabel jlabel = new JLabel();
        JButton jbutton = new JButton();
        JTextField jtextField = new JTextField(1);
        /**
         * Tworzenie czesci interfejsu dla miasta
         */
        {
            jlabel.setText("<html>Village:<br>Population:<br>Treasury:<br>Sleeping Caravans:</html>");
            jlabel.setName("Location Descriptor");

            //jlabel.setPreferredSize(new Dimension(150, 10));
            sidePanel.add(jlabel);
            getVillageRelated_().add(jlabel);
            sidePanel.add(jlabel, BorderLayout.LINE_START);
            jbutton.setText("Send caravan from village");
            jbutton.setName("Village Send Caravan");
            jbutton.setActionCommand("Send caravan");
            jbutton.addActionListener(el_);
            //jbutton.setPreferredSize(new Dimension(150, 50));
            sidePanel.add(jbutton);
            getVillageRelated_().add(jbutton);
            jbutton = new JButton();
            jbutton.setName("Capital Send Legion");
            jbutton.setText("Dispatch a legion");
            jbutton.setActionCommand("Send legion");
            jbutton.addActionListener(el_);
            sidePanel.add(jbutton);
            getVillageRelated_().add(jbutton);
            jlabel = new JLabel();
            jlabel.setText("Product type");
            jlabel.setName("Village produce label");
            sidePanel.add(jlabel);
            getVillageRelated_().add(jlabel);
            jtextField.setName("Village product type");
            jtextField.setActionCommand("Set res");
            jtextField.setMaximumSize(new Dimension(200, 20));
            jtextField.addActionListener(el_);
            sidePanel.add(jtextField);
            getVillageRelated_().add(jtextField);
            jbutton = new JButton();
            jbutton.setName("Village produce");
            jbutton.setText("Produce");
            jbutton.setActionCommand("Produce");
            jbutton.addActionListener(el_);
            sidePanel.add(jbutton);
            getVillageRelated_().add(jbutton);
            this.window_.add(sidePanel, BorderLayout.LINE_END);
        }
        /**
         * Tworzenie czesci interfejsu dla jednostki
         */
        {
            jlabel = new JLabel();
            jlabel.setName("Unit Descriptor");
            jlabel.setText("<html><br>Unit:<br>Type:<br>Target:<br>Speed:</html>");
            sidePanel.add(jlabel);
            getUnitRelated_().add(jlabel);
            jbutton = new JButton();
            jbutton.setName("Caravan state");
            jbutton.setText("Break/fix caravan");
            jbutton.setActionCommand("Break");
            jbutton.addActionListener(el_);
            sidePanel.add(jbutton);
            getUnitRelated_().add(jbutton);
            jbutton = new JButton();
            jbutton.setName("Caravan Kill");
            jbutton.setText("Kill Caravan");
            jbutton.setActionCommand("Kill");
            jbutton.addActionListener(el_);
            sidePanel.add(jbutton);
            getUnitRelated_().add(jbutton);
        }
        jbutton = new JButton();
        jbutton.setName("Help");
        jbutton.setText("Help");
        jbutton.setActionCommand("Help");
        jbutton.addActionListener(el_);
        sidePanel.add(jbutton);
        this.window_.setPreferredSize(new Dimension((size_ * scaleMultiplier_) + 200, size_ * scaleMultiplier_));
        this.window_.setResizable(false);
        this.window_.add(this, BorderLayout.LINE_START);
        this.window_.pack();
        for (JComponent jComponent : villageRelated_) {
            jComponent.setVisible(false);
            jComponent.setEnabled(false);
        }
        for (JComponent jComponent : unitRelated_) {
            jComponent.setVisible(false);
            jComponent.setEnabled(false);
        }
        this.window_.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.window_.setVisible(true);
    }

    /**
     * Metoda początkowa, generuje mapę oraz tworzy okno z symulacją.
     *
     * @param args
     */
    public static void main(String[] args) {
        boolean badMap = true;
        int randomlocation;
        while (badMap) {
            badMap = false;
            try {
                generateCrossings();
                generateVillages();
            } catch (BadMapDesignException e) {
                Database.clearCrossingList();
                Database.clearLocationList();
                Database.clearVillageList();
                badMap = true;
            }
        }
        generateRoads();
        Database.setIdCount(idCount_);
        AntyczneImperium game = new AntyczneImperium();
        game.setupJFrame();
        game.setupThread();

    }

    /**
     * Operacje przygotowywania wątku
     */
    private synchronized void setupThread() {
        running_ = true;
        mainLoopThread_ = new Thread(this);
        mainLoopThread_.setDaemon(true);
        mainLoopThread_.start();
    }

    /**
     * Metoda pętli dla wiosek
     */
    private void updateAll() {
        for (Village v : Database.getVillageList()) {
            v.update();
        }
    }

    /**
     * Metoda rysująca wszystkie obiekty w symulatorze
     */
    private void drawAll() {
        Color bgColor = new Color(50, 160, 0);
        BufferStrategy buffer = getBufferStrategy();
        if (buffer == null) {
            createBufferStrategy(2);
            return;
        }
        Graphics g = buffer.getDrawGraphics();
        g.setColor(bgColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        /**
         * Tutaj idzie cala reszta rysowalnych obiektow (na poczatku statyczne,
         * potem ruchome)
         */
        for (ConnectableLoc c : Database.getLocationList()) {
            c.draw(g, getScaleMultiplier_());
        }
        for (Road r : Database.getRoadList()) {
            r.draw(g, getScaleMultiplier_());
        }

        for (Caravan c : Database.getCaravanList()) {
            c.draw(g, getScaleMultiplier_());
        }

        for (Barbarians b : Database.getBarbarianList()) {
            b.draw(g, getScaleMultiplier_());
        }

        for (Legion l : Database.getLegionList_()) {
            l.draw(g, getScaleMultiplier_());
        }
        g.dispose();
        buffer.show();

    }

    /**
     * Metoda wykonywana podczas trwania symulatora. Jej główną częścią jest
     * pętla, w której obsługujemy działąnia wiosek, tworzymy barbarzyńców oraz
     * rysujemy mapę. Drugą częścią tej metody jest warunek końcowy oraz
     * serializacja do XML wyników.
     */
    @Override
    public void run() {
        int time = 0;
        long timer = System.currentTimeMillis();
        int FpsCounter = 0;
        long lastTimeCheck = System.currentTimeMillis();
        long lastTimeDrawAll = System.currentTimeMillis();
        while (running_) {
            if (ticks > 5000) {
                ticks = 5000;
            }
            if (System.currentTimeMillis() - lastTimeCheck > (1000) / 20) {
                if (Math.random() * 1500000 > (1500000 - ticks)) {
                    spawnBarbs();
                }
                updateAll();
                lastTimeCheck = System.currentTimeMillis();
                ticks++;
                if (Database.getNumOfVillagesLeft() == 0) {
                    this.running_ = false;
                }
            }
            if (System.currentTimeMillis() - lastTimeDrawAll > (1000 / 60)) {
                drawAll();
                lastTimeDrawAll = System.currentTimeMillis();
                FpsCounter++;

            }
            if (System.currentTimeMillis() - timer > 1000) {
                timer = System.currentTimeMillis();
                window_.setTitle("Ancient Empire FPS: " + FpsCounter + "; Ticks: " + ticks + " Villages: " + Database.getNumOfVillagesLeft());
                FpsCounter = 0;
                time++;
            }
        }

        for (Unit u : Database.getBarbarianList()) {
            u.setRunning(false);
        }
        for (Unit u : Database.getCaravanList()) {
            u.setRunning(false);
        }
        for (Unit u : Database.getLegionList_()) {
            u.setRunning(false);
        }

        File f = new File("res.txt");
        ArrayList<Result> results = new ArrayList<>(5);
        if (f.exists()) {
            try {
                boolean readMore = true;
                FileInputStream fin = new FileInputStream(f);
                BufferedInputStream bin = new BufferedInputStream(fin);
                XMLDecoder decoder = new XMLDecoder(bin);
                while (readMore) {
                    try {
                        Result r = (Result) decoder.readObject();
                        results.add(r);
                    } catch (ArrayIndexOutOfBoundsException e) {
                        readMore = false;
                    }
                }
                decoder.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(AntyczneImperium.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        while (results.size() < 5) {
            results.add(new Result());
        }
        JFrame endPanel = new JFrame("Koniec");
        JLabel endLabel = new JLabel();
        String endString = new String("<html>Best results:<br>");
        for (Result result : results) {
            endString = endString.concat(result.getName() + ", Seconds: " + String.valueOf(result.getPoints()) + "<br>");
        }
        endString = endString.concat("Your result is " + String.valueOf(time) + ", what is your name?</html>");
        endLabel.setText(endString);
        JTextField endField = new JTextField("Give Me A Name");
        endField.setActionCommand("Name get");
        endField.addActionListener(el_);
        endLabel.setVisible(true);
        endField.setVisible(true);
        endField.setEnabled(true);
        endPanel.add(endLabel, BorderLayout.NORTH);
        endPanel.add(endField, BorderLayout.SOUTH);
        endPanel.pack();
        endPanel.setVisible(true);
        while (!isFinishIt());
        Result r = new Result();
        r.setName(endField.getText());
        r.setPoints(time);
        results.add(r);
        Collections.sort(results);
        while (results.size() > 5) {
            results.remove(0);
        }
        for (Result result : results) {
            System.out.println(result);
        }
        f.delete();
        try {
            FileOutputStream fout = new FileOutputStream(f);
            BufferedOutputStream bout = new BufferedOutputStream(fout);
            XMLEncoder encoder = new XMLEncoder(bout);
            while (!results.isEmpty()) {
                encoder.writeObject(results.remove(0));
            }
            encoder.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AntyczneImperium.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.exit(0);
    }

    /**
     * @return the villageRelated_
     */
    public static ArrayList<JComponent> getVillageRelated_() {
        return villageRelated_;
    }

    /**
     * @param villageRelated_ the villageRelated_ to set
     */
    public static void setVillageRelated_(ArrayList<JComponent> villageRelated_) {
        villageRelated_ = villageRelated_;
    }

    /**
     * @return the unitRelated_
     */
    public static ArrayList<JComponent> getUnitRelated_() {
        return unitRelated_;
    }

    /**
     * @param unitRelated_ the unitRelated_ to set
     */
    public static void setUnitRelated_(ArrayList<JComponent> unitRelated_) {
        unitRelated_ = unitRelated_;
    }

    /**
     * @return the scaleMultiplier
     */
    public static int getScaleMultiplier_() {
        return scaleMultiplier_;
    }

    /**
     * @return the finishIt
     */
    public static boolean isFinishIt() {
        return finishIt;
    }

    /**
     * @param aFinishIt the finishIt to set
     */
    public static void setFinishIt(boolean aFinishIt) {
        finishIt = aFinishIt;
    }

}
