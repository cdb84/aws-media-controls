package client;

import com.amazonaws.regions.Regions;

public class Client{

    public static void Main(String [] args){
        String bucketName = null;
        String exec;
        if (args.length <= 1){
            throw new Exception();
            System.exit(1);
        } 
        if (args.length == 2){
            bucketName = args[1];
        }
        if (args.length == 3){
            exec = args[2];
        }
		Handler h = new Handler(bucketName, Regions.US_EAST_2);
    }
}