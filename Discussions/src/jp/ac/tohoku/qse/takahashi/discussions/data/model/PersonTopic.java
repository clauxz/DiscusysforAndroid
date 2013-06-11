package jp.ac.tohoku.qse.takahashi.discussions.data.model;

import jp.ac.tohoku.qse.takahashi.discussions.data.provider.DiscussionsContract.PersonsTopics;

import android.content.ContentValues;

public class PersonTopic implements Value {

	private final int personId;
	private final int topicId;

	public PersonTopic(final int personId, final int topicId) {

		super();
		this.personId = personId;
		this.topicId = topicId;
	}

	@Override
	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(PersonsTopics.Columns.PERSON_ID, personId);
		cv.put(PersonsTopics.Columns.TOPIC_ID, topicId);
		return cv;
	}

	@Override
	public String toMyString() {

		StringBuilder sb = new StringBuilder();
		sb.append(PersonsTopics.Columns.PERSON_ID).append(':').append(personId).append('\n');
		sb.append(PersonsTopics.Columns.TOPIC_ID).append(':').append(topicId).append('\n');
		return sb.toString();
	}
}
