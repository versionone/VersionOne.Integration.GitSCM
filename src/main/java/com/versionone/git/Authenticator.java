package com.versionone.git;

import org.eclipse.jgit.util.CachedAuthenticator;

import java.net.PasswordAuthentication;

public class Authenticator extends CachedAuthenticator {
	private String username;
    private String password;

    public Authenticator(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Install this authenticator implementation into the JVM.
     */
	public static void install(String username, String password) {
		setDefault(new Authenticator(username, password));
	}

	@Override
	protected PasswordAuthentication promptPasswordAuthentication() {
		return new PasswordAuthentication(username, password.toCharArray());
	}
}

