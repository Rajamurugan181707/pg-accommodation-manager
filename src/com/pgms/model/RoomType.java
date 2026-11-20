package com.pgms.model;

/** Room sharing type. Each type fixes how many beds the room has. */
public enum RoomType {
    SINGLE(1), DOUBLE(2), TRIPLE(3);

    private final int beds;

    RoomType(int beds) {
        this.beds = beds;
    }

    public int beds() {
        return beds;
    }
}
