package com.embabel.template.agent;

import com.embabel.agent.api.annotation.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.domain.io.UserInput;

@Agent(description = "Help the user to generate a Itinerary or find a flight")
@Profile("!test")
public class OpenAIConditionalTravelAgent {

    static final Logger logger = LoggerFactory.getLogger(OpenAIConditionalTravelAgent.class);

    private interface Request {
    }

    private record ItineraryRequest(UserInput userInput) implements Request {
    }

    private record FlightRequest(UserInput userInput) implements Request {
    }

    private interface Recommandable {
        String recommendation();
    }

    private record Itinerary(String text) implements Recommandable {
        @Override
        public String recommendation() {
            return text;
        }
    }

    private record Flight(String flightNumber, String departure, String arrival) implements Recommandable {
        @Override
        public String recommendation() {
            return String.format("Flight Number: %s\nDeparture: %s\nArrival: %s",
                    flightNumber, departure, arrival);
        }
    }

    public record Recomendation(String text) {
    }

    public enum Option {
        ITINERARY,
        FLIGHT
    }

    @Action(description = "Classify the user input", post = {"travelItineraryCondition", "travelFlightCondition"})
    Option classify(UserInput userInput, OperationContext context) {

        logger.debug("In Classification Action");

        Option option = context.ai()
                .withAutoLlm()
                .createObject(String.format("""
                                You are a helpful travel assistant for classifying whether a message is about an itinerary or a flight
                                
                                # User input
                                %s
                                """,
                        userInput.getContent()
                ).trim(), Option.class);


        // Explicitly set conditions based on result
        if (option == Option.FLIGHT) {
            context.setCondition("travelFlightCondition", true);
            context.setCondition("travelItineraryCondition", false);
            logger.debug("Set travelFlightCondition=TRUE, travelItineraryCondition=FALSE");
        } else if (option == Option.ITINERARY) {
            context.setCondition("travelFlightCondition", false);
            context.setCondition("travelItineraryCondition", true);
            logger.debug("Set travelFlightCondition=FALSE, travelItineraryCondition=TRUE");
        }

        return option;
    }

    @Action(pre = {"travelItineraryCondition"})
    ItineraryRequest decideOnItinerary(UserInput userInput, OperationContext context, Option option) {

        logger.debug("In decideOnItinerary Action");
        return new ItineraryRequest(userInput);

    }

    @Action(pre = {"travelFlightCondition"})
    FlightRequest decideOnFlight(UserInput userInput, OperationContext context, Option option) {

        logger.debug("In decideOnFlight Action");
        return new FlightRequest(userInput);

    }

    @Action(description = "Build a travel itinerary based on user input", pre = {"travelItineraryCondition"})
    Itinerary itineraryAction(ItineraryRequest request, OperationContext context, Option option) {
        return context.ai()
                .withAutoLlm()
                .createObject(String.format("""
                                You are a travel assistant, so build a concise itinerary based on the user input
                                
                                # User input
                                %s
                                """,
                        request.userInput().getContent()
                ).trim(), Itinerary.class);
    }

    @Action(description = "Find a flight based on user input", pre = {"travelFlightCondition"})
    Flight flightAction(FlightRequest request, OperationContext context, Option option) {
        return context.ai()
                .withAutoLlm()
                .createObject(String.format("""
                                You are a travel assistant. Always recommend a specific flight to go to. Use airport codes.
                                
                                # User input
                                %s
                                """,
                        request.userInput().getContent()
                ).trim(), Flight.class);
    }


    @AchievesGoal(
            description = "The user has received a travel itinerary"
    )
    @Action
    Recomendation getItineraryRecommendation (Itinerary recommendable) {
        return new Recomendation(recommendable.recommendation());
    }

    @AchievesGoal(
            description = "The user has received a flight recommendation"
    )
    @Action
    Recomendation getFlightRecommendation (Flight recommendable) {
        return new Recomendation(recommendable.recommendation());
    }


    @Condition
    boolean travelItineraryCondition(Option option) {

        logger.debug("In Itinerary Condition");
        return option == Option.ITINERARY;
    }//

    @Condition
    boolean travelFlightCondition(Option option) {

        logger.debug("In Flight Condition");
        return option == Option.FLIGHT;
    }//


}
