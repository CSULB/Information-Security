<?php
  
namespace App\Http\Controllers;
  
use App\Models\User;
use App\Http\Controllers\Controller;
use Illuminate\Http\Request;
  
  
class UserController extends Controller{

	public function login(Request $request) {
		$user = User::create($request->all());
		$user->save();
		return response()->json($user);
	}

}
?>