package example;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

/**
 * @author adam.kotwasinski
 * @since test
 */
public class ExampleUnitTest {

    @Test
    public void shouldDoSomething() {
        assertThat(13 + 42, equalTo(55));
    }

}
