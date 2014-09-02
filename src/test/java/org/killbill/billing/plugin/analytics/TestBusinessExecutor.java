/*
 * Copyright 2010-2014 Ning, Inc.
 * Copyright 2014 The Billing Project, LLC
 *
 * Ning licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.killbill.billing.plugin.analytics;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestBusinessExecutor extends AnalyticsTestSuiteNoDB {

    @Test(groups = "fast")
    public void testRejectionPolicy() throws Exception {
        final Executor executor = BusinessExecutor.newCachedThreadPool(osgiConfigPropertiesService);
        final CompletionService<Integer> completionService = new ExecutorCompletionService<Integer>(executor);

        final int totalTasksSize = BusinessExecutor.getNbThreads(osgiConfigPropertiesService) * 50;
        final AtomicInteger taskCounter = new AtomicInteger(totalTasksSize);
        for (int i = 0; i < totalTasksSize; i++) {
            completionService.submit(new Callable<Integer>() {
                @Override
                public Integer call() throws Exception {
                    // Sleep a bit to trigger the rejection
                    Thread.sleep(100);
                    taskCounter.getAndDecrement();
                    return 1;
                }
            });
        }

        int results = 0;
        for (int i = 0; i < totalTasksSize; i++) {
            try {
                // We want to make sure the policy didn't affect the completion queue of the ExecutorCompletionService
                results += completionService.take().get();
            } catch (InterruptedException e) {
                Assert.fail();
            } catch (ExecutionException e) {
                Assert.fail();
            }
        }
        Assert.assertEquals(taskCounter.get(), 0);
        Assert.assertEquals(results, totalTasksSize);
    }
}
