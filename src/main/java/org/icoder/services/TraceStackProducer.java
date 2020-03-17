package org.icoder.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.icoder.mapping.Call;
import org.icoder.mapping.Trace;
import org.icoder.mapping.TraceStack;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;


public class TraceStackProducer {

    private static Logger logger = LogManager.getLogger(TraceStackProducer.class);

    private volatile List<TraceStack> traceStacks = new ArrayList<>();
    private volatile List<Trace> traces = new ArrayList<>();
    private volatile HashMap<String, Integer> traceIndexMap = new HashMap<>();

    private AtomicBoolean readDone = new AtomicBoolean(false);
    private AtomicBoolean traceStackProcess = new AtomicBoolean(false);
    private AtomicLong traceStackIterationsNum = new AtomicLong(0);

    /**
     * Set read flag when all data is read from file
     */
    public void setReadDone() {
        readDone.set(true);
    }

    /**
     * Read and process done
     *
     * @return Check if process is done
     */
    public boolean isProcessDone() {
        return traceStackProcess.get() && readDone.get();
    }

    /**
     * Get trace list
     *
     * @return List<Trace
                    */
    public List<Trace> getTraces() {
        return traces;
    }

    /**
     * Append line
     *
     * @param line String
     */
    public void append(String line) {
        List<String> stringList = Arrays.asList(line.split("\\s"));
        List<String> callers = Arrays.asList(stringList.get(4).split("->"));
        TraceStack traceStack = new TraceStack(
                stringList.get(2),
                stringList.get(3),
                stringList.get(0),
                stringList.get(1),
                callers.get(0),
                callers.get(1)
        );
        synchronized (this) {
            traceStacks.add(traceStack);
        }
    }


    /**
     * Process all other traces which are not null or root ones
     *
     * @param identifier int thread identifier so we avoid multiple threads tread going over same data
     */
    public void processTreeStack(int identifier, int finSize) {
        int i = -1;

        while (true) {
            int size = getTraceStackSize();
            // thread start lookup from index logic
            if (i == -1) {
                i = (size / identifier);
            }
            // we have to exit loop if processing is done!
            if (size == 0 || i < 0) {
                if (readDone.get() && size == 0) {
                    logger.debug("All traces process done!");
                    traceStackProcess.set(true);
                    break;
                }
                continue;
            }

            TraceStack traceStack = getTraceStack(i);
            updateTraceStackIterationsNum();

            // try to split work equally so threads don't repeat each other
            int divideBy = finSize - identifier;
            if (divideBy == 0) {
                divideBy = 1;
            }
            // speed also depends on null processor because all roots has to be created first
            if (size > 0 && i >= (size / divideBy)) {
                --i;
            } else {
                i = -1;
            }

//            logger.debug(
//                    " thread id {} trace id {}",
//                    Thread.currentThread().getId(),
//                    traceStack != null ? traceStack.getId() + "_" + traceStack.getCaller() + "->" + traceStack.getCalls() : null
//            );


            Call call = findCaller(traceStack);

            if (call != null && traceStack != null) {
                synchronized (this) {
                    traceStacks.remove(traceStack);
                    Call nCall = fromStackTrace(traceStack);
                    if (!isCallPresent(call, nCall)) {
                        call.appendCall(nCall);
                    }

                }
            }

        }
    }

    /**
     * Processing null tree search on data
     */
    public void processNullTreeStack() {
        int last = 10; // check last added values only
        int i = 0;
        while (true) {
            int size = getTraceStackSize();
            // we have to exit loop if processing is done!
            if (readDone.get() && size == 0) {
                logger.debug("All null traces process done!");
                traceStackProcess.set(true);
                break;
            } else if (i >= size) {
                // lets find only last added items to queue
                if (size > last) {
                    i = size - last;
                } else {
                    i = 0;
                }
            }
            // if size in any case is 0 continue until we get data to process
            if (size == 0) {
                continue;
            }

            TraceStack traceStack = getTraceStack(i);

            if (traceStack != null && traceStack.getCaller().equals("null")) {
                Trace trace = new Trace();
                trace.setId(traceStack.getId());
                trace.setRoot(fromStackTrace(traceStack));
                synchronized (this) {
                    traceStacks.remove(traceStack);
                    traces.add(trace);
                    traceIndexMap.put(traceStack.getId(), traces.indexOf(trace));
                }
            }

            ++i;
        }
    }

    /**
     * Show status update
     */
    public void tickStatus() {
        logger.debug("Items to process {} trace iterations {}", getTraceStackSize(), traceStackIterationsNum.get());
        synchronized (this) {
            logger.debug("Traces created {}", traces.size());
        }
    }

    /**
     * Update trace stack number
     */
    private void updateTraceStackIterationsNum() {
        traceStackIterationsNum.set(traceStackIterationsNum.get() + 1);
    }

    /**
     * Get trace stack by index
     *
     * @param index int
     * @return TraceStack
     */
    private TraceStack getTraceStack(int index) {
        synchronized (this) {
            int size = getTraceStackSize();
            if (size == 0 || index >= size || index < 0) {
                return null;
            }
            return traceStacks.get(index);
        }
    }

    /**
     * Get trace stack size
     *
     * @return int
     */
    private int getTraceStackSize() {
        return traceStacks.size();
    }

    /**
     * Build caller from trace stack
     *
     * @param traceStack TraceStack
     * @return Call
     */
    private Call fromStackTrace(TraceStack traceStack) {
        Call call = new Call();
        call.setStart(traceStack.getStart());
        call.setEnd(traceStack.getEnd());
        call.setService(traceStack.getService());
        call.setSpan(traceStack.getCalls());
        return call;
    }

    /**
     * Find caller including root
     *
     * @param traceStack TraceStack
     * @return Call
     */
    private Call findCaller(TraceStack traceStack) {
        return findCaller(getRootCaller(traceStack), traceStack);
    }

    /**
     * Check If is present
     *
     * @param root Call
     * @param call Call
     * @return boolean
     */
    private boolean isCallPresent(Call root, Call call) {
        return root.getCalls().stream().anyMatch(item -> item.getSpan().equals(call.getSpan()));
    }

    /**
     * Recursive find caller
     *
     * @param caller     Call
     * @param traceStack TraceStack
     * @return Call
     */
    private Call findCaller(Call caller, TraceStack traceStack) {
        synchronized (this) {
            if (caller != null && caller.getSpan().equals(traceStack.getCaller())) {
                return caller;
            }
        }
        if (caller != null && caller.getCalls().size() > 0) {
            synchronized (this) {
                for (Call call : caller.getCalls()) {
                    Call cCall = findCaller(call, traceStack);
                    if (cCall != null) {
                        return cCall;
                    }
                }
            }

        }
        return null;
    }

    /**
     * Get root caller
     *
     * @param traceStack TraceStack
     * @return Call
     */
    private Call getRootCaller(TraceStack traceStack) {
        try {
            Integer index = traceIndexMap.get(traceStack.getId());
            return traces.get(index).getRoot();
        } catch (Exception e) {
            return null;
        }
    }

}
