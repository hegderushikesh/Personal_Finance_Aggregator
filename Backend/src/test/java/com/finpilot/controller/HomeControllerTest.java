package com.finpilot.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HomeControllerTest {

    @Test
    void testHome() {
        HomeController controller = new HomeController();
        String result = controller.home();
        assertEquals("FinPilot Backend is running successfully!", result);
    }
}

