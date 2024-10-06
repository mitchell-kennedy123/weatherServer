package common;

public interface NetworkNode {

    public static final int DEFAULT_PORT = 4567;
    public static final String DEFAULT_SERVER_ADDRESS = "localhost";

	/**
	 * @return The integer identifier that this node receives and sends messages with
	 */
	public String getServerAddress();

  /**
   * @return The port that this node receives and sends messages with
   */
  public int getPort();

  public LamportClock getLamportClock();

  /**
   * Start the node, performs any needed activities to make the node ready
   * 
   * @return True of the node started up correctly, false if there was a problem
   */
  public boolean startup();

    /**
     * Shuts down the server gracefully
     *
     * @return True of the node shut down correctly, false if there was a problem
     */
  public boolean shutdown();

  /**
   * @return True if node is in the running state, False if it has terminated
   */
  public boolean isRunning();
	
}