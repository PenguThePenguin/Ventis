package me.pengu.ventis;

import me.pengu.ventis.connection.Connection;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

// Todo: create tests
public class VentisTest {

    static Connection tester;

    @BeforeClass()
    public static void testSetup() {
    }

    @AfterClass
    public static void testCleanup() {
        tester.close();
    }
}