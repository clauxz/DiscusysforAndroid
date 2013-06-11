package jp.ac.tohoku.qse.takahashi.discussions.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ArgPointChanged implements Parcelable {

	public static final Parcelable.Creator<ArgPointChanged> CREATOR = new Parcelable.Creator<ArgPointChanged>() {

		@Override
		public ArgPointChanged createFromParcel(final Parcel in) {

			return new ArgPointChanged(in);
		}

		@Override
		public ArgPointChanged[] newArray(final int size) {

			return new ArgPointChanged[size];
		}
	};
	private int eventType;
	private int pointId;
	private int topicId;

	public ArgPointChanged() {

		eventType = Byte.MIN_VALUE;
		pointId = Integer.MIN_VALUE;
		topicId = Integer.MIN_VALUE;
	}

	private ArgPointChanged(final Parcel in) {

		eventType = in.readByte();
		pointId = in.readInt();
		topicId = in.readInt();
	}

	@Override
	public int describeContents() {

		return 0;
	}

	public int getEventType() {

		return eventType;
	}

	public int getPointId() {

		return pointId;
	}

	public int getTopicId() {

		return topicId;
	}

	public void setEventType(final int eventType) {

		this.eventType = eventType;
	}

	public void setPointId(final int pointId) {

		this.pointId = pointId;
	}

	public void setTopicId(final int topicId) {

		this.topicId = topicId;
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {

		out.writeInt(eventType);
		out.writeInt(pointId);
		out.writeInt(topicId);
	}
}
