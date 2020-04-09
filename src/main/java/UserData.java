import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

/**
 * Data structure for holding user data.
 */
public class UserData implements Serializable {
    private String loginName;
    private String realName;
    private String password;

    private UUID uuid;
    private String ip;
    private Date lastRequestDate;
    private Date lastChangeDate;

    /**
     * Constructor.
     *
     * @param loginName The user login name
     * @param realName The real name of the user
     * @param password The password hash
     * @param ip The ip address of the user
     */
    public UserData(String loginName, String realName, String password, String ip) {
        this.loginName = loginName;
        this.realName = realName;
        this.password = password;
        this.ip = ip;
        uuid = UUID.randomUUID();
        lastChangeDate = new Date();
        lastRequestDate = new Date();
    }

    /**
     * Getter method for the login name.
     *
     * @return login name
     */
    public String getLoginName() {
        return loginName;
    }

    /**
     * Setter method for login name.
     *
     * @param loginName The new user login name
     */
    public void setLoginName(String loginName) {
        lastChangeDate = new Date();
        this.loginName = loginName;
    }

    /**
     * Getter method for real name.
     *
     * @return The user real name
     */
    public String getRealName() {
        return realName;
    }

    /**
     * Setter method for real name.
     *
     * @param realName The new user real name
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }

    /**
     * Getter method for user password hash.
     *
     * @return The user password hash
     */
    public String getPassword() {
        return password;
    }

    /**
     * Setter method for the user password hash.
     *
     * @param password The new user password hash
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Method for determining if a password is set.
     *
     * @return True if password is set, otherwise false
     */
    public boolean hasPassword() {
        return password != null;
    }

    /**
     * Getter method for the user UUID.
     *
     * @return The user UUID
     */
    public UUID getUUID() {
        return uuid;
    }

    /**
     * Getter method for the user IP address.
     *
     * @return The user IP address
     */
    public String getIp() {
        return ip;
    }

    /**
     * Method for updating the last request date.
     * When method is called, the current date is used.
     */
    public void updateLastRequestDate() {
        lastRequestDate = new Date();
    }

    /**
     * Getter for the user last request date.
     *
     * @return The user last request date.
     */
    public Date getLastRequestDate() {
        return lastRequestDate;
    }

    /**
     * Getter for the user last change date.
     *
     * @return The last change date
     */
    public Date getLastChangeDate() {
        return lastChangeDate;
    }

    /**
     * Method used for providing a string representation of the UserData object.
     *
     * @return A string representation of the object
     */
    public String toString() {
        String retVal = "";
        retVal += "User: "+this.getLoginName()+"\n";
        retVal += "Real Name: "+this.getRealName()+"\n";
        retVal += "UUID: "+this.getUUID()+"\n";
        retVal += "IP: "+this.getIp()+"\n";
        retVal += "Last Change: "+this.getLastChangeDate()+"\n";
        retVal += "Last Request: "+this.getLastRequestDate()+"\n";

        return retVal;
    }
}
