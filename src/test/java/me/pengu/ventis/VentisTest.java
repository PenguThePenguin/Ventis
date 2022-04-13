package me.pengu.ventis;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

// Todo: create tests
public class VentisTest {

    static Ventis tester;

    @BeforeClass()
    public static void testSetup() {
        VentisConfig config = VentisConfig.builder()
                .channel("bukkit")
                .address("localhost")
                .port(6379)
                .build();

        tester = new Ventis(config);
        assert tester.isConnected();
    }

    @AfterClass
    public static void testCleanup() {
        tester.close();
    }
}