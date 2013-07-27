package com.versionone.git;

import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;

public class SshSessionFactory extends JschConfigSessionFactory {
	private String password;
    private String passphrase;

    public SshSessionFactory(String password, String passphrase) {
        this.password = password;
        this.passphrase = passphrase;
    }

    /**
     * Install this session factory implementation into the JVM.
     */
	public static void installWithCredentials(String password, String passphrase) {
		SshSessionFactory.setInstance(new SshSessionFactory(password, passphrase));
	}

	@Override
	protected void configure(final OpenSshConfig.Host hc, final Session session) {
		if (!hc.isBatchMode()) {
			session.setUserInfo(new SilentUserInfo(password, passphrase));
        }
	}
}