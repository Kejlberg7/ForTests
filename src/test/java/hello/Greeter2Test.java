package hello;

import org.junit.jupiter.api.Test;

public class Greeter2Test {

    @Test
    @Fast
    public void testMinus() {
        Greeter2 greeter = new Greeter2();
        int result = greeter.minus(1, 3);
        System.out.println("result: " + result);
    }
}
