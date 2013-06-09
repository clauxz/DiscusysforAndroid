package com.TohokuUniversity.discussions.photon;

public class DiscussionUser {

	private int mActorNumber;
	private int mUserId;
	private String mUserName;

	public DiscussionUser() {

		// default values
		mActorNumber = Integer.MIN_VALUE;
		mUserId = Integer.MIN_VALUE;
		mUserName = "Unknown user";
	}

	public int getActorNumber() {

		return mActorNumber;
	}

	public int getUserId() {

		return mUserId;
	}

	public String getUserName() {

		return mUserName;
	}

	public void setActorNumber(final int actorNumber) {

		this.mActorNumber = actorNumber;
	}

	public void setUserId(final int userId) {

		this.mUserId = userId;
	}

	public void setUserName(final String userName) {

		this.mUserName = userName;
	}
}
