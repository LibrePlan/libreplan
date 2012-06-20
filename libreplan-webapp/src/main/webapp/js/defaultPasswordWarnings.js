/*
 * This file is part of LibrePlan
 *
 * Copyright (C) 2011-2012 Igalia, S.L.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function showOrHideDefaultPasswordWarnings(
        adminNotDefaultPassword,
        wsreaderNotDefaultPassword,
        wswriterNotDefaultPassword,
        wssubcontractingNotDefaultPassword,
        managerNotDefaultPassword,
        hresourcesNotDefaultPassword,
        outsourcingNotDefaultPassword,
        reportsNotDefaultPassword) {

    setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdadmin"),
        adminNotDefaultPassword);

    var otherDefaultPassword = adminNotDefaultPassword && (
            !wsreaderNotDefaultPassword ||
            !wswriterNotDefaultPassword ||
            !wssubcontractingNotDefaultPassword ||
            !managerNotDefaultPassword ||
            !hresourcesNotDefaultPassword ||
            !outsourcingNotDefaultPassword ||
            !reportsNotDefaultPassword);
    setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdOthers"),
        !otherDefaultPassword);

    if (otherDefaultPassword) {
        setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdWsreader"),
            wsreaderNotDefaultPassword);
        setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdWswriter"),
            wswriterNotDefaultPassword);
        setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdWssubcontracting"),
                wssubcontractingNotDefaultPassword);
        setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdManager"),
                managerNotDefaultPassword);
        setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdHresources"),
                hresourcesNotDefaultPassword);
        setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdOutsourcing"),
                outsourcingNotDefaultPassword);
        setDisplayNoneOrInline(document.getElementById("warningDefaultPasswdReports"),
                reportsNotDefaultPassword);
    }
}

function setDisplayNoneOrInline(component, boolean) {
    component.style["display"] = boolean ? "none" : "inline";
}
