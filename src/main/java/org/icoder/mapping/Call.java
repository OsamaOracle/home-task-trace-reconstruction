package org.icoder.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Call {

    private String start;
    private String end;
    private String service;
    private String span;
    private List<Call> calls = new ArrayList<>();


    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public List<Call> getCalls() {
        return calls;
    }

    public void appendCall(Call call) {
        this.calls.add(call);
    }


    public String getSpan() {
        return span;
    }

    public void setSpan(String span) {
        this.span = span;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Call that = (Call) o;

        return that.getStart().equals(this.getStart()) &&
                that.getEnd().equals(this.getEnd()) &&
                that.getService().equals(this.getService()) &&
                that.getSpan().equals(this.getSpan()) &&
                that.getCalls().size() == this.getCalls().size() &&
                that.getCalls().stream().allMatch(item -> {
                    Optional<Call> curr = this.getCalls().stream().filter(cFilt -> cFilt.getSpan().equals(item.getSpan())).findFirst();
                    return curr.map(call -> call.equals(item)).orElse(false);
                });
    }
}
