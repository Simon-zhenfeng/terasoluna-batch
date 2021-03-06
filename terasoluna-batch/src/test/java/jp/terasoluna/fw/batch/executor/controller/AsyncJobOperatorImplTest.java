/*
 * Copyright (c) 2016 NTT DATA Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jp.terasoluna.fw.batch.executor.controller;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import jp.terasoluna.fw.batch.exception.BatchException;
import jp.terasoluna.fw.batch.executor.repository.JobControlFinder;
import jp.terasoluna.fw.batch.executor.vo.BatchJobListResult;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

/**
 * {@code AsyncJobOperatorImpl}のテストケース。<br>
 *
 * @since 3.6
 */
public class AsyncJobOperatorImplTest {

    protected JobControlFinder jobControlFinder;

    protected AsyncJobLauncher asyncJobLauncher;

    protected AsyncBatchStopper asyncBatchStopper;

    private final TestLogger logger =
            TestLoggerFactory.getTestLogger(AsyncJobOperatorImpl.class);

    /**
     * テスト前処理。<br>
     */
    @Before
    public void setUp() {
        this.jobControlFinder = mock(JobControlFinder.class);
        this.asyncJobLauncher = mock(AsyncJobLauncher.class);
        this.asyncBatchStopper = mock(AsyncBatchStopper.class);
        this.logger.clear();
    }

    /**
     * コンストラクタのテスト 【異常系】
     * <pre>
     * 事前条件
     * ・とくになし
     * 確認項目
     * ・{@code JobControlFinder}がnullであるとき、アサーションエラーとして
     * 　{@code IllegalArgumentException}がスローされること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testAsyncJobOperatorImpl01() throws Exception {
        try {
            // テスト実行
            new AsyncJobOperatorImpl(null, asyncJobLauncher, asyncBatchStopper);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "[EAL025056] [Assertion failed] - AsyncJobOperatorImpl requires to set JobControlFinder. please confirm the settings.",
                    e.getMessage());
        }
    }

    /**
     * コンストラクタのテスト 【異常系】
     * <pre>
     * 事前条件
     * ・とくになし
     * 確認項目
     * ・{@code AsyncJobLauncher}がnullであるとき、アサーションエラーとして
     * {@code IllegalArgumentException}がスローされること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testAsyncJobOperatorImpl02() throws Exception {
        try {
            // テスト実行
            new AsyncJobOperatorImpl(jobControlFinder, null,
                    asyncBatchStopper);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "[EAL025056] [Assertion failed] - AsyncJobOperatorImpl requires to set AsyncJobLauncher. please confirm the settings.",
                    e.getMessage());
        }
    }

    /**
     * コンストラクタのテスト 【異常系】
     * <pre>
     * 事前条件
     * ・とくになし
     * 確認項目
     * ・{@code AsyncBatchStopper}がnullであるとき、アサーションエラーとして
     * {@code IllegalArgumentException}がスローされること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testAsyncJobOperatorImpl03() throws Exception {
        try {
            // テスト実行
            new AsyncJobOperatorImpl(jobControlFinder, asyncJobLauncher,
                    null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(
                    "[EAL025056] [Assertion failed] - AsyncJobOperatorImpl requires to set AsyncBatchStopper. please confirm the settings.",
                    e.getMessage());
        }
    }

    /**
     * コンストラクタのテスト 【正常系】
     * <pre>
     * 事前条件
     * ・とくになし
     * 確認項目
     * ・コンストラクタ引数で渡された引数がフィールドに退避されていること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testAsyncJobOperatorImpl04() throws Exception {
        // テスト実行
        AsyncJobOperatorImpl asyncJobOperator = new AsyncJobOperatorImpl(
                jobControlFinder, asyncJobLauncher, asyncBatchStopper);

        assertSame(jobControlFinder,
                asyncJobOperator.jobControlFinder);
        assertSame(asyncJobLauncher, asyncJobOperator.asyncJobLauncher);
        assertSame(asyncBatchStopper, asyncJobOperator.asyncBatchStopper);
    }

    /**
     * {@code start}のテスト 【正常系】
     * <pre>
     * 事前条件
     * ・コンストラクタのアサーションを全て成功させていること。
     * 確認項目
     * ・{@code AsyncBatchStopper#canStop()}が{@code false}を返す時、ループ内の処理が
     * 行われず終了すること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testStart01() throws Exception {
        doReturn(true).when(asyncBatchStopper).canStop();
        String[] args = new String[] {};
        doReturn(new BatchJobListResult()).when(jobControlFinder)
                .resolveBatchJobResult(args);
        AsyncJobOperatorImpl asyncJobOperator = new AsyncJobOperatorImpl(
                jobControlFinder, asyncJobLauncher, asyncBatchStopper);

        // テスト実行
        assertEquals(0, asyncJobOperator.start(new String[] {}));

        // JobControlFinder#resolveBatchJobResult()が１度も呼ばれていないこと。
        verify(jobControlFinder, never()).resolveBatchJobResult(args);

        // シャットダウン処理が呼び出されていること。
        verify(asyncJobLauncher).shutdown();
    }

    /**
     * {@code start}のテスト 【正常系】
     * <pre>
     * 事前条件
     * ・コンストラクタのアサーションを全て成功させていること。
     * 確認項目
     * ・{@code JobControlFinder#resolveBatchJobResult()}が{@code null}
     * を返す時、ポーリングスリープが行われてジョブが実行されないこと。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testStart02() throws Exception {
        doReturn(false).doReturn(true).when(asyncBatchStopper).canStop();
        String[] args = new String[] {};
        doReturn(null).when(jobControlFinder).resolveBatchJobResult(args);
        doNothing().when(asyncJobLauncher).executeJob(anyString());
        AsyncJobOperatorImpl asyncJobOperator = new AsyncJobOperatorImpl(
                jobControlFinder, asyncJobLauncher, asyncBatchStopper);

        // テスト実行
        assertEquals(0, asyncJobOperator.start(new String[] {}));

        // AsyncJobLauncherが一度も実行されないこと。
        verify(asyncJobLauncher, never()).executeJob(anyString());

        // シャットダウン処理が呼び出されていること。
        verify(asyncJobLauncher).shutdown();
    }

    /**
     * {@code start}のテスト 【正常系】
     * <pre>
     * 事前条件
     * ・コンストラクタのアサーションを全て成功させていること。
     * 確認項目
     * ・実行対象のジョブが1回取得できた場合、1度だけジョブが実行されること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testStart03() throws Exception {
        doReturn(false).doReturn(true).when(asyncBatchStopper).canStop();
        String[] args = new String[] {};
        BatchJobListResult result = new BatchJobListResult() {{
            setJobSequenceId("jobSequenceId");
        }};
        doReturn(result).when(jobControlFinder)
                .resolveBatchJobResult(args);
        doNothing().when(asyncJobLauncher).executeJob(anyString());
        AsyncJobOperatorImpl asyncJobOperator = new AsyncJobOperatorImpl(
                jobControlFinder, asyncJobLauncher, asyncBatchStopper);

        // テスト実行
        assertEquals(0, asyncJobOperator.start(new String[] {}));

        // AsyncJobLauncher#executeJob()が1回だけ呼び出されていること
        verify(asyncJobLauncher, times(1)).executeJob("jobSequenceId");

        // シャットダウン処理が呼び出されていること。
        verify(asyncJobLauncher).shutdown();
    }

    /**
     * {@code start}のテスト 【正常系】
     * <pre>
     * 事前条件
     * ・コンストラクタのアサーションを全て成功させていること。
     * 確認項目
     * ・実行対象のジョブが3回取得できた場合、3回ジョブが実行されること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testStart04() throws Exception {
        doReturn(false).doReturn(false).doReturn(false).doReturn(true)
                .when(asyncBatchStopper).canStop();
        String[] args = new String[] {};
        BatchJobListResult result = new BatchJobListResult() {{
            setJobSequenceId("jobSequenceId");
        }};
        doReturn(result).when(jobControlFinder)
                .resolveBatchJobResult(args);
        doNothing().when(asyncJobLauncher).executeJob(anyString());
        AsyncJobOperatorImpl asyncJobOperator = new AsyncJobOperatorImpl(
                jobControlFinder, asyncJobLauncher, asyncBatchStopper);

        // テスト実行
        assertEquals(0, asyncJobOperator.start(new String[] {}));

        // AsyncJobLauncher#executeJob()が3回呼び出されていること
        verify(asyncJobLauncher, times(3)).executeJob("jobSequenceId");

        // シャットダウン処理が呼び出されていること。
        verify(asyncJobLauncher).shutdown();
    }

    /**
     * {@code start}のテスト 【正常系】
     * <pre>
     * 事前条件
     * ・コンストラクタのアサーションを全て成功させていること。
     * 確認項目
     * ・ジョブの実行中に例外が発生した場合、例外がそのままスローされること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testStart05() throws Exception {
        doReturn(false).when(asyncBatchStopper).canStop();
        String[] args = new String[] {};
        BatchJobListResult result = new BatchJobListResult() {{
            setJobSequenceId("jobSequenceId");
        }};
        doReturn(result).when(jobControlFinder)
                .resolveBatchJobResult(args);
        doNothing().when(asyncJobLauncher).executeJob(anyString());
        IllegalStateException e = new IllegalStateException(
                "job execution failed.");
        doThrow(e).when(asyncJobLauncher).executeJob("jobSequenceId");
        AsyncJobOperatorImpl asyncJobOperator = new AsyncJobOperatorImpl(
                jobControlFinder, asyncJobLauncher, asyncBatchStopper);

        // テスト実行
        try {
            asyncJobOperator.start(new String[] {});
            fail();
        } catch (IllegalStateException ise) {
            assertThat(ise, is(e));
        }

        // シャットダウン処理が呼び出されていること。
        verify(asyncJobLauncher).shutdown();
    }

    /**
     * {@code testPollingSleep}のテスト 【正常系】
     * <pre>
     * 事前条件
     * ・とくになし
     * 確認項目
     * ・ポーリングループ時のスリープで例外が発生しないこと。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testPollingSleep01() throws Exception {
        Thread.interrupted(); // あらかじめ割り込み状態をクリアしておく。
        AsyncJobOperatorImpl asyncJobOperator = new AsyncJobOperatorImpl(
                jobControlFinder, asyncJobLauncher, asyncBatchStopper);
        asyncJobOperator.jobIntervalTime = 1L;

        // テスト実行
        asyncJobOperator.pollingSleep();
    }
    
    /**
     * {@code testPollingSleep}のテスト 【正常系】
     * <pre>
     * 事前条件
     * ・とくになし
     * 確認項目
     * ・ポーリングループ時のスリープで割り込みが発生した場合、InterruptedExceptionをラップした
     * 　BatchExceptionが発生すること。
     * </pre>
     *
     * @throws Exception 予期しない例外
     */
    @Test
    public void testPollingSleep02() throws Exception {
        AsyncJobOperatorImpl asyncJobOperator = new AsyncJobOperatorImpl(
                jobControlFinder, asyncJobLauncher, asyncBatchStopper);
        asyncJobOperator.jobIntervalTime = 1L;

        // スリープの呼び出し前から割り込み状態にする。
        Thread.currentThread().interrupt();

        try {
            // テスト実行
            asyncJobOperator.pollingSleep();
            fail();
        } catch (BatchException e) {
            assertTrue(e.getCause() instanceof InterruptedException);
        }
    }
    
}
