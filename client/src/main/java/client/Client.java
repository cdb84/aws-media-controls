package client;

import com.amazonaws.regions.Regions;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;

public class Client{

    public static void main(String [] args) throws Exception{
        String bucketName = null;
        String exec = null;
        if (args.length <= 0){
            throw new IllegalArgumentException();
        } 
        if (args.length == 1){
            bucketName = args[0];
        }
        if (args.length == 2){
            exec = args[1];
        }
        System.err.println("Initializing handler for "+bucketName+" executing "+exec);
        Handler h = new Handler(bucketName, Regions.US_EAST_2);
        
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    }
}