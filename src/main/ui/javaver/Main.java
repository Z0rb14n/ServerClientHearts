package ui.javaver;

import ui.console.Console;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MainFrame.getFrame();
        Timer timer = new Timer(100, e -> MainFrame.getFrame().update());
        timer.start();
        Console.getConsole();
    }
}
