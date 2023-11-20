package hello;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GreeterTest {

    @Test
    @Fast
    public void testAdd() {
        Greeter greeter = new Greeter();
        int result = greeter.add(1, 2);
        assertEquals(3, result);
    }

    @Test
    @Slow
    public void testAdd2() {
        Greeter greeter = new Greeter();
        int result = greeter.add(1, 2);
        assertEquals(3, result);
    }


}
