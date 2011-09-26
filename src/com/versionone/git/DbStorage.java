package com.versionone.git;

import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class DbStorage implements IDbStorage {
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
        List changes = getSession().createCriteria(PersistentChange.class).list();
        return (List<PersistentChange>)changes;
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
                .add(Restrictions.eq("hash", change.getHash()));
        Integer count = (Integer) criteria.setProjection(Projections.rowCount()).uniqueResult();
        return count > 0;
    }

    public void persistLastCommit(String commitHash) {
        DictionaryItem lastHash = new DictionaryItem();
        lastHash.setId(LAST_COMMIT_HASH);
        lastHash.setValue(commitHash);

        Transaction tr = getSession().beginTransaction();
        getSession().saveOrUpdate(lastHash);
        tr.commit();
        getSession().flush();
    }

    public String getLastCommit(){
        Criteria criteria = getSession().createCriteria(DictionaryItem.class).add(Restrictions.eq("id", LAST_COMMIT_HASH));
        DictionaryItem result = (DictionaryItem)criteria.uniqueResult();
        getSession().evict(result);
        return result == null ? null : result.getValue();
    }
}