import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icoder.mapping.Trace;
import org.icoder.services.LogConsumer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @author igorzg on 13.03.18.
 * @since 1.0
 */
public class TraceSmallTest extends AbstractTest {

    private Logger logger = LogManager.getLogger(TraceSmallTest.class);

    @Test
    public void buildSmallTraceAndCompareWithResultCase() throws Exception {
        logger.debug("Start processing small-log.txt");
        LogConsumer logConsumer = new LogConsumer();
        List<Trace> traceProgramList = logConsumer.process(getInputFile("small-log.txt"), null, false);
        List<Trace> traceResultList = inputFileToTraceList("small-traces.txt");
        assertEquals(traceProgramList.size(), traceResultList.size());
        assertTrue(areListEqual(traceProgramList, traceResultList));
    }


}
