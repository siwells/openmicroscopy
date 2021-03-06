/*
 *   Copyright 2010 Glencoe Software, Inc. All rights reserved.
 *   Use is subject to license terms supplied in LICENSE.txt
 */

package ome.services.ldap;

import java.io.File;
import java.util.Map;
import java.util.UUID;

import ome.api.local.LocalQuery;
import ome.api.local.LocalUpdate;
import ome.conditions.SecurityViolation;
import ome.logic.LdapImpl;
import ome.model.meta.Experimenter;
import ome.model.meta.Session;
import ome.security.auth.LdapConfig;
import ome.security.auth.LdapPasswordProvider;
import ome.security.auth.PasswordUtil;
import ome.security.auth.RoleProvider;
import ome.services.sessions.SessionManager;
import ome.services.util.Executor;
import ome.services.util.Executor.Work;
import ome.system.EventContext;
import ome.system.OmeroContext;
import ome.system.Principal;
import ome.system.Roles;
import ome.system.ServiceFactory;
import ome.tools.spring.InternalServiceFactory;
import ome.util.SqlAction;

import org.springframework.aop.target.HotSwappableTargetSource;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Extends {@link LdapTest} to use a real db.
 */
@Test(groups = "integration")
public class LdapIntegrationTest extends LdapTest {

    Object old;
    OmeroContext mCtx;
    HotSwappableTargetSource hsts;
    SessionManager sessionManager;
    Executor executor;
    Principal p;

    @BeforeClass
    public void swap() {
        mCtx = OmeroContext.getManagedServerContext();
        hsts = (HotSwappableTargetSource) mCtx.getBean("contextSourceSwapper");
        old = hsts.getTarget();
        sessionManager = (SessionManager) mCtx.getBean("sessionManager");
        executor = (Executor) mCtx.getBean("executor");
    }

    RoleProvider provider() {
        return (RoleProvider) mCtx.getBean("roleProvider");
    }

    @BeforeMethod
    public void login() {
        p = newSession(null, "root", "system", null);
    }

    public Principal newSession(final Fixture fixture, final String username,
            final String group, final String password) {

        if (fixture != null && password != null) {
            // Will cause synchronization.
            Boolean check =
                (Boolean) executor.execute(p, new Executor.SimpleWork(this, "newSession"){
                @Transactional(readOnly = false)
                public Object doWork(org.hibernate.Session session,
                        ServiceFactory sf) {
                    return fixture.provider.checkPassword(username, password, true);
                }});

            if (check != null && Boolean.FALSE.equals(check.booleanValue())) {
                throw new SecurityViolation("false returned.");
            }

        }

        Principal tmp = new Principal(username, group, "Test");
        Session s = sessionManager.createWithAgent(tmp,
                "AbstractManagedContext");
        return new Principal(s.getUuid(), group, "Test");
    }

    @AfterMethod
    public void logout() {
        sessionManager.closeAll();
    }

    @Override
    // Critical that this is overriding.
    protected Fixture createFixture(File ctxFile) throws Exception {

        Fixture fixture = new Fixture() {
            @Override
            public void createUserWithGroup(final LdapTest t, String dn,
                    final String group) {
                executor.execute(p, new Executor.SimpleWork(this,
                        "createUserWithGroup") {
                    @Transactional(readOnly = false)
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        ((LdapIntegrationTest) t).provider().createGroup(group,
                                null, false);
                        return null;
                    }
                });
            }

            @Override
            public boolean createUserFromLdap(final String user,
                    final String password) {

                // To keep things simple, if a user already exists,
                // it gets renamed. Otherwise, it would be necessary to generate
                // the ldif on every execution.
                executor.execute(p,
                        new Executor.SimpleWork(this, "renameUser") {
                            @Transactional(readOnly = false)
                            public Object doWork(org.hibernate.Session session,
                                    ServiceFactory sf) {

                                Experimenter exp = null;
                                try {
                                    exp = sf.getAdminService()
                                            .lookupExperimenter(user);
                                } catch (Exception e) {
                                    // goodl
                                }

                                if (exp != null) {
                                    exp.setOmeName(UUID.randomUUID().toString());
                                    sf.getAdminService()
                                            .updateExperimenter(exp);
                                }

                                return null;
                            }
                        });

                return (Boolean) executor.execute(p, new Executor.SimpleWork(
                        this, "createUserFromLdap") {
                    @Transactional(readOnly = false)
                    public Object doWork(org.hibernate.Session session,
                            ServiceFactory sf) {
                        return ldap.createUserFromLdap(user, "password");
                    }
                });
            }

            @Override
            public EventContext login(String username, String group, String password) {
                Principal user = newSession(this, username, group, password);
                EventContext ec = (EventContext) executor.execute(user,
                        new Executor.SimpleWork(this, "simpleCall") {
                            @Transactional(readOnly = false)
                            public Object doWork(org.hibernate.Session session,
                                    ServiceFactory sf) {
                                return sf.getAdminService().getEventContext();
                            }
                        });
                return ec;
            }

            @Override
            public Object execute(Work work) {
                return executor.execute(p, work);
            }

        };

        fixture.ctx = new FileSystemXmlApplicationContext("file:"
                + ctxFile.getAbsolutePath());
        fixture.config = (LdapConfig) fixture.ctx.getBean("config");

        Map<String, LdapContextSource> sources = fixture.ctx
                .getBeansOfType(LdapContextSource.class);

        LdapContextSource source = sources.values().iterator().next();
        String[] urls = source.getUrls();
        assertEquals(1, urls.length);

        hsts.swap(source);

        fixture.template = (LdapTemplate) mCtx.getBean("ldapTemplate");
        fixture.template.setContextSource(source);

        InternalServiceFactory isf = new InternalServiceFactory(mCtx);
        SqlAction sql = (SqlAction) mCtx.getBean("simpleSqlAction");
        fixture.ldap = new LdapImpl(source, fixture.template, new Roles(),
                fixture.config, provider(), sql);
        fixture.ldap.setQueryService((LocalQuery) isf.getQueryService());
        fixture.ldap.setUpdateService((LocalUpdate) isf.getUpdateService());

        fixture.provider = new LdapPasswordProvider(
                new PasswordUtil(sql), fixture.ldap);
        fixture.provider.setApplicationContext(mCtx);
        return fixture;
    }

    @AfterClass
    public void unswap() {
        if (old != null && mCtx != null) {
            hsts.swap(old);
        }
    }

}
