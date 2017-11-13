package br.les.opus.dengue.api.builders;

import java.util.List;

import br.les.opus.commons.geojson.Feature;
import br.les.opus.commons.geojson.FeatureCollection;
import br.les.opus.commons.geojson.Point;
import br.les.opus.dengue.core.domain.NotablePoint;
import br.les.opus.dengue.core.domain.PointOfInterest;
import br.les.opus.instagram.domain.Media;
import br.les.opus.twitter.domain.Tweet;

public class FeatureCollectionBuilder {
	
	private FeatureCollection featureCollection;

	public FeatureCollectionBuilder() {
		featureCollection = new FeatureCollection();
	}
	
	public void addPointOfInterest(PointOfInterest poi) {
		Feature feature = new Feature();
		feature.setId(poi.getId().toString());
		feature.setProperties(poi);
		
		Point point = new Point();
		point.addValue(poi.getLocation().getX());
		point.addValue(poi.getLocation().getY());
		feature.setGeometry(point);
		
		this.featureCollection.addFeature(feature);
	}
	
	public void addTweet(Tweet tweet) {
		Feature feature = new Feature();
		feature.setId(tweet.getId().toString());
		feature.setProperties(tweet);
		
		Point point = new Point();
		point.addValue(tweet.getGeolocation().getX());
		point.addValue(tweet.getGeolocation().getY());
		feature.setGeometry(point);
		
		this.featureCollection.addFeature(feature);
	}
	
	public void addInstagramMedia(Media media) {
		Feature feature = new Feature();
		feature.setId(media.getId().toString());
		feature.setProperties(media);
		
		Point point = new Point();
		point.addValue(media.getLocation().getLongitude());
		point.addValue(media.getLocation().getLatitude());
		feature.setGeometry(point);
		
		this.featureCollection.addFeature(feature);
	}
	
	public void addNotablePoint(NotablePoint notablePoint) {
		Feature feature = new Feature();
		feature.setId(notablePoint.getId());
		
		Point point = new Point();
		point.addValue(notablePoint.getLongitude());
		point.addValue(notablePoint.getLatitude());
		feature.setGeometry(point);
		
		this.featureCollection.addFeature(feature);
	}
	
	public void addNotablePoints(List<NotablePoint> points) {
		for (NotablePoint point : points) {
			this.addNotablePoint(point);
		}
	}
	
	public void addPointsOfInterest(List<PointOfInterest> pois) {
		for (PointOfInterest pointOfInterest : pois) {
			this.addPointOfInterest(pointOfInterest);
		}
	}
	
	public void addTweets(List<Tweet> tweets) {
		for (Tweet tweet : tweets) {
			this.addTweet(tweet);
		}
	}
	
	public void addInstagramMedias(List<Media> medias) {
		for (Media media : medias) {
			this.addInstagramMedia(media);
		}
	}
	
	public FeatureCollection build() {
		return featureCollection;
	}
}
