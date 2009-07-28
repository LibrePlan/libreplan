package org.navalplanner.web.workreports;

import org.navalplanner.business.workreports.entities.WorkReportType;
import org.navalplanner.web.common.entrypoints.EntryPoint;
import org.navalplanner.web.common.entrypoints.EntryPoints;

@EntryPoints(page = "/workreports/workReport.zul", registerAs = "workReportCRUD")
public interface IWorkReportCRUDControllerEntryPoints {

    @EntryPoint("edit")
    public abstract void goToCreateForm(WorkReportType workReportType);

}