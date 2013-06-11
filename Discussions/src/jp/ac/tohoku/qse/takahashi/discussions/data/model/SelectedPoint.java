package jp.ac.tohoku.qse.takahashi.discussions.data.model;

import android.os.Parcel;
import android.os.Parcelable;

public class SelectedPoint implements Parcelable {

	public static final Parcelable.Creator<SelectedPoint> CREATOR = new Parcelable.Creator<SelectedPoint>() {

		@Override
		public SelectedPoint createFromParcel(final Parcel in) {

			return new SelectedPoint(in);
		}

		@Override
		public SelectedPoint[] newArray(final int size) {

			return new SelectedPoint[size];
		}
	};
	private int mDiscussionId;
	private int mPersonId;
	private int mPointId;
	private int mTopicId;

	public SelectedPoint() {

		mDiscussionId = Integer.MIN_VALUE;
		mPersonId = Integer.MIN_VALUE;
		mPointId = Integer.MIN_VALUE;
		mTopicId = Integer.MIN_VALUE;
	}

	private SelectedPoint(final Parcel in) {

		mDiscussionId = in.readInt();
		mPersonId = in.readInt();
		mPointId = in.readInt();
		mTopicId = in.readInt();
	}

	@Override
	public int describeContents() {

		return 0;
	}

	public int getDiscussionId() {

		return mDiscussionId;
	}

	public int getPersonId() {

		return mPersonId;
	}

	public int getPointId() {

		return mPointId;
	}

	public int getTopicId() {

		return mTopicId;
	}

	public void setDiscussionId(final int discussionId) {

		mDiscussionId = discussionId;
	}

	public void setPersonId(final int personId) {

		mPersonId = personId;
	}

	public void setPointId(final int pointId) {

		mPointId = pointId;
	}

	public void setTopicId(final int topicId) {

		mTopicId = topicId;
	}

	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append("DiscussionId: ");
		sb.append(mDiscussionId);
		sb.append(", ");
		sb.append("PersonId: ");
		sb.append(mPersonId);
		sb.append(", ");
		sb.append("PointId: ");
		sb.append(mPointId);
		sb.append(", ");
		sb.append("TopicId: ");
		sb.append(mTopicId);
		sb.append(", ");
		return sb.toString();
	}

	@Override
	public void writeToParcel(final Parcel out, final int flags) {

		out.writeInt(mDiscussionId);
		out.writeInt(mPersonId);
		out.writeInt(mPointId);
		out.writeInt(mTopicId);
	}
}
