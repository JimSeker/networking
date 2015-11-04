<?php

$title = addslashes($_REQUEST['title']);
$body = addslashes($_REQUEST['body']);


  include 'lib.php';



if($title != "" and $body != "") {
   $sql = "INSERT into restdata (title, body) values";
   $sql .= "('$title','$body')";
   //echo "$sql\n";
   $result = mysql_query($sql);
  echo mysql_affected_rows($link_id);
} else {
  echo 0;
}
 mysql_close($link_id);
 
?>
