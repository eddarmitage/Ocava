/*
 * Copyright © 2017-2022 Ocado (Ocava)
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
package com.ocadotechnology.scenario;

import org.junit.jupiter.api.Assertions;

import com.ocadotechnology.scenario.StepManager.CheckStepExecutionType;
import com.ocadotechnology.simulation.Simulation;

public class TestEventThenSteps extends AbstractThenSteps<Simulation, TestEventThenSteps> {

    TestEventThenSteps(StepManager<Simulation> stepManager, NotificationCache notificationCache, CheckStepExecutionType checkStepExecutionType) {
        super(stepManager, notificationCache, checkStepExecutionType);
    }

    @Override
    protected TestEventThenSteps create(StepManager<Simulation> stepManager, NotificationCache notificationCache, CheckStepExecutionType executionType) {
        return new TestEventThenSteps(stepManager, notificationCache, executionType);
    }

    public void occurs(String name) {
        addCheckStep(TestEventNotification.class, n -> n.name.equals(name));
    }

    public void occursStrictlyNext(String name) {
        addCheckStep(TestEventNotification.class, n -> {
            Assertions.assertEquals(name, n.name, "Incorrect event triggered next.");
            return true;
        });
    }

    public void executeStep(boolean shouldFail) {
        addExecuteStep(() -> Assertions.assertFalse(shouldFail, "Test setup requires a failure"));
    }
}
