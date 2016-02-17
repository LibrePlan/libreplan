package org.libreplan.web.common;

import org.libreplan.business.common.VersionInformation;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.web.orders.IOrderModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Class represents all data that will be sent to server.
 *
 * If you want to use it, just create a new object and set 2 private variables.
 * All needed data will be already in object.
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * on 02/08/2016.
 */

public class GatheredUsageStats {

    private IUserDAO userDAO;

    private IOrderModel orderModel;


    // Version of this statistics implementation
    private int jsonObjectVersion = 1;

    // Unique system identifier (MD5 - ip + hostname)
    private String id;

    // Version of LibrePlan
    private String version = VersionInformation.getVersion();

    // Number of users in application
    private Number users;

    // Number of projects in application
    private int projects;

    private Number getUserRows(){
        return userDAO.getRowCount();
    }

    private String generateID(){
        // Make hash of ip + hostname
        WebAuthenticationDetails details = (WebAuthenticationDetails) SecurityContextHolder.getContext()
                .getAuthentication().getDetails();
        String ip = details.getRemoteAddress();

        Execution execution = Executions.getCurrent();
        String hostname = execution.getServerName();

        String message = ip + hostname;
        byte[] encoded = null;
        StringBuffer sb = null;

        try {
            byte[] bytesOfMessage = message.getBytes("UTF-8");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            encoded = md5.digest(bytesOfMessage);

            // Convert bytes to hex format
            sb = new StringBuffer();
            for (int i = 0; i < encoded.length; i++) sb.append(Integer.toString((encoded[i] & 0xff) + 0x100, 16)
                    .substring(1));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    // It needed because i do not need to call default constructor on Autowiring
    private void myConstructor(){
        setId(generateID());
        setUsers(getUserRows());
        setProjects(orderModel.getOrders().size());
    }

    public void sendGatheredUsageStatsToServer(){
        JSONObject json = new JSONObject();

        json.put("json-version", jsonObjectVersion);
        json.put("id", id);
        json.put("version", version);
        json.put("users", users);
        json.put("projects", projects);

        HttpURLConnection connection = null;

        Properties properties = new Properties();
        InputStream inputStream = null;

        try {
            String filename = "libreplan.properties";
            inputStream = GatheredUsageStats.class.getClassLoader().getResourceAsStream(filename);
            properties.load(inputStream);

            URL url = new URL(properties.getProperty("statsPage"));
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-urlencoded");
            connection.setRequestProperty("Content-Language", "en-GB");
            connection.setRequestProperty("Content-Length", Integer.toString(json.toJSONString().getBytes().length));
            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);
            // If the connection lasts > 2 sec throw Exception
            connection.setConnectTimeout(2000);

            // Send request
            DataOutputStream dataOutputStream = new DataOutputStream(connection.getOutputStream());
            dataOutputStream.writeBytes(json.toJSONString());
            dataOutputStream.flush();
            dataOutputStream.close();

            // No needed code, but it is not working without id
            connection.getInputStream();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if ( connection != null ) {
                connection.disconnect();
            }
        }
    }

    public void setupNotAutowiredClasses(IUserDAO userDAO, IOrderModel orderModel){
        this.userDAO = userDAO;
        this.orderModel = orderModel;
        myConstructor();
    }

    public int getJsonObjectVersion() {
        return jsonObjectVersion;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public Number getUsers() {
        return users;
    }
    public void setUsers(Number users) {
        this.users = users;
    }

    public int getProjects() {
        return projects;
    }
    public void setProjects(int projects) {
        this.projects = projects;
    }
}
