package client;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.SdkClientException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.net.URL;
import java.util.List;

public class Handler {

	public static void main(String[] args) {
		String bucket_name = "big-media";

		System.out.format("Objects in S3 bucket %s:\n", bucket_name);
		final AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build();
		ListObjectsV2Result result = s3.listObjectsV2(bucket_name);
		List<S3ObjectSummary> objects = result.getObjectSummaries();
		// for (S3ObjectSummary os : objects) {
		// 	System.out.println("* " + os.getKey());
		// }

		try {
			java.util.Date expiration = new java.util.Date();
						long expTimeMillis = expiration.getTime();
						expTimeMillis += 1000 * 60 * 60;
						expiration.setTime(expTimeMillis);

						// Generate the presigned URL.
						System.out.println("Generating pre-signed URL.");
						GeneratePresignedUrlRequest generatePresignedUrlRequest =
										new GeneratePresignedUrlRequest(bucket_name, objects.get(1).getKey())
														.withMethod(HttpMethod.GET)
														.withExpiration(expiration);
						URL url = s3.generatePresignedUrl(generatePresignedUrlRequest);
						System.out.println(objects.get(1).getKey());
						System.out.println("Pre-Signed URL: " + url.toString());
		} catch (AmazonServiceException e){
			e.printStackTrace();
		} catch (SdkClientException e){
			e.printStackTrace();
		}

	}
}
