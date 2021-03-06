package client;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.net.URL;
import java.util.List;
import java.util.Stack;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Date;

public class Handler {
	private String bucketName;
	private final AmazonS3 s3Instance;
	private ListObjectsV2Result cache;
	private DirectoryTreeNode<String> bucketStructure;

	Handler(String bucketName, AmazonS3 s3Instance) {
		this.bucketName = bucketName;
		this.s3Instance = s3Instance;
		refreshCache();
	}

	Handler(String bucketName, Regions region) {
		this.bucketName = bucketName;
		this.s3Instance = AmazonS3ClientBuilder.standard().withRegion(region).build();
		refreshCache();
	}

	/**
	 * Use this after making any updates to the S3 bucket in program, or if it's
	 * been a while since the last refresh.
	 */
	private void refreshCache() {
		this.cache = s3Instance.listObjectsV2(this.bucketName);
		refreshStructure();
	}

	private void refreshStructure() {
		ArrayList<Stack<String>> pathStacks = new ArrayList<Stack<String>>();
		this.bucketStructure = new DirectoryTreeNode<String>();
		for (S3ObjectSummary object : this.returnListOfAllObjectSummaries()) {
			String[] pathAsArray = object.getKey().split("/");
			Stack<String> pathAsStack = new Stack<String>();
			for (int i = pathAsArray.length - 1; i >= 0; i--) {
				String item = pathAsArray[i];
				pathAsStack.push(item);
			}
			pathStacks.add(pathAsStack);
		}

		for (Stack<String> pathAsStack : pathStacks) {
			bucketStructure.addPath(pathAsStack);
		}
	}

	public List<S3ObjectSummary> returnListOfAllObjectSummaries() {
		return this.cache.getObjectSummaries();
	}

	public List<DirectoryTreeNode<String>> returnEverythingAbove(String value) throws ValueNotFoundError {
		DirectoryTreeNode<String> found = bucketStructure.findValue(value);
		return found.getParents();
	}

	public List<DirectoryTreeNode<String>> returnEverythingUnder(String value) throws ValueNotFoundError {
		DirectoryTreeNode<String> found = bucketStructure.findValue(value);
		return found.getChildren();
	}

	public List<String> returnListOfAllTopLevelFolders() {
		List<String> res = new ArrayList<String>();
		for (S3ObjectSummary object : this.returnListOfAllTopLevelFolderSummaries()) {
			res.add(object.getKey());
		}
		return res;
	}

	public List<S3ObjectSummary> returnListOfAllTopLevelFolderSummaries() {
		List<S3ObjectSummary> res = new ArrayList<S3ObjectSummary>();

		for (S3ObjectSummary object : this.returnListOfAllObjectSummaries()) {
			if (returnDelimitedPath(object).length == 1) {
				res.add(object);
			}
		}

		return res;
	}

	public URL generatePresignedUrlFromKey(String key) {
		Date expiration = new java.util.Date();
		long expTimeMillis = expiration.getTime();
		expTimeMillis += (1000 * 60 * 60) * 6;
		expiration.setTime(expTimeMillis);
		return this.generatePresignedUrlFromKey(key, expiration);
	}

	public URL generatePresignedUrlFromKey(String key, Date expiration) {
		try {
			GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(this.bucketName, key)
					.withMethod(HttpMethod.GET).withExpiration(expiration);
			return s3Instance.generatePresignedUrl(generatePresignedUrlRequest);
		} catch (AmazonServiceException e) {
			e.printStackTrace();
		} catch (SdkClientException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String[] returnListOfAllParentDirectories(S3ObjectSummary object) {
		String[] delimitedPath = returnDelimitedPath(object);
		if (delimitedPath.length > 1) {
			return Arrays.copyOfRange(delimitedPath, 0, delimitedPath.length - 1);
		} else {
			return new String[0];
		}
	}

	public static String[] returnDelimitedPath(S3ObjectSummary object) {
		String objKey = object.getKey();
		String[] res = objKey.split("/");
		return res;
	}
}
