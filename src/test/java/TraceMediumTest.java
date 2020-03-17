import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icoder.mapping.Trace;
import org.icoder.services.LogConsumer;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author igorzg on 13.03.18.
 * @since 1.0
 */

public class TraceMediumTest extends AbstractTest {

    private Logger logger = LogManager.getLogger(TraceMediumTest.class);

    @Test
    public void buildMediumTraceAndCompareWithResultCase() throws Exception {
        logger.debug("Start processing medium-log.txt");
        LogConsumer logConsumer = new LogConsumer();
        List<Trace> traceProgramList = logConsumer.process(getInputFile("medium-log.txt"), null, false);
        List<Trace> traceResultList = inputFileToTraceList("medium-traces.txt");
        assertEquals(traceProgramList.size(), traceResultList.size());
        assertTrue(areListEqual(traceProgramList, traceResultList));
    }


}
