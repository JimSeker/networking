<?php

$id = addslashes($_REQUEST['id']);
$title = addslashes($_REQUEST['title']);
$body = addslashes($_REQUEST['body']);


  include 'lib.php';



if($title != "" and $body != "" and $id != "") {
      $sql = "UPDATE restdata SET title = '$title',";
      $sql .= "body = '$body'";
      $sql .= " WHERE id = $id";

//   echo "$sql\n";
   $result = mysql_query($sql);
  echo mysql_affected_rows($link_id);
} else {
  echo 0;
}
 mysql_close($link_id);
 
?>
