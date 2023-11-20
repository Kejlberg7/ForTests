package hello;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Greeter3Test {

    @Test
    @Fast
    public void testMultiply() {
        Greeter3 greeter = new Greeter3();
        int result = greeter.multiply(1, 2, 3);
        assertEquals(6, result);
    }

}
