/*
 * Copyright © 2017-2021 Ocado (Ocava)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ocadotechnology.event.scheduling;

import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.ocadotechnology.notification.Notification;
import com.ocadotechnology.notification.NotificationRouter;
import com.ocadotechnology.notification.TestBus;
import com.ocadotechnology.notification.TestSchedulerType;

class ExecutorEventSchedulerTest {
    private ExecutorEventScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new ExecutorEventScheduler(TimeUnit.MILLISECONDS, "ExecutorEventSchedulerTest", true, TestSchedulerType.TEST_SCHEDULER_TYPE);
    }

    @AfterEach
    void tearDown() {
        NotificationRouter.get().clearAllHandlers();
        if (scheduler != null) {
            scheduler.stop();
        }
    }

    @Test
    void testGetQueueSizeIncludesQueuedButNotExecutingEvents() throws InterruptedException {
        Assertions.assertEquals(0, scheduler.getQueueSize());

        Exchanger<Void> rendezvous = new Exchanger<>();
        scheduler.doIn(500, () -> rendezvousRunnable(rendezvous));
        scheduler.doIn(500, () -> rendezvousRunnable(rendezvous));

        // An executing job is not on the queue:
        rendezvous.exchange(null);  // wait for first event to start
        Assertions.assertEquals(1, scheduler.getQueueSize());
        rendezvous.exchange(null);  // permit first event to complete

        rendezvous.exchange(null);  // wait for second event to start
        Assertions.assertEquals(0, scheduler.getQueueSize());
        rendezvous.exchange(null);  // permit second event to complete
    }

    private static void rendezvousRunnable(Exchanger<Void> rendezvous) {
        try {
            rendezvous.exchange(null);
            // wait for main thread to read queue size
            rendezvous.exchange(null);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testStopSchedulerThenCallDoNowQuietlyIgnoresTheDoNow() {
        AtomicBoolean failedWasCalled = new AtomicBoolean(false);
        scheduler.registerFailureListener(t -> failedWasCalled.set(true));

        NotificationRouter.get().registerExecutionLayer(scheduler, new TestBus(Notification.class));
        scheduler.stop();

        AtomicBoolean taskWasRun = new AtomicBoolean(false);
        scheduler.doNow(() -> taskWasRun.set(true));

        while (!scheduler.isStopped()) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Assertions.assertFalse(failedWasCalled.get());
        Assertions.assertFalse(taskWasRun.get());
    }
}
