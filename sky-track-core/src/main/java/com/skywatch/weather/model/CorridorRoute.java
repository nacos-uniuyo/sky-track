package com.skywatch.weather.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "corridor_routes")
public class CorridorRoute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_city_id")
    private TrackedCity originCity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_city_id")
    private TrackedCity destinationCity;

    @Column(name = "waypoint_count", nullable = false)
    private int waypointCount;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public void setUser(AppUser user) {
        this.user = user;
    }

    public TrackedCity getOriginCity() {
        return originCity;
    }

    public void setOriginCity(TrackedCity originCity) {
        this.originCity = originCity;
    }

    public TrackedCity getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(TrackedCity destinationCity) {
        this.destinationCity = destinationCity;
    }

    public int getWaypointCount() {
        return waypointCount;
    }

    public void setWaypointCount(int waypointCount) {
        this.waypointCount = waypointCount;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
