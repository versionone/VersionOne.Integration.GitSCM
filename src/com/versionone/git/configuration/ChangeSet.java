package com.versionone.git.configuration;

import javax.xml.bind.annotation.XmlElement;
import java.lang.reflect.Type;

public class ChangeSet {

    @XmlElement(name = "AlwaysCreate")
    private boolean alwaysCreate;
    @XmlElement(name = "ChangeComment")
    private String changeComment;
    @XmlElement(name = "ReferenceAttribute")
    private String referenceAttribute;
    @XmlElement(name = "ReferenceExpression")
    private String referenceExpression;

    public boolean isAlwaysCreate() {
        return alwaysCreate;
    }

    public String getReferenceAttribute() {
        return referenceAttribute;
    }

    public String getReferenceExpression() {
        return referenceExpression;
    }

    public String getChangeComment() {
        return changeComment;
    }
}
