package org.icoder.mapping;

public class TraceStack {
    private String id;
    private String service;
    private String start;
    private String end;
    private String caller;
    private String calls;

    public TraceStack(String id, String service, String start, String end, String from, String to) {
        this.id = id;
        this.service = service;
        this.start = start;
        this.end = end;
        this.caller = from;
        this.calls = to;
    }

    public String getId() {
        return id;
    }

    public String getService() {
        return service;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getCaller() {
        return caller;
    }

    public String getCalls() {
        return calls;
    }

    @Override
    public String toString() {
        return String.format(
                "id %s, service %s, start %s, end %s, caller %s, calls %s",
                id, service, start, end, caller, calls
        );
    }
}
