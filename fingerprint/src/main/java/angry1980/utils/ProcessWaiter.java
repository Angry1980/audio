package angry1980.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class ProcessWaiter {

    private static final int BUFFER_SIZE = 8192;
    private static final int OUTPUT_STREAM_INDEX = 0;
    private static final int ERROR_STREAM_INDEX = 1;

    public static Result waitFor(Process process, int timeout) throws IOException, InterruptedException {
        return waitFor(process, timeout, true, true, true);
    }

    public static Result waitFor(Process process, int timeout, boolean terminateProcess,
            boolean useErrorStream, boolean useInputStream) throws IOException, InterruptedException {

        StreamPair[] streamPairs = new StreamPair[] {
                new StreamPair(process.getInputStream(), new ByteArrayOutputStream(BUFFER_SIZE), useInputStream),
                new StreamPair(process.getErrorStream(), new ByteArrayOutputStream(BUFFER_SIZE), useErrorStream)};

        boolean finished = false;
        byte[] buffer = new byte[BUFFER_SIZE];

        try {
            for (long startTime = System.nanoTime(), timeoutNS = TimeUnit.MILLISECONDS.toNanos(timeout);;
                 timeoutNS = TimeUnit.MILLISECONDS.toNanos(timeout) - (System.nanoTime() - startTime)) {

                for (StreamPair streamPair : streamPairs) {
                    while (streamPair.useStream && streamPair.inputStream.available() > 0) {
                        int count = streamPair.inputStream.read(buffer);

                        if (count > 0) {
                            streamPair.outputStream.write(buffer, 0, count);
                        }
                    }
                }

                if (timeoutNS < 0) {
                    return new Result(streamPairs[OUTPUT_STREAM_INDEX].outputStream, streamPairs[ERROR_STREAM_INDEX].outputStream);
                }

                try {
                    int code = process.exitValue();
                    finished = true;
                    return new Result(code,
                            streamPairs[OUTPUT_STREAM_INDEX].outputStream,
                            streamPairs[ERROR_STREAM_INDEX].outputStream);
                } catch(IllegalThreadStateException ex) {
                    if (timeoutNS > 0) {
                        Thread.sleep(Math.min(TimeUnit.NANOSECONDS.toMillis(timeoutNS) + 1, 100));
                    }
                }
            }
        } finally {
            for (StreamPair streamPair : streamPairs) {
                streamPair.inputStream.close();
            }
            if (terminateProcess && !finished) {
                process.destroy();
            }
        }
    }

    public static class Result {
        private int code;
        private boolean timeout;
        private ByteArrayOutputStream outputStream;
        private ByteArrayOutputStream errorStream;

        private Result(int code, ByteArrayOutputStream outputStream, ByteArrayOutputStream errorStream) {
            this.code = code;
            this.outputStream = outputStream;
            this.errorStream = errorStream;
        }

        private Result(ByteArrayOutputStream outputStream, ByteArrayOutputStream errorStream) {
            this.code = -1;
            this.timeout = true;
            this.outputStream = outputStream;
            this.errorStream = errorStream;
        }

        public boolean isTimeout() {
            return timeout;
        }

        public int getCode() {
            return code;
        }

        public ByteArrayOutputStream getOutputStream() {
            return outputStream;
        }

        public ByteArrayOutputStream getErrorStream() {
            return errorStream;
        }
    }

    private static class StreamPair {
        InputStream inputStream;
        ByteArrayOutputStream outputStream;
        boolean useStream;

        StreamPair(InputStream inputStream, ByteArrayOutputStream outputStream, boolean useStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
            this.useStream = useStream;
        }
    }
}
