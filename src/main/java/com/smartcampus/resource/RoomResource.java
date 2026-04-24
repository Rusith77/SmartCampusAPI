package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    @GET
    public List<Room> getAllRooms() {
        return new ArrayList<>(DataStore.rooms.values());
    }

    @POST
    public Room createRoom(Room room) {
        DataStore.rooms.put(room.getId(), room);
        return room;
    }

@GET
@Path("/{roomId}")
public Room getRoomById(@PathParam("roomId") String roomId) {

    Room room = DataStore.rooms.get(roomId);

    if (room == null) {
        throw new ResourceNotFoundException("Room not found: " + roomId);
    }

    return room;
}

  @DELETE
@Path("/{roomId}")
public Room deleteRoom(@PathParam("roomId") String roomId) {

    Room room = DataStore.rooms.get(roomId);

    if (room == null) {
        throw new ResourceNotFoundException("Room not found: " + roomId);
    }

    if (!room.getSensorIds().isEmpty()) {
    throw new RoomNotEmptyException("Room has sensors, cannot delete");
}

    return DataStore.rooms.remove(roomId);
}
}