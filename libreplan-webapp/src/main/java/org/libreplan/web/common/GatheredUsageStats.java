package org.libreplan.web.common;

import org.libreplan.business.common.VersionInformation;
import org.libreplan.business.orders.entities.Order;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.web.orders.IOrderModel;
import org.libreplan.web.expensesheet.IExpenseSheetModel;
import org.libreplan.web.materials.IMaterialsModel;
import org.libreplan.web.orders.IAssignedTaskQualityFormsToOrderElementModel;
import org.libreplan.web.resources.machine.IMachineModel;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.libreplan.web.workreports.IWorkReportModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.zkoss.json.JSONObject;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * Class represents all data that will be sent to server.
 *
 * If you want to use it, just create a new object and set 2 private variables.
 * All needed data will be already in object.
 *
 * Created by
 * @author Vova Perebykivskiy <vova@libreplan-enterprise.com>
 * @author Bogdan Bodnarjuk <bogdan@libreplan-enterprise.com>
 * on 02.08.2016.
 */

public class GatheredUsageStats {

    private IUserDAO userDAO;

    private IOrderModel orderModel;

    private IWorkReportModel workReportModel;

    private IWorkerModel workerModel;

    private IMachineModel machineModel;

    private IExpenseSheetModel expenseSheetModel;

    private IMaterialsModel materialsModel;

    private IAssignedTaskQualityFormsToOrderElementModel assignedQualityFormModel;

    // Version of this statistics implementation.
    // Just increment it, if you will change something related to JSON object.
    private int jsonObjectVersion = 3;

    // Unique system identifier (MD5 - ip + hostname)
    private String id;

    // Version of LibrePlan
    private String version = VersionInformation.getVersion();

    // Number of users in application
    private Number users;

    // Number of projects in application
    private int projects;

    // Number of timesheets in application
    private int timesheets;

    // Number of workers in application
    private int workers;

    // Number of machines in application
    private int machines;

    // Number of expense sheets in application
    private int expensesheets;

    // Number of materials in application
    private int materials;

    // Number of assigned quality forms in application
    private int assignedQualityForms;

    // The oldestDate in the projects
    private String oldestDate;

	private String generateID() {
		String ip = null;
		String hostname = null;

		// Make hash of ip + hostname
		try {
			WebAuthenticationDetails details = (WebAuthenticationDetails) SecurityContextHolder.getContext()
					.getAuthentication().getDetails();
			ip = details.getRemoteAddress();
			Execution execution = Executions.getCurrent();
			hostname = execution.getServerName();
		} catch (Exception e) {
			try {
				InetAddress address = InetAddress.getLocalHost();
				ip = address.getHostAddress();
				hostname = address.getHostName();
			} catch (UnknownHostException uhe) {
				uhe.printStackTrace();
			}
		}

		String message = ip + hostname;
		byte[] encoded;
		StringBuffer sb = null;

		try {
			byte[] bytesOfMessage = message.getBytes("UTF-8");
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			encoded = md5.digest(bytesOfMessage);

			// Convert bytes to hex format
			sb = new StringBuffer();
			for (int i = 0; i < encoded.length; i++) {
				sb.append(Integer.toString((encoded[i] & 0xff) + 0x100, 16)
						.substring(1));
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

    // It needed because i do not need to call default constructor on Autowiring
    private void myConstructor() {
        List<Order> allOrders = orderModel.getAllOrders();

        setId(generateID());
        setUsers(getUserRows());
        setProjects(allOrders.size());
        setTimesheets(workReportModel.getWorkReportDTOs().size());
        setWorkers(workerModel.getWorkers().size());
        setMachines(machineModel.getMachines().size());
        setExpensesheets(expenseSheetModel.getExpenseSheets().size());
        setMaterials(materialsModel.getMaterials().size());
        setQualityForms(assignedQualityFormModel.getAssignedQualityForms().size());
        setOldestDate(allOrders);
    }

    public void sendGatheredUsageStatsToServer(){
        JSONObject json = new JSONObject();

        json.put("json-version", jsonObjectVersion);
        json.put("id", id);
        json.put("version", version);
        json.put("users", users);
        json.put("projects", projects);
        json.put("timesheets", timesheets);
        json.put("workers", workers);
        json.put("machines", machines);
        json.put("expensesheets", expensesheets);
        json.put("materials", materials);
        json.put("assigned-quality-forms", assignedQualityForms);
        json.put("oldestDate", oldestDate);


        HttpURLConnection connection = null;

        Properties properties = new Properties();
        InputStream inputStream = null;

        try {
            // You can find it in libreplan-business/src/main/resouces
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

            // No needed code, but it is not working without it
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

    public void setupNotAutowiredClasses(
            IUserDAO userDAO,
            IOrderModel orderModel,
            IWorkReportModel workReportModel,
            IWorkerModel workerModel,
            IMachineModel machineModel,
            IExpenseSheetModel expenseSheetModel,
            IMaterialsModel materialsModel,
            IAssignedTaskQualityFormsToOrderElementModel assignedQualityFormModel){

        this.userDAO = userDAO;
        this.orderModel = orderModel;
        this.workReportModel = workReportModel;
        this.workerModel = workerModel;
        this.machineModel = machineModel;
        this.expenseSheetModel = expenseSheetModel;
        this.materialsModel = materialsModel;
        this.assignedQualityFormModel = assignedQualityFormModel;

        myConstructor();
    }


    private Number getUserRows(){
        return userDAO.getRowCount();
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUsers(Number users) {
        this.users = users;
    }

    public void setProjects(int projects) {
        this.projects = projects;
    }

    public void setTimesheets(int timesheets) {
        this.timesheets = timesheets;
    }

    public void setWorkers(int workers) {
        this.workers = workers;
    }

    public void setMachines(int machines) {
        this.machines = machines;
    }

    public void setExpensesheets(int expensesheets) {
        this.expensesheets = expensesheets;
    }

    public void setMaterials(int materials) {
        this.materials = materials;
    }

    public void setQualityForms(int qualityForms) {
        this.assignedQualityForms = qualityForms;
    }

    private void setOldestDate(List<Order> list){
        if(!list.isEmpty()) {
            Date date = list.get(0).getInitDate();
            for (int i = 1; i < list.size(); i++) {
                if (list.get(i).getInitDate().compareTo(date) < 0) {
                    date = list.get(i).getInitDate();
                }
            }
            this.oldestDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'").format(date);
        } else {
            this.oldestDate = "0";
        }
    }
}
