package dk.cphbusiness.flightdemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.cphbusiness.flightdemo.dtos.FlightDTO;
import dk.cphbusiness.flightdemo.dtos.FlightInfoDTO;
import dk.cphbusiness.utils.Utils;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
            System.out.println("Task 1:");
            printTotalFlightTimeByAirline(getTotalFlightTimePerAirline(flightInfoDTOList, "IndiGo"), "IndiGo");
            System.out.println();

            //round-2
            System.out.println("Task 2:");
            printAverageFlightTimeByAirline(getAverageFlightTimePerAirline(flightInfoDTOList, "IndiGo"), "IndiGo");
            System.out.println();

            //round-3
            System.out.println("Task 3:");
            printFlightsBetweenAirports(getFlightsBetweenAirports(flightInfoDTOList, "Fukuoka", "Haneda Airport"), "Fukuoka", "Haneda Airport");
            System.out.println();

            //round-4
            System.out.println("Task 4:");
            LocalTime testTime = LocalTime.of(1, 00);
            printFlightsBeforeTime(getFlightsBeforeTime(flightInfoDTOList, testTime), testTime);
            System.out.println();

            //round-5
            System.out.println("Task 5:\nAverage flight times by Airlines:");
            Map<String, Duration> avgDurations = getAverageFlightTimeForAllAirlines(flightInfoDTOList);
            avgDurations.forEach((airline, duration) ->
                    System.out.println(airline + ": " + duration.toHours() + "h " + duration.toMinutesPart() + "m"));
            System.out.println();

            //round-6
            System.out.println("Task 6:\nFlights sorted by Arrival time:");
            List<FlightInfoDTO> flightsByArrival = sortByArrival(flightInfoDTOList);
            flightsByArrival.forEach((System.out::println));
            System.out.println();

            //round-7
            System.out.println("Task 7:\nTotal flight times by Airlines:");
            Map<String, Duration> totalDurations = getTotalFlightTimeForAllAirlines(flightInfoDTOList);
            totalDurations.forEach((airline, duration) ->
                    System.out.println(airline + ": " + duration.toHours() + "h" + duration.toMinutesPart() + "m"));
            System.out.println();

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
                .map(flight -> flight.getDuration())
                .reduce(Duration.ZERO, Duration::plus);
    }

    public static void printTotalFlightTimeByAirline(Duration duration, String airline)
    {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        System.out.println("Airline " + airline + "'s total flighttime: " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds");
    }

    public static Duration getAverageFlightTimePerAirline(List<FlightInfoDTO> flightList, String airline)
    {
        List<FlightInfoDTO> filteredFlights = filterByAirline(flightList, airline);

        int flightCount = filteredFlights.size();

        if (flightCount == 0)
        {
            return Duration.ZERO; // Avoid division by zero
        }

        Duration totalDuration = filteredFlights.stream()
                .map(flight -> flight.getDuration())
                .reduce(Duration.ZERO, Duration::plus);

        return totalDuration.dividedBy(flightCount);
    }

    public static Map<String, Duration> getAverageFlightTimeForAllAirlines(List<FlightInfoDTO> flightList)
    {
        return flightList.stream()
                .filter(flight -> flight.getAirline() != null)
                .collect(Collectors.groupingBy(
                        FlightInfoDTO::getAirline,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                flights ->
                                {
                                    int flightCount = flights.size();
                                    if (flightCount == 0) return Duration.ZERO;

                                    Duration totalDuration = flights.stream()
                                            .map(flight -> flight.getDuration())
                                            .reduce(Duration.ZERO, Duration::plus);
                                    return totalDuration.dividedBy(flightCount);
                                }
                        )
                ));
    }

    public static Map<String, Duration> getTotalFlightTimeForAllAirlines(List<FlightInfoDTO> flightList)
    {
        return flightList.stream()
                .filter(flight -> flight.getAirline() != null)
                .collect(Collectors.groupingBy(
                        FlightInfoDTO::getAirline,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                flights ->
                                {
                                    int flightCount = flights.size();
                                    if (flightCount == 0) return Duration.ZERO;

                                    Duration totalDuration = flights.stream()
                                            .map(flight -> flight.getDuration())
                                            .reduce(Duration.ZERO, Duration::plus);
                                    return totalDuration;
                                }
                        )
                ));
    }

    public static void printAverageFlightTimeByAirline(Duration duration, String airline)
    {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        System.out.println("Airline " + airline + "'s average flighttime: " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds");
    }

    public static List<FlightInfoDTO> getFlightsBetweenAirports(List<FlightInfoDTO> flightList, String airport1, String airport2)
    {
        return flightList.stream()
                .filter(flight ->
                        (Objects.equals(flight.getOrigin(), airport1) || Objects.equals(flight.getOrigin(), airport2)) &&
                                (Objects.equals(flight.getDestination(), airport1) || Objects.equals(flight.getDestination(), airport2))
                )
                .collect(Collectors.toList());
    }

    public static void printFlightsBetweenAirports(List<FlightInfoDTO> flightList, String airport1, String airport2)
    {
        System.out.println("There are a total of " + flightList.size() + " flights, between " + airport1 + " and " + airport2 + ".");
    }

    public static List<FlightInfoDTO> getFlightsBeforeTime(List<FlightInfoDTO> flightList, LocalTime time)
    {
        return flightList.stream()
                .filter(flight -> flight.getDeparture().toLocalTime().isAfter(time))
                .collect(Collectors.toList());
    }

    public static void printFlightsBeforeTime(List<FlightInfoDTO> flightList, LocalTime time)
    {
        System.out.println("There are " + flightList.size() + " flights before: " + time);
    }

    public static List<FlightInfoDTO> sortByArrival(List<FlightInfoDTO> flightList)
    {
        return flightList.stream()
                .sorted(Comparator.comparing(FlightInfoDTO::getArrival).reversed())
                .collect(Collectors.toList());
    }

}
