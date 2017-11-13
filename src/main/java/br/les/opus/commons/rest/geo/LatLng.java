package br.les.opus.commons.rest.geo;

import javax.validation.constraints.NotNull;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class LatLng {
	
	@NotNull
	private Double lat;
	
	@NotNull
	private Double lng;
	
	public static final int GOOGLE_SRID = 4326;
	
	
	public LatLng() {

	}
	
	public LatLng(Point point) {
		this.lat = point.getCoordinate().y;
		this.lng = point.getCoordinate().x;
	}
	
	public Point toPoint() {
		GeometryFactory factory = new GeometryFactory();
		Point point = factory.createPoint(new Coordinate(lng, lat));
		point.setSRID(GOOGLE_SRID);
		return point;
	}

	public Double getLat() {
		return lat;
	}

	public void setLat(Double lat) {
		this.lat = lat;
	}

	public Double getLng() {
		return lng;
	}

	public void setLng(Double lng) {
		this.lng = lng;
	}
	
}
