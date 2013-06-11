package jp.ac.tohoku.qse.takahashi.discussions.data.odata;


import android.content.ContentResolver;
import android.content.Context;
import android.util.Log;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.Filterable;

import org.odata4j.consumer.ODataClientRequest;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.behaviors.OClientBehavior;
import org.odata4j.core.OEntity;
import org.odata4j.format.FormatType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;
import org.odata4j.jersey.consumer.ODataJerseyConsumer.Builder;
import org.odata4j.jersey.consumer.behaviors.JerseyClientBehavior;

import java.util.Map;

import jp.ac.tohoku.qse.takahashi.discussions.ApplicationConstants;
import jp.ac.tohoku.qse.takahashi.discussions.data.PreferenceHelper;

public class BaseOdataClient {

	private static final String TAG = "BaseOdataClient";
	protected final ODataConsumer mConsumer;
	protected final ContentResolver mContentResolver;
	protected final Context mContext;

	/** Sets service root uri by default to japan server */
	public BaseOdataClient(final Context context) {

		// FIXME: check if network is accessible
		// FIXME catch 404 errors from HTTP RESPONSE
		mContext = context;
		String odataServerUrl = getOdataServerUrl();
		mConsumer = createOdataConsumer(odataServerUrl);
		if (ApplicationConstants.ODATA_DUMP_LOG) {
			ODataConsumer.dump.all(true);
		}
		mContentResolver = context.getContentResolver();
	}

	private static ODataJerseyConsumer createOdataConsumer(final String odataServerUrl) {

		Builder builder = ODataJerseyConsumer.newBuilder(odataServerUrl);
		builder.setFormatType(FormatType.JSON);
		builder.setClientBehaviors(TimeoutBehavior.reduceTimeout());
		return builder.build();
	}

	public void logServerMetaData() {

		ODataReportUtil.reportMetadata(mConsumer.getMetadata());
	}

	protected String getOdataServerUrl() {

		return PreferenceHelper.getOdataUrl(mContext);
	}

	protected void sanitizeEntity(final OEntity entity, final String tableName, final String idColumn) {

		Log.e(TAG, "One of related links (foreign key) is null for " + tableName + " , id:"
				+ getAsInt(entity, idColumn));
		if (ApplicationConstants.ODATA_SANITIZE) {
			mConsumer.deleteEntity(tableName, getAsInt(entity, idColumn)).execute();
		}
	}

	public static int getAsInt(final OEntity entity, final String valueColumn) {

		return (Integer) entity.getProperty(valueColumn).getValue();
	}

	private enum TimeoutBehavior implements JerseyClientBehavior {
		INSTANCE;

		@Override
		public ODataClientRequest transform(final ODataClientRequest request) {

			return request;
		}

		@Override
		public void modify(final ClientConfig clientConfig) {

			Map<String, Object> properties = clientConfig.getProperties();
			properties.put(ClientConfig.PROPERTY_CONNECT_TIMEOUT, 10 * 1000);
			properties.put(ClientConfig.PROPERTY_READ_TIMEOUT, 10 * 1000);
		}

		@Override
		public void modifyClientFilters(final Filterable filterable) {

		}

		@Override
		public void modifyWebResourceFilters(final Filterable filterable) {

		}

		public static OClientBehavior reduceTimeout() {

			return TimeoutBehavior.INSTANCE;
		}
	}
}
