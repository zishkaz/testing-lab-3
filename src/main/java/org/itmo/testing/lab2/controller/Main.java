package org.itmo.testing.lab2.controller;

import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {
        Javalin app = UserAnalyticsController.createApp();
        app.start(7000);
    }
}
