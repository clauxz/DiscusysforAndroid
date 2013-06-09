package com.TohokuUniversity.discussions.data.odata;

import com.TohokuUniversity.discussions.ApplicationConstants;
import com.TohokuUniversity.discussions.data.DataIoException;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Attachments;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Comments;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Descriptions;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Discussions;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Persons;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.PersonsTopics;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Points;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Seats;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Sessions;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Sources;
import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Topics;
import com.TohokuUniversity.discussions.utils.MyLog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.core4j.Enumerable;
import org.joda.time.LocalDateTime;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public class OdataReadClient extends BaseOdataClient {

	// TODO: get rid of Enumarable.count() because of poor perfomance
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final boolean LOGV = false && ApplicationConstants.DEV_MODE;
	private static final String TAG = OdataReadClient.class.getSimpleName();

	public OdataReadClient(final Context context) {

		super(context);
	}

	private static void logd(final String message) {

		if (DEBUG) {
			Log.d(TAG, message);
		}
	}

	private static ContentValues OEntityToContentValue(final OEntity entity) {

		final ContentValues cv = new ContentValues();
		for (OProperty<?> property : entity.getProperties()) {
			if (LOGV) {
				MyLog.v(TAG, property.getName() + ":" + property.getType() + ":" + property.getValue());
			}
			put(cv, property);
		}
		return cv;
	}

	private static ContentValues put(final ContentValues cv, final OProperty<?> property) {

		EdmSimpleType<?> type = (EdmSimpleType<?>) property.getType();
		Class<? extends Object> classType = type.getCanonicalJavaType();
		if (classType.equals(Integer.class)) {
			cv.put(property.getName(), (Integer) property.getValue());
		} else if (classType.equals(String.class)) {
			cv.put(property.getName(), (String) property.getValue());
		} else if (classType.equals(Boolean.class)) {
			cv.put(property.getName(), (Boolean) property.getValue());
		} else if (classType.equals(byte[].class)) {
			cv.put(property.getName(), (byte[]) property.getValue());
		} else if (classType.equals(LocalDateTime.class)) {
			cv.put(property.getName(), property.getValue().toString());
		} else {
			throw new IllegalArgumentException("Unknown property name: " + property.getName() + " type: "
					+ property.getType() + " value: " + property.getValue() + "javaType: "
					+ classType.getCanonicalName());
		}
		return cv;
	}

	public void refreshAttachments() {

		logd("[refreshAttachments]");
		Enumerable<OEntity> attachments = getAttachmentsEntities();
		int deletedCount = mContentResolver.delete(Attachments.CONTENT_URI, "1", null);
		logd("[refreshAttachments] attachments was deleted: " + deletedCount);
		for (OEntity attachment : attachments) {
			insertAttachment(attachment);
		}
		logd("[refreshAttachments] attachments was inserted: " + attachments.count());
	}

	public void refreshComments() {

		logd("[refreshComments]");
		Enumerable<OEntity> comments;
		if (ApplicationConstants.ODATA_SANITIZE) {
			comments = getCommentsEntities();
		} else {
			comments = getFilteredCommentsEntities();
		}
		int deletedCount = mContentResolver.delete(Comments.CONTENT_URI, "1", null);
		logd("[refreshComments] comments was deleted: " + deletedCount);
		for (OEntity comment : comments) {
			insertComment(comment);
		}
		logd("[refreshComments] comments was inserted: " + comments.count());
	}

	public void refreshDescription(final int pointId) {

		String filter = Points.TABLE_NAME + "/" + Points.Columns.ID + " eq " + String.valueOf(pointId);
		Enumerable<OEntity> description = mConsumer.getEntities(Descriptions.TABLE_NAME).filter(filter)
				.expand(Points.TABLE_NAME + "," + Discussions.TABLE_NAME).execute();
		if (description.count() == 1) {
			insertDescription(description.first());
		} else {
			throw new IllegalStateException("Should be one description, was: " + description.count());
		}
	}

	public void refreshDescriptions() {

		logd("[refreshDescriptions]");
		Enumerable<OEntity> descriptions = getDescriptionsEntities();
		int deletedCount = mContentResolver.delete(Descriptions.CONTENT_URI, "1", null);
		logd("[refreshDescriptions] descriptions was deleted: " + deletedCount);
		for (OEntity description : descriptions) {
			insertDescription(description);
		}
		logd("[refreshDescriptions] descriptions was inserted: " + descriptions.count());
	}

	public void refreshDiscussions() {

		logd("[refreshDiscussions]");
		Enumerable<OEntity> discussions = mConsumer.getEntities(Discussions.TABLE_NAME).execute();
		int deletedCount = mContentResolver.delete(Discussions.CONTENT_URI, "1", null);
		logd("[refreshDiscussions] discussions was deleted: " + deletedCount);
		for (OEntity discussion : discussions) {
			ContentValues cv = OEntityToContentValue(discussion);
			mContentResolver.insert(Discussions.CONTENT_URI, cv);
		}
		logd("[refreshDiscussions] discussions was inserted: " + discussions.count());
	}

	public void refreshPersons() {

		logd("[refreshPersons]");
		Enumerable<OEntity> persons = mConsumer.getEntities(Persons.TABLE_NAME).execute();
		int deletedCount = mContentResolver.delete(Persons.CONTENT_URI, "1", null);
		logd("[refreshPersons] persons was deleted: " + deletedCount);
		for (OEntity person : persons) {
			ContentValues cv = OEntityToContentValue(person);
			mContentResolver.insert(Persons.CONTENT_URI, cv);
		}
		logd("[refreshPersons] persons was inserted: " + persons.count());
	}

	public void refreshPoint(final int pointId) {

		OEntity entity = mConsumer.getEntity(Points.TABLE_NAME, pointId).expand(
				Topics.TABLE_NAME + "," + Persons.TABLE_NAME).execute();
		insertPoint(entity);
	}

	public void refreshPoints() {

		logd("[refreshPoints]");
		Enumerable<OEntity> points = getPointsEntities();
		int deletedCount = mContentResolver.delete(Points.CONTENT_URI, "1", null);
		logd("[refreshPoints] points was deleted: " + deletedCount);
		for (OEntity point : points) {
			insertPoint(point);
		}
		logd("[refreshPoints] points was inserted: " + points.count());
	}

	public void refreshPoints(final int topicId) {

		logd("[refreshPoints] topic id: " + topicId);
		Enumerable<OEntity> points = getPointsEntities(topicId);
		logd("[refreshPoints] points entities count: " + points.count());
		List<Integer> serversIds = new ArrayList<Integer>(points.count());
		for (OEntity point : points) {
			serversIds.add(getAsInt(point, Points.Columns.ID));
			insertPoint(point);
		}
		logd("[refreshPoints] all points was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Points.CONTENT_URI, new String[] { Points.Columns.ID,
				BaseColumns._ID }, Points.Columns.TOPIC_ID + "=?", new String[] { String.valueOf(topicId) },
				null);
		logd("[refreshPoints] db points count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Points.Columns.ID);
			int localIdIndex = cur.getColumnIndexOrThrow(BaseColumns._ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int pointId = cur.getInt(idIndex);
				if (!serversIds.contains(pointId)) {
					// delete this row
					int rowId = cur.getInt(localIdIndex);
					logd("[refreshPoints] delete point: " + rowId);
					Uri uri = Points.buildTableUri(rowId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	public void refreshSeats() {

		logd("[refreshSeats]");
		Enumerable<OEntity> seats = mConsumer.getEntities(Seats.TABLE_NAME).execute();
		int deletedCount = mContentResolver.delete(Seats.CONTENT_URI, "1", null);
		logd("[refreshSeats] seats was deleted: " + deletedCount);
		for (OEntity seat : seats) {
			ContentValues cv = OEntityToContentValue(seat);
			mContentResolver.insert(Seats.CONTENT_URI, cv);
		}
		logd("[refreshSeats] seats was inserted: " + seats.count());
	}

	public void refreshSessions() {

		logd("[refreshSessions]");
		Enumerable<OEntity> sessions = mConsumer.getEntities(Sessions.TABLE_NAME).execute();
		int deletedCount = mContentResolver.delete(Sessions.CONTENT_URI, "1", null);
		logd("[refreshSessions] sessions was deleted: " + deletedCount);
		for (OEntity session : sessions) {
			ContentValues cv = OEntityToContentValue(session);
			mContentResolver.insert(Sessions.CONTENT_URI, cv);
		}
		logd("[refreshSessions] sessions was inserted: " + sessions.count());
	}

	public void refreshSources() {

		logd("[refreshSources]");
		Enumerable<OEntity> sources = getSourcesEntities();
		int deletedCount = mContentResolver.delete(Sources.CONTENT_URI, "1", null);
		logd("[refreshSources] sources was deleted: " + deletedCount);
		for (OEntity source : sources) {
			insertSource(source);
		}
		logd("[refreshSources] sources was inserted: " + sources.count());
	}

	public void refreshTopics() {

		logd("[refreshTopics] ");
		Enumerable<OEntity> topics = getTopicsEntities();
		int deletedTopicCount = mContentResolver.delete(Topics.CONTENT_URI, "1", null);
		// TODO: how to delete related person-topics table
		logd("[refreshTopics] topics was deleted: " + deletedTopicCount);
		int insertedTopicsCount = 0;
		for (OEntity topic : topics) {
			insertTopic(topic);
			insertPersonsTopics(topic);
			insertedTopicsCount++;
		}
		logd("[refreshTopics] topics was inserted: " + insertedTopicsCount);
	}

	public void updatePoint(final int pointId) {

		OEntity point = mConsumer.getEntity(Points.TABLE_NAME, pointId).expand(
				Topics.TABLE_NAME + "," + Persons.TABLE_NAME + "," + "Description").execute();
		updatePoint(point);
		updatePointComments(pointId);
		updatePointAttachments(pointId);
		updatePointSources(point);
	}

	public void updateTopicPoints(final int topicId) {

		logd("[updatePointsFromTopic] topic id: " + topicId);
		Enumerable<OEntity> points = mConsumer.getEntities(Points.TABLE_NAME).expand(
				Topics.TABLE_NAME + "," + Persons.TABLE_NAME + "," + "Description").filter(
				"Topic/Id eq " + String.valueOf(topicId)).execute();
		logd("[updatePointsFromTopic] points entities count: " + points.count());
		List<Integer> serversIds = new ArrayList<Integer>(points.count());
		for (OEntity point : points) {
			int pointId = getAsInt(point, Points.Columns.ID);
			serversIds.add(pointId);
			updatePoint(point);
			updatePointComments(pointId);
			updatePointAttachments(pointId);
			updatePointSources(point);
		}
		logd("[updatePointsFromTopic] all points was inserted");
		// check if server has a deleted points
		Cursor cur = mContentResolver.query(Points.CONTENT_URI, new String[] { Points.Columns.ID,
				BaseColumns._ID }, Points.Columns.TOPIC_ID + "=?", new String[] { String.valueOf(topicId) },
				null);
		logd("[updatePointsFromTopic] db points count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Points.Columns.ID);
			int localIdIndex = cur.getColumnIndexOrThrow(BaseColumns._ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int pointId = cur.getInt(idIndex);
				if (!serversIds.contains(pointId)) {
					// delete this row
					int rowId = cur.getInt(localIdIndex);
					logd("[updatePointsFromTopic] delete point: " + rowId);
					Uri uri = Points.buildTableUri(rowId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	private Enumerable<OEntity> getAttachmentsEntities() {

		return mConsumer.getEntities(Attachments.TABLE_NAME).expand(
				Points.TABLE_NAME + "," + Discussions.TABLE_NAME).filter(
				"ArgPoint/Id ne null or Discussion/Id ne null").execute();
	}

	private Enumerable<OEntity> getAttachmentsEntities(final int pointId) {

		return mConsumer.getEntities(Attachments.TABLE_NAME).expand(Points.TABLE_NAME).filter(
				"ArgPoint/Id eq " + String.valueOf(pointId)).execute();
	}

	private Enumerable<OEntity> getCommentsEntities() {

		return mConsumer.getEntities(Comments.TABLE_NAME)
				.expand(Points.TABLE_NAME + "," + Persons.TABLE_NAME).execute();
	}

	private Enumerable<OEntity> getCommentsEntities(final int pointId) {

		return mConsumer.getEntities(Comments.TABLE_NAME)
				.expand(Points.TABLE_NAME + "," + Persons.TABLE_NAME).filter(
						"ArgPoint/Id eq " + String.valueOf(pointId)).execute();
	}

	private Enumerable<OEntity> getDescriptionsEntities() {

		return mConsumer.getEntities(Descriptions.TABLE_NAME).expand(
				Points.TABLE_NAME + "," + Discussions.TABLE_NAME).filter(
				"ArgPoint/Id ne null or Discussion/Id ne null").execute();
	}

	private Enumerable<OEntity> getFilteredCommentsEntities() {

		return mConsumer.getEntities(Comments.TABLE_NAME)
				.expand(Points.TABLE_NAME + "," + Persons.TABLE_NAME).filter(
						"ArgPoint/Id ne null and Person/Id ne null").execute();
	}

	private Enumerable<OEntity> getPointsEntities() {

		return mConsumer.getEntities(Points.TABLE_NAME).expand(Topics.TABLE_NAME + "," + Persons.TABLE_NAME)
				.filter("Topic/Id ne null and Person/Id ne null").execute();
	}

	private Enumerable<OEntity> getPointsEntities(final int topicId) {

		return mConsumer.getEntities(Points.TABLE_NAME).expand(Topics.TABLE_NAME + "," + Persons.TABLE_NAME)
				.filter("Topic/Id eq " + String.valueOf(topicId)).execute();
	}

	private Enumerable<OEntity> getSourcesEntities() {

		return mConsumer.getEntities(Sources.TABLE_NAME).expand(Descriptions.TABLE_NAME).execute();
	}

	private Enumerable<OEntity> getSourcesEntities(final int descrtiptionId) {

		return mConsumer.getEntities(Sources.TABLE_NAME).expand(Descriptions.TABLE_NAME).filter(
				"RichText/Id eq " + String.valueOf(descrtiptionId)).execute();
	}

	private Enumerable<OEntity> getTopicsEntities() {

		ODataConsumer mConsumerXml = ODataJerseyConsumer.newBuilder(getOdataServerUrl()).build();
		return mConsumerXml.getEntities(Topics.TABLE_NAME).expand(
				Discussions.TABLE_NAME + "," + Persons.TABLE_NAME).execute();
	}

	private Uri insertAttachment(final OEntity attachment) {

		ContentValues cv = OEntityToContentValue(attachment);
		OEntity point = attachment.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if ((point == null)) {
			OEntity discussion = attachment.getLink(Discussions.TABLE_NAME, ORelatedEntityLinkInline.class)
					.getRelatedEntity();
			if ((discussion == null)) {
				return null;
			}
			cv.put(Attachments.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
			return mContentResolver.insert(Attachments.CONTENT_URI, cv);
		}
		cv.put(Attachments.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
		return mContentResolver.insert(Attachments.CONTENT_URI, cv);
	}

	private Uri insertComment(final OEntity comment) {

		// get properties
		ContentValues cv = OEntityToContentValue(comment);
		// get related point id
		OEntity point = comment.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		if (point == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related point link was null for comment: " + getAsInt(comment, Comments.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete comment: " + getAsInt(comment, Comments.Columns.ID));
				mConsumer.deleteEntity(Comments.TABLE_NAME, getAsInt(comment, Comments.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Comments.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
		// get related person id
		OEntity person = comment.getLink(Persons.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (person == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related person link was null for comment: " + getAsInt(comment, Comments.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete comment: " + getAsInt(comment, Comments.Columns.ID));
				mConsumer.deleteEntity(Comments.TABLE_NAME, getAsInt(comment, Comments.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Comments.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
		try {
			return mContentResolver.insert(Comments.CONTENT_URI, cv);
		} catch (DataIoException e) {
			// TODO: send an exception here. in case of db structure change it would not be able to understood
			// that applications is not working
			Log.e(TAG, "Unable insert comment " + getAsInt(comment, Comments.Columns.ID), e);
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete comment: " + getAsInt(comment, Comments.Columns.ID));
				mConsumer.deleteEntity(Comments.TABLE_NAME, getAsInt(comment, Comments.Columns.ID)).execute();
			}
			String where = Comments.Columns.ID + "=?";
			String[] args = new String[] { String.valueOf(getAsInt(comment, Comments.Columns.ID)) };
			mContentResolver.delete(Comments.CONTENT_URI, where, args);
			return null;
		}
	}

	private Uri insertDescription(final OEntity description) {

		// get properties
		ContentValues cv = OEntityToContentValue(description);
		// get related point id
		OEntity point = description.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (point != null) {
			cv.put(Descriptions.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
		}
		// or related discussion id
		OEntity discussion = description.getLink(Discussions.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (discussion != null) {
			cv.put(Descriptions.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
		}
		// delete row if it is lost
		if ((discussion == null) && (point == null)) {
			// TODO: thwo ex here
			Log.e(TAG, "Both descriptions foreign key was null "
					+ getAsInt(description, Descriptions.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + getAsInt(description, Descriptions.Columns.ID));
				mConsumer.deleteEntity(Descriptions.TABLE_NAME,
						getAsInt(description, Descriptions.Columns.ID)).execute();
			}
			return null;
		}
		return insertDescriptionToDb(cv);
	}

	private Uri insertDescriptionToDb(final ContentValues descriptionValues) {

		try {
			return mContentResolver.insert(Descriptions.CONTENT_URI, descriptionValues);
		} catch (DataIoException e) {
			// TODO: send an exception here. in case of db structure change it would not be able to understood
			// that applications is not working
			int descriptionId = descriptionValues.getAsInteger(Descriptions.Columns.ID);
			Log.e(TAG, "Unable insert description " + descriptionId, e);
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + descriptionId);
				mConsumer.deleteEntity(Descriptions.TABLE_NAME, descriptionId).execute();
			}
			String where = Descriptions.Columns.ID + "=?";
			String[] args = new String[] { String.valueOf(descriptionId) };
			mContentResolver.delete(Descriptions.CONTENT_URI, where, args);
			return null;
		}
	}

	private Uri insertOrDelete(final Uri contentUri, final ContentValues cv, final String idColumnName) {

		try {
			return mContentResolver.insert(contentUri, cv);
		} catch (DataIoException e) {
			MyLog.e(TAG, "Failed insert into: " + contentUri + ", values: " + cv.toString(), e);
			// delete row in case of local db has one with same id
			int id = cv.getAsInteger(idColumnName);
			String where = idColumnName + "=?";
			String[] args = new String[] { String.valueOf(id) };
			mContentResolver.delete(contentUri, where, args);
			// TODO: delete row from server too to stop this
			return null;
		}
	}

	private void insertPersonsTopics(final OEntity topic) {

		// get topic id
		int topicId = getAsInt(topic, Topics.Columns.ID);
		// get related persons
		List<OEntity> persons = topic.getLink(Topics.Columns.PERSON_ID, ORelatedEntitiesLinkInline.class)
				.getRelatedEntities();
		// insert many-to-many relationship
		for (OEntity person : persons) {
			ContentValues cv = new ContentValues();
			cv.put(PersonsTopics.Columns.TOPIC_ID, topicId);
			cv.put(PersonsTopics.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
			// TODO: provide special PersonsTopics uri for insert
			mContentResolver.insert(Persons.buildTopicUri(1231231), cv);
		}
	}

	private Uri insertPoint(final OEntity point) {

		// get properties
		ContentValues cv = OEntityToContentValue(point);
		// get related topic id
		OEntity topic = point.getLink(Topics.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		if (topic == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related topic link was null for point: " + getAsInt(point, Points.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + getAsInt(point, Points.Columns.ID));
				mConsumer.deleteEntity(Points.TABLE_NAME, getAsInt(point, Points.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Points.Columns.TOPIC_ID, getAsInt(topic, Topics.Columns.ID));
		// get related person id
		OEntity person = point.getLink(Persons.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		if (person == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related person link was null for point: " + getAsInt(point, Points.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + getAsInt(point, Points.Columns.ID));
				mConsumer.deleteEntity(Points.TABLE_NAME, getAsInt(point, Points.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Points.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
		return insertOrDelete(Points.CONTENT_URI, cv, Points.Columns.ID);
	}

	private Uri insertSource(final OEntity source) {

		ContentValues cv = OEntityToContentValue(source);
		OEntity description = source.getLink(Descriptions.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if ((description == null)) {
			// TODO: thwo ex here
			return null;
		}
		cv.put(Sources.Columns.DESCRIPTION_ID, getAsInt(description, Sources.Columns.ID));
		return mContentResolver.insert(Sources.CONTENT_URI, cv);
	}

	private Uri insertTopic(final OEntity entity) {

		// get properties
		ContentValues cv = OEntityToContentValue(entity);
		// get related discussion id
		OEntity discussion = entity.getLink(Topics.Columns.DISCUSSION_ID, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (discussion == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related discussion link is null for topic: " + getAsInt(entity, Topics.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				mConsumer.deleteEntity(Topics.TABLE_NAME, getAsInt(entity, Topics.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Topics.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
		return mContentResolver.insert(Topics.CONTENT_URI, cv);
	}

	private Uri updatePoint(final OEntity point) {

		// get properties
		ContentValues cv = OEntityToContentValue(point);
		// get related topic id
		OEntity topic = point.getLink(Topics.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		if (topic == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related topic link was null for point: " + getAsInt(point, Points.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + getAsInt(point, Points.Columns.ID));
				mConsumer.deleteEntity(Points.TABLE_NAME, getAsInt(point, Points.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Points.Columns.TOPIC_ID, getAsInt(topic, Topics.Columns.ID));
		// get related person id
		OEntity person = point.getLink(Persons.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		if (person == null) {
			// TODO: thwo ex here
			Log.e(TAG, "Related person link was null for point: " + getAsInt(point, Points.Columns.ID));
			if (ApplicationConstants.ODATA_SANITIZE) {
				Log.w(TAG, "Try to delete point: " + getAsInt(point, Points.Columns.ID));
				mConsumer.deleteEntity(Points.TABLE_NAME, getAsInt(point, Points.Columns.ID)).execute();
			}
			return null;
		}
		cv.put(Points.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
		Uri uri = mContentResolver.insert(Points.CONTENT_URI, cv);
		// description update
		OEntity description = point.getLink("Description", ORelatedEntityLinkInline.class).getRelatedEntity();
		if (description != null) {
			// get properties
			ContentValues cvDescription = OEntityToContentValue(description);
			// // get related point id
			cvDescription.put(Descriptions.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
			// mContentResolver.insert(Descriptions.CONTENT_URI, cvDescription);
			insertDescriptionToDb(cvDescription);
		}
		return uri;
	}

	private void updatePointAttachments(final int pointId) {

		Enumerable<OEntity> attachments = getAttachmentsEntities(pointId);
		logd("[updatePointAttachments] entities count: " + attachments.count());
		if (attachments.count() == 0) {
			return;
		}
		List<Integer> serversIds = new ArrayList<Integer>(attachments.count());
		for (OEntity attachment : attachments) {
			serversIds.add(getAsInt(attachment, Attachments.Columns.ID));
			insertAttachment(attachment);
		}
		logd("[updatePointAttachments] all attachments was inserted");
		// check if server has a deleted points
		String where = Attachments.Columns.POINT_ID + "=" + pointId;
		Cursor cur = mContentResolver.query(Attachments.CONTENT_URI, new String[] { Attachments.Columns.ID },
				where, null, null);
		logd("[updatePointAttachments] local count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Attachments.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int attachmentId = cur.getInt(idIndex);
				if (!serversIds.contains(attachmentId)) {
					// delete this row
					logd("[updatePointAttachments] delete attachment: " + attachmentId);
					Uri uri = Attachments.buildTableUri(attachmentId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	private void updatePointComments(final int pointId) {

		logd("[refreshComments]");
		Enumerable<OEntity> comments = getCommentsEntities(pointId);
		logd("[refreshComments] comment entities count: " + comments.count());
		if (comments.count() == 0) {
			return;
		}
		List<Integer> serversIds = new ArrayList<Integer>(comments.count());
		for (OEntity comment : comments) {
			serversIds.add(getAsInt(comment, Comments.Columns.ID));
			insertComment(comment);
		}
		logd("[refreshComments] all comments was inserted");
		// check if server has a deleted points
		String where = Comments.Columns.POINT_ID + "=?";
		String[] args = new String[] { String.valueOf(pointId) };
		Cursor cur = mContentResolver.query(Comments.CONTENT_URI, new String[] { Comments.Columns.ID, },
				where, args, null);
		logd("[refreshComments] db comments count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Comments.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int commentId = cur.getInt(idIndex);
				if (!serversIds.contains(commentId)) {
					// delete this row
					logd("[refreshComments] delete point: " + commentId);
					Uri uri = Comments.buildTableUri(commentId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}

	private void updatePointSources(final OEntity point) {

		OEntity description = point.getLink("Description", ORelatedEntityLinkInline.class).getRelatedEntity();
		if (description == null) {
			return;
		}
		int descriptionId = getAsInt(description, Descriptions.Columns.ID);
		Enumerable<OEntity> sources = getSourcesEntities(descriptionId);
		logd("[refreshSources] entities count: " + sources.count());
		if (sources.count() == 0) {
			return;
		}
		List<Integer> serversIds = new ArrayList<Integer>(sources.count());
		for (OEntity source : sources) {
			serversIds.add(getAsInt(source, Attachments.Columns.ID));
			insertSource(source);
		}
		logd("[refreshSources] all sources was inserted");
		// check if server has a deleted points
		String where = Sources.Columns.DESCRIPTION_ID + "=" + descriptionId;
		Cursor cur = mContentResolver.query(Sources.CONTENT_URI, new String[] { Sources.Columns.ID }, where,
				null, null);
		logd("[refreshSources] local sources count: " + cur.getCount());
		if (cur.getCount() > serversIds.size()) {
			// local storage has deleted data
			int idIndex = cur.getColumnIndexOrThrow(Sources.Columns.ID);
			for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
				int sourceId = cur.getInt(idIndex);
				if (!serversIds.contains(sourceId)) {
					// delete this row
					logd("[refreshSources] delete source: " + sourceId);
					Uri uri = Sources.buildTableUri(sourceId);
					mContentResolver.delete(uri, null, null);
				}
			}
		}
		cur.close();
	}
}
