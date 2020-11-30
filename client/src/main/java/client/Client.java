package client;

import java.io.IOException;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class Client{

    public static void main(String [] args) throws Exception{
        String bucketName = null;
        String exec = null;
        if (args.length <= 0){
            throw new IllegalArgumentException();
        } 
        if (args.length >= 1){
            bucketName = args[0];
        }
        if (args.length == 2){
            exec = args[1];
        }else{
            exec = "vlc"; // probably best default in mind
        }
        System.err.println("Initializing handler for "+bucketName+" executing "+exec);
        Handler h = new Handler(bucketName, AmazonS3ClientBuilder.standard().withRegion(Regions.US_EAST_2).build());
        
        DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();

        Terminal terminal = null;
        try{
            terminal = defaultTerminalFactory.createTerminal();
        } catch (IOException e){
            e.printStackTrace();
        } finally {
            if (terminal != null){
                try{
                    terminal.close();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }
}