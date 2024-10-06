package common;

public class LamportClock {
  private int currentTime;

  public LamportClock() {
    this.currentTime = 0; // Initialize the clock at time 0
  }

  /**
   * Maintain the internal Lamport Clock when a message is received
   *
   * @param messageTimestamp a received Message
   * @return the current Lamport Clock Value
   */
  public int processEvent(int messageTimestamp) {
    // Assuming the message contains a timestamp to compare with the local clock
    currentTime = Math.max(currentTime, messageTimestamp) + 1;
    return currentTime;
  }

  /**
   * Update the internal Lamport Clock for events other than message receive
   *
   * @return the current Lamport Clock value
   */
  public int processEvent() {
    // For internal events not related to message passing
    currentTime += 1;
    return currentTime;
  }

  /**
   * Get the current Lamport Time
   *
   * @return the current Lamport clock value
   */
  public int getTime() {
    return currentTime;
  }

  /**
   * Synchronizes the clock to a specified time
   */
  public void synchroniseClock(int timestamp) {
    currentTime = timestamp;
  }
}