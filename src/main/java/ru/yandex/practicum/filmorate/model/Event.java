package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    private int eventId;
    private long timestamp;
    private int userId;
    private EventType eventType;
    private EventOperation operation;
    private int entityId;
}
