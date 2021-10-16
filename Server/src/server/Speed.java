package server;

import java.text.DecimalFormat;
import static server.InformationTransferFile.*;

public class Speed extends Thread {
    private final int ID;
    private boolean isRun;

    public Speed(int ID) {
        this.ID = ID;
        this.isRun = false;
    }

    @Override
    public void run() {
        isRun = true;
        long startTransfer = System.currentTimeMillis();
        long previousTime = startTransfer;
        int previousCountBytes = 0;

        while (!Thread.currentThread().isInterrupted() && isRun) {
            synchronized (this) {
                try {
                    this.wait(3000);
                } catch (InterruptedException e) {
                    System.out.println("Error");
                    e.printStackTrace();
                }
            }

            long currentTime = System.currentTimeMillis();
            double timeout = ((double) currentTime - startTransfer) / 1000.0;
            DecimalFormat decimalFormat = new DecimalFormat("####.##");
            double allBytes = countBytes(ID);
            double countBytes = countBytes(ID) - previousCountBytes;
            double time = (double) (currentTime - previousTime) / 1000;

            System.out.println("Average speed client " + ID + " " + decimalFormat.format(allBytes / timeout) +
                            " byte/sec");
            System.out.println("Instantaneous speed client " + ID + " " + decimalFormat.format(countBytes / time) +
                            " byte/sec");

            previousCountBytes = countBytes(ID);
            previousTime = currentTime;
        }
    }

    public void finish() {
        if (isRun) {
            isRun = false;
            synchronized (this) {
                this.notifyAll();
            }
        }
    }
}
