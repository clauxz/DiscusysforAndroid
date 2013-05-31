package com.slobodastudio.discussions.service;

import com.slobodastudio.discussions.utils.MyLog;

import android.net.Uri;
import android.os.Environment;
import android.util.Base64;

import com.nostra13.universalimageloader.utils.FileUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

public class FileDownloader {

	public final static String PREFIX_BASE64="data:image/jpeg;base64,";
	
	private static final String DIRECTORY = Environment.DIRECTORY_DOWNLOADS;

	public static File createFile(final String fileName) {

		// Create a path where we will place our picture in the user's
		// public pictures directory. Note that you should be careful about
		// what you place here, since the user often manages these files. For
		// pictures and other media owned by the application, consider
		// Context.getExternalMediaDir().
		File path = Environment.getExternalStoragePublicDirectory(DIRECTORY);
		if (path.exists() == false) {
			path.mkdirs();
		}
		File directory = new File(path, "Discussions");
		if (directory.exists() == false) {
			directory.mkdir();
		}
		return new File(directory, fileName);
	}

	private void deleteFile(final String fileName) {

		// Create a path where we will place our picture in the user's
		// public pictures directory and delete the file. If external
		// storage is not currently mounted this will fail.
		File path = Environment.getExternalStoragePublicDirectory(DIRECTORY);
		File file = new File(path, fileName);
		file.delete();
	}

	public static boolean hasFileDownloaded(final String fileName) {

		// Create a path where we will place our picture in the user's
		// public pictures directory and check if the file exists. If
		// external storage is not currently mounted this will think the
		// picture doesn't exist.
		File file = createFile(fileName);
		return file.exists();
	}

	public static void downloadFromUrlMultiThread(final String downloadUrl, final String fileName) {

		try {
			URL website = new URL(downloadUrl);
			ReadableByteChannel rbc = Channels.newChannel(website.openStream());
			File file = createFile(fileName);
			FileOutputStream fos = new FileOutputStream(file);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		}
		catch (IOException e) {
			MyLog.e("FileDownloader", "Failed to download file: " + downloadUrl, e);
		} catch (OutOfMemoryError e) {
			MyLog.e("FileDownloader", "Failed to download file: " + downloadUrl, new RuntimeException(e));
		}
	}

	public static void downloadFromUrl(final String downloadUrl, final String fileName) {

		// If previous compression wasn't needed or failed
		// Download and save original image
		try {
			URL website = new URL(downloadUrl);
			InputStream is = website.openStream();
			try {
				File file = createFile(fileName);
				OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
				try {
					FileUtils.copyStream(is, os);
				} finally {
					os.close();
				}
			} finally {
				is.close();
			}
		} catch (IOException e) {
			MyLog.e("FileDownloader", "Failed to download file: " + downloadUrl, e);
		} catch (OutOfMemoryError e) {
			MyLog.e("FileDownloader", "Failed to download file: " + downloadUrl, new RuntimeException(e));
		}
	}
	
	
	
	public static void saveFileFromBase64(final String url, final String fileName)
	{
		try
		{
			if(url!=null && url.length()!=0)
			{
				String url2=url.replace(PREFIX_BASE64, "");
				
				byte[] data=Base64.decode(url2, Base64.DEFAULT);
				ByteArrayInputStream is = new ByteArrayInputStream(data);
				try{
					File file = createFile(fileName);
					OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
					try{
						FileUtils.copyStream(is, os);
					}
					finally{
						os.close();
					}
				}finally{
					is.close();
				}
			}
			
		} catch (IOException e) {
			MyLog.e("FileDownloader", "Failed to save Base64 file: " + String.valueOf(url), e);
		} catch (OutOfMemoryError e) {
			MyLog.e("FileDownloader", "Failed to save Base64 file: " + String.valueOf(url), new RuntimeException(e));
		}
		
	}
	
}
