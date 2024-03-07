package uk.gov.di.mobile.wallet.cri;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppTest {
    @Test
    public void appHasAGreeting() {
        MockCriApp classUnderTest = new MockCriApp();
        assertNotNull("app should have a greeting", classUnderTest.getGreeting());
    }
}
