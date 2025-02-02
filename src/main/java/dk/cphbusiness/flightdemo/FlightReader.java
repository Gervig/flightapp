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
            totalFlightTimePerAirlineToString(
                    getTotalFlightTimePerAirline(flightInfoDTOList, "Jet Linx Aviation"), "Jet Linx Aviation"
            );
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

    public static Duration getTotalFlightTimePerAirline(List<FlightInfoDTO> flightList, String airline)
    {
        return flightList.stream()
                .filter(flight -> Objects.equals(flight.getAirline(), airline))
                .map(flight -> Duration.between(
                        flight.getDeparture(),
                        flight.getArrival()))
                .reduce(Duration.ZERO, Duration::plus);
    }

    public static void totalFlightTimePerAirlineToString(Duration duration, String airline)
    {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        System.out.println("Airline " + airline + "'s total flighttime: " +  hours + " hours, " + minutes + " minutes, " + seconds + " seconds");
    }

}
