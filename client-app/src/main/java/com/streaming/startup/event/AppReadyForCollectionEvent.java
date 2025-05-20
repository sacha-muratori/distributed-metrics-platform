package com.streaming.startup.event;

import org.springframework.context.ApplicationEvent;

public class AppReadyForCollectionEvent extends ApplicationEvent {
    public AppReadyForCollectionEvent(Object source) {
        super(source);
    }
}
