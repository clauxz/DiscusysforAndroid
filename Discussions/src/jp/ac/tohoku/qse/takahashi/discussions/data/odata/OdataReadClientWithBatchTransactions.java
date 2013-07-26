package jp.ac.tohoku.qse.takahashi.discussions.data.odata;


import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.graphics.Point;
import android.net.Uri;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import org.core4j.Enumerable;
import org.joda.time.LocalDateTime;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.core.OProperty;
import org.odata4j.core.ORelatedEntitiesLinkInline;
import org.odata4j.core.ORelatedEntityLinkInline;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

import java.util.ArrayList;
import java.util.List;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Attachment;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Comment;
import jp.ac.tohoku.qse.takahashi.discussions.data.model.Person;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Attachments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Comments;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.CommentsPersonReadEntry;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Descriptions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Discussions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Persons;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.PersonsTopics;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Points;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Seats;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sessions;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Sources;
import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.Topics;
import jp.ac.tohoku.qse.takahashi.discussions.utils.MyLog;

public class OdataReadClientWithBatchTransactions extends BaseOdataClient {

	// TODO: call applyBatch after each method is required
	// TODO: get rid of Enumarable.count() because of poor perfomance
	private static final boolean DEBUG = true && ApplicationConstants.DEV_MODE;
	private static final boolean LOGV = false && ApplicationConstants.DEV_MODE;
	private static final String TAG = OdataReadClientWithBatchTransactions.class.getSimpleName();
	private final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

	public OdataReadClientWithBatchTransactions(final Context context) {

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
			String value = (String) property.getValue();
			if (value == null) {
				cv.put(property.getName(), "");
			} else {
				cv.put(property.getName(), value);
			}
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

	public void applyBatchOperations() {

		try {
			mContentResolver.applyBatch(DiscussionsContract.CONTENT_AUTHORITY, operations);
		} catch (RemoteException e) {
			Log.e(TAG, "Failed to insert batch operations", e);
		} catch (OperationApplicationException e) {
			Log.e(TAG, "Failed to insert batch operations", e);
		}
	}

	public void refreshAttachments() {

		logd("[refreshAttachments]");
		deleteAllValues(Attachments.CONTENT_URI);
		Enumerable<OEntity> attachments = getAttachmentsEntities();
		for (OEntity attachment : attachments) {
			insertAttachment(attachment);
		}
		logd("[refreshAttachments] attachments was inserted: " + attachments.count());
	}

	public void refreshAttachments(final String personSelection, final String discussionSelection) {

		logd("[refreshAttachments]");
		deleteAllValues(Attachments.CONTENT_URI);
		if (!TextUtils.isEmpty(personSelection)) {
			Enumerable<OEntity> attachments = getAttachmentsEntitiesFromPerson(personSelection,
					discussionSelection);
			for (OEntity attachment : attachments) {
				insertAttachment(attachment);
			}
			logd("[refreshAttachments] attachments was inserted: " + attachments.count());
		}
	}

	public void refreshComments() {

		logd("[refreshComments]");
		deleteAllValues(Comments.CONTENT_URI);
		Enumerable<OEntity> comments;
		if (ApplicationConstants.ODATA_SANITIZE) {
			comments = getCommentsEntities();
		} else {
			comments = getFilteredCommentsEntities();
		}
		for (OEntity comment : comments) {
			insertComment(comment);
		}
		logd("[refreshComments] comments was inserted: " + comments.count()); 
	}

	
	
	/**
	 * Refresh data in local DB for comments which was viewed.
	 */
	public void refreshCommentsPersonEntry(){
		logd("[refreshCommentsPersonEntry]");
		
		deleteAllValues(CommentsPersonReadEntry.CONTENT_URI);
		Enumerable<OEntity> commentsPersonEntries;
		
		
		if(ApplicationConstants.ODATA_SANITIZE)
		{
			Log.i("Disc","person entities");
			commentsPersonEntries=getCommentsPersonEntryEntities();
		}
		else
		{
			Log.i("Disc","filtered person entities");
			commentsPersonEntries=getFilteredCommentsPersonEntryEntities();
		}
		
		
		Log.i("Disc","insertCommentPersonEntry count:"+String.valueOf(commentsPersonEntries.count()));
		
		
		for(OEntity commentPersonEntry:commentsPersonEntries){
			//Log.i("Disc item","name:"+commentPersonEntry.getEntitySetName()+" tag:"
			//		+commentPersonEntry.getEntityTag());
	
			Log.i("Disc item",commentPersonEntry.toString());
			
			//for(OProperty property:commentPersonEntry.getProperties()){			
			//	Log.i("Disc property",property.toString());
			//}
			
			insertCommentPersonEntry(commentPersonEntry);
		}
	}
	
	
	
	public void refreshCommentsFromPerson(final String selection) {

		logd("[refreshComments]");
		deleteAllValues(Comments.CONTENT_URI);
		if (!TextUtils.isEmpty(selection)) {
			Enumerable<OEntity> comments = getCommentsEntitiesFromPerson(selection);
			for (OEntity comment : comments) {
				insertComment(comment);
			}
			logd("[refreshComments] comments was inserted: " + comments.count());
		}
	}

	public void refreshDescriptions() {

		logd("[refreshDescriptions]");
		deleteAllValues(Descriptions.CONTENT_URI);
		Enumerable<OEntity> descriptions = getDescriptionsEntities();
		for (OEntity description : descriptions) {
			insertDescription(description);
		}
		logd("[refreshDescriptions] descriptions was inserted: " + descriptions.count());
	}

	public Enumerable<OEntity> refreshDiscussions() {

		logd("[refreshDiscussions]");
		deleteAllValues(Discussions.CONTENT_URI);
		Enumerable<OEntity> discussions = mConsumer.getEntities(Discussions.TABLE_NAME).execute();
		for (OEntity discussion : discussions) {
			ContentValues cv = OEntityToContentValue(discussion);
			insertValues(Discussions.CONTENT_URI, cv);
		}
		logd("[refreshDiscussions] discussions was inserted: " + discussions.count());
		return discussions;
	}

	public void refreshPersons() {

		logd("[refreshPersons]");
		deleteAllValues(Persons.CONTENT_URI);
		Enumerable<OEntity> persons = mConsumer.getEntities(Persons.TABLE_NAME).execute();
		for (OEntity person : persons) {
			ContentValues cv = OEntityToContentValue(person);
			insertValues(Persons.CONTENT_URI, cv);
		}
		logd("[refreshPersons] persons was inserted: " + persons.count());
	}

	public Enumerable<OEntity> refreshPersonsFromSession(final int sessionId) {

		logd("[refreshPersonsFromSession] sessionId: " + sessionId);
		deleteAllValues(Persons.CONTENT_URI);
		Enumerable<OEntity> persons = getPersonsEntities(sessionId);
		for (OEntity person : persons) {
			ContentValues cv = OEntityToContentValue(person);
			insertValues(Persons.CONTENT_URI, cv);
		}
		logd("[refreshPersonsFromSession] persons was inserted: " + persons.count());
		return persons;
	}

	public void refreshPoints() {

		logd("[refreshPoints]");
		deleteAllValues(Points.CONTENT_URI);
		Enumerable<OEntity> points = getPointsEntities();
		for (OEntity point : points) {
			insertPoint(point);
		}
		logd("[refreshPoints] points was inserted: " + points.count());
	}

	public void refreshPointsFromPerson(final String selection) {

		logd("[refreshPoints]");
		deleteAllValues(Points.CONTENT_URI);
		if (!TextUtils.isEmpty(selection)) {
			Enumerable<OEntity> points = getPointsEntitiesFromPerson(selection);
			for (OEntity point : points) {
				insertPoint(point);
			}
			logd("[refreshPoints] points was inserted: " + points.count());
		}
	}

	public void refreshSeats() {

		logd("[refreshSeats]");
		deleteAllValues(Seats.CONTENT_URI);
		Enumerable<OEntity> seats = mConsumer.getEntities(Seats.TABLE_NAME).execute();
		for (OEntity seat : seats) {
			ContentValues cv = OEntityToContentValue(seat);
			insertValues(Seats.CONTENT_URI, cv);
		}
		logd("[refreshSeats] seats was inserted: " + seats.count());
	}

	public void refreshSessions() {

		logd("[refreshSessions]");
		deleteAllValues(Sessions.CONTENT_URI);
		Enumerable<OEntity> sessions = mConsumer.getEntities(Sessions.TABLE_NAME).execute();
		for (OEntity session : sessions) {
			ContentValues cv = OEntityToContentValue(session);
			insertValues(Sessions.CONTENT_URI, cv);
		}
		logd("[refreshSessions] sessions was inserted: " + sessions.count());
	}

	public void refreshSources() {

		logd("[refreshSources]");
		deleteAllValues(Sources.CONTENT_URI);
		Enumerable<OEntity> sources = getSourcesEntities();
		for (OEntity source : sources) {
			insertSource(source);
		}
		logd("[refreshSources] sources was inserted: " + sources.count());
	}

	public void refreshTopics() {

		logd("[refreshTopics] ");
		deleteAllValues(Topics.CONTENT_URI);
		Enumerable<OEntity> topics = getTopicsEntities();
		// TODO: how to delete related person-topics table
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
	}

	public void updateTopicPoints(final int topicId) {

		logd("[updateTopicPoints] topic id: " + topicId);
		Enumerable<OEntity> points = mConsumer.getEntities(Points.TABLE_NAME).expand(
				Topics.TABLE_NAME + "," + Persons.TABLE_NAME + "," + "Description").filter(
				"Topic/Id eq " + String.valueOf(topicId)).execute();
		logd("[updateTopicPoints] points entities count: " + points.count());
		
		String selection = Points.Columns.TOPIC_ID + "=?";
		String[] args = { String.valueOf(topicId) };
		deleteValues(Points.CONTENT_URI, selection, args);
		for (OEntity point : points) {
			updatePoint(point);
		}
		
		logd("[updateTopicPoints] all points was inserted");
	}

	private void deleteAllValues(final Uri uri) {
		deleteValues(uri, "1", null);
	}

	private void deleteValues(final Uri uri, final String selection, final String[] args) {
		operations.add(ContentProviderOperation.newDelete(uri).withSelection(selection, args).build());
	}

	private Enumerable<OEntity> getAttachmentsEntities() {
		return mConsumer.getEntities(Attachments.TABLE_NAME).expand(
				Points.TABLE_NAME + "," + Discussions.TABLE_NAME).filter(
				"ArgPoint/Id ne null or Discussion/Id ne null").execute();
	}

	private Enumerable<OEntity> getAttachmentsEntitiesFromPerson(final String personSelection,
			final String discussionSelection) {
		return mConsumer.getEntities(Attachments.TABLE_NAME).expand(
				Points.TABLE_NAME + "," + Discussions.TABLE_NAME).filter(
				"(ArgPoint/Id ne null and (" + personSelection + ")) or (" + discussionSelection + ")")
				.execute();
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
	
	
	
	
	//check expand tables Poins and Person
	private Enumerable<OEntity> getCommentsPersonEntryEntities(){
		return mConsumer.getEntities(CommentsPersonReadEntry.TABLE_NAME)
				.expand(Persons.TABLE_NAME + "," + Comments.TABLE_NAME).execute();
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
	
	
	
	//check expand tables Poins and Person
	private Enumerable<OEntity> getFilteredCommentsPersonEntryEntities(){
		return mConsumer.getEntities(CommentsPersonReadEntry.TABLE_NAME)
				.expand(Persons.TABLE_NAME + "," + Comments.TABLE_NAME).filter(
						"Person/Id ne null and Comment/Id ne null").execute();
	}
	
	

	private Enumerable<OEntity> getCommentsEntitiesFromPerson(final String selection) {

		return mConsumer.getEntities(Comments.TABLE_NAME)
				.expand(Points.TABLE_NAME + "," + Persons.TABLE_NAME).filter(
						"ArgPoint/Id ne null and (" + selection + ")").execute();
	}

	private Enumerable<OEntity> getPointsEntities() {

		return mConsumer.getEntities(Points.TABLE_NAME).expand(Topics.TABLE_NAME + "," + Persons.TABLE_NAME)
				.filter("Topic/Id ne null and Person/Id ne null").execute();
	}

	private Enumerable<OEntity> getPointsEntitiesFromPerson(final String selection) {

		return mConsumer.getEntities(Points.TABLE_NAME).expand(Topics.TABLE_NAME + "," + Persons.TABLE_NAME)
				.filter("Topic/Id ne null and ( " + selection + ")").execute();
	}

	private Enumerable<OEntity> getPersonsEntities(final int sessionId) {

		return mConsumer.getEntities(Persons.TABLE_NAME).filter(
				Sessions.TABLE_NAME + "/Id eq " + String.valueOf(sessionId)).execute();
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

	private void insertAttachment(final OEntity attachment) {

		OEntity point = attachment.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (point != null) {
			ContentValues cv = OEntityToContentValue(attachment);
			cv.put(Attachments.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
			
			//cv.put(Attachments.Columns.ISNEW,1);
			
			insertValues(Attachments.CONTENT_URI, cv);
		} else {
			OEntity discussion = attachment.getLink(Discussions.TABLE_NAME, ORelatedEntityLinkInline.class)
					.getRelatedEntity();
			if (discussion != null) {
				ContentValues cv = OEntityToContentValue(attachment);
				cv.put(Attachments.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
				
				//cv.put(Attachments.Columns.ISNEW,1);
				
				insertValues(Attachments.CONTENT_URI, cv);
			} else {
				sanitizeEntity(attachment, Attachments.TABLE_NAME, Attachments.Columns.ID);
			}
		}
	}

	private void insertComment(final OEntity comment) {
		OEntity point = comment.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		OEntity person = comment.getLink(Persons.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if ((point == null) || (person == null)) {
			sanitizeEntity(comment, Comments.TABLE_NAME, Comments.Columns.ID);
		} else {
			ContentValues cv = OEntityToContentValue(comment);
			cv.put(Comments.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
			cv.put(Comments.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
			
			Log.i("Disc insertComment","ContentValues: "+String.valueOf(cv.toString()));
			
			insertValues(Comments.CONTENT_URI, cv);
		}
	}

	
	
	private void insertCommentPersonEntry(final OEntity commentpersonEntity){
		
		OEntity comment=commentpersonEntity.getLink(Comments.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		OEntity person=commentpersonEntity.getLink(Persons.TABLE_NAME,ORelatedEntityLinkInline.class).getRelatedEntity();
		
		//Log.i("Disc","comment:"+String.valueOf(comment)+" person:"+String.valueOf(person));
		
		if(comment==null || person==null){
			sanitizeEntity(commentpersonEntity, CommentsPersonReadEntry.TABLE_NAME, CommentsPersonReadEntry.Columns.ID);
		}else{
			ContentValues cv=OEntityToContentValue(commentpersonEntity);
			cv.put(CommentsPersonReadEntry.Columns.COMMENT_ID, getAsInt(comment,Comments.Columns.ID));
			cv.put(CommentsPersonReadEntry.Columns.PERSON_ID,getAsInt(person, Persons.Columns.ID));
			
			Log.i("Disc","insertCommentPersonEntry: "+cv.toString());
			
			insertValues(CommentsPersonReadEntry.CONTENT_URI, cv);
		}
	}
	
	
	
	private void insertDescription(final OEntity description) {

		OEntity point = description.getLink(Points.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (point != null) {
			ContentValues cv = OEntityToContentValue(description);
			cv.put(Descriptions.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
			insertValues(Descriptions.CONTENT_URI, cv);
		} else {
			OEntity discussion = description.getLink(Discussions.TABLE_NAME, ORelatedEntityLinkInline.class)
					.getRelatedEntity();
			if (discussion != null) {
				ContentValues cv = OEntityToContentValue(description);
				cv.put(Descriptions.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
				insertValues(Descriptions.CONTENT_URI, cv);
			} else {
				sanitizeEntity(description, Descriptions.TABLE_NAME, Descriptions.Columns.ID);
			}
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
			insertValues(Persons.buildTopicUri(1231231), cv);
		}
	}

	private void insertPoint(final OEntity point) {

		OEntity topic = point.getLink(Topics.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		OEntity person = point.getLink(Persons.TABLE_NAME, ORelatedEntityLinkInline.class).getRelatedEntity();
		ContentValues cv = OEntityToContentValue(point);
		if ((topic != null) && (person != null)) {
			cv.put(Points.Columns.TOPIC_ID, getAsInt(topic, Topics.Columns.ID));
			cv.put(Points.Columns.PERSON_ID, getAsInt(person, Persons.Columns.ID));
			
			cv.put(Points.Columns.ISNEW,1);
			
			Log.i("Disc","NEW POINT CV:"+String.valueOf(cv));
			
			insertValues(Points.CONTENT_URI, cv);
		} else {
			sanitizeEntity(point, Points.TABLE_NAME, Points.Columns.ID);
		}
	}

	private void insertPointDescription(final OEntity description, final OEntity point) {

		ContentValues cv = OEntityToContentValue(description);
		cv.put(Descriptions.Columns.POINT_ID, getAsInt(point, Points.Columns.ID));
		insertValues(Descriptions.CONTENT_URI, cv);
	}

	private void insertSource(final OEntity source) {

		OEntity description = source.getLink(Descriptions.TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (description != null) {
			ContentValues cv = OEntityToContentValue(source);
			cv.put(Sources.Columns.DESCRIPTION_ID, getAsInt(description, Sources.Columns.ID));
			insertValues(Sources.CONTENT_URI, cv);
		} else {
			sanitizeEntity(source, Sources.TABLE_NAME, Sources.Columns.ID);
		}
	}

	private void insertTopic(final OEntity topic) {

		OEntity discussion = topic.getLink(Topics.Columns.DISCUSSION_ID, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (discussion != null) {
			ContentValues cv = OEntityToContentValue(topic);
			cv.put(Topics.Columns.DISCUSSION_ID, getAsInt(discussion, Discussions.Columns.ID));
			
			//Log.i("Disc insertTopic ContentValues",String.valueOf(cv));
			Log.i("Disc insertTopic topic",String.valueOf(topic));
			
			
			insertValues(Topics.CONTENT_URI, cv);
		} else {
			sanitizeEntity(topic, Topics.TABLE_NAME, Topics.Columns.ID);
		}
	}

	private void insertValues(final Uri uri, final ContentValues cv) {

		logd("[insertValues] values: " + cv.toString());
		operations.add(ContentProviderOperation.newInsert(uri).withValues(cv).build());
	}

	private void updatePoint(final OEntity point) {

		insertPoint(point);
		OEntity description = point.getLink(Descriptions.SERVER_TABLE_NAME, ORelatedEntityLinkInline.class)
				.getRelatedEntity();
		if (description != null) {
			insertPointDescription(description, point);
			updateSourcesFromDescription(description);
		}
		int pointId = getAsInt(point, Points.Columns.ID);
		updatePointComments(pointId);
		updatePointAttachments(pointId);
	}

	private void updatePointAttachments(final int pointId) {

		Enumerable<OEntity> attachments = getAttachmentsEntities(pointId);
		logd("[updatePointAttachments] entities count: " + attachments.count());
		String where = Attachments.Columns.POINT_ID + "=" + pointId;
		deleteValues(Attachments.CONTENT_URI, where, null);
		for (OEntity attachment : attachments) {
			insertAttachment(attachment);
		}
	}

	private void updatePointComments(final int pointId) {

		Enumerable<OEntity> comments = getCommentsEntities(pointId);
		logd("[updatePointComments] comment entities count: " + comments.count());
		String selection = Comments.Columns.POINT_ID + "=?";
		String[] args = new String[] { String.valueOf(pointId) };
		
		Log.i("Disc point comments args",String.valueOf(args));
		
		deleteValues(Comments.CONTENT_URI, selection, args);
		for (OEntity comment : comments) {
			insertComment(comment);
		}
		logd("[updatePointComments] all comments was inserted");
	}

	private void updateSourcesFromDescription(final OEntity description) {

		int descriptionId = getAsInt(description, Descriptions.Columns.ID);
		Enumerable<OEntity> sources = getSourcesEntities(descriptionId);
		logd("[updateSourcesFromDescription] entities count: " + sources.count());
		String where = Sources.Columns.DESCRIPTION_ID + "=" + descriptionId;
		deleteValues(Sources.CONTENT_URI, where, null);
		for (OEntity source : sources) {
			insertSource(source);
		}
		logd("[updateSourcesFromDescription] all sources was inserted");
	}
}
