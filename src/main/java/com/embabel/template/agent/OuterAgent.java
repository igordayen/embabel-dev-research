package com.embabel.template.agent;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.RunSubagent;
import com.embabel.agent.domain.io.UserInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent(description = "Outer agent that delegates to an injected subagent")
public class OuterAgent {

    private static final Logger logger = LoggerFactory.getLogger(OuterAgent.class);

    private final InnerSubAgent innerSubAgent;

    public OuterAgent(InnerSubAgent innerSubAgent) {
        this.innerSubAgent = innerSubAgent;
    }

    @Action
    public TaskOutput start(UserInput input) {
        logger.info("OuterAgent.start() - delegating to subagent with input: {}", input.getContent());
        return RunSubagent.fromAnnotatedInstance(
            innerSubAgent,
            TaskOutput.class
        );
    }

    @Action
    @AchievesGoal(description = "Processing complete")
    public TaskOutput done(TaskOutput output) {
        logger.info("OuterAgent.done() - subagent completed with: {}", output.result());
        return output;
    }
}
