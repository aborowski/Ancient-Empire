/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package antyczne.imperium;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Klasa nasłuchująca na zdarzenia związane z GUI, oraz uaktualniająca informacje.
 */
public class Eventlistener implements Runnable, ActionListener, MouseListener {
 
    private ConnectableLoc activeLocation;
    private Unit activeUnit;
    private boolean running;
    private Thread thread;
    private int resType;
    private JFrame helpFrame_;

    /**
     * Konstruktor
     */
    public Eventlistener() {
        running = true;
        thread = new Thread(this);
        thread.setDaemon(true);
        resType = 0;
        thread.start();
        helpFrame_ = new JFrame("Help");
        JLabel jl = new JLabel();
        jl.setText("<html>MAP LEGEND:<br>"
                + "Light blue squares: these are your villages!<br>"
                + "One of them is the capital of the empire, distinguished by the suffix 'City'<br>"
                + "Gray (seemingly purple) squares: these are the crossroads, where roads of the empire meet!<br>"
                + "Brown lines: these are the arteries of communication. Units will move across them in order to relocate.<br>"
                + "They are attached to villages and crossroads the more common European way. No need to make it difficult, brits.<br>"
                + "Small red squares: these are the merchant caravans! They buy goods at one place and sell them at another place.<br>"
                + "Small yellow squares: Barbarians are bad! they will kill your willagers and take their loot... goods! Kill them with Legions<br>"
                + "Small white squares: These are your legions.<br>"
                + "-----<br>"
                + "MECHANICS<br>"
                + "The villages:<br>"
                + "Produce goods whenever required number of ticks of the main loop pass.<br>"
                + "The higher the type of the resource, the rarer it will be auto-produced.<br>"
                + "Production costs 40% of the resource's market price.<br>"
                + "Similarly whenever enough loop ticks pass every village attempts to consume goods, which yields a gain in population equal to 10%<br>"
                + "of market price of the resource.<br>"
                + "At the same time as a village attempts to consume, it collects taxes (Gain in treasury of 5% of population).<br>"
                + "The merchants:<br>"
                + "They are automatically created whenever a village's sellable goods stock is full (the village cannot produce a resource).<br>"
                + "They buy goods at 60% of market price, and sell at 100%.<br>"
                + "If they cannot afford the cheapest resource in a village, they decide to settle down giving up half of their gold to build a home.<br>"
                + "------<br>"
                + "CONTROLS<br>"
                + "Most controls will be fairly obvious, however these are the most essential:<br>"
                + "Click on an object on the map (LMB) to select it. Depending on what you have selected more options will become available on the right<br>"
                + "side menu. In the 'Product type' text field type any power of 2, from 2^0 (1) to 2^9 (512) and confirm selection by pressing enter.<br>"
                + "If the circumstances are favourable (enough stock space, enough gold and the village allows to produce that type) the resource will be<br>"
                + "produced! If you have selected a non-hostile unit you can force it to change target destination by right clicking on a village or<br>"
                + "crossing on the map.<br>"
                + "-----<br>"
                + "EXPLANATION OF SIDE MENU INFO:<br>"
                + "For a location, residents tells us about the units that are recorded as being 'on' it<br>"
                + "Available items and consumable items tells first about the number of items, and after the coma about how much space is used of the<br>"
                + "available stock space.<br>"
                + "For a unit, speed (and speed penalty for caravans) means how fast, or slow the unit moves. The more value (max 3) the slower they move.<br>"
                + "Caravan's Wares describe all the goods a merchant owns, T - type, Vol - volume (space usage) of the resource, W - weight, needed to<br>"
                + "measure the speed penalty, and Time - time in seconds before food (or alcohol) is spoilt.<br>"
                + "Resources and their value: 1 - food, worth 10; 2 - wood, worth 50; 4 - stone, worth 200; 8 - cattle, worth 1200; 16 - alcohol, worth 1600;<br>"
                + "32 - iron, worth 2400; 64 - salt, worth 3200; 128 - silk cloth, worth 6400; 256 - ceramics, worth 12800; 512 - amber, worth 25600"
                + "</html>"
        );
        jl.setVisible(true);
        helpFrame_.add(jl, BorderLayout.CENTER);
        helpFrame_.pack();
    }

    /**
     * Uaktualnianie informacji na widocznych polach z tekstem.
     */
    public synchronized void refreshVisibleLabels() {
        String s = new String("<html>Name: ");
        if (this.activeLocation != null) {
            for (JComponent jc : AntyczneImperium.getVillageRelated_()) {
                if (jc.getName().contains("Location") && jc.isVisible()) {
                    if (!(this.activeLocation instanceof Crossing)) {
                        textLabelLocation(jc, s, (Village) this.activeLocation);
                    } else {
                        textLabelLocation(jc, s, null);
                    }
                    break;
                }
            }
        }
        if (this.activeUnit != null && this.activeUnit.isRunning()) {
            for (JComponent jc : AntyczneImperium.getUnitRelated_()) {
                if (jc.getName().contains("Unit") && jc.isVisible()) {
                    if (this.activeUnit instanceof Caravan) {
                        textLabelUnit(jc, s, (Caravan) this.activeUnit, null);
                    } else {
                        if (this.activeUnit instanceof Barbarians) {
                            textLabelUnit(jc, s, null, (Barbarians) this.activeUnit);
                        } else {
                            textLabelUnit(jc, s, null, null);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tekst do uaktualnienia dla jednostek
     */
    private synchronized void textLabelUnit(JComponent jc, String printableToLabel, Caravan caravan, Barbarians barb) {
        JLabel castLabel;
        printableToLabel = printableToLabel.concat(this.activeUnit.getName());
        if (caravan != null) {
            printableToLabel = printableToLabel.concat(" " + caravan.getSurname());
        }
        printableToLabel = printableToLabel.concat("<br>Target Destination: " + this.activeUnit.getTargetDest().getName());
        printableToLabel = printableToLabel.concat("<br>Speed (1 = best): " + String.valueOf(this.activeUnit.getSpeed()) + "<br>");
        if (caravan != null) {
            printableToLabel = printableToLabel.concat("Speed Penalty: " + String.valueOf(caravan.getSpeedPenalty()) + "<br>");
            printableToLabel = printableToLabel.concat("Wares: ");
            if (caravan.getLoad().isEmpty()) {
                printableToLabel = printableToLabel.concat("None<br>");
            } else {
                for (Res res : caravan.getLoad()) {
                    printableToLabel = printableToLabel.concat("T: " + String.valueOf(res.getResType()) + " Vol: " + String.valueOf(res.getVolume()) + " W: " + String.valueOf(res.getWeight()));
                    if (res.getResType() == 1 || res.getResType() == 16) {
                        printableToLabel = printableToLabel.concat(" Time: " + String.valueOf((int) (res.getTimeDue() - System.currentTimeMillis()) / 1000));
                    }
                    printableToLabel = printableToLabel.concat("<br>");
                }
            }
            printableToLabel = printableToLabel.concat("Capacity: " + String.valueOf(caravan.getCurCapacity()) + "/" + String.valueOf(caravan.getMaxCapacity()) + "<br>");
            printableToLabel = printableToLabel.concat("Wallet: " + String.valueOf(caravan.getWallet()));
        }
        if (barb != null) {
            printableToLabel = printableToLabel.concat("Population: " + String.valueOf(barb.getPopulation()) + "<br>Weaponry: " + barb.getWeaponry());
        }
        printableToLabel = printableToLabel.concat("</html>");
        castLabel = (JLabel) jc;
        castLabel.setText(printableToLabel);

    }

    /**
     * Tekst do uaktualnienia dla lokacji
     */
    private synchronized void textLabelLocation(JComponent jc, String printableToLabel, Village village) {
        JLabel castLabel;
        printableToLabel = printableToLabel.concat(this.activeLocation.getName() + "<br>Residents: ");
        if (this.activeLocation.getResidentList_().isEmpty()) {
            printableToLabel = printableToLabel.concat("None<br>");
        }
        for (int i = 0; i < this.activeLocation.getResidentList_().size(); i++) {
            printableToLabel = printableToLabel.concat(this.activeLocation.getResidentList_().get(i).getName() + "<br>");
        }
        if (village != null) {
            printableToLabel = printableToLabel.concat("Population: " + String.valueOf(village.getPopulation()) + "<br>");
            printableToLabel = printableToLabel.concat("Treasury: " + String.valueOf(village.getTreasury()) + "<br>");
            printableToLabel = printableToLabel.concat("Produces:<br>");
            for (int i = 0; i < 10; i++) {
                if ((village.getResCreated() & (1 << i)) == (1 << i)) {
                    printableToLabel = printableToLabel.concat(String.valueOf(1 << i) + " ");
                }

            }
            printableToLabel = printableToLabel.concat("<br>Available items: " + String.valueOf(village.getResourcesKept().size()) + ", " + String.valueOf(village.getCurStockSell()) + "/" + String.valueOf(village.getMaxStock()) + "<br>");
            printableToLabel = printableToLabel.concat("Uses:<br>");
            for (int i = 0; i < 10; i++) {
                if ((village.getResUsed() & (1 << i)) == (1 << i)) {
                    printableToLabel = printableToLabel.concat(String.valueOf(1 << i) + " ");
                }
            }
            printableToLabel = printableToLabel.concat("<br>Consumable items: " + String.valueOf(village.getResourcesKeptConsumed().size()) + ", " + String.valueOf(village.getCurStockBuy()) + "/" + String.valueOf(village.getMaxStockBuy()) + "<br>");
            printableToLabel = printableToLabel.concat("Sleeping Caravans: " + String.valueOf(village.getSleepingCaravans().size()) + "</html>");
        }
        castLabel = (JLabel) jc;
        castLabel.setText(printableToLabel);
    }

    /**
     * Odpowiednie obsługi zdarzeń na GUI (poza mapą)
     *
     * @param e zdarzenie
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Village v;
        Capital c;
        switch (e.getActionCommand()) {
            case "Send caravan":
                v = (Village) this.activeLocation;
                v.sendCaravan();
                break;
            case "Send legion":
                c = (Capital) this.activeLocation;
                c.dispatchLegion();
                break;
            case "Set res":
                JTextField jt;
                for (JComponent jc : AntyczneImperium.getVillageRelated_()) {
                    if (jc instanceof JTextField) {
                        jt = (JTextField) jc;
                        try {
                            this.resType = Integer.parseInt(jt.getText());
                        } catch (NumberFormatException ex) {
                            System.out.println("Bad text in field");
                        }
                    }
                }
                break;
            case "Produce":
                Res resTemp;
                v = (Village) this.activeLocation;
                if ((v.getResCreated() & this.resType) == this.resType && this.resType != 0) {
                    v.produceStuff(resType);
                }
                break;
            case "Break":
                Caravan caravan;
                caravan = (Caravan) this.activeUnit;
                if (caravan.isBroken()) {
                    caravan.setBroken(false);
                } else {
                    caravan.setBroken(true);
                }
                break;
            case "Kill":
                this.activeUnit.removeUnit();
                break;
            case "Help":
                helpFrame_.setVisible(true);
                break;
            case "Name get":
                AntyczneImperium.setFinishIt(true);
                break;
            default:
                break;
        }
    }

    /**
     * Nic
     */
    @Override
    public void mouseClicked(MouseEvent e) {

    }

    /**
     * Obsługa znajdowania obiektu na który nacisneliśmy na mapie.
     *
     * @param e zdarzenie kliknięcia na mapie.
     */
    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            this.activeLocation = null;
            this.activeUnit = null;
            JButton castButton;
            JLabel castLabel;
            for (ConnectableLoc cl : Database.getLocationList()) {
                if (e.getX() > (cl.getX() * AntyczneImperium.getScaleMultiplier_())
                        && e.getX() < (cl.getxEnd() * AntyczneImperium.getScaleMultiplier_())
                        && e.getY() > (cl.getY() * AntyczneImperium.getScaleMultiplier_())
                        && e.getY() < (cl.getyEnd() * AntyczneImperium.getScaleMultiplier_())) {
                    this.activeLocation = cl;
                    break;
                }
            }
            for (Unit u : Database.getLegionList_()) {
                if (e.getX() > ((u.getX() - 1) * AntyczneImperium.getScaleMultiplier_())
                        && e.getX() < ((u.getxEnd() + 1) * AntyczneImperium.getScaleMultiplier_())
                        && e.getY() > ((u.getY() - 1) * AntyczneImperium.getScaleMultiplier_())
                        && e.getY() < ((u.getyEnd() + 1) * AntyczneImperium.getScaleMultiplier_())) {
                    this.activeUnit = u;
                    break;
                }
            }
            if (this.activeUnit == null) {
                for (Unit u : Database.getBarbarianList()) {
                    if (e.getX() > ((u.getX() - 1) * AntyczneImperium.getScaleMultiplier_())
                            && e.getX() < ((u.getxEnd() + 1) * AntyczneImperium.getScaleMultiplier_())
                            && e.getY() > ((u.getY() - 1) * AntyczneImperium.getScaleMultiplier_())
                            && e.getY() < ((u.getyEnd() + 1) * AntyczneImperium.getScaleMultiplier_())) {
                        this.activeUnit = u;
                        break;
                    }
                }
                if (this.activeUnit == null) {
                    for (Unit u : Database.getCaravanList()) {
                        if (e.getX() > ((u.getX() - 1) * AntyczneImperium.getScaleMultiplier_())
                                && e.getX() < ((u.getxEnd() + 1) * AntyczneImperium.getScaleMultiplier_())
                                && e.getY() > ((u.getY() - 1) * AntyczneImperium.getScaleMultiplier_())
                                && e.getY() < ((u.getyEnd() + 1) * AntyczneImperium.getScaleMultiplier_())) {
                            this.activeUnit = u;
                            break;
                        }
                    }
                }
            }
            String printableToLabel = new String("<html>Name: ");
            if (this.activeLocation != null) {
                Village village = null;
                if (!(this.activeLocation instanceof Crossing)) {
                    village = (Village) this.activeLocation;
                }
                for (JComponent jc : AntyczneImperium.getVillageRelated_()) {
                    if (jc.getName().contains("Location")) {
                        jc.setVisible(true);
                        jc.setEnabled(true);
                        textLabelLocation(jc, printableToLabel, village);
                    } else {
                        if (!(this.activeLocation instanceof Crossing)) {
                            if (jc.getName().contains("Village")) {
                                jc.setVisible(true);
                                jc.setEnabled(true);
                            } else {
                                if (village instanceof Capital) {
                                    jc.setVisible(true);
                                    jc.setEnabled(true);
                                } else {
                                    jc.setVisible(false);
                                    jc.setEnabled(false);
                                }
                            }
                        } else {
                            jc.setVisible(false);
                            jc.setEnabled(false);
                        }
                    }
                }
            } else {
                for (JComponent jc : AntyczneImperium.getVillageRelated_()) {
                    jc.setVisible(false);
                    jc.setEnabled(false);
                }
            }
            printableToLabel = new String("<html>Name: ");
            if (this.activeUnit != null) {
                Caravan caravan = null;
                Barbarians barb = null;
                if (this.activeUnit instanceof Caravan) {
                    caravan = (Caravan) this.activeUnit;
                } else if (this.activeUnit instanceof Barbarians) {
                    barb = (Barbarians) this.activeUnit;
                }
                for (JComponent jc : AntyczneImperium.getUnitRelated_()) {
                    if (jc.getName().contains("Unit")) {
                        jc.setVisible(true);
                        jc.setEnabled(true);
                        textLabelUnit(jc, printableToLabel, caravan, barb);
                    } else {
                        if (jc.getName().contains("Caravan")) {
                            if (caravan != null) {
                                jc.setVisible(true);
                                jc.setEnabled(true);
                            } else {
                                jc.setVisible(false);
                                jc.setEnabled(false);
                            }
                        }
                    }
                }
            } else {
                for (JComponent jc : AntyczneImperium.getUnitRelated_()) {
                    jc.setVisible(false);
                    jc.setEnabled(false);
                }
            }
        } else {
            if (SwingUtilities.isRightMouseButton(e)) {
                System.out.println("PRZYCISK 2");
                ConnectableLoc target = null;
                for (ConnectableLoc cl : Database.getLocationList()) {
                    if (e.getX() > (cl.getX() * AntyczneImperium.getScaleMultiplier_())
                            && e.getX() < (cl.getxEnd() * AntyczneImperium.getScaleMultiplier_())
                            && e.getY() > (cl.getY() * AntyczneImperium.getScaleMultiplier_())
                            && e.getY() < (cl.getyEnd() * AntyczneImperium.getScaleMultiplier_())) {
                        target = cl;
                        break;
                    }
                }
                if (target != null && this.activeUnit != null && !(this.activeUnit instanceof Barbarians)) {
                    this.activeUnit.clearRoute();
                    this.activeUnit.setTargetDest(target);
                    this.activeUnit.pickNextStop();
                }
            }
        }
    }

    /**
     * Pętla dla wątku uaktualniającego.
     */
    @Override
    public void run() {
        while (isRunning()) {
            refreshVisibleLabels();
            try {
                this.thread.sleep(1000 / 60);
            } catch (InterruptedException ex) {
                Logger.getLogger(Eventlistener.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Nic
     */
    @Override
    public void mouseReleased(MouseEvent e) {

    }

    /**
     * Nic
     */
    @Override
    public void mouseEntered(MouseEvent e) {

    }

    /**
     * Nic
     */
    @Override
    public void mouseExited(MouseEvent e) {

    }

    /**
     * @return the activeLocation
     */
    public ConnectableLoc getActiveLocation() {
        return activeLocation;
    }

    /**
     * @param activeLocation the activeLocation to set
     */
    public void setActiveLocation(ConnectableLoc activeLocation) {
        this.activeLocation = activeLocation;
    }

    /**
     * @return the activeUnit
     */
    public Unit getActiveUnit() {
        return activeUnit;
    }

    /**
     * @param activeUnit the activeUnit to set
     */
    public void setActiveUnit(Unit activeUnit) {
        this.activeUnit = activeUnit;
    }

    /**
     * @return the running
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * @param running the running to set
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
