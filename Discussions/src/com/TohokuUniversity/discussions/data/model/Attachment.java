package com.TohokuUniversity.discussions.data.model;

import com.TohokuUniversity.discussions.data.provider.DiscussionsContract.Attachments;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

public class Attachment implements Parcelable {

	public static final Parcelable.Creator<Attachment> CREATOR = new Parcelable.Creator<Attachment>() {

		@Override
		public Attachment createFromParcel(final Parcel in) {

			return new Attachment(in);
		}

		@Override
		public Attachment[] newArray(final int size) {

			return new Attachment[size];
		}
	};
	private int attachmentId;
	private int discussionId;
	private int format;
	private String link;
	private String name;
	private int personId;
	private int pointId;
	private String title;
	private String videoEmbedURL;
	private String videoLinkURL;
	private String videoThumbURL;
	private int orderNumber;

	public Attachment() {

		attachmentId = Integer.MIN_VALUE;
		discussionId = Integer.MIN_VALUE;
		format = Integer.MIN_VALUE;
		link = "";
		name = "";
		personId = Integer.MIN_VALUE;
		pointId = Integer.MIN_VALUE;
		title = "";
		videoEmbedURL = "";
		videoLinkURL = "";
		videoThumbURL = "";
		orderNumber = 0;
	}

	private Attachment(final Parcel in) {

		attachmentId = in.readInt();
		discussionId = in.readInt();
		format = in.readInt();
		link = in.readString();
		name = in.readString();
		personId = in.readInt();
		pointId = in.readInt();
		title = in.readString();
		videoEmbedURL = in.readString();
		videoLinkURL = in.readString();
		videoThumbURL = in.readString();
		orderNumber = in.readInt();
	}

	@Override
	public int describeContents() {

		return 0;
	}

	public int getAttachmentId() {

		return attachmentId;
	}

	public int getDiscussionId() {

		return discussionId;
	}

	public int getFormat() {

		return format;
	}

	public String getLink() {

		return link;
	}

	public String getName() {

		return name;
	}

	public int getPersonId() {

		return personId;
	}

	public int getPointId() {

		return pointId;
	}

	public String getTitle() {

		return title;
	}

	public String getVideoEmbedURL() {

		return videoEmbedURL;
	}

	public String getVideoLinkURL() {

		return videoLinkURL;
	}

	public String getVideoThumbURL() {

		return videoThumbURL;
	}

	public int getOrderNumber() {

		return orderNumber;
	}

	public void setAttachmentId(final int attachmentId) {

		this.attachmentId = attachmentId;
	}

	public void setDiscussionId(final int discussionId) {

		this.discussionId = discussionId;
	}

	public void setFormat(final int format) {

		this.format = format;
	}

	public void setLink(final String link) {

		this.link = link;
	}

	public void setName(final String name) {

		this.name = name;
	}

	public void setPersonId(final int personId) {

		this.personId = personId;
	}

	public void setPointId(final int pointId) {

		this.pointId = pointId;
	}

	public void setTitle(final String title) {

		this.title = title;
	}

	public void setVideoEmbedURL(final String videoEmbedURL) {

		this.videoEmbedURL = videoEmbedURL;
	}

	public void setVideoLinkURL(final String videoLinkURL) {

		this.videoLinkURL = videoLinkURL;
	}

	public void setVideoThumbURL(final String videoThumbURL) {

		this.videoThumbURL = videoThumbURL;
	}

	public void setOrderNumber(final int orderNumber) {

		this.orderNumber = orderNumber;
	}

	public ContentValues toContentValues() {

		ContentValues cv = new ContentValues();
		cv.put(Attachments.Columns.ID, attachmentId);
		cv.put(Attachments.Columns.DISCUSSION_ID, discussionId);
		cv.put(Attachments.Columns.FORMAT, format);
		cv.put(Attachments.Columns.LINK, link);
		cv.put(Attachments.Columns.NAME, name);
		cv.put(Attachments.Columns.PERSON_ID, personId);
		cv.put(Attachments.Columns.POINT_ID, pointId);
		cv.put(Attachments.Columns.TITLE, title);
		cv.put(Attachments.Columns.VIDEO_EMBED_URL, videoEmbedURL);
		cv.put(Attachments.Columns.VIDEO_LINK_URL, videoLinkURL);
		cv.put(Attachments.Columns.VIDEO_THUMB_URL, videoThumbURL);
		cv.put(Attachments.Columns.ORDER_NUMBER, orderNumber);
		return cv;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {

		out.writeInt(attachmentId);
		out.writeInt(discussionId);
		out.writeInt(format);
		out.writeString(link);
		out.writeString(name);
		out.writeInt(personId);
		out.writeInt(pointId);
		out.writeString(title);
		out.writeString(videoEmbedURL);
		out.writeString(videoLinkURL);
		out.writeString(videoThumbURL);
		out.writeInt(orderNumber);
	}
}
