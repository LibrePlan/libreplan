<?php

$host="domain:port";
$dbuser="username";
$dbpass="password";
$dbname="jiradb";

$conn = mysql_connect($host, $dbuser, $dbpass)
    or die("Connection Failed");

mysql_select_db($dbname, $conn) or die ($dbname . " Database not found. ");

$query = "SELECT DISTINCT label FROM label";

$result = mysql_db_query($dbname, $query) or die("Failed Query " . $query);
$resp = "";
while($row = mysql_fetch_row($result)) {
    $label = $row[0];
    $label = str_replace("\r\n", "", $label);
    $resp .=   $label . ",";
}

mysql_close($conn);

$resp = substr_replace($resp ,"",-1);

echo $resp;
?>
