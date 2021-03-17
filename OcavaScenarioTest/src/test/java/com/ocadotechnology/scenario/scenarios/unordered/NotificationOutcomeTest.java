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
package com.ocadotechnology.scenario.scenarios.unordered;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;
import com.ocadotechnology.scenario.AbstractFrameworkTestStory;
import com.ocadotechnology.scenario.StepFuture;
import com.ocadotechnology.scenario.Story;

/**
 * Test the case when 2 unordered steps are sent
 * the same instance of TestEventNotification.
 * If the first step fails, then second step should receive this notification.
 * If the first step succeeds, then second step should not be executed.
 */
@Story
class NotificationOutcomeTest extends AbstractFrameworkTestStory {

    private static final String FINISHING_STEP = "Finishing Step";
    private static final String NONFINISHING_STEP = "Non-finishing Step";

    private static final String SENT_EVENT = "Sent Event";
    private static final String UNSENT_EVENT = "Unsent Event";

    @Test
    public void firstStepFailsThenSecondStepIsExecuted() {
        when.simStarts();
        when.testEvent.scheduled(1, SENT_EVENT);

        then.testEvent.unordered(NONFINISHING_STEP).occurs(UNSENT_EVENT);
        then.testEvent.unordered(FINISHING_STEP).occurs(SENT_EVENT);

        StepFuture<List<String>> finishedSteps = then.unordered.waitForAnyOfSteps(FINISHING_STEP, NONFINISHING_STEP);

        then.futures.assertEquals(ImmutableList.of(FINISHING_STEP), finishedSteps);
    }

    @Test
    public void firstStepSucceedsThenSecondStepNotExecuted() {
        when.simStarts();
        when.testEvent.scheduled(1, SENT_EVENT);

        then.testEvent.unordered(FINISHING_STEP).occurs(SENT_EVENT);
        then.testEvent.unordered(NONFINISHING_STEP).occurs(SENT_EVENT);

        StepFuture<List<String>> finishedSteps = then.unordered.waitForAnyOfSteps(FINISHING_STEP, NONFINISHING_STEP);

        then.futures.assertEquals(ImmutableList.of(FINISHING_STEP), finishedSteps);
    }
}
