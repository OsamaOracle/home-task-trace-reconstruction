import com.fasterxml.jackson.databind.ObjectMapper;
import org.icoder.mapping.Trace;

import java.io.BufferedReader;
import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author igorzg on 13.03.18.
 * @since 1.0
 */
class AbstractTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    File getInputFile(String fileName) {
        return new File(TraceSmallTest.class.getClassLoader().getResource(fileName).getFile());
    }

    List<Trace> inputFileToTraceList(String fileName) throws Exception {
        File fileInput = getInputFile(fileName);
        BufferedReader bufferedReader = Files.newBufferedReader(fileInput.toPath());
        final long count = Files.lines(fileInput.toPath(), Charset.defaultCharset()).count();
        List<Trace> traceList = new ArrayList<>();
        for (long i = count; i > 0; --i) {
            String line = bufferedReader.readLine();
            Trace trace = objectMapper.treeToValue(objectMapper.readTree(line), Trace.class);
            traceList.add(trace);
        }
        return traceList;
    }

    boolean areListEqual(List<Trace> traceProgramList, List<Trace> traceResultList) {
        return traceResultList.size() == traceResultList.size() && traceResultList.stream().allMatch(item -> {
            Optional<Trace> trace = traceProgramList.stream().filter(cItem -> cItem.getId().equals(item.getId())).findFirst();
            return trace.map(trace1 -> trace1.equals(item)).orElse(false);
        });
    }
}
