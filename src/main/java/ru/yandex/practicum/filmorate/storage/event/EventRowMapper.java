package ru.yandex.practicum.filmorate.storage.event;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventOperation;
import ru.yandex.practicum.filmorate.model.EventType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class EventRowMapper implements RowMapper<Event> {

    @Override
    public Event mapRow(ResultSet rs, int rowNum) throws SQLException {
        Event event = new Event();

        event.setEventId(rs.getInt("event_id"));
        event.setTimestamp(rs.getLong("timestamp"));
        event.setUserId(rs.getInt("user_id"));
        event.setEventType(EventType.valueOf(rs.getString("event_type")));
        event.setOperation(EventOperation.valueOf(rs.getString("operation")));
        event.setEntityId(rs.getInt("entity_id"));

        return event;
    }
}
