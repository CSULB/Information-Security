<?php

namespace App\Http\Controllers;

use App\Models\User;
use App\Libraries\Curve25519;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;

/*
Using https://github.com/lt/PHP-Curve25519 for the diffie-hellman key exchange.
*/
class KeyController extends Controller{
	
	public function diffie(Request $request) {

		// var_dump($request->all()); exit;

		$vars = $request->input('nameValuePairs');
		$theirPublic = hex2bin($vars['public_key']);
		// print_r($theirPublic); exit;

		$mySecret = random_bytes(32);
		// echo gettype($mySecret); exit;
		$curve = new Curve25519();
		// echo $mySecret; exit;
		$myPublic = $curve->publicKey($mySecret);
		$shared   = $curve->sharedKey($mySecret, $theirPublic);
		// echo $shared; exit;
		
		$arrayName = array('server' => bin2hex($shared), 'public_key' => bin2hex($myPublic));

		return response()->json($arrayName);
	}
}