package com.versionone.git.storage;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class DbStorage implements IDbStorage {

    private static final Logger LOG = Logger.getLogger("GitIntegration");
    private final String LAST_COMMIT_HASH = "LastCommitHash";
    private final Session session;

    private Session getSession() {
        return session;
    }

    public DbStorage() {
        SessionFactory factory = new Configuration().configure().buildSessionFactory();
        session = factory.openSession();
    }

    public List<PersistentChange> getPersistedChanges() {
        List<PersistentChange> changes = getSession().createCriteria(PersistentChange.class).list();
        if (changes != null) {
        	return (List<PersistentChange>)changes;
        }
        return new ArrayList<PersistentChange>();
    }

    public void persistChange(PersistentChange change) {
        Transaction tr = getSession().beginTransaction();
        getSession().save(change);
        tr.commit();
        getSession().flush();
    }

    public boolean isChangePersisted(PersistentChange change) {
        Criteria criteria = getSession()
                .createCriteria(PersistentChange.class)
                .add(Restrictions.eq("hash", change.getHash()))
                .add(Restrictions.eq("repositoryId", change.getRepositoryId()));
        Long count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return count > 0;
    }

    public void persistLastCommit(String commitHash, String repositoryId, String branchRef) {
        LOG.debug(String.format("Persisting to local store commit %1$s in repository %2$s on branch %3$s", commitHash, repositoryId, branchRef));

        LastProcessedItem lastHash = new LastProcessedItem();
        //lastHash.setId(LAST_COMMIT_HASH + "||" + repositoryId);
        lastHash.setRepositoryId(repositoryId);
        lastHash.setBranchRef(branchRef);
        lastHash.setValue(commitHash);

        Transaction tr = getSession().beginTransaction();
        getSession().saveOrUpdate(lastHash);
        tr.commit();
        getSession().flush();
    }

    public String getLastCommit(String repositoryId, String branchRef){
        LOG.debug(String.format("Querying local store for last commit in repository %1$s on branch %2$s", repositoryId, branchRef));

        Criteria criteria = getSession().
                                    createCriteria(LastProcessedItem.class)
                                    //.add(Restrictions.eq("id", LAST_COMMIT_HASH + "||" + repositoryId))
                                    .add(Restrictions.eq("repositoryId", repositoryId))
                                    .add(Restrictions.eq("branchRef", branchRef));
        LastProcessedItem result = (LastProcessedItem)criteria.uniqueResult();
        // Not sure why the result needs to be evicted. First time will be null, causing NPE.
        //getSession().evict(result);
        return result == null ? null : result.getValue();
    }
}