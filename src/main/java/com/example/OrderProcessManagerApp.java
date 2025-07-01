package com.example;

import org.apache.camel.main.Main;

public class OrderProcessManagerApp {
    public static void main(String[] args) throws Exception {
        Main main = new Main();
        main.configure().addRoutesBuilder(new OrderProcessRoute());
        main.run(args);
    }
}
