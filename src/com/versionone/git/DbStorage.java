package com.versionone.git;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.sql.SQLException;
import java.util.Collection;

public class DbStorage implements IDbStorage {
    private final Session session;

    private Session getSession() {
        return session;
    }

    public DbStorage() throws SQLException {
        SessionFactory factory = new Configuration().configure().buildSessionFactory();
        session = factory.openSession();
    }

    public Collection<PersistentChange> getPersistedChanges() {
        return getSession().createCriteria(PersistentChange.class).list(); 
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
}
