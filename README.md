# aws-media-console
A Java program that brings a GUI to Amazon's S3, allowing you to graphically comb through whatever media you have stored and view it using a media player of your choosing.

![Screenshot A](https://raw.githubusercontent.com/cdb84/aws-media-controls/main/Console1.png)

## How to build

In `client` directory, use:

`mvn package`

## How to run

In `client` directory:

`java -jar target/client-1.0-Stable.jar <S3 bucket name> <path to media player executable> --gui`

The `--gui` option specifies whether the console runs in your terminal, or as a new Swing window.

Alternatively, if the jar is marked as executable, you can run it from the desktop environment. It will ask you to input the bucket name and executable. On Windows, it is recommended that you include the entire path to your media player, e.g. `C:\Program Files\VideoLAN\VLC\vlc.exe`.

You must have access to the bucket in question---either use a public bucket, or set up your AWS credentials using the AWS CLI configurator. 