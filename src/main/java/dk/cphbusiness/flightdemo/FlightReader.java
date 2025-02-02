package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Purpose:
 *
 * @author: Thomas Hartmann
 */
public class FlightReader
{

    public static void main(String[] args)
    {
        try
        {
            List<FlightDTO> flightList = getFlightsFromFile("flights.json");
            List<FlightInfoDTO> flightInfoDTOList = getFlightInfoDetails(flightList);
            flightInfoDTOList.forEach(System.out::println);

            System.out.println();

            //round-1
            printTotalFlightTimeByAirline(getTotalFlightTimePerAirline(flightInfoDTOList, "IndiGo"), "IndiGo");

            //round-2
            printAverageFlightTimeByAirline(getAverageFlightTimePerAirline(flightInfoDTOList, "IndiGo"), "IndiGo");
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static List<FlightDTO> getFlightsFromFile(String filename) throws IOException
    {

        ObjectMapper objectMapper = Utils.getObjectMapper();

        // Deserialize JSON from a file into FlightDTO[]
        FlightDTO[] flightsArray = objectMapper.readValue(Paths.get("flights.json").toFile(), FlightDTO[].class);

        // Convert to a list
        List<FlightDTO> flightsList = List.of(flightsArray);
        return flightsList;
    }

    public static List<FlightInfoDTO> getFlightInfoDetails(List<FlightDTO> flightList)
    {
        List<FlightInfoDTO> flightInfoList = flightList.stream()
                .map(flight ->
                {
                    LocalDateTime departure = flight.getDeparture().getScheduled();
                    LocalDateTime arrival = flight.getArrival().getScheduled();
                    Duration duration = Duration.between(departure, arrival);
                    FlightInfoDTO flightInfo =
                            FlightInfoDTO.builder()
                                    .name(flight.getFlight().getNumber())
                                    .iata(flight.getFlight().getIata())
                                    .airline(flight.getAirline().getName())
                                    .duration(duration)
                                    .departure(departure)
                                    .arrival(arrival)
                                    .origin(flight.getDeparture().getAirport())
                                    .destination(flight.getArrival().getAirport())
                                    .build();

                    return flightInfo;
                })
                .toList();
        return flightInfoList;
    }

    public static List<FlightInfoDTO> filterByAirline(List<FlightInfoDTO> flightList, String airline)
    {
        return flightList.stream()
                .filter(flight -> Objects.equals(flight.getAirline(), airline))
                .collect(Collectors.toList());
    }

    public static Duration getTotalFlightTimePerAirline(List<FlightInfoDTO> flightList, String airline)
    {
        flightList = filterByAirline(flightList, airline);

        return flightList.stream()
                .map(flight -> Duration.between(
                        flight.getDeparture(),
                        flight.getArrival()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    public static void printTotalFlightTimeByAirline(Duration duration, String airline)
    {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        System.out.println("Airline " + airline + "'s total flighttime: " +  hours + " hours, " + minutes + " minutes, " + seconds + " seconds");
    }

    public static Duration getAverageFlightTimePerAirline(List<FlightInfoDTO> flightList, String airline)
    {
        List<FlightInfoDTO> filteredFlights = filterByAirline(flightList, airline);

        int flightCount = filteredFlights.size();

        if (flightCount == 0) {
            return Duration.ZERO; // Avoid division by zero
        }

        Duration totalDuration = filteredFlights.stream()
                .map(flight -> Duration.between(
                        flight.getDeparture(),
                        flight.getArrival()))
                .reduce(Duration.ZERO, Duration::plus);

        return totalDuration.dividedBy(flightCount);
    }

    public static void printAverageFlightTimeByAirline(Duration duration, String airline)
    {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        System.out.println("Airline " + airline + "'s average flighttime: " +  hours + " hours, " + minutes + " minutes, " + seconds + " seconds");
    }

}
