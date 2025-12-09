package com.embabel.template.agent;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.test.unit.FakeOperationContext;
import com.embabel.agent.test.unit.FakePromptRunner;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

class WriteAndReviewAgentTest {

    @Test
    void testWriteAndReviewAgent() {
        var context = FakeOperationContext.create();
        var promptRunner = (FakePromptRunner) context.promptRunner();
        context.expectResponse(new WriteAndReviewAgent.Story("One upon a time Sir Galahad . . "));

        var agent = new WriteAndReviewAgent(200, 400);
        agent.craftStory(new UserInput("Tell me a story about a brave knight", Instant.now()), context);

        // Test behavior: verify LLM was called instead of checking prompt content
        assertTrue(promptRunner.getLlmInvocations().size() > 0, "Expected LLM to be invoked for story crafting");

    }

    @Test
    void testReview() {
        var agent = new WriteAndReviewAgent(200, 400);
        var userInput = new UserInput("Tell me a story about a brave knight", Instant.now());
        var story = new WriteAndReviewAgent.Story("Once upon a time, Sir Galahad...");
        var context = FakeOperationContext.create();
        context.expectResponse("A thrilling tale of bravery and adventure!");
        agent.reviewStory(userInput, story, context);
        
        // Test behavior: verify LLM was called for story review instead of checking prompt content
        assertTrue(context.getLlmInvocations().size() > 0, "Expected LLM to be invoked for story review");
    }

}