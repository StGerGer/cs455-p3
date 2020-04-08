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

    public UserData(String loginName, String realName, String password, String ip) {
        this.loginName = loginName;
        this.realName = realName;
        this.password = password;
        this.ip = ip;
        uuid = UUID.randomUUID();
        lastChangeDate = new Date();
        lastRequestDate = new Date();
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        lastChangeDate = new Date();
        this.loginName = loginName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean hasPassword() {
        return password != null;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getIp() {
        return ip;
    }

    public void updateLastRequestDate() {
        lastRequestDate = new Date();
    }

    public Date getLastRequestDate() {
        return lastRequestDate;
    }

    public Date getLastChangeDate() {
        return lastChangeDate;
    }

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
