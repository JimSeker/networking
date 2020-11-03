<?php

$id = addslashes($_REQUEST['id']);


  include 'lib.php';


if($id != "" ) {
   $sql = "DELETE from restdata where id= $id";
   //echo "$sql\n";
   $result = mysqli_query($link_id,$sql);
   echo mysql_affected_rows($link_id);

} else {
  echo 0;
}
 mysqli_close($link_id);
 
?>
