package com.versionone.git;

import org.junit.Assert;
import org.junit.Test;

import static com.versionone.git.Utilities.escapeHTML;

public class UtilityTester {
    @Test
    public void shouldEscapeQuotes() {
        Assert.assertEquals("quotes", "&#34;hello&#34;", escapeHTML("\"hello\""));
    }

    @Test
    public void shouldEscapeTagBrackets() {
        Assert.assertEquals("brackets", "John Doe &#60;john.doe@me.com&#62;\n", escapeHTML("John Doe <john.doe@me.com>\n"));
    }

    @Test
    public void shouldEscapeAmpersand() {
        Assert.assertEquals("brackets", "Bibb &#38; Tucker", escapeHTML("Bibb & Tucker"));
    }

    @Test
    public void shouldEscapeNonAscii() {
        Assert.assertEquals("brackets", "Mich&#145;l", escapeHTML("Mich\u0091l"));
    }
}
