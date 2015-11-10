package com.easyapp.googlemap_route_record.model;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * Created by easyapp_jim on 2015/11/6.
 */

@Table(name = "Point")
public class Point extends Model {


    @Column(name = "route_name")
    public String route_name;
    @Column(name = "latitude")
    public String latitude;
    @Column(name = "longitude")
    public String longitude;

    public Point() {
        super();
    }

    public String getRoute_name() {
        return route_name;
    }

    public void setRoute_name(String route_name) {
        this.route_name = route_name;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public static List<Point> getAll() {
        return new Select()
                .from(Point.class)
                .execute();
    }
}
