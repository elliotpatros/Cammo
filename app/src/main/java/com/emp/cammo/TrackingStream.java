package com.emp.cammo;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TrackingStream {
    //----------------------------------------------------------------------------------------------
    // member variables
    //----------------------------------------------------------------------------------------------
    private boolean mIsStreaming;
    private StreamingTask mStreamingTask = null;
    private int mStreamInterval = 50;

    private String mIpAddress;
    private int mPortNumber;

    //----------------------------------------------------------------------------------------------
    // public methods
    //----------------------------------------------------------------------------------------------
    public void startStream(final String ipAddress, final int portNumber) {
        // stop stream (if old one exists)
        stopStream();

        // update streaming data
        mIpAddress = ipAddress;
        mPortNumber = portNumber;

        // start new stream
        mStreamingTask = new StreamingTask();
        mIsStreaming = true;
        mStreamingTask.execute();
    }

    public void stopStream() {
        mIsStreaming = false;

        // wait for task to stop
        if (null != mStreamingTask) {
            try {
                mStreamingTask.get(mStreamInterval, TimeUnit.MILLISECONDS);
            } catch (ExecutionException | InterruptedException | TimeoutException e) {
                e.printStackTrace();
            } finally {
                mStreamingTask = null;
            }
        }
    }

    //----------------------------------------------------------------------------------------------
    // streaming task
    //----------------------------------------------------------------------------------------------
    private class StreamingTask extends AsyncTask<Void, Void, Void> {
        private DatagramSocket socket = null;
        private String errorMessage = "";

        private byte[] paddedByteMessage(final String address, final float value) {
            final byte[] packet = address.getBytes();
            final int nBytes = (packet.length + 3) & ~3;
            final ByteBuffer buffer = ByteBuffer.allocate(nBytes + 4);
            buffer.put(packet);
            buffer.putFloat(nBytes, value);
            return buffer.array();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // connect to server
                final InetAddress host = InetAddress.getByName(mIpAddress);

                // make socket
                socket = new DatagramSocket();

                // stream data
                while (mIsStreaming) {
                    // make packet
                    byte[] packet = paddedByteMessage("/face", 3.141f);

                    // send packet
                    socket.send(new DatagramPacket(packet, packet.length, host, mPortNumber));

                    Log.i("doInBackground", "streamed something");

                    // sleep until next time
                    Thread.sleep(mStreamInterval);
                }
            } catch (IOException | InterruptedException e) {
                errorMessage = e.getMessage();
                e.printStackTrace();
            } finally {
                // close connection
                if (null != socket) {
                    socket.close();
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("onPreExecute", "starting");
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.i("onPostExecute", "done streaming");
        }
    }

    //----------------------------------------------------------------------------------------------
    // convenience functions
    //----------------------------------------------------------------------------------------------
}
