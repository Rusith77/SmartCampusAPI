package com.smartcampus.resource;

import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/sensors/{sensorId}/readings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReadingResource {

   @GET
public List<SensorReading> getReadings(@PathParam("sensorId") String sensorId) {
    Sensor sensor = DataStore.sensors.get(sensorId);

    if (sensor == null) {
        throw new ResourceNotFoundException("Sensor not found: " + sensorId);
    }

    return DataStore.readings.getOrDefault(sensorId, new ArrayList<>());
}

@POST
public SensorReading addReading(@PathParam("sensorId") String sensorId, SensorReading reading) {

    Sensor sensor = DataStore.sensors.get(sensorId);

    if (sensor == null) {
        throw new ResourceNotFoundException("Sensor not found: " + sensorId);
    }

    if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
        throw new SensorUnavailableException("Sensor is in maintenance mode: " + sensorId);
    }

    List<SensorReading> list = DataStore.readings.get(sensorId);

    if (list == null) {
        list = new ArrayList<>();
        DataStore.readings.put(sensorId, list);
    }

    list.add(reading);

    // update sensor currentValue
    sensor.setCurrentValue(reading.getValue());

    return reading;
}
}