package com.embabel.template.agent;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Profile;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.common.SomeOf;
import com.embabel.agent.domain.io.UserInput;

//@Agent(description = "Help the user to generate a Itinerary or find a flight")
@Profile("!test")
public class OpenAITravelAgent {

    private interface Request {
    }

    private record ItineraryRequest(UserInput userInput) implements Request {
    } 

    private record FlightRequest(UserInput userInput) implements Request {
    }

    private record ItineraryOrFlightRequest(ItineraryRequest itineraryRequest, FlightRequest flightRequest) implements SomeOf {}

    private interface Recommendable {
        String recommendation();
    }

    private record Itinerary(String text) implements Recommendable 
    {
        @Override
        public String recommendation() {
            return text;
        }
    }

    private record Flight(String flightNumber, String departure, String arrival, LocalDateTime date) implements Recommendable {
        @Override
        public String recommendation() {
            return String.format("Flight Number: %s\nDeparture: %s\nArrival: %s\nDate: %s",
                flightNumber, departure, arrival, date);
        }
    }

    private record FlightOrItinerary(Flight flight, Itinerary itinerary) implements SomeOf {}

    public record Recomendation(String text) {
    }

    public enum Option {
        ITINERARY,
        FLIGHT
    }

    //@Action(description = "Classify the user input")
    Option classify(UserInput userInput, OperationContext context) {
        Option option = context.ai()
            .withAutoLlm()
            .createObject(String.format("""
                You are a helpful travel assistant for classifying whether a message is about an itinerary or a flight

                # User input
                %s
                """,
                userInput.getContent()
            ).trim(), Option.class);

        return option;
    }

    //@Action
    ItineraryOrFlightRequest decide(UserInput userInput, OperationContext context, Option option) {
        if (option == Option.ITINERARY) {
            return new ItineraryOrFlightRequest(new ItineraryRequest(userInput), null);
        } else {
            return new ItineraryOrFlightRequest(null, new FlightRequest(userInput));
        }
    }
    

    //@Action(description = "Build a travel itinerary based on user input")
    Itinerary itineraryAction(ItineraryRequest request, OperationContext context) {
        Itinerary itinerary = context.ai()
            .withAutoLlm()
            .createObject(String.format("""
                You are a travel assistant, so build a concise itinerary based on the user input

                Today is: %s
                User input: %s
                """,
                LocalDateTime.now(),
                request.userInput().getContent()
            ).trim(), Itinerary.class);
        return itinerary;
    }

    //@Action(description = "Find a flight based on user input")
    Flight flightAction(FlightRequest request, OperationContext context) {
        Flight flight = context.ai()
            .withAutoLlm()
            .createObject(String.format("""
                You are a travel assistant. Always recommend a specific flight to go to. Use airport codes.

                Today is: %s
                User input: %s
                """,
                LocalDateTime.now(),
                request.userInput().getContent()
            ).trim(), Flight.class);
        return flight;
    }

    @AchievesGoal(
        description = "The user has received a travel itinerary"
    )
    //@Action
    Recomendation provideTravelRecommendationAsItinerary(Itinerary recommendable) {
        return new Recomendation(recommendable.recommendation());
    }

    @AchievesGoal(
        description = "The user has received a flight recommendation"
    )
    //@Action
    Recomendation provideTravelRecommendationAsFlight(Flight recommendable) {
        return new Recomendation(recommendable.recommendation());
    }
}