package org.navalplanner.web.workreports;

import org.navalplanner.web.common.entrypoints.EntryPoint;
import org.navalplanner.web.common.entrypoints.EntryPoints;

@EntryPoints(page = "/workreports/workReportTypes.zul", registerAs = "workReportTypeCRUD")
public interface IWorkReportTypeCRUDControllerEntryPoints {

    @EntryPoint("list")
    public abstract void goToList();

}
