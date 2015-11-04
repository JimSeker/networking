<?php

  $id = $_REQUEST['id'];

  include 'lib.php';

if($id == "") {
   $sql = 'SELECT id,title,body FROM restdata';
} else {
   $sql = 'SELECT id,title,body FROM restdata WHERE id='.$id;
}

$result = mysql_query($sql);
while($ary = mysql_fetch_array($result)) {
  $id = stripslashes($ary["id"]);
  $title = stripslashes($ary["title"]);
  $body = stripslashes($ary["body"]);
  echo "$id,$title,$body\n";
}
 mysql_close($link_id);

?>
