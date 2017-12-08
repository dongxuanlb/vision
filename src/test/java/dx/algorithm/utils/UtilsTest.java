package dx.algorithm.utils;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

/**
 * Created by dongxuan on 11/05/2017.
 */
public class UtilsTest {

    @Test
    public void testFastAbs() {
        StopWatch sw = new StopWatch();
        double a = .0;
        sw.start();
        for (int i=0;i<1000000000;i++) {
            a = Math.abs(-134);
        }
        System.out.println(sw.getNanoTime() + ":" + a);
        sw = new StopWatch();
        sw.start();
        for (int i=0;i<1000000000;i++) {
            a = Maths.abs(-134);
        }
        System.out.println(sw.getNanoTime() + ":" + a);

        sw = new StopWatch();
        sw.start();
        for (int i=0;i<1000000000;i++) {
            a = Maths.abs(-134);
        }
        System.out.println(sw.getNanoTime() + ":" + a);

        sw = new StopWatch();
        sw.start();
        for (int i=0;i<1000000000;i++) {
            a = Math.abs(-134);
        }
        System.out.println(sw.getNanoTime() + ":" + a);

        sw = new StopWatch();
        sw.start();
        for (int i=0;i<1000000000;i++) {
            a = Math.abs(-134);
        }
        System.out.println(sw.getNanoTime() + ":" + a);
    }

}
