package org.libreplan.web;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.libreplan.business.common.daos.IConfigurationDAO;
import org.libreplan.business.users.daos.IUserDAO;
import org.libreplan.web.common.GatheredUsageStats;
import org.libreplan.web.expensesheet.IExpenseSheetModel;
import org.libreplan.web.materials.IMaterialsModel;
import org.libreplan.web.orders.IAssignedTaskQualityFormsToOrderElementModel;
import org.libreplan.web.orders.IOrderModel;
import org.libreplan.web.resources.machine.IMachineModel;
import org.libreplan.web.resources.worker.IWorkerModel;
import org.libreplan.web.security.SecurityUtils;
import org.libreplan.web.workreports.IWorkReportModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class StartupApplicationListener implements ApplicationListener {

	private static final Log LOG = LogFactory.getLog(StartupApplicationListener.class);

	@Autowired
	private IConfigurationDAO configurationDAO;

	@Autowired
	private IUserDAO userDAO;

	@Autowired
	private IOrderModel orderModel;

	@Autowired
	private IWorkReportModel workReportModel;

	@Autowired
	private IWorkerModel workerModel;

	@Autowired
	private IMachineModel machineModel;

	@Autowired
	private IExpenseSheetModel expenseSheetModel;

	@Autowired
	private IMaterialsModel materialsModel;

	@Autowired
	private IAssignedTaskQualityFormsToOrderElementModel assignedQualityFormsModel;


	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		// Send data to server
		if (SecurityUtils.isGatheredStatsAlreadySent) {
			return;
		}
		if (configurationDAO.getConfigurationWithReadOnlyTransaction() == null || configurationDAO.getConfigurationWithReadOnlyTransaction().isAllowedToGatherUsageStatsEnabled()) {
			GatheredUsageStats gatheredUsageStats = new GatheredUsageStats(userDAO, orderModel, workReportModel, workerModel, machineModel,
					expenseSheetModel, materialsModel, assignedQualityFormsModel);

			gatheredUsageStats.sendGatheredUsageStatsToServer();
			SecurityUtils.isGatheredStatsAlreadySent = true;
		}
	}
}


