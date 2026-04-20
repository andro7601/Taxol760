package com.taxol760;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class AppTest {
    @Test
    void greetingReturnsProjectMessage() {
        assertEquals("Hello from Taxol760", App.greeting());
    }
}
