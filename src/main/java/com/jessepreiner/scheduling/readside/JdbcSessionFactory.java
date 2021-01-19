package com.jessepreiner.scheduling.readside;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class JdbcSessionFactory {
    private final EntityManagerFactory entityManagerFactory;

    public JdbcSessionFactory() {
        this.entityManagerFactory = Persistence.createEntityManagerFactory("akka-projection-hibernate");
    }

    public static PlainJdbcSession newInstance() {
        return new PlainJdbcSession();
    }
}
