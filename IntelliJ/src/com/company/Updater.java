package com.company;

import java.time.Duration;

/**
 * Created by brian on 10/16/16.
 */
public class Updater implements Runnable {

    private static Duration updatePeriod = null;

    @Override
    public void run() {
        for(int count = 0; true; ++count) {
            System.out.println("Update " + count);

            try {
                Thread.sleep(updatePeriod.toMillis());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    Updater(Duration timeBetweenUpdates) {
        updatePeriod = timeBetweenUpdates;
    }
}
