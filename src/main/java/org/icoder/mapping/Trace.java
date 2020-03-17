package org.icoder.mapping;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Trace {
    private String id;
    private Call root;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Call getRoot() {
        return root;
    }

    public void setRoot(Call root) {
        this.root = root;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Trace that = (Trace) o;

        return that.getId().equals(this.getId()) && that.getRoot().equals(this.getRoot());
    }
}
