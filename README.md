# aws-media-controls
A Java program that brings a GUI to Amazon's S3, allowing you to graphically comb through whatever media you have stored

![Screenshot A](https://raw.githubusercontent.com/cdb84/aws-media-controls/main/Console1.png)

## How to build

In `client` directory, use:

`mvn package`

## How to run

In `client` directory:

`java -jar target/client-1.0-SNAPSHOT.jar <bucket-name> <media player executable> --gui`

The `--gui` option specified whether the console runs in your terminal, or as a new Swing window. The latter is helpful if you just want to click-to-run the jar file.

Alternatively, if the jar is marked as executable, you can run it from the desktop environment. It will ask you to input the bucket name and executable.
