/**
 * 
 */
package jp.terasoluna.fw.collector.db;

import jp.terasoluna.fw.collector.Collector;
import jp.terasoluna.fw.collector.CollectorTestUtil;
import jp.terasoluna.fw.collector.dao.UserListQueryResultHandleDao;
import jp.terasoluna.fw.collector.util.MemoryInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.springframework.test.context.ContextConfiguration;
import jp.terasoluna.fw.collector.unit.testcase.junit4.DaoTestCaseJunit4;
import jp.terasoluna.fw.collector.unit.testcase.junit4.loader.DaoTestCaseContextLoader;

/**
 * DaoCollectorTest
 */
@ContextConfiguration(locations = {
        "classpath:jp/terasoluna/fw/collector/db/dataSource.xml" }, loader = DaoTestCaseContextLoader.class)
public class DaoCollector005Test extends DaoTestCaseJunit4 {

    /**
     * Log.
     */
    private static Log logger = LogFactory.getLog(DaoCollector005Test.class);

    private UserListQueryResultHandleDao userListQueryResultHandleDao = null;

    private int previousThreadCount = 0;

    public void setUserListQueryResultHandleDao(
            UserListQueryResultHandleDao userListQueryResultHandleDao) {
        this.userListQueryResultHandleDao = userListQueryResultHandleDao;
    }

    @Before
    public void onSetUp() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(MemoryInfo.getMemoryInfo());
        }
        System.gc();
        if (logger.isInfoEnabled()) {
            logger.info(MemoryInfo.getMemoryInfo());
        }
        this.previousThreadCount = CollectorTestUtil.getCollectorThreadCount();
    }

    @After
    public void onTearDown() throws Exception {
        if (logger.isInfoEnabled()) {
            logger.info(MemoryInfo.getMemoryInfo());
        }
        System.gc();
        if (logger.isInfoEnabled()) {
            logger.info(MemoryInfo.getMemoryInfo());
        }
        CollectorTestUtil.allInterrupt();
    }

    /**
     * {@link jp.terasoluna.fw.collector.db.DaoCollector#DaoCollector(Object, String, Object, int)} のためのテスト・メソッド。
     */
    @Test
    public void testDaoCollectorObjectStringObjectInt003() throws Exception {
        if (this.userListQueryResultHandleDao == null) {
            fail("userListQueryResultHandleDaoがnullです。");
        }

        int count_first = 0;

        Collector<UserBean> it = new DaoCollector<UserBean>(this.userListQueryResultHandleDao, "collect", null, 1);
        try {
            while (it.hasNext()) {
                it.next();
                count_first++;
                if (count_first > 10) {
                    break;
                }
            }
        } finally {
            DaoCollector.closeQuietly(it);
        }

        // コレクタスレッド数チェック
        assertTrue(CollectorTestUtil.lessThanCollectorThreadCount(1
                + this.previousThreadCount));

        for (int i = 0; i < 10; i++) {
            int count = 0;

            long startTime = System.currentTimeMillis();

            Collector<UserBean> it2 = new DaoCollector<UserBean>(this.userListQueryResultHandleDao, "collect", null, 1);
            try {
                for (UserBean user : it2) {
                    if (logger.isInfoEnabled() && user == null) {
                        logger.info("UserBean is null.##############");
                    }

                    count++;

                    if (i % 2 == 0 && count > 10) {
                        break;
                    }
                }
            } finally {
                DaoCollector.closeQuietly(it2);
            }

            // コレクタスレッド数チェック
            assertTrue(CollectorTestUtil.lessThanCollectorThreadCount(1
                    + this.previousThreadCount));

            long endTime = System.currentTimeMillis();

            if (logger.isInfoEnabled()) {
                StringBuilder sb = new StringBuilder();
                sb.append("i:[");
                sb.append(String.format("%04d", i));
                sb.append("]");
                sb.append(" milliSec:[");
                sb.append(String.format("%04d", (endTime - startTime)));
                sb.append("]");
                logger.info(sb.toString());
            }

            if (i % 2 == 0) {
                assertEquals(count_first, count);
            }
        }
    }

}
