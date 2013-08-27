package com.ning.billing.osgi.bundles.analytics;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ning.billing.ObjectType;
import com.ning.billing.account.api.Account;
import com.ning.billing.account.api.AccountUserApi;
import com.ning.billing.notification.plugin.api.ExtBusEvent;
import com.ning.billing.util.api.AuditLevel;
import com.ning.billing.util.api.AuditUserApi;
import com.ning.billing.util.api.CustomFieldUserApi;
import com.ning.billing.util.api.RecordIdApi;
import com.ning.billing.util.api.TagUserApi;
import com.ning.billing.util.audit.AuditLog;
import com.ning.billing.util.callcontext.TenantContext;
import com.ning.billing.util.customfield.CustomField;
import com.ning.billing.util.tag.Tag;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillAPI;
import com.ning.killbill.osgi.libs.killbill.OSGIKillbillLogService;

import com.google.common.collect.ImmutableList;
import com.jayway.awaitility.Awaitility;

public class TestAnalyticsNotificationQueue extends AnalyticsTestSuiteWithEmbeddedDB {

    private final Properties properties = new Properties();

    @BeforeMethod(groups = "slow")
    public void prepareMocks() throws Exception {
        logService = Mockito.mock(OSGIKillbillLogService.class);
        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(final InvocationOnMock invocation) throws Throwable {
                //logger.info(Arrays.toString(invocation.getArguments()));
                return null;
            }
        }).when(logService).log(Mockito.anyInt(), Mockito.anyString());

        account = Mockito.mock(Account.class);
        Mockito.when(account.getId()).thenReturn(UUID.randomUUID());

        final AccountUserApi accountUserApi = Mockito.mock(AccountUserApi.class);
        Mockito.when(accountUserApi.getAccountById(Mockito.<UUID>any(), Mockito.<TenantContext>any())).thenReturn(account);

        final RecordIdApi recordIdApi = Mockito.mock(RecordIdApi.class);
        Mockito.when(recordIdApi.getRecordId(Mockito.<UUID>any(), Mockito.<ObjectType>any(), Mockito.<TenantContext>any())).thenReturn(1L);

        final TagUserApi tagUserApi = Mockito.mock(TagUserApi.class);
        Mockito.when(tagUserApi.getTagsForObject(Mockito.<UUID>any(), Mockito.<ObjectType>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<Tag>of());

        customField = Mockito.mock(CustomField.class);
        Mockito.when(customField.getId()).thenReturn(UUID.randomUUID());
        Mockito.when(customField.getObjectId()).thenReturn(UUID.randomUUID());
        Mockito.when(customField.getObjectType()).thenReturn(ObjectType.ACCOUNT);
        Mockito.when(customField.getFieldName()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(customField.getFieldValue()).thenReturn(UUID.randomUUID().toString());
        Mockito.when(customField.getCreatedDate()).thenReturn(new DateTime(2016, 1, 22, 10, 56, 57, DateTimeZone.UTC));

        final CustomFieldUserApi customFieldUserApi = Mockito.mock(CustomFieldUserApi.class);
        Mockito.when(customFieldUserApi.getCustomFieldsForAccount(Mockito.<UUID>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<CustomField>of(customField));

        final AuditUserApi auditUserApi = Mockito.mock(AuditUserApi.class);
        Mockito.when(auditUserApi.getAuditLogs(Mockito.<UUID>any(), Mockito.<ObjectType>any(), Mockito.<AuditLevel>any(), Mockito.<TenantContext>any())).thenReturn(ImmutableList.<AuditLog>of());

        killbillAPI = Mockito.mock(OSGIKillbillAPI.class);
        Mockito.when(killbillAPI.getAccountUserApi()).thenReturn(accountUserApi);
        Mockito.when(killbillAPI.getRecordIdApi()).thenReturn(recordIdApi);
        Mockito.when(killbillAPI.getTagUserApi()).thenReturn(tagUserApi);
        Mockito.when(killbillAPI.getCustomFieldUserApi()).thenReturn(customFieldUserApi);
        Mockito.when(killbillAPI.getAuditUserApi()).thenReturn(auditUserApi);

        properties.setProperty("killbill.billing.notificationq.tableName", "analytics_notifications");
        properties.setProperty("killbill.billing.notificationq.historyTableName", "analytics_notifications_history");
    }

    @Test(groups = "slow")
    public void testSendOneEvent() throws Exception {
        final AnalyticsListener analyticsListener = new AnalyticsListener(logService, killbillAPI, killbillDataSource, BusinessExecutor.newCachedThreadPool(), properties);
        analyticsListener.start();

        // Verify the original state
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(1L, 1L, callContext).size(), 0);

        analyticsListener.handleKillbillEvent(Mockito.mock(ExtBusEvent.class));
        // Shouldn't be anything right after it
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(1L, 1L, callContext).size(), 0);

        // Wait for the notification to kick in
        Awaitility.await().atMost(10, TimeUnit.SECONDS).until(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return analyticsSqlDao.getAccountFieldsByAccountRecordId(1L, 1L, callContext).size() == 1;
            }
        });

        analyticsListener.shutdownNow();
    }

    @Test(groups = "slow")
    public void testVerifyNoDups() throws Exception {
        final AnalyticsListener analyticsListener = new AnalyticsListener(logService, killbillAPI, killbillDataSource, BusinessExecutor.newCachedThreadPool(), properties);
        // Don't start the dequeuer
        Assert.assertEquals(analyticsListener.getJobQueue().getFutureNotificationForSearchKey1(AnalyticsJob.class, 1L).size(), 0);

        // Verify the original state
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(1L, 1L, callContext).size(), 0);

        // Send the first event
        final ExtBusEvent firstEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(firstEvent.getObjectType()).thenReturn(ObjectType.ACCOUNT);
        analyticsListener.handleKillbillEvent(firstEvent);

        // Verify the size of the queue
        Assert.assertEquals(analyticsListener.getJobQueue().getFutureNotificationForSearchKey1(AnalyticsJob.class, 1L).size(), 1);

        // Send the same event
        analyticsListener.handleKillbillEvent(firstEvent);

        // Verify the size of the queue
        Assert.assertEquals(analyticsListener.getJobQueue().getFutureNotificationForSearchKey1(AnalyticsJob.class, 1L).size(), 1);

        // Now, send a different event type
        final ExtBusEvent secondEvent = Mockito.mock(ExtBusEvent.class);
        Mockito.when(secondEvent.getObjectType()).thenReturn(ObjectType.TENANT);
        analyticsListener.handleKillbillEvent(secondEvent);

        // Verify the size of the queue
        Assert.assertEquals(analyticsListener.getJobQueue().getFutureNotificationForSearchKey1(AnalyticsJob.class, 1L).size(), 2);

        // Verify the final state
        Assert.assertEquals(analyticsSqlDao.getAccountFieldsByAccountRecordId(1L, 1L, callContext).size(), 0);
    }
}
