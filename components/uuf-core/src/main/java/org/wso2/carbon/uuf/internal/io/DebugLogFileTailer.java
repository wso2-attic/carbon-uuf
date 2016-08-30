package org.wso2.carbon.uuf.internal.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.wso2.carbon.uuf.spi.DebugLogTailerListner;
import org.wso2.carbon.uuf.exception.UUFException;

public class DebugLogFileTailer extends Thread {

    private long sampleInterval = 5000;
    private File logfile;
    private boolean startAtBeginning = false;
    private boolean tailing = false;
    private Set listeners = new HashSet();

    public DebugLogFileTailer(File file, long sampleInterval, boolean startAtBeginning) {
        this.logfile = file;
        this.sampleInterval = sampleInterval;
        this.startAtBeginning = startAtBeginning;
    }

    public void addLogFileTailerListener(DebugLogTailerListner l) {
        this.listeners.add(l);
    }

    private void fireNewLogFileLine(String line) {
        for (Iterator i = this.listeners.iterator(); i.hasNext(); ) {
            DebugLogTailerListner l = (DebugLogTailerListner) i.next();
            l.hasNewLogLine(line);
        }
    }

    @Override
    public void run() {
        long filePointer = 0;

        if (this.startAtBeginning) {
            filePointer = 0;
        } else {
            filePointer = this.logfile.length();
        }

            this.tailing = true;
        try {
            RandomAccessFile file = new RandomAccessFile(logfile, "r");
            while (this.tailing) {
                    long fileLength = this.logfile.length();
                    if (fileLength < filePointer) {
                        //log file deleted or rotated, reopen the file and set pointer to 0
                        file = new RandomAccessFile(logfile, "r");
                        filePointer = 0;
                    } else {
                        file.seek(filePointer);
                        String line = file.readLine();
                        while (line != null) {
                            this.fireNewLogFileLine(line);
                            line = file.readLine();
                        }
                            filePointer = file.getFilePointer();
                    }

                try {
                    sleep(this.sampleInterval);
                } catch (InterruptedException e) {
                    throw new UUFException("Thread has been interrupted. ", e);
                }
            }

            file.close();
        } catch (IOException e) {
            throw new UUFException("Error while accessing the UUF debug log. ", e);
        }
    }
}