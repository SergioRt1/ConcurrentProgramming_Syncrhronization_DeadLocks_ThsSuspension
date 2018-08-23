package edu.eci.arsw.highlandersim;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.rowset.spi.SyncResolver;

public class Immortal extends Thread {

    private ImmortalUpdateReportCallback updateCallback = null;

    private int health;

    private int defaultDamageValue;

    private final List<Immortal> immortalsPopulation;

    private final String name;

    private final Random r = new Random(System.currentTimeMillis());

    private final Thread mainThread;
    private final AtomicBoolean isPause;
    private final AtomicBoolean isStop;

    public Immortal(String name, List<Immortal> immortalsPopulation, int health, int defaultDamageValue, ImmortalUpdateReportCallback ucb, Thread mainThread, AtomicBoolean isPause, AtomicBoolean isStop) {
        super(name);
        this.updateCallback = ucb;
        this.name = name;
        this.immortalsPopulation = immortalsPopulation;
        this.health = health;
        this.defaultDamageValue = defaultDamageValue;
        this.mainThread = mainThread;
        this.isPause = isPause;
        this.isStop = isStop;
    }

    public void run() {

        while (!isDeath() && !isStop.get()) {
            Immortal im;

            int myIndex = immortalsPopulation.indexOf(this);

            int nextFighterIndex = r.nextInt(immortalsPopulation.size());

            //avoid self-fight
            if (nextFighterIndex == myIndex) {
                nextFighterIndex = ((nextFighterIndex + 1) % immortalsPopulation.size());
            }

            im = immortalsPopulation.get(nextFighterIndex);
            this.fight(im);

            while (isPause.get()) {
                synchronized (mainThread) {
                    try {
                        mainThread.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Immortal.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
        synchronized(this){
            synchronized(immortalsPopulation){
                immortalsPopulation.remove(this);
            }
        }
        

    }
    
    public boolean isDeath(){
        return this.getHealth() <= 0;
    }

    public void fight(Immortal i2) {

        if (i2.getHealth() > 0) {
            synchronized (i2) {
                i2.changeHealth(i2.getHealth() - defaultDamageValue);
            }
            synchronized (this) {
                this.health += defaultDamageValue;
            }
            updateCallback.processReport("Fight: " + this + " vs " + i2 + "\n");

        } else {
            updateCallback.processReport(this + " says:" + i2 + " is already dead!\n");
        }

    }

    public void changeHealth(int v) {
        health = v;
    }

    public int getHealth() {
        return health;
    }

    @Override
    public String toString() {

        return name + "[" + health + "]";
    }

}
