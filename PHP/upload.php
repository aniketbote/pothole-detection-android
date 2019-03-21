<?php
error_reporting(0);

$image = $_POST['image'];

$image_name = (string)$_POST['image_name'];

$sensor_size_height = (string)$_POST['sensor_size_height'];
 
$sensor_size_width = (string)$_POST['sensor_size_width'];
 
$focal_length =(string) $_POST['focal_length'];

 
$pitch = (string)$_POST['pitch'];
 
$roll = (string)$_POST['roll'];

$latitude = (string)$_POST['latitude'];

$longitude = (string)$_POST['longitude'];


$upload_path = "$image_name.jpg"; 
//echo($upload_path);

file_put_contents($upload_path,base64_decode($image));
$myfile = fopen("Record.txt", "a+") or die("Unable to open file!");
$txta = "The name of the image is $image_name ";
$txtb = "The Sensor Height of the device is $sensor_size_height ";
$txtc = "The Sensor Width of the device is $sensor_size_width ";
$txtd = "The Focal Length of the device is $focal_length ";
$txte = "The Focal Length of the device is $latitude ";
$txtf = "The Focal Length of the device is $longitude ";


$txt = "PHP TO PYTHON CALL\n";
fwrite($myfile, $txt);

$a = exec('python final\new_obj_det.py ' .$upload_path.' '.$focal_length.' '.$sensor_size_height.' '.$sensor_size_width.' '.$pitch.' '.$roll.' '.$latitude.' '.$longitude);

$txt1 = "PYTHON End";
fwrite($myfile, $txt1);


$response['message'] = 'hi';
header('Content-Type: application/json');

echo json_encode($response);	
	?>

