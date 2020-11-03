<?php

$title = addslashes($_REQUEST['title']);
$body = addslashes($_REQUEST['body']);


  include 'lib.php';



if($title != "" and $body != "") {
   $sql = "INSERT into restdata (title, body) values ";
   $sql .= "('$title','$body')";
   echo "$sql\n";
   $result = mysqli_query($link_id, $sql) or die(mysql_error());
  echo mysqli_affected_rows($link_id);
} else {
  echo 0;
}
 mysqli_close($link_id);
 
?>
