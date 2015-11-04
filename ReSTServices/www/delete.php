<?php

$id = addslashes($_REQUEST['id']);


  include 'lib.php';


if($id != "" ) {
   $sql = "DELETE from restdata where id= $id";
   //echo "$sql\n";
   $result = mysql_query($sql);
   echo mysql_affected_rows($link_id);

} else {
  echo 0;
}
 mysql_close($link_id);
 
?>
