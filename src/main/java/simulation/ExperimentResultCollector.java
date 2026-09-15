package simulation;

import model.ExperimentResult;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public final class ExperimentResultCollector {

    private static CountDownLatch latch =
            new CountDownLatch(1);

    private static volatile ExperimentResult result;

    private ExperimentResultCollector() {
    }

    public static synchronized void reset() {
        result = null;
        latch = new CountDownLatch(1);
    }

    public static void complete(ExperimentResult experimentResult) {
        result = experimentResult;
        latch.countDown();
    }

    public static ExperimentResult awaitResult(
            long timeout,
            TimeUnit unit
    ) throws InterruptedException {

        boolean completed =
                latch.await(timeout, unit);

        if (!completed) {
            return null;
        }

        return result;
    }
}