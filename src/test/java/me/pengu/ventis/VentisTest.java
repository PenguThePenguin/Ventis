package me.pengu.ventis;

import me.pengu.ventis.messenger.Messenger;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

// Todo: create tests
public class VentisTest {

    static Messenger tester;

    @BeforeClass()
    public static void testSetup() {
    }

    @AfterClass
    public static void testCleanup() {
        tester.close();
    }
}