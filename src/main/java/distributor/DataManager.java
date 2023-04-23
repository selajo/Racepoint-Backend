package distributor;

import distributor.observermessages.ObserverMessage;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataManager extends Thread {

    private static DataManager instance = new DataManager();
    private LinkedBlockingDeque<ObserverMessage> messages;
    private final Logger logger = Logger.getLogger("DataManager");
    private boolean disconnected = false;

    private DataManager() {
    }

    public static DataManager getInstance() {
        return instance;
    }

    void init() {
        logger.log(Level.INFO, "Init DataManager");
        messages = new LinkedBlockingDeque<>();
    }

    /**
     * Receives a message from DataReceiver and queues it for application
     *
     * @param message the message from node, received by DataReceiver
     */
    void receiveMessage(ObserverMessage message) {
        try {
            logger.log(Level.FINE, "Received message with type " + message.getType());
            messages.put(message);
        } catch (InterruptedException e) {
            logger.log(Level.WARNING, "receiveMessage was interrupted: " + e.getLocalizedMessage(), e);
        }
    }

    /**
     * applies a message received from node
     */
    private void apply(ObserverMessage message) {
        ControlManager.getInstance().getCurrentMatch().apply(message);
    }

    @Override
    public void run() {
        logger.log(Level.INFO, "DataManager started");
        while (!isInterrupted()) {
            try {
                try {
                    ObserverMessage message = messages.takeFirst();
                    apply(message);
                } catch (NullPointerException e) {
                    if (!disconnected) {
                        logger.log(Level.WARNING, "NullPointerException in DataManager: " + e.getLocalizedMessage(), e);
                    } else {
                        logger.log(Level.INFO, "DataManager finished after disconnect");
                        return;
                    }
                }
            } catch (InterruptedException e) {
                if (!disconnected) {
                    logger.log(Level.WARNING, "DataManager interrupted: " + e.getLocalizedMessage(), e);
                } else {
                    logger.log(Level.INFO, "DataManager finished after disconnect");
                    return;
                }
            }
        }
        logger.log(Level.INFO, "DataManager finished...");
    }

    /**
     * stops data manager
     */
    public void stopDataManager() {
        logger.log(Level.INFO, "Stopping Data Manager");
        disconnected = true;
        this.interrupt();
        messages = null;
        instance = new DataManager();
    }
}

