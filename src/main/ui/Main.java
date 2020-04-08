package ui;

import processing.core.PApplet;

public class Main {
    public static void main(String[] args) {
        //noinspection SpellCheckingInspection
        String[] processingArgs = {"lmao"};
        ServerClientHearts sch = new ServerClientHearts();
        PApplet.runSketch(processingArgs, sch);
    }
}
