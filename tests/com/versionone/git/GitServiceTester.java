package com.versionone.git;

import com.versionone.git.configuration.Configuration;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class GitServiceTester {
    private JUnit4Mockery context;
    private IGitConnector gitConnectorMock;
    private IDbStorage storageMock;
    private IChangeSetWriter v1ConnectorMock;

    @Before
    public void before() {
        context = new JUnit4Mockery();
        gitConnectorMock = context.mock(IGitConnector.class);
        storageMock = context.mock(IDbStorage.class);
        v1ConnectorMock = context.mock(IChangeSetWriter.class);
    }

    @Test
    public void emptyChangesetTest() throws GitException, VersionOneException {
        GitService service = new GitService(storageMock, gitConnectorMock, v1ConnectorMock);

        context.checking(new Expectations() {{
            oneOf(gitConnectorMock).cleanupLocalDirectory();
            oneOf(gitConnectorMock).initRepository();
            oneOf(gitConnectorMock).getCommits(); will(returnValue(new LinkedList()));
        }});

        service.initialize();
        service.onInterval();
    }

    @Test
    public void branchCommitsTest() throws GitException, VersionOneException {
        GitService service = new GitService(storageMock, gitConnectorMock, v1ConnectorMock);

        final ChangeSetInfo firstChange = new ChangeSetInfo("user", "first commit", "1", new Date());
        final ChangeSetInfo secondChange = new ChangeSetInfo("user", "second commit", "2", new Date());
        final List<ChangeSetInfo> changes = new LinkedList<ChangeSetInfo>();
        changes.add(firstChange);
        changes.add(secondChange);

        context.checking(new Expectations() {{
            oneOf(gitConnectorMock).cleanupLocalDirectory();
            oneOf(gitConnectorMock).initRepository();
            oneOf(gitConnectorMock).getCommits();
                will(returnValue(changes));
            PersistentChange firstPersistentChange = PersistentChange.createNew(firstChange.getRevision());
            PersistentChange secondPersistentChange = PersistentChange.createNew(secondChange.getRevision());
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
    public void branchNamesTest() throws GitException, VersionOneException {
        GitService service = new GitService(storageMock, gitConnectorMock, v1ConnectorMock);

        final ChangeSetInfo firstChange = new ChangeSetInfo("user", "first commit", "1", new Date());
        final ChangeSetInfo secondChange = new ChangeSetInfo("user", "second commit", "2", new Date());
        final List<ChangeSetInfo> changes = new LinkedList<ChangeSetInfo>();
        changes.add(firstChange);
        changes.add(secondChange);

        context.checking(new Expectations() {{
            oneOf(gitConnectorMock).cleanupLocalDirectory();
            oneOf(gitConnectorMock).initRepository();
            oneOf(gitConnectorMock).getCommits();
                will(returnValue(changes));
            PersistentChange firstPersistentChange = PersistentChange.createNew(firstChange.getRevision());
            PersistentChange secondPersistentChange = PersistentChange.createNew(secondChange.getRevision());
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