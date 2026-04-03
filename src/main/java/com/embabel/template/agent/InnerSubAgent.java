package com.embabel.template.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent(description = "Inner subagent that processes input")
public class InnerSubAgent {

    private static final Logger logger = LoggerFactory.getLogger(InnerSubAgent.class);

    @Action
    public Intermediate stepOne(UserInput input) {
        logger.info("InnerSubAgent.stepOne() - processing input: {}", input.getContent());
        return new Intermediate(input.getContent());
    }

    @Action
    @AchievesGoal(description = "Subagent complete")
    public TaskOutput stepTwo(Intermediate data) {
        logger.info("InnerSubAgent.stepTwo() - transforming: {}", data.value());
        return new TaskOutput(data.value().toUpperCase());
    }
}
