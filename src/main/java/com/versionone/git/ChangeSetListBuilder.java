package com.versionone.git;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class ChangeSetListBuilder {
    private final List<ChangeSetInfo> changes = new LinkedList<ChangeSetInfo>();
    private final Pattern regexp;

    public ChangeSetListBuilder(Pattern pattern) {
        regexp = pattern;
    }

    protected boolean matchByPattern(String valueToMatch) {
        return regexp.matcher(valueToMatch).find();
    }

    public abstract boolean shouldAdd(ChangeSetInfo changeSet);
    
    public ChangeSetListBuilder add(ChangeSetInfo changeSet) {
        if(shouldAdd(changeSet)) {
            changes.add(changeSet);
        }

        return this;
    }

    public List<ChangeSetInfo> build() {
        return changes;
    }
}
