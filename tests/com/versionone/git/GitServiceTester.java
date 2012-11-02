package com.versionone.git;

import com.versionone.git.configuration.Configuration;
import com.versionone.git.storage.IDbStorage;
import com.versionone.git.storage.PersistentChange;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GitServiceTester {
    private JUnit4Mockery context;
    private Configuration configuration;
    private IGitConnector gitConnectorMock;
    private IDbStorage storageMock;
    private IChangeSetWriter v1ConnectorMock;

    @Before
    public void before() {
        context = new JUnit4Mockery();
        configuration = Configuration.getInstance(ConfigurationTester.class.getResource("test_configuration.xml").getPath());
        gitConnectorMock = context.mock(IGitConnector.class);
        storageMock = context.mock(IDbStorage.class);
        v1ConnectorMock = context.mock(IChangeSetWriter.class);
    }

    @Test
    public void emptyChangeset() throws GitException, VersionOneException {
        GitService service = new GitService(storageMock, gitConnectorMock, v1ConnectorMock, "repo id");

        context.checking(new Expectations() {{
            oneOf(gitConnectorMock).initRepository();
            oneOf(gitConnectorMock).watchedBranchExists();
                will(returnValue(true));
            oneOf(gitConnectorMock).getWatchedBranchName();
                will(returnValue("refs/remotes/origin/master"));
            oneOf(gitConnectorMock).getChangeSets(); will(returnValue(new LinkedList()));
        }});

        service.initialize();
        service.onInterval();
    }

    @Test
    public void branchCommits() throws GitException, VersionOneException {
        final String repositoryId = "repo id";
        GitService service = new GitService(storageMock, gitConnectorMock, v1ConnectorMock, repositoryId);

        final ChangeSetInfo firstChange = new ChangeSetInfo(configuration.getGitConnections().get(0), "user", "first commit", "1", new Date());
        final ChangeSetInfo secondChange = new ChangeSetInfo(configuration.getGitConnections().get(0), "user", "second commit", "2", new Date());
        final List<ChangeSetInfo> changes = new LinkedList<ChangeSetInfo>();
        changes.add(firstChange);
        changes.add(secondChange);

        context.checking(new Expectations() {{
            oneOf(gitConnectorMock).initRepository();
            oneOf(gitConnectorMock).watchedBranchExists();
                will(returnValue(true));
            oneOf(gitConnectorMock).getWatchedBranchName();
                will(returnValue("refs/remotes/origin/master"));
            oneOf(gitConnectorMock).getChangeSets();
                will(returnValue(changes));
            PersistentChange firstPersistentChange = PersistentChange.createNew(firstChange.getRevision(), repositoryId);
            PersistentChange secondPersistentChange = PersistentChange.createNew(secondChange.getRevision(), repositoryId);
            oneOf(storageMock).isChangePersisted(firstPersistentChange);
                will(returnValue(false));
            oneOf(v1ConnectorMock).publish(firstChange);
            oneOf(storageMock).persistChange(firstPersistentChange);
            oneOf(storageMock).isChangePersisted(secondPersistentChange);
                will(returnValue(true));
            never(v1ConnectorMock).publish(secondChange);
            never(storageMock).persistChange(secondPersistentChange);
        }});

        service.initialize();
        service.onInterval();
    }

    @Test
    public void branchNames() throws GitException, VersionOneException {
        final String repositoryId = "repo id";
        GitService service = new GitService(storageMock, gitConnectorMock, v1ConnectorMock, repositoryId);

        final ChangeSetInfo firstChange = new ChangeSetInfo(configuration.getGitConnections().get(0), "user", "first commit", "1", new Date());
        final ChangeSetInfo secondChange = new ChangeSetInfo(configuration.getGitConnections().get(0), "user", "second commit", "2", new Date());
        final List<ChangeSetInfo> changes = new LinkedList<ChangeSetInfo>();
        changes.add(firstChange);
        changes.add(secondChange);

        context.checking(new Expectations() {{
            oneOf(gitConnectorMock).initRepository();
            oneOf(gitConnectorMock).watchedBranchExists();
                will(returnValue(true));
            oneOf(gitConnectorMock).getWatchedBranchName();
                will(returnValue("refs/remotes/origin/master"));
            oneOf(gitConnectorMock).getChangeSets();
                will(returnValue(changes));
            PersistentChange firstPersistentChange = PersistentChange.createNew(firstChange.getRevision(), repositoryId);
            PersistentChange secondPersistentChange = PersistentChange.createNew(secondChange.getRevision(), repositoryId);
            oneOf(storageMock).isChangePersisted(firstPersistentChange);
                will(returnValue(false));
            oneOf(v1ConnectorMock).publish(firstChange);
            oneOf(storageMock).persistChange(firstPersistentChange);
            oneOf(storageMock).isChangePersisted(secondPersistentChange);
                will(returnValue(false));
            oneOf(v1ConnectorMock).publish(secondChange);
            oneOf(storageMock).persistChange(secondPersistentChange);
        }});

        service.initialize();
        service.onInterval();
    }
}