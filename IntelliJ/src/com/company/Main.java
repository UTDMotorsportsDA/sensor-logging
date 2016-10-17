package com.company;

import java.time.Duration;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello from BeagleBone.");

        (new Thread(new Updater(Duration.ofMillis(500)))).start();
    }
}
